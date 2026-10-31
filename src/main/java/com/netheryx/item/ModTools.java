package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemSword;

/**
 * Outils et arme d'un materiau du mod. Les constructeurs vanilla de la pioche et de la hache sont proteges en 1.7.10 :
 * d'ou ces petites sous-classes.
 */
public final class ModTools {
    private ModTools() {
    }

    private static <T extends Item> T setup(T item, String name) {
        item.setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        item.setTextureName(NetheryX.id(name));
        item.setCreativeTab(NetheryXTab.TAB);
        return item;
    }

    public static Item sword(Item.ToolMaterial material, String name) {
        return setup(new ItemSword(material), name);
    }

    public static Item pickaxe(Item.ToolMaterial material, String name) {
        return setup(new Pickaxe(material), name);
    }

    public static Item axe(Item.ToolMaterial material, String name) {
        return setup(new Axe(material), name);
    }

    public static Item shovel(Item.ToolMaterial material, String name) {
        return setup(new ItemSpade(material), name);
    }

    public static Item hoe(Item.ToolMaterial material, String name) {
        return setup(new ItemHoe(material), name);
    }

    private static final class Pickaxe extends ItemPickaxe {
        Pickaxe(Item.ToolMaterial material) {
            super(material);
        }
    }

    private static final class Axe extends ItemAxe {
        Axe(Item.ToolMaterial material) {
            super(material);
        }
    }
}
