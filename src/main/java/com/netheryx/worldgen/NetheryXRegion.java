package com.netheryx.worldgen;

import com.mojang.datafixers.util.Pair;
import com.netheryx.NetheryX;
import com.netheryx.config.NetheryXConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
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
    private static final float LOW = -0.22F;
    private static final float MID = 0.0F;
    private static final float HIGH = 0.22F;
    private static final float[] STEPS = {LOW, MID, HIGH};

    /**
     * Trois regions, chacune avec une famille de biomes sur une grille de 3 x 3 (temperature en ligne, humidite en colonne).
     * Avec peu de biomes par region, chacun s'etend bien plus largement, et le Nether alterne de vraies contrees. Un biome
     * repete sur des cases voisines forme une seule grande etendue.
     */
    private final ResourceKey<Biome>[][] grid;

    @SafeVarargs
    private NetheryXRegion(String name, int weight, ResourceKey<Biome>[]... grid) {
        super(NetheryX.id(name), RegionType.NETHER, weight);
        this.grid = grid;
    }

    @SafeVarargs
    private static ResourceKey<Biome>[] row(ResourceKey<Biome>... biomes) {
        return biomes;
    }

    public static void registerRegionAndSurfaceRules() {
        int weight = NetheryXConfig.REGION_WEIGHT.get();
        if (weight > 0) {
            int each = Math.max(1, weight / 3);
            // Contrees de feu : prairies et forets embrasees, soufre et verre en fusion
            Regions.register(new NetheryXRegion("nether_ember", each,
                    row(ModBiomes.MOLTEN_GLASS_LAKE, ModBiomes.EMBER_GROVE, ModBiomes.HELLWOOD_JUNGLE),
                    row(ModBiomes.SULFUR_SPRINGS, ModBiomes.EMBER_MEADOW, ModBiomes.HELLWOOD_JUNGLE),
                    row(ModBiomes.SULFUR_SPRINGS, ModBiomes.EMBER_MEADOW, ModBiomes.EMBER_GROVE)));
            // Contrees mortes : cendre, bois fletri, os et givre
            Regions.register(new NetheryXRegion("nether_ash", each,
                    row(ModBiomes.ASHEN_WASTES, ModBiomes.ASHEN_WASTES, ModBiomes.WITHERED_FOREST),
                    row(ModBiomes.RIMEFROST_CHASM, ModBiomes.WITHERED_FOREST, ModBiomes.MARROW_THICKET),
                    row(ModBiomes.RIMEFROST_CHASM, ModBiomes.RIMEFROST_CHASM, ModBiomes.MARROW_THICKET)));
            // Contrees minerales et vivantes : quartz, onyx et verrues
            Regions.register(new NetheryXRegion("nether_crystal", each,
                    row(ModBiomes.QUARTZ_REEF, ModBiomes.QUARTZ_REEF, ModBiomes.PETRIFIED_ONYX_FOREST),
                    row(ModBiomes.QUARTZ_REEF, ModBiomes.WARTVINE_TANGLE, ModBiomes.PETRIFIED_ONYX_FOREST),
                    row(ModBiomes.WARTVINE_TANGLE, ModBiomes.WARTVINE_TANGLE, ModBiomes.PETRIFIED_ONYX_FOREST)));
        }
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.NETHER, NetheryX.MOD_ID, NetheryXSurfaceRules.makeRules());
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        for (int t = 0; t < 3; t++) {
            for (int h = 0; h < 3; h++) {
                this.addBiome(mapper, Climate.parameters(STEPS[t], STEPS[h], 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), this.grid[t][h]);
            }
        }
    }
}
