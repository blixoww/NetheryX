package com.netheryx.world.biome;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Repartition des biomes du Nether : un pavage de Voronoi (cellules d'environ 110 blocs aux bords irreguliers),
 * chaque cellule recevant un biome tire d'apres la graine du monde. Le Nether vanilla (« Enfer ») en fait partie.
 */
public class NetherBiomeManager extends WorldChunkManagerHell {
    private static final int CELL = 112;
    private final long seed;
    private final BiomeGenBase[] biomes;

    public NetherBiomeManager(long seed, BiomeGenBase[] biomes) {
        super(BiomeGenBase.hell, 0.0F);
        this.seed = seed;
        this.biomes = biomes;
    }

    private long hash(long a, long b) {
        long h = this.seed ^ (a * 0x9E3779B97F4A7C15L) ^ (b * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 31;
        h *= 0x7FB5D329728EA185L;
        h ^= h >>> 27;
        return h;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        // Bords ondules : petit decalage pseudo-aleatoire de la position avant de chercher la cellule
        int wx = x + (int) ((hash(x >> 4, z >> 4) & 15) - 8);
        int wz = z + (int) (((hash(z >> 4, x >> 4)) & 15) - 8);
        int cx = Math.floorDiv(wx, CELL);
        int cz = Math.floorDiv(wz, CELL);
        long best = Long.MAX_VALUE;
        long bestHash = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long h = hash(cx + dx, cz + dz);
                int px = (cx + dx) * CELL + (int) ((h >>> 8) & 0x7F) % CELL;
                int pz = (cz + dz) * CELL + (int) ((h >>> 20) & 0x7F) % CELL;
                long d = (long) (px - wx) * (px - wx) + (long) (pz - wz) * (pz - wz);
                if (d < best) {
                    best = d;
                    bestHash = h;
                }
            }
        }
        return this.biomes[(int) ((bestHash >>> 40) % this.biomes.length)];
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] array, int x, int z, int width, int depth) {
        if (array == null || array.length < width * depth) {
            array = new BiomeGenBase[width * depth];
        }
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                array[i * width + j] = getBiomeGenAt((x + j) * 4, (z + i) * 4);
            }
        }
        return array;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] array, int x, int z, int width, int depth) {
        return getBiomeGenAt(array, x, z, width, depth, false);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] array, int x, int z, int width, int depth, boolean cache) {
        if (array == null || array.length < width * depth) {
            array = new BiomeGenBase[width * depth];
        }
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                array[i * width + j] = getBiomeGenAt(x + j, z + i);
            }
        }
        return array;
    }

    @Override
    public float[] getRainfall(float[] array, int x, int z, int width, int depth) {
        if (array == null || array.length < width * depth) {
            array = new float[width * depth];
        }
        Arrays.fill(array, 0, width * depth, 0.0F);
        return array;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ChunkPosition findBiomePosition(int x, int z, int range, List allowed, Random random) {
        for (int i = 0; i < 64; i++) {
            int px = x - range + random.nextInt(range * 2 + 1);
            int pz = z - range + random.nextInt(range * 2 + 1);
            if (allowed.contains(getBiomeGenAt(px, pz))) {
                return new ChunkPosition(px, 0, pz);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean areBiomesViable(int x, int z, int radius, List allowed) {
        for (int dx = -radius; dx <= radius; dx += 4) {
            for (int dz = -radius; dz <= radius; dz += 4) {
                if (!allowed.contains(getBiomeGenAt(x + dx, z + dz))) {
                    return false;
                }
            }
        }
        return true;
    }
}
