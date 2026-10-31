package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * Objet simple (materiau, trophee) avec nom, texture et onglet de NetheryX. Options : rarete, reflet enchante, pile
 * maximale et infobulle (cle {@code item.netheryx.<nom>.tooltip}).
 */
public class ItemBasic extends Item {
    private EnumRarity rarity = EnumRarity.common;
    private boolean glint;
    private boolean tooltip;

    public ItemBasic(String name) {
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    public ItemBasic rarity(EnumRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemBasic glint() {
        this.glint = true;
        return this;
    }

    public ItemBasic tooltip() {
        this.tooltip = true;
        return this;
    }

    public ItemBasic stack(int size) {
        setMaxStackSize(size);
        return this;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return this.rarity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        return this.glint || super.hasEffect(stack, pass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        if (this.tooltip) {
            addTooltip(lines, getUnlocalizedName() + ".tooltip");
        }
    }

    /** Ajoute une ligne d'infobulle traduite (grise), coupee sur les « \n » du fichier de langue. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addTooltip(List lines, String key, Object... args) {
        String text = StatCollector.translateToLocalFormatted(key, args);
        for (String line : text.split("\\\\n")) {
            lines.add(EnumChatFormatting.GRAY + line);
        }
    }
}
