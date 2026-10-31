package com.netheryx.entity;

import com.netheryx.registry.ModItems;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/** Cube d'onyx : gelee cristalline qui se divise comme un slime ; les plus petits lachent des eclats d'onyx. */
public class EntityOnyxCube extends EntitySlime {
    public EntityOnyxCube(World world) {
        super(world);
        this.isImmuneToFire = true;
    }

    @Override
    protected EntitySlime createInstance() {
        return new EntityOnyxCube(this.worldObj);
    }

    @Override
    protected Item getDropItem() {
        return getSlimeSize() == 1 ? ModItems.onyxShard : null;
    }

    @Override
    protected String getSlimeParticle() {
        return "portal";
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.difficultySetting != net.minecraft.world.EnumDifficulty.PEACEFUL
                && this.worldObj.checkNoEntityCollision(this.boundingBox)
                && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                && !this.worldObj.isAnyLiquid(this.boundingBox);
    }
}
