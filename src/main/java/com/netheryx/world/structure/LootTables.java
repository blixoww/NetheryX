package com.netheryx.world.structure;

import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Butin des coffres des structures (equivalents 1.7.10 des tables de butin de la version 1.20.1). */
public final class LootTables {
    private static final Map<String, Object[]> TABLES = new HashMap<String, Object[]>();

    private LootTables() {
    }

    private static WeightedRandomChestContent e(Item item, int min, int max, int weight) {
        return new WeightedRandomChestContent(new ItemStack(item), min, max, weight);
    }

    private static void table(String name, int rolls, WeightedRandomChestContent... entries) {
        TABLES.put(name, new Object[]{rolls, entries});
    }

    public static void register() {
        WeightedRandomChestContent gold = e(Items.gold_ingot, 1, 4, 10);
        WeightedRandomChestContent nuggets = e(Items.gold_nugget, 3, 9, 12);
        WeightedRandomChestContent onyx = e(ModItems.onyx, 1, 3, 8);
        WeightedRandomChestContent shards = e(ModItems.onyxShard, 2, 6, 10);
        WeightedRandomChestContent bread = e(ModItems.cinderBread, 1, 3, 10);
        WeightedRandomChestContent apple = e(Items.golden_apple, 1, 1, 3);
        table("small_treasure", 5, gold, nuggets, onyx, shards, bread, apple, e(Items.diamond, 1, 2, 3), e(ModItems.glimmerElixir, 1, 1, 4),
                e(ModItems.recallStone, 1, 1, 2), e(ModItems.soulCompass, 1, 1, 2));
        table("traveler_camp", 5, bread, nuggets, onyx, apple, e(ModItems.cookedStalkerMeat, 1, 3, 8), e(ModItems.onyxPickaxe, 1, 1, 2),
                e(Items.diamond, 1, 1, 2), e(ModItems.glimmerElixir, 1, 1, 3));
        table("nether_garden", 6, e(ModItems.blazePepperSeeds, 2, 6, 10), e(ModItems.cinderWheatSeeds, 2, 6, 10), e(ModItems.soulTuber, 2, 5, 10),
                e(ModItems.fireBerries, 2, 6, 10), bread, apple, e(ModItems.onyxHoe, 1, 1, 2), e(Item.getItemFromBlock(ModBlocks.glowcap), 2, 6, 6));
        table("nomad_caravan", 6, gold, nuggets, onyx, bread, e(Items.saddle, 1, 1, 4), e(ModItems.fireBerries, 2, 6, 8),
                e(ModItems.onyxInfusionTemplate, 1, 1, 3), e(ModItems.sulfurBomb, 2, 4, 6), e(ModItems.soulCompass, 1, 1, 2));
        table("wyvern_nest", 5, e(Items.bone, 2, 6, 20), nuggets, e(ModItems.cookedBlazePork, 1, 3, 10), shards, e(ModItems.magmaShell, 1, 2, 6),
                gold);
        table("basalt_forge", 6, e(ModItems.basaltCarapace, 1, 3, 14), e(ModItems.magmaShell, 1, 2, 12), onyx, gold,
                e(Items.diamond, 1, 2, 4), e(ModItems.onyxInfusionTemplate, 1, 1, 4), e(Items.iron_ingot, 2, 6, 10));
        table("onyx_citadel", 6, gold, onyx, shards, bread, e(ModItems.onyxSword, 1, 1, 3), e(ModItems.onyxHelmet, 1, 1, 2),
                e(Items.diamond, 1, 2, 4), e(ModItems.wraithCloth, 1, 3, 6), apple, e(ModItems.onyxInfusionTemplate, 1, 1, 3));
        table("onyx_citadel_vault", 8, e(ModItems.onyx, 3, 8, 12), e(Items.diamond, 2, 5, 10), e(ModItems.soulScythe, 1, 1, 4),
                e(ModItems.onyxHammer, 1, 1, 4), e(ModItems.sentinelCore, 1, 1, 3), e(Items.golden_apple, 1, 2, 6), e(ModItems.onyxApple, 1, 1, 4));
        table("infernal_sanctum", 6, gold, onyx, bread, e(Items.blaze_rod, 2, 6, 10), e(ModItems.magmaShell, 1, 3, 8), e(ModItems.glimmerElixir, 1, 2, 6),
                e(Items.diamond, 1, 2, 3), apple);
        table("infernal_sanctum_vault", 8, e(ModItems.onyx, 3, 8, 12), e(Items.diamond, 2, 5, 10), e(ModItems.warlockStaff, 1, 1, 4),
                e(ModItems.emberChestplate, 1, 1, 4), e(Items.golden_apple, 1, 2, 6), e(ModItems.onyxApple, 1, 1, 4), e(Items.blaze_rod, 4, 10, 8));
        table("withered_catacombs", 6, gold, onyx, bread, e(ModItems.witheredBone, 2, 6, 12), e(ModItems.wraithEssence, 1, 3, 8),
                e(Items.diamond, 1, 2, 3), apple, e(ModItems.wraithCloth, 1, 3, 6));
        table("withered_catacombs_vault", 8, e(ModItems.onyx, 3, 8, 12), e(Items.diamond, 2, 5, 10), e(ModItems.witheredBlade, 1, 1, 4),
                e(ModItems.wraithRobe, 1, 1, 4), e(Items.golden_apple, 1, 2, 6), e(ModItems.onyxApple, 1, 1, 4), e(Items.skull, 1, 1, 2));
    }

    /** Remplit un inventaire avec le butin de la table nommee (petit tresor par defaut). */
    public static void fill(IInventory inventory, String name, Random random) {
        Object[] table = TABLES.containsKey(name) ? TABLES.get(name) : TABLES.get("small_treasure");
        if (table == null) {
            return;
        }
        int rolls = (Integer) table[0];
        WeightedRandomChestContent.generateChestContents(random, (WeightedRandomChestContent[]) table[1], inventory, rolls + random.nextInt(3));
    }
}
