package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Piece d'armure d'un ensemble du mod. Les textures portees sont celles de la version 1.20.1
 * (textures/models/armor/<ensemble>_layer_1.png, et _layer_2 pour les jambieres). L'ensemble complet donne un bonus
 * (voir {@code ArmorSets}), decrit dans l'infobulle.
 */
public class ItemModArmor extends ItemArmor {
    private final String set;

    /** type : 0 casque, 1 plastron, 2 jambieres, 3 bottes. */
    public ItemModArmor(ArmorMaterial material, String set, int type, String name) {
        super(material, 0, type);
        this.set = set;
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    public String getSet() {
        return this.set;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        return NetheryX.MOD_ID + ":textures/models/armor/" + this.set + "_layer_" + (this.armorType == 2 ? 2 : 1) + ".png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        if ("magma".equals(this.set)) {
            ItemBasic.addTooltip(lines, getUnlocalizedName() + ".tooltip");
        } else {
            ItemBasic.addTooltip(lines, "item.netheryx." + this.set + "_armor.set_bonus");
        }
    }
}
