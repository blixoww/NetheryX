package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

/** Poisson de magme : nage dans les mers de lave (et y apparait) ; hors de la lave, il suffoque. */
public class EntityMagmafish extends EntityWaterMob {
    private double swimX;
    private double swimY;
    private double swimZ;

    public EntityMagmafish(World world) {
        super(world);
        setSize(0.6F, 0.4F);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6.0D);
    }

    /** La lave joue pour lui le role de l'eau (nage et respiration). */
    @Override
    public boolean isInWater() {
        return handleLavaMovement();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (handleLavaMovement()) {
            if (this.rand.nextInt(40) == 0 || (this.swimX == 0.0D && this.swimZ == 0.0D) || this.isCollidedHorizontally) {
                this.swimX = (this.rand.nextFloat() * 2.0F - 1.0F) * 0.12D;
                this.swimY = (this.rand.nextFloat() * 2.0F - 1.0F) * 0.05D;
                this.swimZ = (this.rand.nextFloat() * 2.0F - 1.0F) * 0.12D;
            }
            this.motionX = this.swimX;
            this.motionY = this.swimY;
            this.motionZ = this.swimZ;
            this.renderYawOffset = this.rotationYaw = (float) (-Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
        } else {
            this.motionY -= 0.08D;
            this.motionX *= 0.8D;
            this.motionZ *= 0.8D;
        }
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        moveEntity(this.motionX, this.motionY, this.motionZ);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(ModItems.magmafish, 1);
        if (this.rand.nextInt(3) == 0) {
            dropItem(ModItems.magmafishScale, 1);
        }
    }
}
