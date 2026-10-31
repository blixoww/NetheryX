package com.netheryx.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

/** Verre de quartz : transparent, les faces entre deux blocs identiques ne sont pas dessinees ; se recupere casse. */
public class BlockClearGlass extends BlockBasic {
    public BlockClearGlass(String name) {
        super(name, Material.glass, 0.5F, 1.5F, soundTypeGlass);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlock(x, y, z) != this && super.shouldSideBeRendered(world, x, y, z, side);
    }
}
