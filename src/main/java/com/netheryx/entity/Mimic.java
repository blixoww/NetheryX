package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
 * Coffre mimique : se fait passer pour un coffre dans les donjons. Il se reveille quand on tente de l'ouvrir,
 * qu'on le frappe ou qu'on s'en approche sans etre accroupi, puis bondit sur sa proie.
 * Il garde un butin bien superieur a celui d'un coffre ordinaire.
 */
public class Mimic extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DORMANT = SynchedEntityData.defineId(Mimic.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation SLEEP = NetheryXAnimations.loop("mimic", "dormant");
    private static final RawAnimation CHOMP = NetheryXAnimations.loop("mimic", "chomp");
    private static final RawAnimation HOP = NetheryXAnimations.loop("mimic", "hop");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Mimic(EntityType<? extends Mimic> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DORMANT, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true) {
            @Override
            public boolean canUse() {
                return !Mimic.this.isDormant() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !Mimic.this.isDormant() && super.canUse();
            }
        });
    }

    public boolean isDormant() {
        return this.entityData.get(DORMANT);
    }

    private void wake(Entity target) {
        if (this.isDormant()) {
            this.entityData.set(DORMANT, false);
            this.playSound(SoundEvents.CHEST_OPEN, 1.0F, 0.6F);
            this.playSound(SoundEvents.RAVAGER_ROAR, 0.6F, 1.6F);
            if (target instanceof Player player && !player.getAbilities().instabuild) {
                this.setTarget(player);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isDormant()) {
            this.setDeltaMovement(Vec3.ZERO.add(0.0D, this.getDeltaMovement().y, 0.0D));
            this.setYRot(Math.round(this.getYRot() / 90.0F) * 90.0F);
            this.yBodyRot = this.yHeadRot = this.getYRot();
            if (!this.level().isClientSide && this.tickCount % 10 == 0) {
                Player near = this.level().getNearestPlayer(this, 2.0D);
                if (near != null && !near.isShiftKeyDown() && !near.isSpectator() && !near.getAbilities().instabuild) {
                    this.wake(near);
                }
            }
        } else {
            MobParticles.ambient(this, ModParticles.EMBER_SPARK.get(), 6, 0.8D, 0.02D);
            if (!this.level().isClientSide && this.onGround() && this.getTarget() != null && this.random.nextInt(12) == 0) {
                Vec3 toward = this.getTarget().position().subtract(this.position()).normalize().scale(0.45D);
                this.setDeltaMovement(toward.x, 0.42D, toward.z);
                this.playSound(SoundEvents.CHEST_CLOSE, 0.8F, 0.7F + this.random.nextFloat() * 0.3F);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isDormant()) {
            if (!this.level().isClientSide) {
                this.wake(player);
                this.doHurtTarget(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.wake(source.getEntity());
        }
        return hurt;
    }

    @Override
    public boolean isPushable() {
        return !this.isDormant() && super.isPushable();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Dormant", this.isDormant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Dormant")) {
            this.entityData.set(DORMANT, tag.getBoolean("Dormant"));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isDormant() ? null : SoundEvents.CHEST_LOCKED;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOD_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 2, this::predicate));
    }

    private PlayState predicate(AnimationState<Mimic> state) {
        if (this.isDormant()) {
            return state.setAndContinue(SLEEP);
        }
        return state.setAndContinue(this.onGround() ? CHOMP : HOP);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
