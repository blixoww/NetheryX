package com.netheryx.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Positions des Lanternes de garde chargees, par dimension (cote serveur). */
public final class WardLanterns {
    private static final Map<ResourceKey<Level>, Set<BlockPos>> LANTERNS = new ConcurrentHashMap<>();

    private WardLanterns() {
    }

    public static void add(Level level, BlockPos pos) {
        LANTERNS.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void remove(Level level, BlockPos pos) {
        Set<BlockPos> set = LANTERNS.get(level.dimension());
        if (set != null) {
            set.remove(pos);
        }
    }

    /** Vrai si une lanterne de garde protege cette position (sphere de {@code wardLanternRadius} blocs, 24 par defaut). */
    public static boolean isProtected(Level level, double x, double y, double z) {
        Set<BlockPos> set = LANTERNS.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return false;
        }
        double radius = com.netheryx.config.NetheryXConfig.WARD_LANTERN_RADIUS.get();
        double max = radius * radius;
        for (BlockPos pos : set) {
            if (pos.distToCenterSqr(x, y, z) <= max) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        LANTERNS.clear();
    }
}
