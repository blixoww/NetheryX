package com.netheryx.world;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/**
 * Outils de decoration des biomes du Nether. Les coordonnees (x, z) designent le coin du carre de 16 x 16 a decorer,
 * deja decale de +8 par rapport au chunk genere : rien ne deborde sur un chunk pas encore cree.
 */
public final class NetherDecor {
    private NetherDecor() {
    }

    /** Premier bloc d'air au-dessus d'un sol solide (jamais de la lave), ou -1. */
    public static int floor(World world, Random random, int x, int z) {
        int y = 34 + random.nextInt(84);
        if (!world.isAirBlock(x, y, z)) {
            return -1;
        }
        while (y > 33 && world.isAirBlock(x, y - 1, z)) {
            y--;
        }
        Block ground = world.getBlock(x, y - 1, z);
        return y > 33 && ground.getMaterial().isSolid() && !ground.getMaterial().isLiquid() ? y : -1;
    }

    /** Dernier bloc d'air sous un plafond solide, ou -1. */
    public static int ceiling(World world, Random random, int x, int z) {
        int y = 34 + random.nextInt(84);
        if (!world.isAirBlock(x, y, z)) {
            return -1;
        }
        while (y < 125 && world.isAirBlock(x, y + 1, z)) {
            y++;
        }
        return y < 125 && world.getBlock(x, y + 1, z).getMaterial().isSolid() ? y : -1;
    }

    public static void trees(World world, Random random, int x, int z, int count, WorldGenerator tree) {
        for (int i = 0; i < count; i++) {
            int bx = x + random.nextInt(16);
            int bz = z + random.nextInt(16);
            int y = floor(world, random, bx, bz);
            if (y > 0) {
                tree.generate(world, random, bx, y, bz);
            }
        }
    }

    /** Seme des plantes en petits groupes sur les sols ou elles tiennent. */
    public static void plants(World world, Random random, int x, int z, int groups, Block... plants) {
        for (int i = 0; i < groups; i++) {
            int cx = x + 3 + random.nextInt(10);
            int cz = z + 3 + random.nextInt(10);
            int y = floor(world, random, cx, cz);
            if (y < 0) {
                continue;
            }
            for (int j = 0; j < 8; j++) {
                int bx = cx + random.nextInt(7) - 3;
                int bz = cz + random.nextInt(7) - 3;
                for (int by = y + 2; by >= y - 2; by--) {
                    if (world.isAirBlock(bx, by, bz) && !world.isAirBlock(bx, by - 1, bz)) {
                        Block plant = plants[random.nextInt(plants.length)];
                        if (plant.canBlockStay(world, bx, by, bz)) {
                            world.setBlock(bx, by, bz, plant, 0, 2);
                        }
                        break;
                    }
                }
            }
        }
    }

    /** Cristaux poses sur le sol (et parfois au plafond). */
    public static void crystals(World world, Random random, int x, int z, int count, Block crystal) {
        for (int i = 0; i < count; i++) {
            int bx = x + random.nextInt(16);
            int bz = z + random.nextInt(16);
            int y = floor(world, random, bx, bz);
            if (y > 0 && crystal.canBlockStay(world, bx, y, bz)) {
                world.setBlock(bx, y, bz, crystal, 0, 2);
            }
        }
    }

    /** Colonnes (pics de glace, aiguilles de quartz) dressees sur le sol. */
    public static void spikes(World world, Random random, int x, int z, int count, Block block, int minHeight, int maxHeight) {
        for (int i = 0; i < count; i++) {
            int bx = x + 2 + random.nextInt(12);
            int bz = z + 2 + random.nextInt(12);
            int y = floor(world, random, bx, bz);
            if (y < 0) {
                continue;
            }
            int height = minHeight + random.nextInt(maxHeight - minHeight + 1);
            for (int dy = 0; dy < height && world.isAirBlock(bx, y + dy, bz); dy++) {
                world.setBlock(bx, y + dy, bz, block, 0, 2);
                if (dy < height / 3) {
                    for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        if (random.nextInt(3) > 0 && world.isAirBlock(bx + d[0], y + dy, bz + d[1])) {
                            world.setBlock(bx + d[0], y + dy, bz + d[1], block, 0, 2);
                        }
                    }
                }
            }
        }
    }

    /** Mares (de lave ou d'autre chose) creusees dans le sol. */
    public static void pools(World world, Random random, int x, int z, int chance, Block liquid) {
        if (random.nextInt(chance) == 0) {
            int bx = x + random.nextInt(16);
            int bz = z + random.nextInt(16);
            int y = floor(world, random, bx, bz);
            if (y > 40) {
                new WorldGenLakes(liquid).generate(world, random, bx, y, bz);
            }
        }
    }

    /** Couche de sol du biome : remplace le dessus de la netherrack (et la sous-couche) sur tout le carre. */
    public static void surface(World world, int x, int z) {
        for (int bx = x; bx < x + 16; bx++) {
            for (int bz = z; bz < z + 16; bz++) {
                if (!(world.getBiomeGenForCoords(bx, bz) instanceof com.netheryx.world.biome.BiomeNether)) {
                    continue;
                }
                com.netheryx.world.biome.BiomeNether biome = (com.netheryx.world.biome.BiomeNether) world.getBiomeGenForCoords(bx, bz);
                if (biome.surface == Blocks.netherrack) {
                    continue;
                }
                for (int y = 120; y > 31; y--) {
                    if (world.getBlock(bx, y, bz) == Blocks.netherrack && world.isAirBlock(bx, y + 1, bz)) {
                        world.setBlock(bx, y, bz, biome.surface, 0, 2);
                        for (int d = 1; d <= 2; d++) {
                            if (world.getBlock(bx, y - d, bz) == Blocks.netherrack && biome.filler != Blocks.netherrack) {
                                world.setBlock(bx, y - d, bz, biome.filler, 0, 2);
                            }
                        }
                        y -= 3;
                    }
                }
            }
        }
    }
}
