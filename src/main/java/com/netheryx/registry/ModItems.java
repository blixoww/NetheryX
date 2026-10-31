package com.netheryx.registry;

import com.netheryx.item.ItemBasic;
import com.netheryx.item.ItemCodex;
import com.netheryx.item.ItemInfusionTemplate;
import com.netheryx.item.ItemModArmor;
import com.netheryx.item.ItemModFood;
import com.netheryx.item.ItemModSword;
import com.netheryx.item.ItemNetheryXEgg;
import com.netheryx.item.ItemOnyxApple;
import com.netheryx.item.ItemOnyxBow;
import com.netheryx.item.ItemOnyxHammer;
import com.netheryx.item.ItemPlanter;
import com.netheryx.item.ItemRecallStone;
import com.netheryx.item.ItemSealedHeart;
import com.netheryx.item.ItemSoulCompass;
import com.netheryx.item.ItemSulfurBomb;
import com.netheryx.item.ItemWarlockStaff;
import com.netheryx.item.ItemWyvernCrystal;
import com.netheryx.item.ItemWyvernEgg;
import com.netheryx.item.ItemWyvernWhistle;
import com.netheryx.item.ModTools;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;

/** Tous les objets du mod (identifiants de registre identiques a ceux de la version 1.20.1). */
public final class ModItems {
    /** Onyx : un peu au-dessus du diamant (niveau de minage 3, 1800 utilisations). */
    public static final Item.ToolMaterial ONYX_TOOLS = EnumHelper.addToolMaterial("NETHERYX_ONYX", 3, 1800, 8.5F, 3.5F, 12);
    /** Abyssal : l'arme ultime (13 de degats pour la lame). */
    public static final Item.ToolMaterial ABYSSAL_TOOLS = EnumHelper.addToolMaterial("NETHERYX_ABYSSAL", 4, 3000, 10.0F, 9.0F, 22);
    /** Armure d'onyx : protection du diamant, plus durable et plus facile a enchanter. */
    public static final ItemArmor.ArmorMaterial ONYX_ARMOR = EnumHelper.addArmorMaterial("NETHERYX_ONYX", 40, new int[]{3, 8, 6, 3}, 15);
    public static final ItemArmor.ArmorMaterial EMBER_ARMOR = EnumHelper.addArmorMaterial("NETHERYX_EMBER", 24, new int[]{2, 6, 5, 2}, 15);
    public static final ItemArmor.ArmorMaterial WRAITH_ARMOR = EnumHelper.addArmorMaterial("NETHERYX_WRAITH", 18, new int[]{2, 5, 4, 2}, 20);
    public static final ItemArmor.ArmorMaterial CARAPACE_ARMOR = EnumHelper.addArmorMaterial("NETHERYX_CARAPACE", 28, new int[]{2, 7, 5, 2}, 10);
    public static final ItemArmor.ArmorMaterial ABYSSAL_ARMOR = EnumHelper.addArmorMaterial("NETHERYX_ABYSSAL", 70, new int[]{5, 10, 8, 5}, 22);
    public static final ItemArmor.ArmorMaterial MAGMA_ARMOR = EnumHelper.addArmorMaterial("NETHERYX_MAGMA", 20, new int[]{2, 5, 4, 2}, 12);

    // Materiaux et butins de creatures
    public static Item onyx;
    public static Item onyxShard;
    public static Item sulfur;
    public static Item soulDust;
    public static Item wraithEssence;
    public static Item wraithCloth;
    public static Item magmaShell;
    public static Item witheredBone;
    public static Item basaltCarapace;
    public static Item burrowerFang;
    public static Item magmafishScale;
    public static Item rimeShard;
    // Trophees des gardiens et fin de jeu
    public static Item sentinelEye;
    public static Item sentinelCore;
    public static Item warlockSigil;
    public static Item championCrown;
    public static Item basaltHeartstone;
    public static Item sealedHeart;
    public static Item abyssalShard;
    public static Item abyssalTemplate;
    public static Item infernalHeart;
    // Outils et armes
    public static Item onyxSword;
    public static Item onyxPickaxe;
    public static Item onyxAxe;
    public static Item onyxShovel;
    public static Item onyxHoe;
    public static Item onyxHammer;
    public static Item onyxBow;
    public static Item soulScythe;
    public static Item witheredBlade;
    public static Item warlockStaff;
    public static Item abyssalBlade;
    // Armures
    public static Item onyxHelmet;
    public static Item onyxChestplate;
    public static Item onyxLeggings;
    public static Item onyxBoots;
    public static Item emberHelmet;
    public static Item emberChestplate;
    public static Item emberLeggings;
    public static Item emberBoots;
    public static Item wraithHood;
    public static Item wraithRobe;
    public static Item wraithLeggings;
    public static Item wraithBoots;
    public static Item carapaceHelmet;
    public static Item carapaceChestplate;
    public static Item carapaceLeggings;
    public static Item carapaceBoots;
    public static Item abyssalHelmet;
    public static Item abyssalChestplate;
    public static Item abyssalLeggings;
    public static Item abyssalBoots;
    public static Item magmaTreads;
    // Objets utiles
    public static Item soulCompass;
    public static Item recallStone;
    public static Item fireVeil;
    public static Item glimmerElixir;
    public static Item sulfurBomb;
    public static Item onyxInfusionTemplate;
    public static Item codex;
    public static Item ashWyvernEgg;
    public static Item wyvernWhistle;
    public static Item wyvernCrystal;
    public static Item spawnEgg;
    // Cultures et nourriture
    public static Item fireBerries;
    public static Item blazePepperSeeds;
    public static Item blazePepper;
    public static Item cinderWheatSeeds;
    public static Item cinderWheat;
    public static Item cinderBread;
    public static Item soulTuber;
    public static Item bakedSoulTuber;
    public static Item glowcapStew;
    public static Item infernalStew;
    public static Item stalkerMeat;
    public static Item cookedStalkerMeat;
    public static Item crabLeg;
    public static Item cookedCrabLeg;
    public static Item infernalBeef;
    public static Item cookedInfernalBeef;
    public static Item infernalChicken;
    public static Item cookedInfernalChicken;
    public static Item blazePork;
    public static Item cookedBlazePork;
    public static Item emberEgg;
    public static Item friedEmberEgg;
    public static Item magmafish;
    public static Item grilledMagmafish;
    public static Item onyxApple;

    private ModItems() {
    }

    public static void register() {
        onyx = add(new ItemBasic("onyx"));
        onyxShard = add(new ItemBasic("onyx_shard"));
        sulfur = add(new ItemBasic("sulfur"));
        soulDust = add(new ItemBasic("soul_dust"));
        wraithEssence = add(new ItemBasic("wraith_essence"));
        wraithCloth = add(new ItemBasic("wraith_cloth"));
        magmaShell = add(new ItemBasic("magma_shell"));
        witheredBone = add(new ItemBasic("withered_bone"));
        basaltCarapace = add(new ItemBasic("basalt_carapace"));
        burrowerFang = add(new ItemBasic("burrower_fang").tooltip());
        magmafishScale = add(new ItemBasic("magmafish_scale").tooltip());
        rimeShard = add(new ItemBasic("rime_shard").tooltip());

        sentinelEye = add(new ItemBasic("sentinel_eye").rarity(EnumRarity.epic).stack(1).tooltip());
        sentinelCore = add(new ItemBasic("sentinel_core").rarity(EnumRarity.epic).tooltip());
        warlockSigil = add(new ItemBasic("warlock_sigil").rarity(EnumRarity.epic).stack(1).tooltip());
        championCrown = add(new ItemBasic("champion_crown").rarity(EnumRarity.epic).stack(1).tooltip());
        basaltHeartstone = add(new ItemBasic("basalt_heartstone").rarity(EnumRarity.epic).stack(1).tooltip());
        sealedHeart = add(new ItemSealedHeart("sealed_heart"));
        abyssalShard = add(new ItemBasic("abyssal_shard").rarity(EnumRarity.epic));
        abyssalTemplate = add(new ItemBasic("abyssal_upgrade_template").rarity(EnumRarity.epic).tooltip());
        infernalHeart = add(new ItemBasic("infernal_heart").rarity(EnumRarity.epic).stack(1).tooltip());

        onyxSword = add(ModTools.sword(ONYX_TOOLS, "onyx_sword"));
        onyxPickaxe = add(ModTools.pickaxe(ONYX_TOOLS, "onyx_pickaxe"));
        onyxAxe = add(ModTools.axe(ONYX_TOOLS, "onyx_axe"));
        onyxShovel = add(ModTools.shovel(ONYX_TOOLS, "onyx_shovel"));
        onyxHoe = add(ModTools.hoe(ONYX_TOOLS, "onyx_hoe"));
        onyxHammer = add(new ItemOnyxHammer(ONYX_TOOLS, "onyx_hammer"));
        onyxBow = add(new ItemOnyxBow("onyx_bow"));
        soulScythe = add(new ItemModSword(ONYX_TOOLS, "soul_scythe", ItemModSword.Kind.SOUL_SCYTHE, EnumRarity.rare));
        witheredBlade = add(new ItemModSword(ONYX_TOOLS, "withered_blade", ItemModSword.Kind.WITHERED_BLADE, EnumRarity.rare));
        warlockStaff = add(new ItemWarlockStaff("warlock_staff"));
        abyssalBlade = add(new ItemModSword(ABYSSAL_TOOLS, "abyssal_blade", ItemModSword.Kind.ABYSSAL_BLADE, EnumRarity.epic));

        onyxHelmet = armor(ONYX_ARMOR, "onyx", 0, "onyx_helmet");
        onyxChestplate = armor(ONYX_ARMOR, "onyx", 1, "onyx_chestplate");
        onyxLeggings = armor(ONYX_ARMOR, "onyx", 2, "onyx_leggings");
        onyxBoots = armor(ONYX_ARMOR, "onyx", 3, "onyx_boots");
        emberHelmet = armor(EMBER_ARMOR, "ember", 0, "ember_helmet");
        emberChestplate = armor(EMBER_ARMOR, "ember", 1, "ember_chestplate");
        emberLeggings = armor(EMBER_ARMOR, "ember", 2, "ember_leggings");
        emberBoots = armor(EMBER_ARMOR, "ember", 3, "ember_boots");
        wraithHood = armor(WRAITH_ARMOR, "wraith", 0, "wraith_hood");
        wraithRobe = armor(WRAITH_ARMOR, "wraith", 1, "wraith_robe");
        wraithLeggings = armor(WRAITH_ARMOR, "wraith", 2, "wraith_leggings");
        wraithBoots = armor(WRAITH_ARMOR, "wraith", 3, "wraith_boots");
        carapaceHelmet = armor(CARAPACE_ARMOR, "carapace", 0, "carapace_helmet");
        carapaceChestplate = armor(CARAPACE_ARMOR, "carapace", 1, "carapace_chestplate");
        carapaceLeggings = armor(CARAPACE_ARMOR, "carapace", 2, "carapace_leggings");
        carapaceBoots = armor(CARAPACE_ARMOR, "carapace", 3, "carapace_boots");
        abyssalHelmet = armor(ABYSSAL_ARMOR, "abyssal", 0, "abyssal_helmet");
        abyssalChestplate = armor(ABYSSAL_ARMOR, "abyssal", 1, "abyssal_chestplate");
        abyssalLeggings = armor(ABYSSAL_ARMOR, "abyssal", 2, "abyssal_leggings");
        abyssalBoots = armor(ABYSSAL_ARMOR, "abyssal", 3, "abyssal_boots");
        magmaTreads = armor(MAGMA_ARMOR, "magma", 3, "magma_treads");

        soulCompass = add(new ItemSoulCompass("soul_compass"));
        recallStone = add(new ItemRecallStone("recall_stone"));
        fireVeil = add(new ItemBasic("fire_veil").rarity(EnumRarity.rare).stack(1).tooltip());
        glimmerElixir = add(new ItemModFood("glimmer_elixir", 0, 0.0F, false).drink()
                .effect(Potion.nightVision.id, 3600, 0, 1.0F).effect(Potion.fireResistance.id, 600, 0, 1.0F));
        sulfurBomb = add(new ItemSulfurBomb("sulfur_bomb"));
        onyxInfusionTemplate = add(new ItemInfusionTemplate("onyx_infusion_template"));
        codex = add(new ItemCodex("codex"));
        ashWyvernEgg = add(new ItemWyvernEgg("ash_wyvern_egg"));
        wyvernWhistle = add(new ItemWyvernWhistle("wyvern_whistle"));
        wyvernCrystal = add(new ItemWyvernCrystal("wyvern_crystal"));
        spawnEgg = new ItemNetheryXEgg();
        GameRegistry.registerItem(spawnEgg, "spawn_egg");

        fireBerries = add(new ItemPlanter.Food("fire_berries", 2, 0.2F, () -> ModBlocks.fireBerryBush));
        blazePepperSeeds = add(new ItemPlanter("blaze_pepper_seeds", () -> ModBlocks.blazePepperCrop));
        blazePepper = add(new ItemModFood("blaze_pepper", 3, 0.4F, false).always().effect(Potion.fireResistance.id, 400, 0, 1.0F));
        cinderWheatSeeds = add(new ItemPlanter("cinder_wheat_seeds", () -> ModBlocks.cinderWheatCrop));
        cinderWheat = add(new ItemBasic("cinder_wheat"));
        cinderBread = add(new ItemModFood("cinder_bread", 6, 0.7F, false));
        soulTuber = add(new ItemPlanter.Food("soul_tuber", 1, 0.3F, () -> ModBlocks.soulTuberCrop));
        bakedSoulTuber = add(new ItemModFood("baked_soul_tuber", 6, 0.7F, false));
        glowcapStew = add(new ItemModFood("glowcap_stew", 6, 0.7F, false).bowl().effect(Potion.nightVision.id, 1200, 0, 1.0F));
        infernalStew = add(new ItemModFood("infernal_stew", 10, 1.0F, false).bowl()
                .effect(Potion.fireResistance.id, 1200, 0, 1.0F).effect(Potion.damageBoost.id, 600, 0, 1.0F));
        stalkerMeat = add(new ItemModFood("stalker_meat", 3, 0.3F, true));
        cookedStalkerMeat = add(new ItemModFood("cooked_stalker_meat", 8, 0.9F, true));
        crabLeg = add(new ItemModFood("crab_leg", 2, 0.2F, true));
        cookedCrabLeg = add(new ItemModFood("cooked_crab_leg", 6, 0.8F, true));
        infernalBeef = add(new ItemModFood("infernal_beef", 4, 0.4F, true));
        cookedInfernalBeef = add(new ItemModFood("cooked_infernal_beef", 10, 1.0F, true).effect(Potion.fireResistance.id, 200, 0, 1.0F));
        infernalChicken = add(new ItemModFood("infernal_chicken", 2, 0.3F, true));
        cookedInfernalChicken = add(new ItemModFood("cooked_infernal_chicken", 7, 0.8F, true));
        blazePork = add(new ItemModFood("blaze_pork", 3, 0.3F, true));
        cookedBlazePork = add(new ItemModFood("cooked_blaze_pork", 8, 0.8F, true).effect(Potion.fireResistance.id, 300, 0, 1.0F));
        emberEgg = add(new ItemBasic("ember_egg").stack(16));
        friedEmberEgg = add(new ItemModFood("fried_ember_egg", 5, 0.6F, false));
        magmafish = add(new ItemModFood("magmafish", 2, 0.2F, false));
        grilledMagmafish = add(new ItemModFood("grilled_magmafish", 6, 0.8F, false).effect(Potion.fireResistance.id, 400, 0, 1.0F));
        onyxApple = add(new ItemOnyxApple("onyx_apple"));

        // Ingredients de reparation (enclume)
        ONYX_TOOLS.customCraftingMaterial = onyx;
        ONYX_ARMOR.customCraftingMaterial = onyx;
        ABYSSAL_TOOLS.customCraftingMaterial = abyssalShard;
        ABYSSAL_ARMOR.customCraftingMaterial = abyssalShard;
        EMBER_ARMOR.customCraftingMaterial = magmaShell;
        MAGMA_ARMOR.customCraftingMaterial = magmaShell;
        WRAITH_ARMOR.customCraftingMaterial = wraithCloth;
        CARAPACE_ARMOR.customCraftingMaterial = basaltCarapace;

        OreDictionary.registerOre("gemOnyx", onyx);
        OreDictionary.registerOre("dustSulfur", sulfur);
        OreDictionary.registerOre("cropWheat", cinderWheat);
    }

    /** Enregistre l'objet sous son nom (celui de sa cle de traduction « item.netheryx.nom »). */
    private static Item add(Item item) {
        String texture = item.getUnlocalizedName().substring("item.netheryx.".length());
        GameRegistry.registerItem(item, texture);
        return item;
    }

    private static Item armor(ItemArmor.ArmorMaterial material, String set, int type, String name) {
        return add(new ItemModArmor(material, set, type, name));
    }
}
