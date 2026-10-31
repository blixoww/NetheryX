package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * Gabarit d'infusion d'onyx. Vierge (meta 0), il se charge a l'etabli avec un ingredient : creme de magma (fonte),
 * croc de fouisseur (celerite) ou onyx (resilience). Charge, il s'applique a l'enclume sur un outil, une arme ou une
 * armure (voir {@code NetheryXEvents#onAnvil}). La 1.7.10 n'a pas de table de forgeron : d'ou ces deux etapes.
 */
public class ItemInfusionTemplate extends Item {
    public enum Infusion {
        SMELTING, CELERITY, RESILIENCE;

        public static final String TAG = "NetheryXInfusion";

        public String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        /** Meta du gabarit charge de cette infusion. */
        public int meta() {
            return ordinal() + 1;
        }

        public static Infusion byMeta(int meta) {
            return meta >= 1 && meta <= values().length ? values()[meta - 1] : null;
        }

        public static Infusion of(ItemStack stack) {
            if (stack == null || !stack.hasTagCompound()) {
                return null;
            }
            String id = stack.getTagCompound().getString(TAG);
            for (Infusion infusion : values()) {
                if (infusion.id().equals(id)) {
                    return infusion;
                }
            }
            return null;
        }

        public boolean isOn(ItemStack stack) {
            return of(stack) == this;
        }

        public ItemStack applyTo(ItemStack stack) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setString(TAG, id());
            return stack;
        }
    }

    public ItemInfusionTemplate(String name) {
        setHasSubtypes(true);
        setMaxDamage(0);
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int meta = 0; meta <= Infusion.values().length; meta++) {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        return stack.getItemDamage() > 0;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        Infusion infusion = Infusion.byMeta(stack.getItemDamage());
        if (infusion == null) {
            ItemBasic.addTooltip(lines, "item.netheryx.onyx_infusion_template.tooltip");
        } else {
            lines.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("infusion.netheryx." + infusion.id()));
            ItemBasic.addTooltip(lines, "item.netheryx.onyx_infusion_template.charged");
        }
    }
}
