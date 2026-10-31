package com.netheryx;

import com.netheryx.registry.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/** Onglet creatif du mod : tous les blocs et objets de NetheryX. */
public final class NetheryXTab {
    public static final CreativeTabs TAB = new CreativeTabs(NetheryX.MOD_ID) {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return ModItems.onyx;
        }
    };

    private NetheryXTab() {
    }
}
