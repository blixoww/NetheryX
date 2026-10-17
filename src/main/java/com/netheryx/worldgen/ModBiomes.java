package com.netheryx.worldgen;

import com.netheryx.NetheryX;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/** Cles des biomes NetheryX (definis en JSON dans {@code data/netheryx/worldgen/biome}). */
public final class ModBiomes {
    public static final ResourceKey<Biome> EMBER_MEADOW = key("ember_meadow");
    public static final ResourceKey<Biome> EMBER_GROVE = key("ember_grove");
    public static final ResourceKey<Biome> ASHEN_WASTES = key("ashen_wastes");
    public static final ResourceKey<Biome> ONYX_DEPTHS = key("onyx_depths");
    public static final ResourceKey<Biome> SULFUR_SPRINGS = key("sulfur_springs");
    public static final ResourceKey<Biome> SOULFIRE_MARSH = key("soulfire_marsh");
    public static final ResourceKey<Biome> WITHERED_FOREST = key("withered_forest");
    public static final ResourceKey<Biome> MAGMA_RIFT = key("magma_rift");

    private ModBiomes() {
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, NetheryX.id(name));
    }
}
