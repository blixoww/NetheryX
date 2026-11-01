package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Grandes formes qui donnent leur silhouette et leur lumiere aux biomes : colonnes geantes du sol a la voute, gerbes de
 * cristaux, grappes lumineuses suspendues. Chaque biome les decline avec ses propres blocs (voir data_v150.py).
 */
public final class BiomeDecorFeatures {
    private BiomeDecorFeatures() {
    }

    /** Bloc principal, bloc d'accent (souvent lumineux) et taille. */
    public record Config(BlockState main, BlockState accent, int size) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("main").forGetter(Config::main),
                BlockState.CODEC.fieldOf("accent").forGetter(Config::accent),
                Codec.intRange(1, 12).fieldOf("size").forGetter(Config::size)
        ).apply(instance, Config::new));
    }

    /** Colonne en sablier, du sol a la voute : la grande verticale qui manque au Nether. */
    public static class Pillar extends Feature<Config> {
        public Pillar(Codec<Config> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<Config> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            RandomSource random = context.random();
            Config config = context.config();
            if (!level.getBlockState(origin).isAir() || level.getBlockState(origin.below()).isAir()) {
                return false;
            }
            int height = FeatureHelper.ceilingDistance(level, origin, 48);
            if (height < 8) {
                return false;
            }
            float wide = Math.min(6.0F, config.size() + random.nextFloat() * 1.5F);
            float waist = 0.7F + random.nextFloat() * 0.6F;
            int band = 2 + random.nextInt(height - 2);
            for (int y = -1; y <= height; y++) {
                float t = Mth.clamp((float) y / height, 0.0F, 1.0F);
                float bulge = (2.0F * t - 1.0F) * (2.0F * t - 1.0F);
                float radius = waist + (wide - waist) * bulge * bulge;
                int reach = Mth.ceil(radius);
                for (int dx = -reach; dx <= reach; dx++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        double d = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.5D;
                        if (d <= radius) {
                            boolean accent = Math.abs(y - band) <= 1 && d >= radius - 1.2D;
                            FeatureHelper.set(level, origin.offset(dx, y, dz), accent ? config.accent() : config.main());
                        }
                    }
                }
            }
            return true;
        }
    }

    /** Gerbe de grands cristaux inclines, qui partent d'un meme point. */
    public static class CrystalBurst extends Feature<Config> {
        public CrystalBurst(Codec<Config> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<Config> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            RandomSource random = context.random();
            Config config = context.config();
            if (!level.getBlockState(origin).isAir() || level.getBlockState(origin.below()).isAir()) {
                return false;
            }
            int shards = 3 + random.nextInt(3);
            for (int i = 0; i < shards; i++) {
                double yaw = random.nextDouble() * Math.PI * 2.0D;
                double tilt = i == 0 ? 0.05D : 0.2D + random.nextDouble() * 0.5D;
                int length = config.size() + random.nextInt(config.size() + 1) + (i == 0 ? 2 : 0);
                double thick = 1.1D + length * 0.09D;
                for (int k = 0; k <= length; k++) {
                    double t = (double) k / length;
                    double cx = Math.cos(yaw) * tilt * k;
                    double cz = Math.sin(yaw) * tilt * k;
                    double radius = thick * (1.0D - t * t);
                    int reach = Mth.ceil(radius);
                    for (int dx = -reach; dx <= reach; dx++) {
                        for (int dz = -reach; dz <= reach; dz++) {
                            if (dx * dx + dz * dz <= radius * radius + 0.2D) {
                                BlockState state = k >= length - 1 || random.nextFloat() < 0.08F ? config.accent() : config.main();
                                FeatureHelper.set(level, origin.offset(Mth.floor(cx) + dx, k - 1, Mth.floor(cz) + dz), state);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** Grappe lumineuse accrochee a la voute : une masse, des coulures, et de la vraie lumiere dans les cavernes. */
    public static class GlowCluster extends Feature<Config> {
        public GlowCluster(Codec<Config> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<Config> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            RandomSource random = context.random();
            Config config = context.config();
            if (!level.getBlockState(origin).isAir() || level.getBlockState(origin.above()).isAir()) {
                return false;
            }
            int radius = Math.max(1, config.size());
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = 0; dy >= -radius; dy--) {
                        double d = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.6D) + random.nextDouble() * 0.7D;
                        if (d <= radius + 0.2D) {
                            FeatureHelper.set(level, origin.offset(dx, dy, dz), random.nextFloat() < 0.45F ? config.accent() : config.main());
                        }
                    }
                }
            }
            for (int i = 0; i < radius * 3; i++) {
                BlockPos.MutableBlockPos drip = origin.offset(random.nextInt(radius * 2 + 1) - radius, -radius, random.nextInt(radius * 2 + 1) - radius).mutable();
                int length = 1 + random.nextInt(radius + 3);
                for (int k = 0; k < length; k++) {
                    FeatureHelper.set(level, drip, k == length - 1 ? config.accent() : config.main());
                    drip.move(0, -1, 0);
                }
            }
            return true;
        }
    }
}
