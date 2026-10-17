package com.netheryx.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.level.Level;

/** Cube d'onyx : cube de magma cristallise, plus resistant ; il se divise en cubes d'onyx plus petits. */
public class OnyxCube extends MagmaCube {
    public OnyxCube(EntityType<? extends MagmaCube> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MagmaCube.createAttributes().add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ParticleTypes.REVERSE_PORTAL;
    }
}
