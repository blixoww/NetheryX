package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * Coeur du Nether : boss final, reveille par le Coeur scelle. Il vole en tournant autour de sa cible, lance des salves
 * de boules de feu et repousse ce qui l'approche. A 66 % et 33 % de sa vie, il appelle des gardes et s'abrite derriere
 * un bouclier pendant 15 secondes. Il ne se regenere pas.
 */
public class EntityNetherHeart extends EntityNetheryXBoss {
    private float orbit;
    private int shield;
    private boolean firstWave;
    private boolean secondWave;

    public EntityNetherHeart(World world) {
        super(world);
        setSize(2.0F, 2.0F);
        this.arenaRadius = 28;
        this.arenaHeight = 26;
        this.experienceValue = 500;
        this.tasks.taskEntries.clear(); // il vole : pas de deplacement au sol
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(600.0D, 0.3D, 10.0D);
    }

    @Override
    protected int specialInterval() {
        return getHealth() < getMaxHealth() * 0.33F ? 30 : 50;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.worldObj.isRemote) {
            return;
        }
        if (this.shield > 0) {
            this.shield--;
        }
        EntityLivingBase target = getAttackTarget();
        if (target != null) {
            this.orbit += 0.03F;
            double tx = target.posX + Math.cos(this.orbit) * 9.0D;
            double ty = target.posY + 6.0D;
            double tz = target.posZ + Math.sin(this.orbit) * 9.0D;
            this.motionX += (Math.signum(tx - this.posX) * 0.5D - this.motionX) * 0.08D;
            this.motionY += (Math.signum(ty - this.posY) * 0.4D - this.motionY) * 0.08D;
            this.motionZ += (Math.signum(tz - this.posZ) * 0.5D - this.motionZ) * 0.08D;
            getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        } else {
            this.motionY *= 0.8D;
        }
        float ratio = getHealth() / getMaxHealth();
        if (!this.firstWave && ratio < 0.66F) {
            this.firstWave = true;
            summonGuards(target);
        } else if (!this.secondWave && ratio < 0.33F) {
            this.secondWave = true;
            summonGuards(target);
        }
    }

    private void summonGuards(EntityLivingBase target) {
        for (int i = 0; i < 3; i++) {
            EntityWitheredKnight guard = new EntityWitheredKnight(this.worldObj);
            guard.setLocationAndAngles(this.posX + this.rand.nextInt(7) - 3, this.posY - 4.0D, this.posZ + this.rand.nextInt(7) - 3, 0.0F, 0.0F);
            if (target != null) {
                guard.setAttackTarget(target);
            }
            this.worldObj.spawnEntityInWorld(guard);
        }
        this.shield = 300;
        playSound("mob.wither.spawn", 1.0F, 0.8F);
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.91D;
        this.motionY *= 0.91D;
        this.motionZ *= 0.91D;
    }

    @Override
    protected void fall(float distance) {
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.shield > 0 && source != DamageSource.outOfWorld) {
            playSound("random.anvil_land", 0.6F, 1.8F);
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void special(EntityLivingBase target) {
        if (getDistanceToEntity(target) < 6.0F) {
            shockwave(7.0D, 8.0F, 1.6D);
            return;
        }
        for (int i = 0; i < 5; i++) {
            double dx = target.posX - this.posX + this.rand.nextGaussian() * 1.5D;
            double dy = target.posY + 1.0D - (this.posY + 1.0D);
            double dz = target.posZ - this.posZ + this.rand.nextGaussian() * 1.5D;
            EntitySmallFireball fireball = new EntitySmallFireball(this.worldObj, this, dx, dy, dz);
            fireball.setPosition(this.posX, this.posY + 1.0D, this.posZ);
            this.worldObj.spawnEntityInWorld(fireball);
        }
        playSound("mob.ghast.fireball", 1.5F, 0.6F);
    }

    @Override
    public void heal(float amount) {
        // jamais de regeneration
    }

    @Override
    public boolean isFlyingAnimation() {
        return true;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(ModItems.abyssalShard, 4, 6, looting);
        dropItem(ModItems.abyssalTemplate, 1);
        dropItem(ModItems.ashWyvernEgg, 1);
        drop(ModItems.onyx, 6, 12, looting);
        drop(Items.diamond, 2, 4, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.wither.idle";
    }

    @Override
    protected String getHurtSound() {
        return "mob.wither.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.wither.death";
    }
}
