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

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Un seul point vanilla pour garder des transitions naturelles ; chaque biome NetheryX occupe deux points
        // du climat, ce qui les rend nettement plus frequents dans la region.
        add(mapper, 0.0F, 0.0F, 0.0F, Biomes.NETHER_WASTES);

        add(mapper, 0.5F, 0.7F, 0.0F, ModBiomes.EMBER_MEADOW);
        add(mapper, 0.2F, 0.2F, 0.05F, ModBiomes.EMBER_MEADOW);
        add(mapper, 0.7F, 0.3F, 0.0F, ModBiomes.EMBER_GROVE);
        add(mapper, 0.4F, 0.0F, 0.05F, ModBiomes.EMBER_GROVE);
        add(mapper, -0.4F, -0.4F, 0.1F, ModBiomes.ASHEN_WASTES);
        add(mapper, -0.1F, -0.6F, 0.05F, ModBiomes.ASHEN_WASTES);
        add(mapper, -0.6F, 0.5F, 0.2F, ModBiomes.ONYX_DEPTHS);
        add(mapper, -0.3F, 0.25F, 0.15F, ModBiomes.ONYX_DEPTHS);
        add(mapper, 0.35F, -0.7F, 0.1F, ModBiomes.SULFUR_SPRINGS);
        add(mapper, 0.6F, -0.5F, 0.05F, ModBiomes.SULFUR_SPRINGS);
        add(mapper, -0.2F, 0.8F, 0.1F, ModBiomes.SOULFIRE_MARSH);
        add(mapper, 0.0F, -0.5F, 0.0F, ModBiomes.SOULFIRE_MARSH);
        add(mapper, -0.8F, -0.1F, 0.15F, ModBiomes.WITHERED_FOREST);
        add(mapper, -0.6F, -0.7F, 0.1F, ModBiomes.WITHERED_FOREST);
        add(mapper, 0.9F, -0.3F, 0.2F, ModBiomes.MAGMA_RIFT);
        add(mapper, 0.85F, 0.0F, 0.15F, ModBiomes.MAGMA_RIFT);
        add(mapper, -0.5F, 0.95F, 0.15F, ModBiomes.MARROW_THICKET);
        add(mapper, -0.75F, 0.75F, 0.1F, ModBiomes.MARROW_THICKET);
        add(mapper, -0.95F, 0.4F, 0.2F, ModBiomes.RIMEFROST_CHASM);
        add(mapper, -0.95F, 0.0F, 0.15F, ModBiomes.RIMEFROST_CHASM);
        add(mapper, 0.25F, 0.45F, 0.1F, ModBiomes.WARTVINE_TANGLE);
        add(mapper, 0.45F, 0.9F, 0.05F, ModBiomes.WARTVINE_TANGLE);
        add(mapper, 0.1F, -0.95F, 0.15F, ModBiomes.QUARTZ_REEF);
        add(mapper, 0.35F, -0.3F, 0.1F, ModBiomes.QUARTZ_REEF);
        add(mapper, 0.65F, 0.6F, 0.1F, ModBiomes.HELLWOOD_JUNGLE);
        add(mapper, 0.3F, 0.75F, 0.05F, ModBiomes.HELLWOOD_JUNGLE);
        add(mapper, 0.95F, 0.5F, 0.15F, ModBiomes.MOLTEN_GLASS_LAKE);
        add(mapper, 0.75F, -0.85F, 0.1F, ModBiomes.MOLTEN_GLASS_LAKE);
        add(mapper, -0.7F, 0.15F, 0.2F, ModBiomes.PETRIFIED_ONYX_FOREST);
        add(mapper, -0.35F, -0.25F, 0.15F, ModBiomes.PETRIFIED_ONYX_FOREST);
    }

    private void add(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                     float temperature, float humidity, float offset, ResourceKey<Biome> biome) {
        this.addBiome(mapper, Climate.parameters(temperature, humidity, 0.0F, 0.0F, 0.0F, 0.0F, offset), biome);
    }
}
