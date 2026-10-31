package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Chevalier fletri : garde des catacombes et de la foret fletrie ; ses coups fletrissent. */
public class EntityWitheredKnight extends EntityNetheryXMonster {
    public EntityWitheredKnight(World world) {
        super(world);
        setSize(0.7F, 2.2F);
        this.experienceValue = 12;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(40.0D, 0.26D, 7.0D);
    }

    @Override
    protected void onHit(EntityLivingBase target) {
        target.addPotionEffect(new PotionEffect(Potion.wither.id, 80, 0));
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(ModItems.witheredBone, 1, 2, looting);
        if (hit && this.rand.nextInt(40) == 0) {
            dropItem(ModItems.witheredBlade, 1);
        }
    }

    @Override
    protected String getLivingSound() {
        return "mob.skeleton.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.skeleton.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.skeleton.death";
    }
}
