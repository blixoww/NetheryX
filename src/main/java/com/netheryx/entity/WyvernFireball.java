package com.netheryx.entity;

import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Grande boule de feu de la wyverne : une sphere de flammes tourbillonnantes qui laisse une trainee de feu, et
 * explose a l'impact en gerbe de flammes. Elle blesse et enflamme les creatures autour, mais ne casse ni ne brule
 * aucun bloc, et epargne la wyverne, son maitre et les compagnons de son maitre.
 */
public class WyvernFireball extends Fireball {
    private static final double BLAST_RADIUS = 3.5D;
    private static final float BLAST_DAMAGE = 10.0F;

    public WyvernFireball(EntityType<? extends WyvernFireball> type, Level level) {
        super(type, level);
    }

    public WyvernFireball(Level level, LivingEntity shooter, Vec3 direction) {
        super(ModEntities.WYVERN_FIREBALL.get(), shooter, direction.x, direction.y, direction.z, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // La sphere : un coeur blanc et des flammes qui tournent autour
            Vec3 c = this.position().add(0.0D, this.getBbHeight() / 2.0D, 0.0D);
            this.level().addParticle(ModParticles.DRAGON_FLAME_CORE.get(), c.x, c.y, c.z, 0.0D, 0.0D, 0.0D);
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ModParticles.DRAGON_FLAME.get(), c.x + this.random.nextGaussian() * 0.25D,
                        c.y + this.random.nextGaussian() * 0.25D, c.z + this.random.nextGaussian() * 0.25D,
                        this.random.nextGaussian() * 0.03D, this.random.nextGaussian() * 0.03D, this.random.nextGaussian() * 0.03D);
            }
        } else if (this.tickCount > 120) {
            this.discard();
        }
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ModParticles.DRAGON_FLAME.get();
    }

    /** Trainee rectiligne et vitesse plafonnee (environ un bloc par tick). */
    @Override
    protected float getInertia() {
        return 0.9F;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !this.isFriend(entity);
    }

    /** La wyverne qui l'a lancee, son cavalier, son maitre et les compagnons de son maitre. */
    private boolean isFriend(Entity entity) {
        Entity owner = this.getOwner();
        if (owner == null) {
            return false;
        }
        if (entity == owner || owner.hasPassenger(entity) || entity instanceof WyvernFireball) {
            return true;
        }
        if (owner instanceof TamableAnimal wyvern && wyvern.getOwner() != null) {
            LivingEntity master = wyvern.getOwner();
            return entity == master || (entity instanceof TamableAnimal pet && pet.isOwnedBy(master));
        }
        return false;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 c = result.getLocation();
        // Gerbe de flammes dans toutes les directions et champignon de fumee
        for (int i = 0; i < 40; i++) {
            Vec3 dir = new Vec3(this.random.nextGaussian(), this.random.nextGaussian() * 0.6D + 0.3D, this.random.nextGaussian()).normalize();
            level.sendParticles(ModParticles.DRAGON_FLAME.get(), c.x, c.y + 0.3D, c.z, 0, dir.x, dir.y, dir.z, 0.35D + this.random.nextDouble() * 0.3D);
        }
        for (int i = 0; i < 6; i++) {
            level.sendParticles(ModParticles.DRAGON_FLAME_CORE.get(), c.x, c.y + 0.3D, c.z, 0, this.random.nextGaussian(), 0.3D,
                    this.random.nextGaussian(), 0.15D);
        }
        level.sendParticles(ModParticles.DRAGON_SMOKE.get(), c.x, c.y + 1.0D, c.z, 6, 1.0D, 0.6D, 1.0D, 0.03D);
        level.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.4F, 0.9F);
        level.playSound(null, c.x, c.y, c.z, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.2F, 0.6F);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BLAST_RADIUS))) {
            if (this.isFriend(victim) || victim.distanceToSqr(c) > BLAST_RADIUS * BLAST_RADIUS) {
                continue;
            }
            double falloff = 1.0D - Math.sqrt(victim.distanceToSqr(c)) / (BLAST_RADIUS + 1.0D);
            victim.hurt(this.damageSources().fireball(this, owner), (float) (BLAST_DAMAGE * falloff));
            victim.setSecondsOnFire(6);
            Vec3 push = victim.position().subtract(c).multiply(1.0D, 0.0D, 1.0D).normalize().scale(0.9D * falloff);
            victim.setDeltaMovement(victim.getDeltaMovement().add(push.x, 0.45D, push.z));
            victim.hurtMarked = true;
        }
        this.discard();
    }
}
