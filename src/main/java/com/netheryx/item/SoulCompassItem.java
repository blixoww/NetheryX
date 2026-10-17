package com.netheryx.item;

import com.mojang.datafixers.util.Pair;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.OpenStructureSelectionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Boussole des ames : un clic droit ouvre une interface listant toutes les structures (vanilla et moddees)
 * pouvant exister dans la dimension actuelle. La structure choisie est localisee, puis une fleche et la
 * distance guident le joueur tant qu'il tient la boussole.
 */
public class SoulCompassItem extends Item {
    private static final String TAG_TARGET = "TargetPos";
    private static final String TAG_DIMENSION = "TargetDimension";
    private static final String TAG_STRUCTURE = "TargetStructure";
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    public SoulCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenStructureSelectionPacket(locatableStructures(server)));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Structures dont au moins un biome peut exister dans cette dimension (fonctionne avec les mods). */
    public static List<ResourceLocation> locatableStructures(ServerLevel level) {
        Set<Holder<Biome>> biomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return registry.holders()
                .filter(holder -> holder.value().biomes().stream().anyMatch(biomes::contains))
                .map(holder -> holder.key().location())
                .sorted()
                .toList();
    }

    /** Appele quand le joueur choisit une structure dans l'interface. */
    public static void locateFor(ServerPlayer player, ResourceLocation structureId) {
        ItemStack stack = findCompass(player);
        if (stack.isEmpty() || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!locatableStructures(level).contains(structureId)) {
            return;
        }
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> structure = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, structureId));
        if (structure.isEmpty()) {
            return;
        }
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                HolderSet.direct(structure.get()), player.blockPosition(), NetheryXConfig.SOUL_COMPASS_SEARCH_RADIUS.get(), false);
        player.getCooldowns().addCooldown(stack.getItem(), 60);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_STRUCTURE, structureId.toString());
        if (found == null) {
            tag.remove(TAG_TARGET);
            player.displayClientMessage(Component.translatable("item.netheryx.soul_compass.not_found").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        BlockPos pos = found.getFirst();
        tag.put(TAG_TARGET, NbtUtils.writeBlockPos(pos));
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("item.netheryx.soul_compass.found",
                Mth.floor(Math.sqrt(pos.distSqr(player.blockPosition())))).withStyle(ChatFormatting.AQUA), true);
    }

    /**
     * Cree une boussole deja accordee sur la structure donnee la plus proche de {@code origin}
     * (vendue par le marchand piglin). Renvoie une pile vide si la structure est introuvable.
     */
    public static ItemStack attuned(ServerLevel level, BlockPos origin, ResourceLocation structureId) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> structure = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, structureId));
        if (structure.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                HolderSet.direct(structure.get()), origin, 50, false);
        if (found == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(com.netheryx.registry.ModItems.SOUL_COMPASS.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_STRUCTURE, structureId.toString());
        tag.put(TAG_TARGET, NbtUtils.writeBlockPos(found.getFirst()));
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
        return stack;
    }

    private static ItemStack findCompass(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SoulCompassItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !selected || !(entity instanceof Player player) || level.getGameTime() % 10 != 0) {
            return;
        }
        getTarget(stack, level).ifPresent(target -> player.displayClientMessage(directionMessage(player, target), true));
    }

    private static Optional<BlockPos> getTarget(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_TARGET) || !level.dimension().location().toString().equals(tag.getString(TAG_DIMENSION))) {
            return Optional.empty();
        }
        return Optional.of(NbtUtils.readBlockPos(tag.getCompound(TAG_TARGET)));
    }

    private static Component directionMessage(Player player, BlockPos target) {
        double dx = target.getX() + 0.5D - player.getX();
        double dz = target.getZ() + 0.5D - player.getZ();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relative = Mth.wrapDegrees(targetYaw - player.getYRot());
        String arrow = ARROWS[Math.floorMod(Math.round(relative / 45.0F), ARROWS.length)];
        int distance = Mth.floor(Math.sqrt(dx * dx + dz * dz));
        return Component.translatable("item.netheryx.soul_compass.direction", arrow, distance).withStyle(ChatFormatting.AQUA);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_TARGET);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.soul_compass.tooltip").withStyle(ChatFormatting.GRAY));
        if (stack.hasTag() && stack.getTag().contains(TAG_STRUCTURE)) {
            tooltip.add(Component.translatable("item.netheryx.soul_compass.target", stack.getTag().getString(TAG_STRUCTURE))
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
