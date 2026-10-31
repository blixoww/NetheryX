package com.netheryx.block;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** Bloc plein ordinaire : une seule texture, nom et onglet de NetheryX. */
public class BlockBasic extends Block {
    public BlockBasic(String name, Material material, float hardness, float resistance, SoundType sound) {
        super(material);
        setBlockName(NetheryX.MOD_ID + "." + name);
        setBlockTextureName(NetheryX.id(name));
        setHardness(hardness);
        setResistance(resistance);
        setStepSound(sound);
        setCreativeTab(NetheryXTab.TAB);
    }

    /** Bloc lumineux (0 a 15). */
    public BlockBasic light(int level) {
        setLightLevel(level / 15.0F);
        return this;
    }

    public BlockBasic tool(String tool, int level) {
        setHarvestLevel(tool, level);
        return this;
    }
}
