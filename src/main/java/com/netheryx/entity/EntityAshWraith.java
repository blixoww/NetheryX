package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Spectre de cendre : flotte dans les terres cendrees ; son contact aveugle. Il redescend lentement, sans chute. */
public class EntityAshWraith extends EntityNetheryXMonster {
    public EntityAshWraith(World world) {
        super(world);
        setSize(0.6F, 1.9F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(24.0D, 0.28D, 4.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
    }

    @Override
    protected void fall(float distance) {
    }

    @Override
    protected void onHit(EntityLivingBase target) {
        target.addPotionEffect(new PotionEffect(Potion.blindness.id, 60, 0));
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(ModItems.wraithEssence, 0, 2, looting);
        drop(ModItems.wraithCloth, 0, 1, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.ghast.moan";
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
