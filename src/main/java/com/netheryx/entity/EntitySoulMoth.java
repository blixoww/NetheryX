package com.netheryx.entity;

import net.minecraft.world.World;

/** Phalene des ames : grand papillon de nuit pale des marais des ames. */
public class EntitySoulMoth extends EntityNetheryXFlyer {
    public EntitySoulMoth(World world) {
        super(world);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (this.rand.nextInt(2) == 0) {
            dropItem(com.netheryx.registry.ModItems.soulDust, 1);
        }
    }
}
