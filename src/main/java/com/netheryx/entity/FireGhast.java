package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;

/** Ghast de feu : ghast embrase, plus resistant, dont les boules de feu explosent plus fort. */
public class FireGhast extends Ghast {
    public FireGhast(EntityType<? extends Ghast> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Ghast.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D);
    }

    @Override
    public int getExplosionPower() {
        return 2;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.EMBER_SPARK.get(), 1, 1.4D, 0.03D);
        if (this.level().isClientSide && this.random.nextInt(2) == 0) {
            this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 0.0D, 0.02D, 0.0D);
        }
    }
}
