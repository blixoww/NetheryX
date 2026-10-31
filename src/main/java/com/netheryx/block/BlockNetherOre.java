package com.netheryx.block;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/**
 * Minerai du Nether : lache un objet (onyx, soufre, quartz) en quantite variable, augmentee par Fortune comme les
 * minerais vanilla, et de l'experience. Avec Toucher de soie, il se recupere tel quel.
 */
public class BlockNetherOre extends BlockBasic {
    public interface Drop {
        Item get();
    }

    private final Drop drop;
    private final int min;
    private final int max;
    private final int minXp;
    private final int maxXp;

    public BlockNetherOre(String name, int harvestLevel, Drop drop, int min, int max, int minXp, int maxXp) {
        super(name, Material.rock, 3.0F, 5.0F, soundTypeStone);
        tool("pickaxe", harvestLevel);
        this.drop = drop;
        this.min = min;
        this.max = max;
        this.minXp = minXp;
        this.maxXp = maxXp;
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
        if (fortune > 0) {
            int bonus = random.nextInt(fortune + 2) - 1;
            return this.quantityDropped(random) * (Math.max(0, bonus) + 1);
        }
        return this.quantityDropped(random);
    }

    @Override
    public int getExpDrop(IBlockAccess world, int meta, int fortune) {
        return MathHelper.getRandomIntegerInRange(new Random(), this.minXp, this.maxXp);
    }
}
