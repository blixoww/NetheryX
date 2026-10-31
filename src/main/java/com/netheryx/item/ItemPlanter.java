package com.netheryx.item;

import com.netheryx.NetheryX;
import com.netheryx.NetheryXTab;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Graines et fruits qui se plantent : un clic droit sur le dessus d'un bloc pose la culture si elle peut y pousser
 * (sable des ames, netherrack, sols du mod... selon la plante). {@link Food} : la meme chose, mais comestible.
 */
public class ItemPlanter extends Item {
    public interface BlockRef {
        Block get();
    }

    private final BlockRef crop;

    public ItemPlanter(String name, BlockRef crop) {
        this.crop = crop;
        setUnlocalizedName(NetheryX.MOD_ID + "." + name);
        setTextureName(NetheryX.id(name));
        setCreativeTab(NetheryXTab.TAB);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
        return plant(this.crop.get(), stack, player, world, x, y, z, side);
    }

    static boolean plant(Block crop, ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side) {
        if (side != 1 || !player.canPlayerEdit(x, y, z, side, stack) || !player.canPlayerEdit(x, y + 1, z, side, stack)
                || !world.isAirBlock(x, y + 1, z) || !crop.canPlaceBlockAt(world, x, y + 1, z)) {
            return false;
        }
        world.setBlock(x, y + 1, z, crop, 0, 3);
        world.playSoundEffect(x + 0.5D, y + 1.5D, z + 0.5D, crop.stepSound.func_150496_b(), 1.0F, 0.8F);
        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }
        return true;
    }

    /** Fruit ou tubercule comestible qui se plante aussi. */
    public static class Food extends ItemFood {
        private final BlockRef crop;

        public Food(String name, int nutrition, float saturation, BlockRef crop) {
            super(nutrition, saturation, false);
            this.crop = crop;
            setUnlocalizedName(NetheryX.MOD_ID + "." + name);
            setTextureName(NetheryX.id(name));
            setCreativeTab(NetheryXTab.TAB);
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
            return plant(this.crop.get(), stack, player, world, x, y, z, side);
        }
    }
}
