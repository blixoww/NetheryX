package com.netheryx.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Recettes d'artisanat et de cuisson (en 1.7.10, elles sont declarees en Java). */
public final class ModRecipes {
    private ModRecipes() {
    }

    public static void register() {
        // Onyx
        compress(ModItems.onyx, ModBlocks.onyxBlock);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.onyxShard, 4), ModItems.onyx);
        GameRegistry.addRecipe(new ItemStack(ModItems.onyx), "SS", "SS", 'S', ModItems.onyxShard);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.polishedOnyx, 4), "OO", "OO", 'O', ModBlocks.onyxBlock);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.onyxBricks, 4), "PP", "PP", 'P', ModBlocks.polishedOnyx);
        GameRegistry.addSmelting(ModBlocks.onyxOre, new ItemStack(ModItems.onyx), 1.0F);

        // Outils et armure d'onyx (manches : n'importe quel baton du dictionnaire de minerais)
        Item o = ModItems.onyx;
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.onyxSword), "O", "O", "S", 'O', o, 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.onyxPickaxe), "OOO", " S ", " S ", 'O', o, 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.onyxAxe), "OO", "OS", " S", 'O', o, 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.onyxShovel), "O", "S", "S", 'O', o, 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.onyxHoe), "OO", " S", " S", 'O', o, 'S', "stickWood"));
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxHelmet), "OOO", "O O", 'O', o);
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxChestplate), "O O", "OOO", "OOO", 'O', o);
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxLeggings), "OOO", "O O", "O O", 'O', o);
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxBoots), "O O", "O O", 'O', o);
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxApple), "SSS", "SAS", "SSS", 'S', ModItems.onyxShard, 'A', Items.golden_apple);

        // Soufre : poudre a canon avec du charbon (ou du charbon de bois)
        compress(ModItems.sulfur, ModBlocks.sulfurBlock);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.gunpowder, 2), ModItems.sulfur, ModItems.sulfur,
                new ItemStack(Items.coal, 1, OreDictionary.WILDCARD_VALUE));
        GameRegistry.addSmelting(ModBlocks.sulfurOre, new ItemStack(ModItems.sulfur, 2), 0.5F);
        GameRegistry.addSmelting(ModBlocks.richQuartzOre, new ItemStack(Items.quartz, 2), 0.7F);

        // Ames
        GameRegistry.addSmelting(Blocks.soul_sand, new ItemStack(ModItems.soulDust), 0.1F);

        // Pierres, verre et lumieres
        GameRegistry.addRecipe(new ItemStack(ModBlocks.rimestoneBricks, 4), "RR", "RR", 'R', ModBlocks.rimestone);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.magmaBricks, 4), "BM", "MB", 'B', Items.netherbrick, 'M', Items.magma_cream);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.quartzGlass, 2), "GQ", "QG", 'G', Blocks.glass, 'Q', Items.quartz);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.quartzLamp), " G ", "GLG", " G ", 'G', ModBlocks.quartzGlass, 'L', Blocks.glowstone);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.emberLamp, 2), " W ", "WLW", " W ", 'W', ModBlocks.emberWartBlock, 'L', Blocks.glowstone);

        // Bois : planches, charbon de bois
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.emberPlanks, 4), new ItemStack(ModBlocks.emberStem, 1, OreDictionary.WILDCARD_VALUE));
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.witheredPlanks, 4), new ItemStack(ModBlocks.witheredStem, 1, OreDictionary.WILDCARD_VALUE));
        GameRegistry.addSmelting(ModBlocks.emberStem, new ItemStack(Items.coal, 1, 1), 0.15F);
        GameRegistry.addSmelting(ModBlocks.witheredStem, new ItemStack(Items.coal, 1, 1), 0.15F);
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.marrowPlanks, 4), new ItemStack(ModBlocks.marrowStem, 1, OreDictionary.WILDCARD_VALUE));
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.wartstalkPlanks, 4), new ItemStack(ModBlocks.wartstalkStem, 1, OreDictionary.WILDCARD_VALUE));
        GameRegistry.addRecipe(new ItemStack(ModBlocks.netherGrassBlock), "G", "N", 'G', ModBlocks.netherGrass, 'N', Blocks.netherrack);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.rimeIce), "SS", "SS", 'S', ModItems.rimeShard);

        // Teintures des fleurs du Nether
        GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 1, 7), ModBlocks.ashDaisy);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 1, 14), ModBlocks.emberBloom);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 1, 1), ModBlocks.crimsonBellflower);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 1, 11), ModBlocks.sulfurTulip);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.soulDust, 2), ModBlocks.soulLily);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 3, 15), ModItems.witheredBone);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.fire_charge, 3), ModItems.sulfur, Items.blaze_powder,
                new ItemStack(Items.coal, 1, OreDictionary.WILDCARD_VALUE));

        // Cuisine
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.blazePepperSeeds, 2), ModItems.blazePepper);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.cinderWheatSeeds, 2), ModItems.cinderWheat);
        GameRegistry.addRecipe(new ItemStack(ModItems.cinderBread), "###", '#', ModItems.cinderWheat);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.glowcapStew), Items.bowl, ModBlocks.glowcap, ModBlocks.glowcap, ModItems.soulTuber);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.infernalStew), Items.bowl, ModItems.blazePepper, ModItems.bakedSoulTuber,
                Blocks.red_mushroom, Blocks.brown_mushroom);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.glimmerElixir), Items.glass_bottle, ModItems.soulDust, Items.glowstone_dust,
                ModItems.fireBerries);
        cook(ModItems.soulTuber, ModItems.bakedSoulTuber);
        cook(ModItems.stalkerMeat, ModItems.cookedStalkerMeat);
        cook(ModItems.crabLeg, ModItems.cookedCrabLeg);
        cook(ModItems.infernalBeef, ModItems.cookedInfernalBeef);
        cook(ModItems.infernalChicken, ModItems.cookedInfernalChicken);
        cook(ModItems.blazePork, ModItems.cookedBlazePork);
        cook(ModItems.emberEgg, ModItems.friedEmberEgg);
        cook(ModItems.magmafish, ModItems.grilledMagmafish);

        // Equipement des creatures
        GameRegistry.addRecipe(new ItemStack(ModItems.wraithCloth, 2), "EWE", 'E', ModItems.wraithEssence, 'W',
                new ItemStack(Blocks.wool, 1, 0));
        armorSet(ModItems.magmaShell, ModItems.emberHelmet, ModItems.emberChestplate, ModItems.emberLeggings, ModItems.emberBoots);
        armorSet(ModItems.wraithCloth, ModItems.wraithHood, ModItems.wraithRobe, ModItems.wraithLeggings, ModItems.wraithBoots);
        armorSet(ModItems.basaltCarapace, ModItems.carapaceHelmet, ModItems.carapaceChestplate, ModItems.carapaceLeggings,
                ModItems.carapaceBoots);
        GameRegistry.addRecipe(new ItemStack(ModItems.magmaTreads), "M M", "M M", 'M', ModItems.magmaShell);
        GameRegistry.addRecipe(new ItemStack(ModItems.fireVeil), " C ", "CMC", " C ", 'C', ModItems.wraithCloth, 'M', Items.magma_cream);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.sulfurBomb, 2), ModItems.sulfur, ModItems.sulfur, Items.gunpowder, Items.string);

        // Outils speciaux
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxBow), " OS", "R S", " OS", 'O', o, 'R', Items.blaze_rod, 'S', Items.string);
        GameRegistry.addRecipe(new ItemStack(ModItems.onyxHammer), "OBO", " R ", " R ", 'O', o, 'B', ModBlocks.onyxBlock, 'R', Items.blaze_rod);
        GameRegistry.addRecipe(new ItemStack(ModItems.soulCompass), " E ", "ECE", " E ", 'E', ModItems.wraithEssence, 'C', Items.compass);
        GameRegistry.addRecipe(new ItemStack(ModItems.recallStone), " D ", "DOD", " D ", 'D', ModItems.soulDust, 'O', Blocks.obsidian);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.codex), Items.book, Blocks.netherrack);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.soulAltar), "DCD", " P ", "PPP", 'D', ModItems.soulDust, 'C', Blocks.obsidian,
                'P', ModBlocks.polishedOnyx);
        GameRegistry.addRecipe(new ItemStack(ModItems.wyvernCrystal), "OQO", "QEQ", "OQO", 'O', o, 'Q', ModBlocks.quartzGlass,
                'E', Items.ender_eye);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.wyvernWhistle), Items.bone, Items.gold_ingot, Items.feather, ModItems.magmaShell);

        // Infusions d'onyx : copie du gabarit, puis chargement avec un ingredient (application a l'enclume)
        Item template = ModItems.onyxInfusionTemplate;
        GameRegistry.addRecipe(new ItemStack(template, 2, 0), "#T#", "#O#", "###", '#', ModBlocks.onyxBricks, 'T', new ItemStack(template, 1, 0), 'O', o);
        GameRegistry.addShapelessRecipe(new ItemStack(template, 1, 1), new ItemStack(template, 1, 0), Items.magma_cream);
        GameRegistry.addShapelessRecipe(new ItemStack(template, 1, 2), new ItemStack(template, 1, 0), ModItems.burrowerFang);
        GameRegistry.addShapelessRecipe(new ItemStack(template, 1, 3), new ItemStack(template, 1, 0), o);

        // Fin de jeu : coeur scelle, coeur infernal, equipement abyssal
        GameRegistry.addRecipe(new ItemStack(ModItems.sealedHeart), "OCO", "SXW", "OHO", 'O', ModBlocks.onyxBlock, 'C', Blocks.obsidian,
                'S', ModItems.sentinelEye, 'X', ModItems.championCrown, 'W', ModItems.warlockSigil, 'H', ModItems.basaltHeartstone);
        GameRegistry.addRecipe(new ItemStack(ModItems.infernalHeart), "OMO", "MCM", "OMO", 'O', o, 'M', Items.magma_cream, 'C', ModItems.sentinelCore);
        GameRegistry.addRecipe(new ItemStack(ModItems.abyssalTemplate, 2), "#T#", "#O#", "###", '#', ModItems.onyxShard,
                'T', ModItems.abyssalTemplate, 'O', ModItems.abyssalShard);
        GameRegistry.addRecipe(new ItemStack(ModItems.abyssalBlade), "NAN", "AXA", "OSO", 'N', Blocks.diamond_block, 'A', ModItems.abyssalShard,
                'X', Items.nether_star, 'O', ModBlocks.onyxBlock, 'S', ModItems.onyxSword);
        abyssal(ModItems.onyxHelmet, ModItems.abyssalHelmet);
        abyssal(ModItems.onyxChestplate, ModItems.abyssalChestplate);
        abyssal(ModItems.onyxLeggings, ModItems.abyssalLeggings);
        abyssal(ModItems.onyxBoots, ModItems.abyssalBoots);
    }

    /** Four : cuisson simple. */
    private static void cook(Item raw, Item cooked) {
        GameRegistry.addSmelting(raw, new ItemStack(cooked), 0.35F);
    }

    private static void armorSet(Item material, Item helmet, Item chestplate, Item leggings, Item boots) {
        GameRegistry.addRecipe(new ItemStack(helmet), "MMM", "M M", 'M', material);
        GameRegistry.addRecipe(new ItemStack(chestplate), "M M", "MMM", "MMM", 'M', material);
        GameRegistry.addRecipe(new ItemStack(leggings), "MMM", "M M", "M M", 'M', material);
        GameRegistry.addRecipe(new ItemStack(boots), "M M", "M M", 'M', material);
    }

    /** Amelioration abyssale (pas de table de forgeron en 1.7.10) : gabarit + piece d'onyx + eclat abyssal, a l'etabli. */
    private static void abyssal(Item onyxPiece, Item abyssalPiece) {
        GameRegistry.addShapelessRecipe(new ItemStack(abyssalPiece), ModItems.abyssalTemplate,
                new ItemStack(onyxPiece, 1, OreDictionary.WILDCARD_VALUE), ModItems.abyssalShard);
    }

    /** 9 objets <-> 1 bloc. */
    private static void compress(Item item, Block block) {
        GameRegistry.addRecipe(new ItemStack(block), "III", "III", "III", 'I', item);
        GameRegistry.addShapelessRecipe(new ItemStack(item, 9), block);
    }
}
