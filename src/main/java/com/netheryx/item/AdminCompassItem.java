package com.netheryx.item;

import com.mojang.datafixers.util.Pair;
import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.OpenAdminCompassPacket;
import com.netheryx.worldgen.structure.NetherFloorStructure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Boussole du createur : objet d'administration (mode creatif ou operateur uniquement, non fabricable).
 * Elle ouvre une interface listant toutes les structures et tous les biomes de la dimension et y teleporte
 * directement le joueur, sur un emplacement sur.
 */
public class AdminCompassItem extends Item {
    public AdminCompassItem(Properties properties) {
        super(properties);
    }

    public static boolean canUse(Player player) {
        return player.isCreative() || player.hasPermissions(2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUse(player)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.netheryx.admin_compass.denied").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            List<ResourceLocation> biomes = server.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream()
                    .map(holder -> holder.unwrapKey().map(ResourceKey::location).orElse(null))
                    .filter(java.util.Objects::nonNull).sorted().toList();
            NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenAdminCompassPacket(SoulCompassItem.locatableStructures(server), biomes));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Destination : ou poser les pieds, et le point a regarder en arrivant. */
    private record Destination(BlockPos feet, BlockPos lookAt) {
    }

    /** Teleporte le joueur vers la structure ou le biome choisi (verifie de nouveau les droits cote serveur). */
    public static void teleport(ServerPlayer player, ResourceLocation id, boolean biome) {
        if (!canUse(player) || !(player.getMainHandItem().getItem() instanceof AdminCompassItem
                || player.getOffhandItem().getItem() instanceof AdminCompassItem)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Destination destination = biome ? locateBiome(level, player.blockPosition(), id) : locateStructure(level, player.blockPosition(), id);
        BlockPos safe = destination == null ? null : safeSpot(level, destination.feet());
        if (safe == null) {
            player.displayClientMessage(Component.translatable("item.netheryx.admin_compass.not_found", id.toString())
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        // Regard tourne vers la structure (ou droit devant pour un biome)
        float yaw = player.getYRot();
        double dx = destination.lookAt().getX() + 0.5D - (safe.getX() + 0.5D);
        double dz = destination.lookAt().getZ() + 0.5D - (safe.getZ() + 0.5D);
        if (dx * dx + dz * dz > 4.0D) {
            yaw = (float) Math.toDegrees(Mth.atan2(dz, dx)) - 90.0F;
        }
        player.teleportTo(level, safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D, yaw, 0.0F);
        player.fallDistance = 0.0F;
        level.playSound(null, safe, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("item.netheryx.admin_compass.teleported", id.toString(),
                safe.getX(), safe.getY(), safe.getZ()).withStyle(ChatFormatting.GREEN), false);
    }

    /**
     * Structure la plus proche, vraiment generee. Minecraft ne donne que le chunk d'origine, a la hauteur 0 : on lit donc la
     * structure elle-meme pour viser son entree (forteresses NetheryX) ou le centre de ses pieces, a la bonne hauteur.
     */
    @Nullable
    private static Destination locateStructure(ServerLevel level, BlockPos origin, ResourceLocation id) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> structure = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, id));
        if (structure.isEmpty()) {
            return null;
        }
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                HolderSet.direct(structure.get()), origin, 100, false);
        if (found == null) {
            return null;
        }
        ChunkPos chunk = new ChunkPos(found.getFirst());
        StructureStart start = level.getChunk(chunk.x, chunk.z, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(structure.get().value());
        if (start == null || !start.isValid()) {
            return new Destination(new BlockPos(found.getFirst().getX(), 64, found.getFirst().getZ()), found.getFirst());
        }
        BlockPos center = NetherFloorStructure.center(start);
        if (structure.get().value() instanceof NetherFloorStructure ours) {
            return new Destination(ours.arrival(start), center);
        }
        // Structure vanilla ou d'un autre mod : au-dessus de sa piece principale, puis le point sur le plus proche
        BoundingBox box = start.getPieces().isEmpty() ? start.getBoundingBox() : start.getPieces().get(0).getBoundingBox();
        return new Destination(new BlockPos(center.getX(), box.maxY() + 1, center.getZ()), center);
    }

    @Nullable
    private static Destination locateBiome(ServerLevel level, BlockPos origin, ResourceLocation id) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        // Dans le Nether, on cherche le biome au niveau des sols praticables (au-dessus de la mer de lave)
        BlockPos from = level.dimensionType().hasCeiling() ? new BlockPos(origin.getX(), 64, origin.getZ()) : origin;
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(holder -> holder.is(key), from, 6400, 32, 16);
        return found == null ? null : new Destination(found.getFirst(), found.getFirst());
    }

    /**
     * Emplacement sur le plus proche de la cible : sol solide, deux blocs d'air, pas de lave, et assez de place autour
     * pour ne pas arriver dans une poche de roche. Le plus proche d'abord en hauteur, puis en s'eloignant ; null si rien.
     */
    @Nullable
    private static BlockPos safeSpot(ServerLevel level, BlockPos target) {
        if (!level.dimensionType().hasCeiling()) {
            for (int radius = 0; radius <= 16; radius += 2) {
                for (BlockPos column : ring(target, radius)) {
                    BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
                    if (standable(level, top)) {
                        return top;
                    }
                }
            }
            return null;
        }
        // Nether : jamais sur ou au-dessus du plafond de bedrock, jamais dans la mer de lave
        int top = Math.min(level.getMinBuildHeight() + level.dimensionType().logicalHeight(), 128) - 9;
        int bottom = level.getSeaLevel() + 1;
        int startY = Mth.clamp(target.getY(), bottom, top);
        for (int pass = 0; pass < 2; pass++) {
            boolean roomy = pass == 0;
            for (int radius = 0; radius <= 24; radius += 2) {
                for (BlockPos column : ring(target, radius)) {
                    for (int dy = 0; dy <= top - bottom; dy++) {
                        for (int sign : new int[]{-1, 1}) {
                            if (dy == 0 && sign == 1) {
                                continue;
                            }
                            int y = startY + dy * sign;
                            if (y < bottom || y > top) {
                                continue;
                            }
                            BlockPos feet = new BlockPos(column.getX(), y, column.getZ());
                            if (standable(level, feet) && (!roomy || roomy(level, feet))) {
                                return feet;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Colonnes a cette distance (carre) de la cible. */
    private static List<BlockPos> ring(BlockPos center, int radius) {
        List<BlockPos> columns = new java.util.ArrayList<>();
        if (radius == 0) {
            columns.add(center);
            return columns;
        }
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                    columns.add(center.offset(dx, 0, dz));
                }
            }
        }
        return columns;
    }

    private static boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && !level.getBlockState(below).is(Blocks.BEDROCK) && !level.getBlockState(below).is(Blocks.MAGMA_BLOCK)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.getFluidState(below).isEmpty();
    }

    /** Assez de place : un troisieme bloc d'air au-dessus et au moins 5 des 8 cases voisines libres a hauteur des pieds. */
    private static boolean roomy(ServerLevel level, BlockPos feet) {
        if (!level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty()) {
            return false;
        }
        int open = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = feet.relative(direction);
            if (level.getBlockState(side).getCollisionShape(level, side).isEmpty() && level.getFluidState(side).isEmpty()) {
                open++;
            }
            BlockPos corner = side.relative(direction.getClockWise());
            if (level.getBlockState(corner).getCollisionShape(level, corner).isEmpty() && level.getFluidState(corner).isEmpty()) {
                open++;
            }
        }
        return open >= 5;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.admin_compass.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.netheryx.admin_compass.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
