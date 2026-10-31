package com.netheryx.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.Random;

/** Amas de cristaux (onyx, soufre, quartz, givre) : pousse sur la roche, lache quelques eclats. */
public class BlockCrystal extends BlockNetherPlant {
    public interface ItemRef {
        Item get();
    }

    private final ItemRef drop;
    private final int min;
    private final int max;

    public BlockCrystal(String name, int light, ItemRef drop, int min, int max) {
        super(name, light);
        this.drop = drop;
        this.min = min;
        this.max = max;
        setHardness(1.5F);
        setStepSound(soundTypeGlass);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    protected boolean canPlaceBlockOn(Block ground) {
        return ground.isOpaqueCube();
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return this.drop.get();
    }

    @Override
    public int quantityDropped(Random random) {
        return this.min + random.nextInt(this.max - this.min + 1);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return this.quantityDropped(random) + (fortune > 0 ? random.nextInt(fortune + 1) : 0);
    }
}
