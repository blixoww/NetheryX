package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.world.World;

/** Sorcier infernal : boss du Sanctuaire ; salves de boules de feu, et teleportation quand on s'eloigne. */
public class EntityInfernalWarlock extends EntityNetheryXBoss {
    public EntityInfernalWarlock(World world) {
        super(world);
        setSize(0.7F, 2.3F);
        this.arenaRadius = 13;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(260.0D, 0.27D, 8.0D);
    }

    @Override
    protected int specialInterval() {
        return getHealth() < getMaxHealth() * 0.5F ? 35 : 55;
    }

    @Override
    protected void special(EntityLivingBase target) {
        if (getDistanceToEntity(target) > 14.0F) {
            double angle = this.rand.nextDouble() * Math.PI * 2.0D;
            setPositionAndUpdate(target.posX + Math.cos(angle) * 5.0D, target.posY, target.posZ + Math.sin(angle) * 5.0D);
            playSound("mob.endermen.portal", 1.0F, 1.0F);
            return;
        }
        for (int i = 0; i < 3; i++) {
            double dx = target.posX - this.posX + this.rand.nextGaussian() * 0.6D;
            double dy = target.boundingBox.minY + target.height / 2.0F - (this.posY + this.height / 2.0F);
            double dz = target.posZ - this.posZ + this.rand.nextGaussian() * 0.6D;
            EntitySmallFireball fireball = new EntitySmallFireball(this.worldObj, this, dx, dy, dz);
            fireball.setPosition(fireball.posX, this.posY + this.height / 2.0F + 0.5D, fireball.posZ);
            this.worldObj.spawnEntityInWorld(fireball);
        }
        playSound("mob.ghast.fireball", 1.0F, 0.8F);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(ModItems.warlockSigil, 1);
        drop(Items.blaze_rod, 3, 6, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.blaze.breathe";
    }
}
