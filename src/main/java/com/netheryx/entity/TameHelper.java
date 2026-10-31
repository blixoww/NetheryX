package com.netheryx.entity;

import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;

/** Apprivoisement commun aux compagnons du mod (comme le loup vanilla). */
public final class TameHelper {
    private TameHelper() {
    }

    public static void tame(EntityTameable entity, EntityPlayer player) {
        entity.setTamed(true);
        entity.setPathToEntity(null);
        entity.setAttackTarget(null);
        entity.func_152115_b(player.getUniqueID().toString());
        entity.setHealth(entity.getMaxHealth());
        entity.worldObj.setEntityState(entity, (byte) 7);
    }
}
