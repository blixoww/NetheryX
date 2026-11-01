package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.List;

/**
 * Terres sur la mer de lave : selon un bruit continu d'un chunk a l'autre, la lave du biome laisse place a des plateaux en
 * terrasses (Prairie, Jungle...), des banquises (Gouffre de givre) ou des recifs (Recif de quartz). Le biome a ainsi son propre
 * relief, au lieu d'un lac de lave plat. La feature passe une fois par chunk et traite chaque colonne de son biome.
 */
public class LandmassFeature extends Feature<LandmassFeature.Config> {
    public record Config(ResourceLocation biome, BlockState top, BlockState body, float threshold, int height) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("biome").forGetter(Config::biome),
                BlockState.CODEC.fieldOf("top").forGetter(Config::top),
                BlockState.CODEC.fieldOf("body").forGetter(Config::body),
                Codec.floatRange(-2.0F, 2.0F).fieldOf("threshold").forGetter(Config::threshold),
                Codec.intRange(0, 24).fieldOf("height").forGetter(Config::height)
        ).apply(instance, Config::new));
    }

    private static final double SCALE = 0.018D;
    private static final int STEP = 2;
    private static final int BURIED = 4;
    private static volatile Noise cached;

    private record Noise(long seed, PerlinSimplexNoise noise) {
    }

    public LandmassFeature(Codec<Config> codec) {
        super(codec);
    }

    private static PerlinSimplexNoise noise(long seed) {
        Noise current = cached;
        if (current == null || current.seed() != seed) {
            current = new Noise(seed, new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(seed ^ 0x4E455448L)), List.of(0, 1, 2)));
            cached = current;
        }
        return current.noise();
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        Config config = context.config();
        ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, config.biome());
        PerlinSimplexNoise noise = noise(level.getSeed());
        int lavaTop = context.chunkGenerator().getSeaLevel() - 1;
        int minX = context.origin().getX() & ~15;
        int minZ = context.origin().getZ() & ~15;
        boolean changed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                pos.set(x, lavaTop, z);
                if (!level.getBlockState(pos).is(Blocks.LAVA) || !level.getBlockState(pos.above()).isAir() || !level.getBiome(pos).is(biome)) {
                    continue;
                }
                double value = noise.getValue(x * SCALE, z * SCALE, false);
                if (value < config.threshold()) {
                    continue;
                }
                // Terrasses : la hauteur monte par paliers, comme des plateaux
                double t = Math.min(1.0D, (value - config.threshold()) / 0.9D);
                int surface = (int) (t * config.height()) / STEP * STEP;
                for (int y = lavaTop - BURIED; y <= lavaTop + surface; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.LAVA)) {
                        level.setBlock(pos, y == lavaTop + surface ? config.top() : config.body(), Block.UPDATE_CLIENTS);
                    }
                }
                changed = true;
            }
        }
        return changed;
    }
}
