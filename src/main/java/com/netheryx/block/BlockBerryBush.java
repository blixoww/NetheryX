package com.netheryx.block;

import com.netheryx.NetheryX;
import com.netheryx.registry.ModBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

/** Buisson de baies de feu : 4 stades ; mur, un clic droit cueille les baies et le buisson repousse. */
public class BlockBerryBush extends BlockBush {
    public interface ItemRef {
        Item get();
    }

    private final String name;
    private final ItemRef berries;
    @SideOnly(Side.CLIENT)
    private IIcon[] stages;

    public BlockBerryBush(String name, ItemRef berries) {
        super(Material.plants);
        this.name = name;
        this.berries = berries;
        setBlockName(NetheryX.MOD_ID + "." + name);
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setTickRandomly(true);
        setLightLevel(0.3F);
    }

    @Override
    protected boolean canPlaceBlockOn(Block ground) {
        return ground == Blocks.netherrack || ground == Blocks.soul_sand || ground == Blocks.grass || ground == Blocks.dirt
                || ground == ModBlocks.emberNylium || ground == ModBlocks.netherGrassBlock;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        super.updateTick(world, x, y, z, random);
        int age = world.getBlockMetadata(x, y, z);
        if (age < 3 && random.nextInt(5) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, age + 1, 2);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
        if (world.getBlockMetadata(x, y, z) < 3) {
            return false;
        }
        if (!world.isRemote) {
            int count = 1 + world.rand.nextInt(2);
            world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, new ItemStack(this.berries.get(), count)));
            world.setBlockMetadataWithNotify(x, y, z, 1, 2);
        }
        return true;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this.berries.get(), meta >= 3 ? 2 : 1));
        return drops;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.stages = new IIcon[4];
        for (int i = 0; i < 4; i++) {
            this.stages[i] = register.registerIcon(NetheryX.id(this.name + "_stage" + i));
        }
        this.blockIcon = this.stages[3];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.stages[Math.max(0, Math.min(3, meta))];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return this.berries.get();
    }

}
