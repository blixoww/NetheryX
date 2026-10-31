package com.netheryx.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/** Bloc translucide (feuillage cristallise, glace de givre) ; peut lacher un objet au lieu de lui-meme. */
public class BlockTranslucent extends BlockBasic {
    public interface ItemRef {
        Item get();
    }

    private ItemRef drop;
    private int dropChance;

    public BlockTranslucent(String name, Material material, float hardness, SoundType sound) {
        super(name, material, hardness, hardness, sound);
    }

    /** Lache cet objet une fois sur chance (sinon rien) ; sans appel, le bloc se lache lui-meme. */
    public BlockTranslucent drops(ItemRef item, int chance) {
        this.drop = item;
        this.dropChance = chance;
        return this;
    }

    public BlockTranslucent slippery() {
        this.slipperiness = 0.98F;
        return this;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return this.drop == null ? super.getItemDropped(meta, random, fortune) : this.drop.get();
    }

    @Override
    public int quantityDropped(Random random) {
        return this.drop == null ? 1 : (random.nextInt(this.dropChance) == 0 ? 1 : 0);
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
