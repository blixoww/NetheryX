package com.netheryx.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Petit insecte volant (lucioles, phalene) : vole au hasard comme une chauve-souris, sans jamais attaquer. */
public abstract class EntityNetheryXFlyer extends EntityAmbientCreature implements IAnimatedMob {
    private ChunkCoordinates target;

    public EntityNetheryXFlyer(World world) {
        super(world);
        setSize(0.5F, 0.5F);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(2.0D);
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.motionY *= 0.6D;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (this.target != null && (!this.worldObj.isAirBlock(this.target.posX, this.target.posY, this.target.posZ) || this.target.posY < 1)) {
            this.target = null;
        }
        if (this.target == null || this.rand.nextInt(30) == 0
                || this.target.getDistanceSquared((int) this.posX, (int) this.posY, (int) this.posZ) < 4.0F) {
            this.target = new ChunkCoordinates((int) this.posX + this.rand.nextInt(7) - this.rand.nextInt(7),
                    (int) this.posY + this.rand.nextInt(6) - 2, (int) this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
        }
        double dx = this.target.posX + 0.5D - this.posX;
        double dy = this.target.posY + 0.1D - this.posY;
        double dz = this.target.posZ + 0.5D - this.posZ;
        this.motionX += (Math.signum(dx) * 0.5D - this.motionX) * 0.1D;
        this.motionY += (Math.signum(dy) * 0.7D - this.motionY) * 0.1D;
        this.motionZ += (Math.signum(dz) * 0.5D - this.motionZ) * 0.1D;
        float yaw = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) - 90.0F;
        this.moveForward = 0.5F;
        this.rotationYaw += MathHelper.wrapAngleTo180_float(yaw - this.rotationYaw);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void fall(float distance) {
    }

    @Override
    protected void updateFallState(double distance, boolean onGround) {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entity) {
    }

    @Override
    protected void collideWithNearbyEntities() {
    }

    @Override
    public boolean isFlyingAnimation() {
        return true;
    }

    @Override
    public float getActionProgress() {
        return 0.0F;
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }
}
