package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/** Sentinelle d'onyx : boss de la Citadelle ; colosse lent dont le pas fait trembler le sol (onde de choc). */
public class EntityOnyxSentinel extends EntityNetheryXBoss {
    public EntityOnyxSentinel(World world) {
        super(world);
        setSize(1.4F, 3.2F);
        this.arenaRadius = 11;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(300.0D, 0.25D, 12.0D);
    }

    @Override
    protected void special(EntityLivingBase target) {
        shockwave(6.0D, 8.0F, 1.2D);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(ModItems.sentinelEye, 1);
        dropItem(ModItems.sentinelCore, 1);
        drop(ModItems.onyx, 4, 8, looting);
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
