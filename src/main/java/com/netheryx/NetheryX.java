package com.netheryx;

import com.netheryx.altar.GuiHandler;
import com.netheryx.altar.TileSoulAltar;
import com.netheryx.event.NetheryXEvents;
import com.netheryx.proxy.CommonProxy;
import com.netheryx.registry.ModBiomes;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModEnchantments;
import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModItems;
import com.netheryx.registry.ModRecipes;
import com.netheryx.world.NetherWorldGenerator;
import com.netheryx.world.structure.LootTables;
import com.netheryx.world.structure.NetherStructures;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NetheryX pour Minecraft 1.7.10 (Forge 10.13.4.1614).
 * <p>
 * Portage reecrit de zero depuis la version 1.20.1 : la 1.7.10 n'a ni fichiers de donnees (recettes, butins et biomes
 * sont en Java), ni « aplatissement » (les variantes d'un bloc passent par ses metadonnees), ni bibliotheques
 * modernes (GeckoLib, TerraBlender, Patchouli). Tout le contenu y est : biomes, creatures, structures et donjons,
 * gardiens et Coeur du Nether, wyverne, autel des ames, infusions, boussole, pierre de rappel et Codex.
 */
@Mod(modid = NetheryX.MOD_ID, name = NetheryX.NAME, version = NetheryX.VERSION, acceptedMinecraftVersions = "[1.7.10]")
public class NetheryX {
    public static final String MOD_ID = "netheryx";
    public static final String NAME = "NetheryX";
    public static final String VERSION = "1.0.0";
    public static final Logger LOG = LogManager.getLogger(NAME);

    @Mod.Instance(MOD_ID)
    public static NetheryX instance;

    @SidedProxy(clientSide = "com.netheryx.proxy.ClientProxy", serverSide = "com.netheryx.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetheryXConfig.load(event.getSuggestedConfigurationFile());
        ModBlocks.register();
        ModItems.register();
        ModEnchantments.register();
        ModEntities.register();
        ModBiomes.register();
        GameRegistry.registerTileEntity(TileSoulAltar.class, MOD_ID + ":soul_altar");
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModRecipes.register();
        LootTables.register();
        NetherStructures.register();
        GameRegistry.registerWorldGenerator(new NetherWorldGenerator(), 10);
        NetheryXEvents events = new NetheryXEvents();
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance().bus().register(events);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOG.info("NetheryX {} pret.", VERSION);
    }

    /** Nom de texture ou de traduction prefixe par l'identifiant du mod. */
    public static String id(String name) {
        return MOD_ID + ":" + name;
    }
}
