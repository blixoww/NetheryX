package com.netheryx.block;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;

/** Bloc de cendre : poudreux, il tombe comme le sable. */
public class BlockAsh extends BlockFalling {
    public BlockAsh(String name) {
        super(Material.sand);
        setBlockName(NetheryX.MOD_ID + "." + name);
        setBlockTextureName(NetheryX.id(name));
        setHardness(0.5F);
        setStepSound(soundTypeSand);
        setCreativeTab(NetheryXTab.TAB);
        setHarvestLevel("shovel", 0);
    }
}
