package com.netheryx.registry;

import com.netheryx.block.BlockAsh;
import com.netheryx.block.BlockBasic;
import com.netheryx.block.BlockBerryBush;
import com.netheryx.block.BlockClearGlass;
import com.netheryx.block.BlockCrystal;
import com.netheryx.block.BlockNetherCrop;
import com.netheryx.block.BlockNetherOre;
import com.netheryx.block.BlockNetherPlant;
import com.netheryx.block.BlockNetherSoil;
import com.netheryx.block.BlockPillar;
import com.netheryx.block.BlockTranslucent;
import com.netheryx.block.BlockWarded;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraftforge.oredict.OreDictionary;

/** Tous les blocs du mod (identifiants de registre identiques a ceux de la version 1.20.1). */
public final class ModBlocks {
    // Onyx
    public static Block onyxOre;
    public static Block onyxBlock;
    public static Block polishedOnyx;
    public static Block onyxBricks;
    // Soufre, quartz, roches
    public static Block sulfurOre;
    public static Block sulfurBlock;
    public static Block sulfurRock;
    public static Block richQuartzOre;
    public static Block ashBlock;
    public static Block magmaBricks;
    public static Block rimestone;
    public static Block rimestoneBricks;
    // Verre et lumieres
    public static Block quartzGlass;
    public static Block quartzLamp;
    public static Block emberLamp;
    // Bois de braise et bois fletri
    public static Block emberStem;
    public static Block emberPlanks;
    public static Block emberWartBlock;
    public static Block witheredStem;
    public static Block witheredPlanks;
    public static Block witheredWartBlock;
    public static Block petrifiedLog;
    // Sols et flore
    public static Block emberNylium;
    public static Block netherGrassBlock;
    public static Block netherGrass;
    public static Block glowcap;
    public static Block emberBloom;
    public static Block ashDaisy;
    public static Block crimsonBellflower;
    public static Block sulfurTulip;
    public static Block soulLily;
    public static Block blazingGrass;
    public static Block charredShrub;
    public static Block emberRoots;
    public static Block emberFungus;
    public static Block witheredFungus;
    public static Block marrowSprouts;
    // Bois de moelle et de verrutige, autres sols
    public static Block marrowStem;
    public static Block marrowPlanks;
    public static Block marrowCap;
    public static Block wartstalkStem;
    public static Block wartstalkPlanks;
    public static Block wartBulb;
    public static Block witheredSoil;
    public static Block ossifiedNylium;
    public static Block wartMoss;
    public static Block onyxCrystalLeaves;
    public static Block rimeIce;
    // Cristaux
    public static Block onyxCluster;
    public static Block sulfurCluster;
    public static Block quartzCluster;
    public static Block rimeCrystal;
    // Cultures (sans objet : la graine ou le fruit pose le bloc)
    public static Block blazePepperCrop;
    public static Block cinderWheatCrop;
    public static Block soulTuberCrop;
    public static Block fireBerryBush;
    // Donjons
    public static Block wardedBricks;
    public static Block guardianSeal;
    // Autel des ames et lave croutee (semelles de magma)
    public static Block soulAltar;
    public static Block crustedLava;

    private ModBlocks() {
    }

    public static void register() {
        onyxOre = add(new BlockNetherOre("onyx_ore", 2, () -> ModItems.onyx, 1, 1, 3, 7), "onyx_ore");
        onyxBlock = add(new BlockBasic("onyx_block", Material.rock, 5.0F, 1200.0F, Block.soundTypeStone).tool("pickaxe", 2), "onyx_block");
        polishedOnyx = add(new BlockBasic("polished_onyx", Material.rock, 4.0F, 600.0F, Block.soundTypeStone).tool("pickaxe", 2), "polished_onyx");
        onyxBricks = add(new BlockBasic("onyx_bricks", Material.rock, 4.0F, 600.0F, Block.soundTypeStone).tool("pickaxe", 2), "onyx_bricks");

        sulfurOre = add(new BlockNetherOre("sulfur_ore", 1, () -> ModItems.sulfur, 2, 5, 1, 4), "sulfur_ore");
        sulfurBlock = add(new BlockBasic("sulfur_block", Material.rock, 1.5F, 6.0F, Block.soundTypeStone).tool("pickaxe", 0), "sulfur_block");
        sulfurRock = add(new BlockBasic("sulfur_rock", Material.rock, 1.0F, 4.0F, Block.soundTypeStone).tool("pickaxe", 0), "sulfur_rock");
        richQuartzOre = add(new BlockNetherOre("rich_quartz_ore", 1, () -> Items.quartz, 2, 4, 2, 5), "rich_quartz_ore");
        ashBlock = add(new BlockAsh("ash_block"), "ash_block");
        magmaBricks = add(new BlockBasic("magma_bricks", Material.rock, 2.0F, 6.0F, Block.soundTypeStone).tool("pickaxe", 0).light(3), "magma_bricks");
        rimestone = add(new BlockBasic("rimestone", Material.rock, 1.5F, 6.0F, Block.soundTypeStone).tool("pickaxe", 0), "rimestone");
        rimestoneBricks = add(new BlockBasic("rimestone_bricks", Material.rock, 2.0F, 6.0F, Block.soundTypeStone).tool("pickaxe", 0), "rimestone_bricks");

        quartzGlass = add(new BlockClearGlass("quartz_glass"), "quartz_glass");
        quartzLamp = add(new BlockBasic("quartz_lamp", Material.glass, 0.3F, 1.5F, Block.soundTypeGlass).light(15), "quartz_lamp");
        emberLamp = add(new BlockBasic("ember_lamp", Material.glass, 0.3F, 1.5F, Block.soundTypeGlass).light(15), "ember_lamp");

        emberStem = add(new BlockPillar("ember_stem", Material.wood, 2.0F, Block.soundTypeWood).tool("axe", 0), "ember_stem");
        emberPlanks = add(new BlockBasic("ember_planks", Material.wood, 2.0F, 5.0F, Block.soundTypeWood).tool("axe", 0), "ember_planks");
        emberWartBlock = add(new BlockBasic("ember_wart_block", Material.grass, 1.0F, 1.0F, Block.soundTypeGrass).light(4), "ember_wart_block");
        witheredStem = add(new BlockPillar("withered_stem", Material.wood, 2.0F, Block.soundTypeWood).tool("axe", 0), "withered_stem");
        witheredPlanks = add(new BlockBasic("withered_planks", Material.wood, 2.0F, 5.0F, Block.soundTypeWood).tool("axe", 0), "withered_planks");
        witheredWartBlock = add(new BlockBasic("withered_wart_block", Material.grass, 1.0F, 1.0F, Block.soundTypeGrass), "withered_wart_block");
        petrifiedLog = add(new BlockPillar("petrified_log", Material.rock, 3.0F, Block.soundTypeStone).tool("pickaxe", 1), "petrified_log");

        emberNylium = add(new BlockNetherSoil("ember_nylium"), "ember_nylium");
        netherGrassBlock = add(new BlockNetherSoil("nether_grass_block"), "nether_grass_block");
        netherGrass = add(new BlockNetherPlant("nether_grass", 0), "nether_grass");
        glowcap = add(new BlockNetherPlant("glowcap", 10), "glowcap");
        emberBloom = add(new BlockNetherPlant("ember_bloom", 6), "ember_bloom");
        ashDaisy = add(new BlockNetherPlant("ash_daisy", 0), "ash_daisy");
        crimsonBellflower = add(new BlockNetherPlant("crimson_bellflower", 0), "crimson_bellflower");
        sulfurTulip = add(new BlockNetherPlant("sulfur_tulip", 4), "sulfur_tulip");
        soulLily = add(new BlockNetherPlant("soul_lily", 8), "soul_lily");

        blazingGrass = add(new BlockNetherPlant("blazing_grass", 5), "blazing_grass");
        charredShrub = add(new BlockNetherPlant("charred_shrub", 0), "charred_shrub");
        emberRoots = add(new BlockNetherPlant("ember_roots", 3), "ember_roots");
        emberFungus = add(new BlockNetherPlant("ember_fungus", 6), "ember_fungus");
        witheredFungus = add(new BlockNetherPlant("withered_fungus", 0), "withered_fungus");
        marrowSprouts = add(new BlockNetherPlant("marrow_sprouts", 2), "marrow_sprouts");

        marrowStem = add(new BlockPillar("marrow_stem", Material.wood, 2.0F, Block.soundTypeWood).tool("axe", 0), "marrow_stem");
        marrowPlanks = add(new BlockBasic("marrow_planks", Material.wood, 2.0F, 5.0F, Block.soundTypeWood).tool("axe", 0), "marrow_planks");
        marrowCap = add(new BlockBasic("marrow_cap", Material.grass, 1.0F, 1.0F, Block.soundTypeGrass).light(6), "marrow_cap");
        wartstalkStem = add(new BlockPillar("wartstalk_stem", Material.wood, 2.0F, Block.soundTypeWood).tool("axe", 0), "wartstalk_stem");
        wartstalkPlanks = add(new BlockBasic("wartstalk_planks", Material.wood, 2.0F, 5.0F, Block.soundTypeWood).tool("axe", 0), "wartstalk_planks");
        wartBulb = add(new BlockBasic("wart_bulb", Material.grass, 0.5F, 0.5F, Block.soundTypeGrass).light(13), "wart_bulb");
        witheredSoil = add(new BlockBasic("withered_soil", Material.ground, 0.6F, 0.6F, Block.soundTypeGravel).tool("shovel", 0), "withered_soil");
        ossifiedNylium = add(new BlockNetherSoil("ossified_nylium"), "ossified_nylium");
        wartMoss = add(new BlockNetherSoil("wart_moss"), "wart_moss");
        onyxCrystalLeaves = add(new BlockTranslucent("onyx_crystal_leaves", Material.glass, 0.4F, Block.soundTypeGlass)
                .drops(() -> ModItems.onyxShard, 8).light(5), "onyx_crystal_leaves");
        rimeIce = add(new BlockTranslucent("rime_ice", Material.ice, 0.6F, Block.soundTypeGlass).slippery()
                .drops(() -> ModItems.rimeShard, 1), "rime_ice");

        onyxCluster = add(new BlockCrystal("onyx_cluster", 4, () -> ModItems.onyxShard, 1, 3), "onyx_cluster");
        sulfurCluster = add(new BlockCrystal("sulfur_cluster", 3, () -> ModItems.sulfur, 2, 4), "sulfur_cluster");
        quartzCluster = add(new BlockCrystal("quartz_cluster", 2, () -> net.minecraft.init.Items.quartz, 2, 4), "quartz_cluster");
        rimeCrystal = add(new BlockCrystal("rime_crystal", 6, () -> ModItems.rimeShard, 1, 2), "rime_crystal");

        blazePepperCrop = add(new BlockNetherCrop("blaze_pepper_crop", () -> ModItems.blazePepperSeeds, () -> ModItems.blazePepper), "blaze_pepper_crop");
        cinderWheatCrop = add(new BlockNetherCrop("cinder_wheat_crop", () -> ModItems.cinderWheatSeeds, () -> ModItems.cinderWheat), "cinder_wheat_crop");
        soulTuberCrop = add(new BlockNetherCrop("soul_tuber_crop", () -> ModItems.soulTuber, () -> ModItems.soulTuber), "soul_tuber_crop");
        fireBerryBush = add(new BlockBerryBush("fire_berry_bush", () -> ModItems.fireBerries), "fire_berry_bush");

        wardedBricks = add(new BlockWarded("warded_bricks", false), "warded_bricks");
        guardianSeal = add(new BlockWarded("guardian_seal", true), "guardian_seal");
        soulAltar = add(new com.netheryx.altar.BlockSoulAltar("soul_altar"), "soul_altar");
        crustedLava = add(new com.netheryx.block.BlockCrustedLava("crusted_lava"), "crusted_lava");

        // Les planches et troncs du mod comptent comme du bois pour les recettes vanilla (etabli, batons, coffres...)
        OreDictionary.registerOre("plankWood", emberPlanks);
        OreDictionary.registerOre("plankWood", witheredPlanks);
        OreDictionary.registerOre("plankWood", marrowPlanks);
        OreDictionary.registerOre("plankWood", wartstalkPlanks);
        OreDictionary.registerOre("logWood", marrowStem);
        OreDictionary.registerOre("logWood", wartstalkStem);
        OreDictionary.registerOre("logWood", emberStem);
        OreDictionary.registerOre("logWood", witheredStem);
        OreDictionary.registerOre("oreOnyx", onyxOre);
        OreDictionary.registerOre("oreSulfur", sulfurOre);
        OreDictionary.registerOre("blockSulfur", sulfurBlock);
        OreDictionary.registerOre("blockGlass", quartzGlass);
    }

    private static Block add(Block block, String name) {
        GameRegistry.registerBlock(block, name);
        return block;
    }
}
