package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/** Traqueur de braise : predateur rapide des bosquets de braise ; ses morsures enflamment. */
public class EntityEmberStalker extends EntityNetheryXMonster {
    public EntityEmberStalker(World world) {
        super(world);
        setSize(0.9F, 1.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(28.0D, 0.32D, 5.0D);
    }

    @Override
    protected void onHit(EntityLivingBase target) {
        target.setFire(4);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(isBurning() ? ModItems.cookedStalkerMeat : ModItems.stalkerMeat, 1, 2, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.wolf.growl";
    }

    @Override
    protected String getHurtSound() {
        return "mob.wolf.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.wolf.death";
    }
}
