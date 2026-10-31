package com.netheryx.block;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import com.netheryx.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

/** Fleur ou herbe du Nether (rendu en croix) : pousse sur la netherrack, le sable des ames et les sols du mod. */
public class BlockNetherPlant extends BlockBush {
    public BlockNetherPlant(String name, int light) {
        super(Material.plants);
        setBlockName(NetheryX.MOD_ID + "." + name);
        setBlockTextureName(NetheryX.id(name));
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setCreativeTab(NetheryXTab.TAB);
        setLightLevel(light / 15.0F);
        float r = 0.3F;
        setBlockBounds(0.5F - r, 0.0F, 0.5F - r, 0.5F + r, 0.8F, 0.5F + r);
    }

    @Override
    protected boolean canPlaceBlockOn(Block ground) {
        return ground == Blocks.netherrack || ground == Blocks.soul_sand || ground == ModBlocks.emberNylium
                || ground == ModBlocks.netherGrassBlock || ground == ModBlocks.ashBlock || ground == Blocks.grass
                || ground == Blocks.dirt;
    }
}
