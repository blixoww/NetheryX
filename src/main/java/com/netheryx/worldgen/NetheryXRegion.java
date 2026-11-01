package com.netheryx.worldgen;

import com.mojang.datafixers.util.Pair;
import com.netheryx.NetheryX;
import com.netheryx.config.NetheryXConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import java.util.function.Consumer;

/**
 * Region TerraBlender du Nether. TerraBlender repartit les regions de plusieurs mods dans le meme
 * monde, ce qui garantit la compatibilite avec Biomes O' Plenty, Incendium (Forge), etc.
 */
public class NetheryXRegion extends Region {
    private static final float COLD = -0.345F;
    private static final float COOL = -0.1F;
    private static final float WARM = 0.1F;
    private static final float HOT = 0.345F;
    private static final float DRY = -0.345F;
    private static final float DAMP = -0.1F;
    private static final float MOIST = 0.1F;
    private static final float WET = 0.345F;

    public NetheryXRegion(int weight) {
        super(NetheryX.id("nether"), RegionType.NETHER, weight);
    }

    public static void registerRegionAndSurfaceRules() {
        int weight = NetheryXConfig.REGION_WEIGHT.get();
        if (weight > 0) {
            Regions.register(new NetheryXRegion(weight));
        }
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.NETHER, NetheryX.MOD_ID, NetheryXSurfaceRules.makeRules());
    }

    /**
     * Grille de 4 x 4 cases temperature / humidite. Le climat du Nether se concentre pres de zero : les graduations
     * sont les quartiles de sa distribution (mesures avec la sonde des GameTests), pour que chaque biome ait une part
     * comparable. Un point place aux extremes (+/- 0,95) ne genere presque rien.
     */
    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        add(mapper, COLD, DRY, ModBiomes.ASHEN_WASTES);
        add(mapper, COLD, DAMP, ModBiomes.WITHERED_FOREST);
        add(mapper, COLD, MOIST, ModBiomes.RIMEFROST_CHASM);
        add(mapper, COLD, WET, ModBiomes.MARROW_THICKET);

        add(mapper, COOL, DRY, ModBiomes.QUARTZ_REEF);
        add(mapper, COOL, DAMP, Biomes.NETHER_WASTES);
        add(mapper, COOL, MOIST, Biomes.SOUL_SAND_VALLEY);
        add(mapper, COOL, WET, ModBiomes.PETRIFIED_ONYX_FOREST);

        add(mapper, WARM, DRY, ModBiomes.SULFUR_SPRINGS);
        add(mapper, WARM, DAMP, Biomes.BASALT_DELTAS);
        add(mapper, WARM, MOIST, ModBiomes.WARTVINE_TANGLE);
        add(mapper, WARM, WET, ModBiomes.EMBER_MEADOW);

        add(mapper, HOT, DRY, ModBiomes.MOLTEN_GLASS_LAKE);
        add(mapper, HOT, DAMP, ModBiomes.EMBER_GROVE);
        add(mapper, HOT, MOIST, ModBiomes.HELLWOOD_JUNGLE);
        add(mapper, HOT, WET, Biomes.CRIMSON_FOREST);
    }

    private void add(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                     float temperature, float humidity, ResourceKey<Biome> biome) {
        this.addBiome(mapper, Climate.parameters(temperature, humidity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biome);
    }
}
