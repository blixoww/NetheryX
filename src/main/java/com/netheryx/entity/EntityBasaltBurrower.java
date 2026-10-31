package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.world.World;

/** Fouisseur de basalte : ver de roche massif et lent, qui mord fort. */
public class EntityBasaltBurrower extends EntityNetheryXMonster {
    public EntityBasaltBurrower(World world) {
        super(world);
        setSize(0.9F, 0.7F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        stats(32.0D, 0.24D, 6.0D);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        drop(ModItems.burrowerFang, 0, 1, looting);
    }

    @Override
    protected String getLivingSound() {
        return "mob.silverfish.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.silverfish.hit";
    }

    @Override
    protected String getDeathSound() {
        return "mob.silverfish.kill";
    }
}
