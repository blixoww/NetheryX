package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import java.util.ArrayList;

/** Escargot de cristal : lent et paisible ; sa coquille se tond aux cisailles (eclats d'onyx), puis repousse. */
public class EntityCrystalSnail extends EntityNetheryXAnimal implements IShearable {
    private int regrow;

    public EntityCrystalSnail(World world) {
        super(world, null);
        setSize(0.6F, 0.6F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(14.0D, 0.12D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.regrow > 0) {
            this.regrow--;
        }
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
        return this.regrow <= 0 && !isChild();
    }

    @Override
    public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(ModItems.onyxShard, 2 + this.rand.nextInt(3 + fortune)));
        this.regrow = 6000;
        playSound("dig.glass", 1.0F, 1.2F);
        return drops;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) {
        return new EntityCrystalSnail(this.worldObj);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(ModItems.onyxShard, 0, 2, looting);
    }
}
