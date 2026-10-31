package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * Coffre mimique : immobile comme un coffre ordinaire jusqu'a ce qu'un joueur s'en approche (ou le frappe), puis il
 * ouvre la gueule et attaque. Bien meilleur butin qu'un coffre.
 */
public class EntityMimic extends EntityNetheryXMonster {
    public EntityMimic(World world) {
        super(world);
        setSize(0.9F, 0.9F);
        this.experienceValue = 15;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(20, Byte.valueOf((byte) 0));
    }

    public boolean isAwake() {
        return this.dataWatcher.getWatchableObjectByte(20) == 1;
    }

    private void wake() {
        this.dataWatcher.updateObject(20, Byte.valueOf((byte) 1));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(40.0D, 0.3D, 6.0D);
    }

    @Override
    protected void updateAITasks() {
        if (!isAwake()) {
            EntityPlayer player = this.worldObj.getClosestPlayerToEntity(this, 2.5D);
            if (player != null && !player.capabilities.isCreativeMode) {
                wake();
                playSound("random.chestopen", 1.0F, 0.6F);
            } else {
                this.motionX = 0.0D;
                this.motionZ = 0.0D;
                return;
            }
        }
        super.updateAITasks();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.worldObj.isRemote && !isAwake()) {
            wake();
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public float getActionProgress() {
        return isAwake() ? Math.max(0.6F, super.getActionProgress()) : 0.0F;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(Items.gold_ingot, 2, 5, looting);
        drop(ModItems.onyx, 1, 3, looting);
        drop(Items.diamond, 0, 1, looting);
        if (this.rand.nextInt(4) == 0) {
            dropItem(Items.golden_apple, 1);
        }
    }

    @Override
    protected String getLivingSound() {
        return isAwake() ? "random.chestclosed" : null;
    }

    @Override
    protected String getHurtSound() {
        return "random.wood_click";
    }

    @Override
    protected String getDeathSound() {
        return "random.break";
    }
}
