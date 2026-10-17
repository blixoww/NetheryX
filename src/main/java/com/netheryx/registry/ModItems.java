package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.item.GlimmerElixirItem;
import com.netheryx.item.InfernalHeartItem;
import com.netheryx.item.MagmaTreadsItem;
import com.netheryx.item.ObsidianBoatItem;
import com.netheryx.item.ObsidianBucketItem;
import com.netheryx.item.OnyxArmorItem;
import com.netheryx.item.OnyxBowItem;
import com.netheryx.item.OnyxHammerItem;
import com.netheryx.item.RecallStoneItem;
import com.netheryx.item.SoulCompassItem;
import com.netheryx.item.SoulScytheItem;
import com.netheryx.item.SulfurBombItem;
import com.netheryx.item.SulfurItem;
import com.netheryx.item.WarlockStaffItem;
import com.netheryx.item.WitheredBladeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NetheryX.MOD_ID);

    // --- Materiaux --------------------------------------------------------------------------
    public static final RegistryObject<Item> ONYX = register("onyx", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_SHARD = register("onyx_shard", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> SULFUR = register("sulfur", () -> new SulfurItem(new Item.Properties()));
    public static final RegistryObject<Item> WRAITH_ESSENCE = register("wraith_essence", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MAGMA_SHELL = register("magma_shell", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> SENTINEL_CORE = register("sentinel_core",
            () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SOUL_DUST = register("soul_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_BONE = register("withered_bone", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BASALT_CARAPACE = register("basalt_carapace", () -> new Item(new Item.Properties().fireResistant()));

    // --- Outils en Onyx ---------------------------------------------------------------------
    public static final RegistryObject<Item> ONYX_SWORD = register("onyx_sword",
            () -> new SwordItem(ModTiers.ONYX, 3, -2.4F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_PICKAXE = register("onyx_pickaxe",
            () -> new PickaxeItem(ModTiers.ONYX, 1, -2.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_AXE = register("onyx_axe",
            () -> new AxeItem(ModTiers.ONYX, 5.0F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_SHOVEL = register("onyx_shovel",
            () -> new ShovelItem(ModTiers.ONYX, 1.5F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_HOE = register("onyx_hoe",
            () -> new HoeItem(ModTiers.ONYX, -4, 0.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_HAMMER = register("onyx_hammer",
            () -> new OnyxHammerItem(ModTiers.ONYX, 4, -3.2F, new Item.Properties().fireResistant().durability(7000)));
    public static final RegistryObject<Item> SOUL_SCYTHE = register("soul_scythe",
            () -> new SoulScytheItem(ModTiers.ONYX, 5, -2.8F, new Item.Properties().fireResistant().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> WITHERED_BLADE = register("withered_blade",
            () -> new WitheredBladeItem(ModTiers.ONYX, 4, -2.4F, new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WARLOCK_STAFF = register("warlock_staff",
            () -> new WarlockStaffItem(new Item.Properties().durability(256).fireResistant().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ONYX_BOW = register("onyx_bow",
            () -> new OnyxBowItem(new Item.Properties().durability(2400).fireResistant()));

    // --- Armure -----------------------------------------------------------------------------
    public static final RegistryObject<Item> ONYX_HELMET = register("onyx_helmet",
            () -> new OnyxArmorItem(ArmorItem.Type.HELMET, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_CHESTPLATE = register("onyx_chestplate",
            () -> new OnyxArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_LEGGINGS = register("onyx_leggings",
            () -> new OnyxArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ONYX_BOOTS = register("onyx_boots",
            () -> new OnyxArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> MAGMA_TREADS = register("magma_treads",
            () -> new MagmaTreadsItem(new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> CARAPACE_CHESTPLATE = register("carapace_chestplate",
            () -> new ArmorItem(ModArmorMaterials.CARAPACE, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant()));

    // --- Objets utilitaires -----------------------------------------------------------------
    public static final RegistryObject<Item> SOUL_COMPASS = register("soul_compass",
            () -> new SoulCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INFERNAL_HEART = register("infernal_heart",
            () -> new InfernalHeartItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SULFUR_BOMB = register("sulfur_bomb",
            () -> new SulfurBombItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> RECALL_STONE = register("recall_stone",
            () -> new RecallStoneItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> GLIMMER_ELIXIR = register("glimmer_elixir",
            () -> new GlimmerElixirItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> OBSIDIAN_BOAT = register("obsidian_boat",
            () -> new ObsidianBoatItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> OBSIDIAN_BUCKET = register("obsidian_bucket",
            () -> new ObsidianBucketItem(new Item.Properties().stacksTo(1).fireResistant()));

    // --- Cultures et nourriture --------------------------------------------------------------
    public static final RegistryObject<Item> FIRE_BERRIES = register("fire_berries",
            () -> new ItemNameBlockItem(ModBlocks.FIRE_BERRY_BUSH.get(), new Item.Properties().food(ModFoods.FIRE_BERRIES)));
    public static final RegistryObject<Item> BLAZE_PEPPER_SEEDS = register("blaze_pepper_seeds",
            () -> new ItemNameBlockItem(ModBlocks.BLAZE_PEPPER_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> BLAZE_PEPPER = register("blaze_pepper",
            () -> new Item(new Item.Properties().food(ModFoods.BLAZE_PEPPER)));
    public static final RegistryObject<Item> SOUL_TUBER = register("soul_tuber",
            () -> new ItemNameBlockItem(ModBlocks.SOUL_TUBER_CROP.get(), new Item.Properties().food(ModFoods.SOUL_TUBER)));
    public static final RegistryObject<Item> BAKED_SOUL_TUBER = register("baked_soul_tuber",
            () -> new Item(new Item.Properties().food(ModFoods.BAKED_SOUL_TUBER)));
    public static final RegistryObject<Item> STALKER_MEAT = register("stalker_meat",
            () -> new Item(new Item.Properties().food(ModFoods.STALKER_MEAT)));
    public static final RegistryObject<Item> COOKED_STALKER_MEAT = register("cooked_stalker_meat",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_STALKER_MEAT)));
    public static final RegistryObject<Item> CRAB_LEG = register("crab_leg",
            () -> new Item(new Item.Properties().food(ModFoods.CRAB_LEG)));
    public static final RegistryObject<Item> COOKED_CRAB_LEG = register("cooked_crab_leg",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_CRAB_LEG)));
    public static final RegistryObject<Item> INFERNAL_STEW = register("infernal_stew",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(ModFoods.INFERNAL_STEW)));

    public static final RegistryObject<Item> CINDER_WHEAT_SEEDS = register("cinder_wheat_seeds",
            () -> new ItemNameBlockItem(ModBlocks.CINDER_WHEAT_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> CINDER_WHEAT = register("cinder_wheat",
            () -> new Item(new Item.Properties().food(ModFoods.CINDER_WHEAT)));
    public static final RegistryObject<Item> CINDER_BREAD = register("cinder_bread",
            () -> new Item(new Item.Properties().food(ModFoods.CINDER_BREAD)));
    public static final RegistryObject<Item> GLOWCAP = register("glowcap",
            () -> new ItemNameBlockItem(ModBlocks.GLOWCAP.get(), new Item.Properties().food(ModFoods.GLOWCAP)));
    public static final RegistryObject<Item> GLOWCAP_STEW = register("glowcap_stew",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(ModFoods.GLOWCAP_STEW)));
    public static final RegistryObject<Item> INFERNAL_BEEF = register("infernal_beef",
            () -> new Item(new Item.Properties().food(ModFoods.INFERNAL_BEEF)));
    public static final RegistryObject<Item> COOKED_INFERNAL_BEEF = register("cooked_infernal_beef",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_INFERNAL_BEEF)));
    public static final RegistryObject<Item> INFERNAL_CHICKEN = register("infernal_chicken",
            () -> new Item(new Item.Properties().food(ModFoods.INFERNAL_CHICKEN)));
    public static final RegistryObject<Item> COOKED_INFERNAL_CHICKEN = register("cooked_infernal_chicken",
            () -> new Item(new Item.Properties().food(ModFoods.COOKED_INFERNAL_CHICKEN)));
    public static final RegistryObject<Item> EMBER_EGG = register("ember_egg",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FRIED_EMBER_EGG = register("fried_ember_egg",
            () -> new Item(new Item.Properties().food(ModFoods.FRIED_EMBER_EGG)));

    // --- Oeufs d'apparition (couleurs gerees automatiquement par ForgeSpawnEggItem) ------------
    public static final RegistryObject<Item> EMBER_STALKER_SPAWN_EGG = register("ember_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.EMBER_STALKER, 0x3A1A12, 0xFF7A1A, new Item.Properties()));
    public static final RegistryObject<Item> ASH_WRAITH_SPAWN_EGG = register("ash_wraith_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ASH_WRAITH, 0x8F8A86, 0x2B2827, new Item.Properties()));
    public static final RegistryObject<Item> ONYX_SENTINEL_SPAWN_EGG = register("onyx_sentinel_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ONYX_SENTINEL, 0x16131C, 0xA05CFF, new Item.Properties()));
    public static final RegistryObject<Item> MAGMA_CRAB_SPAWN_EGG = register("magma_crab_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MAGMA_CRAB, 0x5A1E0E, 0xFFB020, new Item.Properties()));

    public static final RegistryObject<Item> SOUL_MOTH_SPAWN_EGG = register("soul_moth_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SOUL_MOTH, 0x1E3A44, 0x7FE8FF, new Item.Properties()));
    public static final RegistryObject<Item> WITHERED_KNIGHT_SPAWN_EGG = register("withered_knight_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WITHERED_KNIGHT, 0x2A2A2A, 0x8A8A80, new Item.Properties()));
    public static final RegistryObject<Item> BASALT_BEETLE_SPAWN_EGG = register("basalt_beetle_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BASALT_BEETLE, 0x3A3A40, 0xFF6A1A, new Item.Properties()));
    public static final RegistryObject<Item> INFERNAL_WARLOCK_SPAWN_EGG = register("infernal_warlock_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.INFERNAL_WARLOCK, 0x4A0E0E, 0xFFC531, new Item.Properties()));

    public static final RegistryObject<Item> WITHERED_CHAMPION_SPAWN_EGG = register("withered_champion_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WITHERED_CHAMPION, 0x1A1A1A, 0xD8C060, new Item.Properties()));

    public static final RegistryObject<Item> EMBER_COW_SPAWN_EGG = register("ember_cow_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.EMBER_COW, 0x6A1E0E, 0xFFB347, new Item.Properties()));
    public static final RegistryObject<Item> CINDER_HEN_SPAWN_EGG = register("cinder_hen_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CINDER_HEN, 0x4A4442, 0xFF6A1A, new Item.Properties()));
    public static final RegistryObject<Item> FIRE_GHAST_SPAWN_EGG = register("fire_ghast_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FIRE_GHAST, 0xFF8A1A, 0x8A1E0A, new Item.Properties()));
    public static final RegistryObject<Item> ONYX_CUBE_SPAWN_EGG = register("onyx_cube_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ONYX_CUBE, 0x16131C, 0xB06CFF, new Item.Properties()));

    private ModItems() {
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }
}
