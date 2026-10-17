package com.netheryx;

import com.mojang.logging.LogUtils;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.network.NetheryXNetwork;
import com.netheryx.registry.ModBlockEntities;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModCreativeTabs;
import com.netheryx.registry.ModEffects;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModFeatures;
import com.netheryx.registry.ModRecipes;
import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModItems;
import com.netheryx.registry.ModLootModifiers;
import com.netheryx.registry.ModParticles;
import com.netheryx.registry.ModStructureTypes;
import com.netheryx.registry.ModTiers;
import com.netheryx.util.CompatSetup;
import com.netheryx.worldgen.NetheryXRegion;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Point d'entree du mod NetheryX.
 * <p>
 * Toutes les inscriptions passent par des {@code DeferredRegister} afin de rester compatibles
 * avec les autres mods et avec les futures versions de Forge.
 */
@Mod(NetheryX.MOD_ID)
public final class NetheryX {
    public static final String MOD_ID = "netheryx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NetheryX() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NetheryXConfig.SPEC);

        ModTiers.init();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModStructureTypes.STRUCTURE_TYPES.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModEffects.POTIONS.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);

        NetheryXNetwork.register();
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetheryXRegion.registerRegionAndSurfaceRules();
            CompatSetup.registerVanillaHooks();
        });
    }

    /** Raccourci pour construire un identifiant dans l'espace de noms du mod. */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
