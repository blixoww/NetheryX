package com.netheryx.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** Bombe de soufre lancee : explose a l'impact en mettant le feu (degats de terrain selon la regle mobGriefing). */
public class EntitySulfurBomb extends EntityThrowable {
    public EntitySulfurBomb(World world) {
        super(world);
    }

    public EntitySulfurBomb(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    @Override
    protected void onImpact(MovingObjectPosition hit) {
        if (!this.worldObj.isRemote) {
            boolean grief = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
            this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, 2.0F, true, grief);
            setDead();
        }
    }
}
