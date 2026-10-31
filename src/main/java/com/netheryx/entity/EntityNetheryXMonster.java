package com.netheryx.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/** Monstre du Nether : insensible au feu, attaque au corps a corps les joueurs ; effets et butin par sous-classe. */
public abstract class EntityNetheryXMonster extends EntityMob implements IAnimatedMob {
    private int attackAnim;

    public EntityNetheryXMonster(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.experienceValue = 8;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    protected void stats(double health, double speed, double damage) {
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(health);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(speed);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(damage);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        this.attackAnim = 10;
        this.worldObj.setEntityState(this, (byte) 4);
        boolean hit = super.attackEntityAsMob(target);
        if (hit && target instanceof EntityLivingBase) {
            onHit((EntityLivingBase) target);
        }
        return hit;
    }

    /** Effet supplementaire quand le coup porte. */
    protected void onHit(EntityLivingBase target) {
    }

    @Override
    public void handleHealthUpdate(byte id) {
        if (id == 4) {
            this.attackAnim = 10;
        } else {
            super.handleHealthUpdate(id);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.attackAnim > 0) {
            this.attackAnim--;
        }
    }

    @Override
    public boolean isFlyingAnimation() {
        return false;
    }

    @Override
    public float getActionProgress() {
        return this.attackAnim / 10.0F;
    }

    /** Lache entre min et max objets (+1 par niveau de Butin, au hasard). */
    protected void drop(Item item, int min, int max, int looting) {
        int count = min + this.rand.nextInt(max - min + 1) + (looting > 0 ? this.rand.nextInt(looting + 1) : 0);
        if (count > 0) {
            dropItem(item, count);
        }
    }

    @Override
    protected String getLivingSound() {
        return "mob.blaze.breathe";
    }

    @Override
    protected String getHurtSound() {
        return "mob.blaze.hit";
    }

    @Override
    protected String getDeathSound() {
        return "mob.blaze.death";
    }
}
