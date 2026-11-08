package com.netheryx.worldgen.placement;

import com.mojang.serialization.Codec;
import com.netheryx.registry.ModPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Filtre de placement : refuse un decor du mod (colonne, fleche, arbre, cristal, buisson...) dont l'origine tombe dans
 * l'emprise d'une structure, vue du ciel, marge comprise : structures NetheryX, mais aussi forteresses, bastions, portails
 * en ruine et fossiles vanilla. Les forteresses vanilla sont posees avant les decors, les structures NetheryX apres :
 * sans ce filtre, les grandes colonnes du sol a la voute traverseraient leurs ponts, leurs murs et leurs cours.
 */
public final class OutsideStructuresFilter extends PlacementFilter {
    public static final OutsideStructuresFilter INSTANCE = new OutsideStructuresFilter();
    public static final Codec<OutsideStructuresFilter> CODEC = Codec.unit(() -> INSTANCE);
    /** Marge autour des pieces : un arbre ou une colonne pose juste a cote deborderait encore dedans. */
    private static final int MARGIN = 5;

    private OutsideStructuresFilter() {
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        if (!(context.getLevel() instanceof WorldGenRegion region)) {
            return true;
        }
        StructureManager structures = region.getLevel().structureManager().forWorldGenRegion(region);
        for (StructureStart start : structures.startsForStructure(new ChunkPos(pos), structure -> true)) {
            if (!start.isValid()) {
                continue;
            }
            for (StructurePiece piece : start.getPieces()) {
                var box = piece.getBoundingBox();
                if (pos.getX() >= box.minX() - MARGIN && pos.getX() <= box.maxX() + MARGIN
                        && pos.getZ() >= box.minZ() - MARGIN && pos.getZ() <= box.maxZ() + MARGIN) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacements.OUTSIDE_STRUCTURES.get();
    }
}
