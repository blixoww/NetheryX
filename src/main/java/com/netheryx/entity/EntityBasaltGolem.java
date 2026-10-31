package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/**
 * Golem de basalte : gardien de la Forge de basalte ; frappe le sol (onde de choc qu'on evite en s'eloignant) et
 * charge en ligne droite.
 */
public class EntityBasaltGolem extends EntityNetheryXBoss {
    private int charge;

    public EntityBasaltGolem(World world) {
        super(world);
        setSize(1.4F, 2.9F);
        this.arenaRadius = 12;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(240.0D, 0.24D, 12.0D);
    }

    @Override
    protected void special(EntityLivingBase target) {
        if (this.rand.nextBoolean() && getDistanceToEntity(target) > 5.0F) {
            // Charge : un elan droit vers la cible
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            this.motionX = dx / length * 1.6D;
            this.motionZ = dz / length * 1.6D;
            this.motionY = 0.3D;
            this.charge = 20;
            playSound("mob.irongolem.throw", 1.5F, 0.6F);
        } else {
            shockwave(5.5D, 10.0F, 1.1D);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.charge > 0) {
            this.charge--;
            for (Object o : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(0.6D, 0.0D, 0.6D))) {
                if (o instanceof EntityLivingBase && !(o instanceof EntityNetheryXBoss)) {
                    attackEntityAsMob((EntityLivingBase) o);
                    ((EntityLivingBase) o).addVelocity(this.motionX, 0.5D, this.motionZ);
                    this.charge = 0;
                }
            }
        }
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(ModItems.basaltHeartstone, 1);
        drop(ModItems.basaltCarapace, 2, 4, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.irongolem.walk";
    }

    @Override
    protected String getHurtSound() {
        return "mob.irongolem.hit";
    }

    @Override
    protected String getDeathSound() {
        return "mob.irongolem.death";
    }
}
