package com.netheryx.world;

import com.netheryx.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/**
 * Arbre du Nether : tronc oriente, houppier de verrue arrondi (avec quelques blocs lumineux pour l'arbre de braise),
 * et sol converti en nylium autour du pied.
 */
public class WorldGenNetherTree extends WorldGenerator {
    private final Block stem;
    private final Block canopy;
    private final Block ground;
    private final Block soil;
    private final Block glow;
    private final boolean drooping;

    /**
     * @param ground   bloc sur lequel l'arbre peut pousser (null : n'importe quel sol du Nether)
     * @param soil     sol qui remplace le dessus du terrain autour du pied (null : aucun)
     * @param glow     bloc lumineux seme dans le houppier (null : aucun)
     * @param drooping houppier tombant (verrue qui pend sous les bords)
     */
    public WorldGenNetherTree(Block stem, Block canopy, Block ground, Block soil, Block glow, boolean drooping) {
        super(false);
        this.stem = stem;
        this.canopy = canopy;
        this.ground = ground;
        this.soil = soil;
        this.glow = glow;
        this.drooping = drooping;
    }

    /** (x, y, z) : premier bloc d'air au-dessus du sol. */
    @Override
    public boolean generate(World world, Random random, int x, int y, int z) {
        Block below = world.getBlock(x, y - 1, z);
        if (this.ground != null ? below != this.ground : !isNetherGround(below)) {
            return false;
        }
        int height = 4 + random.nextInt(4);
        for (int dy = 0; dy <= height + 2; dy++) {
            if (!world.isAirBlock(x, y + dy, z)) {
                return false;
            }
        }
        // Sol du pied
        if (this.soil != null) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    Block under = world.getBlock(x + dx, y - 1, z + dz);
                    if (dx * dx + dz * dz <= 5 && (this.ground != null ? under == this.ground : isNetherGround(under))
                            && world.isAirBlock(x + dx, y, z + dz)) {
                        world.setBlock(x + dx, y - 1, z + dz, this.soil, 0, 2);
                    }
                }
            }
        }
        // Tronc
        for (int dy = 0; dy < height; dy++) {
            world.setBlock(x, y + dy, z, this.stem, 0, 2);
        }
        // Houppier : une boule aplatie centree sur le sommet du tronc
        int top = y + height;
        for (int dy = -2; dy <= 1; dy++) {
            int radius = dy == 1 ? 1 : (dy == -2 ? 2 : 3);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius + 1 || (dx == 0 && dz == 0 && dy < 0)) {
                        continue;
                    }
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextInt(2) == 0) {
                        continue; // coins irreguliers
                    }
                    int bx = x + dx;
                    int by = top + dy;
                    int bz = z + dz;
                    if (world.isAirBlock(bx, by, bz)) {
                        boolean light = this.glow != null && random.nextInt(12) == 0;
                        world.setBlock(bx, by, bz, light ? this.glow : this.canopy, 0, 2);
                    }
                }
            }
        }
        world.setBlock(x, top, z, this.canopy, 0, 2);
        // Verrue qui pend sous les bords (arbre fletri)
        if (this.drooping) {
            for (int i = 0; i < 6; i++) {
                int bx = x + random.nextInt(7) - 3;
                int bz = z + random.nextInt(7) - 3;
                int by = top - 3;
                if (world.isAirBlock(bx, by, bz) && world.getBlock(bx, by + 1, bz) == this.canopy) {
                    world.setBlock(bx, by, bz, this.canopy, 0, 2);
                    if (random.nextBoolean() && world.isAirBlock(bx, by - 1, bz)) {
                        world.setBlock(bx, by - 1, bz, this.canopy, 0, 2);
                    }
                }
            }
        }
        return true;
    }

    /** Sol du Nether (netherrack, sable des ames, sols de NetheryX). */
    public static boolean isNetherGround(Block block) {
        return block == Blocks.netherrack || block == Blocks.soul_sand || block == ModBlocks.emberNylium
                || block == ModBlocks.netherGrassBlock || block == ModBlocks.ashBlock || block == ModBlocks.witheredSoil
                || block == ModBlocks.ossifiedNylium || block == ModBlocks.wartMoss || block == ModBlocks.sulfurRock
                || block == ModBlocks.rimestone || block == ModBlocks.polishedOnyx;
    }
}
