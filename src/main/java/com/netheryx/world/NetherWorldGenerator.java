package com.netheryx.world;

import com.netheryx.NetheryXConfig;
import com.netheryx.registry.ModBlocks;
import com.netheryx.world.biome.BiomeNether;
import com.netheryx.world.structure.NetherStructures;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.Random;

/**
 * Generation de NetheryX dans le Nether : minerais partout, puis, pour chaque chunk, sol et decoration du biome
 * (voir {@link com.netheryx.registry.ModBiomes}) et structures.
 */
public class NetherWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider generator, IChunkProvider provider) {
        if (world.provider.dimensionId != -1) {
            return;
        }
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        ores(world, random, baseX, baseZ, ModBlocks.onyxOre, 4, NetheryXConfig.onyxVeinsPerChunk, 10, 118);
        ores(world, random, baseX, baseZ, ModBlocks.sulfurOre, 8, NetheryXConfig.sulfurVeinsPerChunk, 10, 118);
        ores(world, random, baseX, baseZ, ModBlocks.richQuartzOre, 6, NetheryXConfig.richQuartzVeinsPerChunk, 10, 118);
        ores(world, random, baseX, baseZ, ModBlocks.sulfurRock, 24, 1, 20, 100);
        ores(world, random, baseX, baseZ, ModBlocks.rimestone, 20, NetheryXConfig.rimestoneBlobsPerChunk, 10, 60);

        // Sol et decoration : carre decale de +8 (au centre des chunks deja generes)
        int x = baseX + 8;
        int z = baseZ + 8;
        NetherDecor.surface(world, x, z);
        BiomeGenBase biome = world.getBiomeGenForCoords(x + 8, z + 8);
        if (biome instanceof BiomeNether) {
            ((BiomeNether) biome).decorateChunk(world, random, x, z);
        } else {
            // Nether vanilla : un peu de flore et quelques arbres, comme avant les biomes
            NetherDecor.plants(world, random, x, z, 1, ModBlocks.netherGrass, ModBlocks.glowcap, ModBlocks.crimsonBellflower);
        }
        NetherStructures.generate(world, random, chunkX, chunkZ);
    }

    private static void ores(World world, Random random, int baseX, int baseZ, Block ore, int size, int count, int minY, int maxY) {
        WorldGenMinable vein = new WorldGenMinable(ore, size, Blocks.netherrack);
        for (int i = 0; i < count; i++) {
            vein.generate(world, random, baseX + random.nextInt(16), minY + random.nextInt(maxY - minY), baseZ + random.nextInt(16));
        }
    }
}
