package com.netheryx.item;

import com.mojang.datafixers.util.Pair;
import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.OpenAdminCompassPacket;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
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

    /** Teleporte le joueur vers la structure ou le biome choisi (verifie de nouveau les droits cote serveur). */
    public static void teleport(ServerPlayer player, ResourceLocation id, boolean biome) {
        if (!canUse(player) || !(player.getMainHandItem().getItem() instanceof AdminCompassItem
                || player.getOffhandItem().getItem() instanceof AdminCompassItem)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        BlockPos found = biome ? locateBiome(level, player.blockPosition(), id) : locateStructure(level, player.blockPosition(), id);
        if (found == null) {
            player.displayClientMessage(Component.translatable("item.netheryx.admin_compass.not_found", id.toString())
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        BlockPos safe = safeSpot(level, found);
        player.teleportTo(level, safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D, player.getYRot(), player.getXRot());
        level.playSound(null, safe, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("item.netheryx.admin_compass.teleported", id.toString(),
                safe.getX(), safe.getY(), safe.getZ()).withStyle(ChatFormatting.GREEN), false);
    }

    @Nullable
    private static BlockPos locateStructure(ServerLevel level, BlockPos origin, ResourceLocation id) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> structure = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, id));
        if (structure.isEmpty()) {
            return null;
        }
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                HolderSet.direct(structure.get()), origin, 100, false);
        return found == null ? null : found.getFirst();
    }

    @Nullable
    private static BlockPos locateBiome(ServerLevel level, BlockPos origin, ResourceLocation id) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(holder -> holder.is(key), origin, 6400, 32, 64);
        return found == null ? null : found.getFirst();
    }

    /** Premier emplacement ou l'on tient debout (sol solide, 2 blocs d'air, pas de lave) autour de la cible. */
    private static BlockPos safeSpot(ServerLevel level, BlockPos target) {
        boolean ceiling = level.dimensionType().hasCeiling();
        for (int radius = 0; radius <= 12; radius += 2) {
            for (int dx = -radius; dx <= radius; dx += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = target.getX() + dx;
                    int z = target.getZ() + dz;
                    if (!ceiling) {
                        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
                        if (standable(level, top)) {
                            return top;
                        }
                        continue;
                    }
                    int startY = Math.max(level.getMinBuildHeight() + 1, Math.min(target.getY(), 118));
                    for (int dy = 0; dy < 100; dy++) {
                        for (int sign : new int[]{1, -1}) {
                            BlockPos pos = new BlockPos(x, startY + dy * sign, z);
                            if (pos.getY() > level.getMinBuildHeight() && pos.getY() < 120 && standable(level, pos)) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target);
    }

    private static boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                && level.getFluidState(below).isEmpty();
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
