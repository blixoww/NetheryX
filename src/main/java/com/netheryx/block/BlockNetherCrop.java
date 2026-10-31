package com.netheryx.block;

import com.netheryx.NetheryX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Culture du Nether (piment de blaze, ble de cendre, tubercule des ames) : 8 etapes de croissance affichees sur
 * 4 textures. Contrairement au ble vanilla, elle pousse sans lumiere (le Nether est sombre), sur la terre labouree
 * ou le sable des ames.
 */
public class BlockNetherCrop extends BlockCrops {
    public interface ItemRef {
        Item get();
    }

    private final String name;
    private final ItemRef seed;
    private final ItemRef crop;
    @SideOnly(Side.CLIENT)
    private IIcon[] stages;

    public BlockNetherCrop(String name, ItemRef seed, ItemRef crop) {
        this.name = name;
        this.seed = seed;
        this.crop = crop;
        setBlockName(NetheryX.MOD_ID + "." + name);
        setBlockTextureName(NetheryX.id(name));
    }

    @Override
    protected boolean canPlaceBlockOn(Block ground) {
        return ground == Blocks.farmland || ground == Blocks.soul_sand;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        this.checkAndDropBlock(world, x, y, z);
        int age = world.getBlockMetadata(x, y, z);
        if (age < 7 && random.nextInt(world.getBlock(x, y - 1, z) == Blocks.farmland ? 6 : 9) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, age + 1, 2);
        }
    }

    @Override
    protected Item func_149866_i() {
        return this.seed.get();
    }

    @Override
    protected Item func_149865_P() {
        return this.crop.get();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.stages = new IIcon[4];
        for (int i = 0; i < 4; i++) {
            this.stages[i] = register.registerIcon(NetheryX.id(this.name.replace("_crop", "") + "_stage" + i));
        }
        this.blockIcon = this.stages[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int age = Math.max(0, Math.min(7, meta));
        return this.stages[age >= 7 ? 3 : age / 2 >= 3 ? 2 : age / 2];
    }
}
