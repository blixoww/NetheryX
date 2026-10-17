package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.worldgen.feature.BoulderFeature;
import com.netheryx.worldgen.feature.FallenLogFeature;
import com.netheryx.worldgen.feature.HellTreeFeature;
import com.netheryx.worldgen.feature.LavaIslandFeature;
import com.netheryx.worldgen.feature.ObsidianSpireFeature;
import com.netheryx.worldgen.feature.PetrifiedTreeFeature;
import com.netheryx.worldgen.feature.MarrowTreeFeature;
import com.netheryx.worldgen.feature.QuartzSpireFeature;
import com.netheryx.worldgen.feature.RimeSpikeFeature;
import com.netheryx.worldgen.feature.WartTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Types de features de generation propres au mod (utilises par les JSON de {@code worldgen/configured_feature}). */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, NetheryX.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MARROW_TREE = FEATURES.register("marrow_tree",
            () -> new MarrowTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> WART_TREE = FEATURES.register("wart_tree",
            () -> new WartTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> QUARTZ_SPIRE = FEATURES.register("quartz_spire",
            () -> new QuartzSpireFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> RIME_SPIKE = FEATURES.register("rime_spike",
            () -> new RimeSpikeFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> HELL_TREE = FEATURES.register("hell_tree",
            () -> new HellTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<BlockStateConfiguration>> BOULDER = FEATURES.register("boulder",
            () -> new BoulderFeature(BlockStateConfiguration.CODEC));
    public static final RegistryObject<Feature<BlockStateConfiguration>> FALLEN_LOG = FEATURES.register("fallen_log",
            () -> new FallenLogFeature(BlockStateConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OBSIDIAN_SPIRE = FEATURES.register("obsidian_spire",
            () -> new ObsidianSpireFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LAVA_ISLAND = FEATURES.register("lava_island",
            () -> new LavaIslandFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> PETRIFIED_TREE = FEATURES.register("petrified_tree",
            () -> new PetrifiedTreeFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }
}
