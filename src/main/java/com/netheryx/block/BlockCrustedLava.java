package com.netheryx.block;

import com.netheryx.NetheryX;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Lave croutee : laissee par les semelles de magma sous les pas de leur porteur. Elle redevient de la lave apres
 * quelques secondes (plus vite si personne ne marche dessus), et se brise en lave si on la casse.
 */
public class BlockCrustedLava extends Block {
    public BlockCrustedLava(String name) {
        super(Material.rock);
        setHardness(0.5F);
        setLightLevel(0.6F);
        setStepSound(soundTypeStone);
        setTickRandomly(false);
        setBlockName(NetheryX.MOD_ID + "." + name);
        setBlockTextureName(NetheryX.id(name));
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, this, 60 + world.rand.nextInt(60));
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.getEntitiesWithinAABB(EntityPlayer.class, getCollisionBoundingBoxFromPool(world, x, y, z).expand(0.0D, 1.0D, 0.0D)).isEmpty()) {
            world.scheduleBlockUpdate(x, y, z, this, 40);
            return;
        }
        world.setBlock(x, y, z, Blocks.lava, 0, 3);
    }

    /** Cassee : la croute cede et la lave revient. */
    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        world.setBlock(x, y, z, Blocks.lava, 0, 3);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return null;
    }
}
