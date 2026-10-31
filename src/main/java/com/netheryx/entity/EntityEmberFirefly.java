package com.netheryx.entity;

import net.minecraft.world.World;

/** Luciole de braise : petit insecte lumineux des prairies et bosquets de braise. */
public class EntityEmberFirefly extends EntityNetheryXFlyer {
    public EntityEmberFirefly(World world) {
        super(world);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (this.rand.nextInt(2) == 0) {
            dropItem(net.minecraft.init.Items.glowstone_dust, 1);
        }
    }
}
