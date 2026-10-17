package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Poisson de magme : petit poisson qui nage dans les mers de lave. Se peche avec la Nasse a magma
 * ou se chasse ; il donne une chair qui protege du feu et des ecailles pour brasser la potion de plongee en lave.
 * Hors de la lave, il s'agite et s'eteint peu a peu.
 */
public class Magmafish extends PathfinderMob implements GeoEntity {
    private static final RawAnimation SWIM = NetheryXAnimations.loop("magmafish", "swim");
    private static final RawAnimation FLOP = NetheryXAnimations.loop("magmafish", "flop");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Vec3 swimTarget;
    private int retargetTimer;
    private int outOfLavaTicks;

    public Magmafish(EntityType<? extends Magmafish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D).add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    public static boolean checkMagmafishSpawnRules(EntityType<Magmafish> type, LevelAccessor level, MobSpawnType reason, BlockPos pos,
                                                   RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.LAVA) && level.getFluidState(pos.above()).is(FluidTags.LAVA);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.5D));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.EMBER_SPARK.get(), 10, 0.5D, 0.03D);
        if (this.level().isClientSide) {
            return;
        }
        if (this.isInLava()) {
            this.outOfLavaTicks = 0;
            this.swim();
        } else {
            this.outOfLavaTicks++;
            if (this.onGround() && this.random.nextInt(10) == 0) {
                this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextFloat() - 0.5F) * 0.2F, 0.35D,
                        (this.random.nextFloat() - 0.5F) * 0.2F));
                this.playSound(SoundEvents.COD_FLOP, 0.6F, 0.6F);
            }
            if (this.outOfLavaTicks > 200 && this.tickCount % 40 == 0) {
                this.hurt(this.damageSources().dryOut(), 1.0F);
            }
        }
    }

    /** Nage vers un point de lave choisi au hasard, sans pathfinding (la lave est un volume ouvert). */
    private void swim() {
        if (this.swimTarget == null || --this.retargetTimer <= 0 || this.position().distanceToSqr(this.swimTarget) < 1.0D) {
            this.retargetTimer = 40 + this.random.nextInt(60);
            for (int attempt = 0; attempt < 10; attempt++) {
                BlockPos candidate = this.blockPosition().offset(this.random.nextInt(13) - 6, this.random.nextInt(5) - 2,
                        this.random.nextInt(13) - 6);
                if (this.level().getFluidState(candidate).is(FluidTags.LAVA)) {
                    this.swimTarget = Vec3.atCenterOf(candidate);
                    break;
                }
            }
        }
        if (this.swimTarget != null) {
            Vec3 direction = this.swimTarget.subtract(this.position()).normalize();
            double speed = this.goalSelector.getRunningGoals().findAny().isPresent() ? 0.05D : 0.025D;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.85D).add(direction.scale(speed)));
            float yaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.rotLerp(0.3F, this.getYRot(), yaw));
            this.yBodyRot = this.getYRot();
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isInLava()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(input);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.hasCustomName();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 5;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<Magmafish> state) {
        return state.setAndContinue(this.isInLava() ? SWIM : FLOP);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
