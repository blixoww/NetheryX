package com.netheryx.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Memoire des portails du Nether empruntes par un joueur (utilisee par la Pierre de rappel).
 * Stockee dans les donnees persistantes du joueur et recopiee a la reapparition.
 */
public final class PortalMemory {
    public static final String TAG = "netheryx_portals";
    public static final int MAX_PORTALS = 8;
    private static final int SAME_PORTAL_DISTANCE_SQR = 16 * 16;

    private PortalMemory() {
    }

    public record Portal(ResourceLocation dimension, BlockPos pos) {
        public ResourceKey<Level> levelKey() {
            return ResourceKey.create(Registries.DIMENSION, this.dimension);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeResourceLocation(this.dimension);
            buf.writeBlockPos(this.pos);
        }

        public static Portal read(FriendlyByteBuf buf) {
            return new Portal(buf.readResourceLocation(), buf.readBlockPos());
        }
    }

    public static List<Portal> get(ServerPlayer player) {
        List<Portal> portals = new ArrayList<>();
        ListTag list = player.getPersistentData().getList(TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("dim"));
            if (dimension != null) {
                portals.add(new Portal(dimension, new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"))));
            }
        }
        return portals;
    }

    public static void set(ServerPlayer player, List<Portal> portals) {
        ListTag list = new ListTag();
        for (Portal portal : portals) {
            CompoundTag entry = new CompoundTag();
            entry.putString("dim", portal.dimension().toString());
            entry.putInt("x", portal.pos().getX());
            entry.putInt("y", portal.pos().getY());
            entry.putInt("z", portal.pos().getZ());
            list.add(entry);
        }
        player.getPersistentData().put(TAG, list);
    }

    /** Memorise le portail sur lequel le joueur vient d'arriver (le plus recent passe en tete de liste). */
    public static void rememberArrival(ServerPlayer player) {
        findPortalNear(player.serverLevel(), player.blockPosition(), 2).ifPresent(pos -> {
            ResourceLocation dimension = player.level().dimension().location();
            List<Portal> portals = get(player);
            portals.removeIf(p -> p.dimension().equals(dimension) && p.pos().distSqr(pos) <= SAME_PORTAL_DISTANCE_SQR);
            portals.add(0, new Portal(dimension, pos.immutable()));
            while (portals.size() > MAX_PORTALS) {
                portals.remove(portals.size() - 1);
            }
            set(player, portals);
        });
    }

    public static Optional<BlockPos> findPortalNear(ServerLevel level, BlockPos center, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius + 1, radius))) {
            if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
                double distance = pos.distSqr(center);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos.immutable();
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
