package com.netheryx.worldgen;

import com.netheryx.NetheryX;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Cles des features configurees referencees par le code.
 * Les features elles-memes sont definies en JSON dans {@code data/netheryx/worldgen}.
 */
public final class ModFeatureKeys {
    public static final ResourceKey<ConfiguredFeature<?, ?>> EMBER_FUNGUS_PLANTED = key("ember_fungus_planted");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WITHERED_FUNGUS_PLANTED = key("withered_fungus_planted");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEADOW_VEGETATION_BONEMEAL = key("meadow_vegetation_bonemeal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> EMBER_VEGETATION_BONEMEAL = key("ember_vegetation_bonemeal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARROW_VEGETATION_BONEMEAL = key("marrow_vegetation_bonemeal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WART_VEGETATION_BONEMEAL = key("wart_vegetation_bonemeal");

    private ModFeatureKeys() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, NetheryX.id(name));
    }
}
