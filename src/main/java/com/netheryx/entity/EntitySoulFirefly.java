package com.netheryx.entity;

import net.minecraft.world.World;

/** Luciole des ames : petit insecte lumineux des vallees des ames. */
public class EntitySoulFirefly extends EntityNetheryXFlyer {
    public EntitySoulFirefly(World world) {
        super(world);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (this.rand.nextInt(2) == 0) {
            dropItem(com.netheryx.registry.ModItems.soulDust, 1);
        }
    }
}
