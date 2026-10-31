package com.netheryx.entity;

import com.netheryx.entity.ai.FollowCaptainGoal;
import com.netheryx.entity.ai.KnightPatrolGoal;
import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModParticles;
import com.netheryx.util.RenderScaled;
import com.netheryx.util.MobParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Chevalier fletri : guerrier squelette de la Foret fletrie. Ses coups infligent le Fletrissement.
 * <p>
 * Un chevalier apparu naturellement peut etre un <b>capitaine</b> : plus grand et plus robuste, il recrute deux ou
 * trois chevaliers et les mene en patrouille. Les siens gardent la formation, recoivent Resistance pres de lui et
 * chargent tous ensemble des que l'un d'eux voit une cible ; a sa mort, ils sont demoralises.
 */
public class WitheredKnight extends Monster implements GeoEntity, RenderScaled {
    private static final EntityDataAccessor<Boolean> CAPTAIN = SynchedEntityData.defineId(WitheredKnight.class,
            EntityDataSerializers.BOOLEAN);
    private static final float CAPTAIN_CHANCE = 0.16F;
    private static final double PATROL_RANGE = 24.0D;
    private static final String NAME = "withered_knight";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation ATTACK = NetheryXAnimations.once(NAME, "attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Capitaine suivi par ce chevalier (null : chevalier isole ou capitaine lui-meme). */
    @Nullable
    private UUID captainId;
    @Nullable
    private WitheredKnight captainCache;

    public WitheredKnight(EntityType<? extends WitheredKnight> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(4, new FollowCaptainGoal(this));
        this.goalSelector.addGoal(5, new KnightPatrolGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    // --- Patrouille ---------------------------------------------------------------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CAPTAIN, false);
    }

    public boolean isCaptain() {
        return this.entityData.get(CAPTAIN);
    }

    private void promoteToCaptain() {
        this.entityData.set(CAPTAIN, true);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(52.0D);
        this.getAttribute(Attributes.ARMOR).setBaseValue(11.0D);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(9.0D);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        this.setHealth(this.getMaxHealth());
        this.xpReward = 30;
    }

    /** Capitaine de ce chevalier s'il est vivant et assez proche, sinon null. */
    @Nullable
    public WitheredKnight findCaptain() {
        if (this.captainId == null || !(this.level() instanceof ServerLevel server)) {
            return null;
        }
        if (this.captainCache == null || this.captainCache.isRemoved()) {
            this.captainCache = server.getEntity(this.captainId) instanceof WitheredKnight knight ? knight : null;
        }
        if (this.captainCache == null || !this.captainCache.isAlive() || this.distanceToSqr(this.captainCache) > 48.0D * 48.0D) {
            return null;
        }
        return this.captainCache;
    }

    /** Vrai si les deux chevaliers appartiennent a la meme patrouille. */
    private boolean sameSquad(WitheredKnight other) {
        UUID squad = this.isCaptain() ? this.getUUID() : this.captainId;
        UUID otherSquad = other.isCaptain() ? other.getUUID() : other.captainId;
        return squad != null && squad.equals(otherSquad);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                                  @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        if (reason == MobSpawnType.NATURAL && this.random.nextFloat() < CAPTAIN_CHANCE) {
            this.promoteToCaptain();
            int recruits = 2 + this.random.nextInt(2);
            for (int i = 0; i < recruits; i++) {
                this.recruit(level, difficulty);
            }
        }
        return result;
    }

    private void recruit(ServerLevelAccessor level, DifficultyInstance difficulty) {
        WitheredKnight knight = ModEntities.WITHERED_KNIGHT.get().create(level.getLevel());
        if (knight == null) {
            return;
        }
        for (int attempt = 0; attempt < 6; attempt++) {
            BlockPos pos = this.blockPosition().offset(this.random.nextInt(7) - 3, 0, this.random.nextInt(7) - 3);
            knight.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, this.getYRot(), 0.0F);
            if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) && level.noCollision(knight)) {
                knight.finalizeSpawn(level, difficulty, MobSpawnType.REINFORCEMENT, null, null);
                knight.captainId = this.getUUID();
                level.addFreshEntityWithPassengers(knight);
                return;
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 10 != 0 || (!this.isCaptain() && this.captainId == null)) {
            return;
        }
        boolean aura = this.isCaptain() && this.tickCount % 40 == 0;
        LivingEntity target = this.getTarget();
        for (WitheredKnight ally : this.level().getEntitiesOfClass(WitheredKnight.class, this.getBoundingBox().inflate(PATROL_RANGE),
                other -> other != this && other.isAlive() && this.sameSquad(other))) {
            // Alerte : toute la patrouille charge la cible reperee par l'un des siens
            if (target != null && target.isAlive() && ally.getTarget() == null) {
                ally.setTarget(target);
            }
            if (aura && !ally.isCaptain() && ally.distanceToSqr(this) < 12.0D * 12.0D) {
                ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, true), this);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.isCaptain() && !this.level().isClientSide) {
            // Le capitaine tombe : sa patrouille, demoralisee, se disperse
            for (WitheredKnight ally : this.level().getEntitiesOfClass(WitheredKnight.class, this.getBoundingBox().inflate(PATROL_RANGE),
                    other -> other != this && this.getUUID().equals(other.captainId))) {
                ally.captainId = null;
                ally.captainCache = null;
                ally.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200), this);
                ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100), this);
            }
        }
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Captain", this.isCaptain());
        if (this.captainId != null) {
            tag.putUUID("CaptainId", this.captainId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(CAPTAIN, tag.getBoolean("Captain"));
        if (this.isCaptain()) {
            this.xpReward = 30;
        }
        this.captainId = tag.hasUUID("CaptainId") ? tag.getUUID("CaptainId") : null;
    }

    @Override
    protected Component getTypeName() {
        return this.isCaptain() ? Component.translatable("entity.netheryx.withered_knight_captain") : super.getTypeName();
    }

    @Override
    public float renderScale() {
        return this.isCaptain() ? 1.15F : 1.0F;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim("attack", "attack");
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100), this);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.15F, 0.9F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, this::idlePredicate)
                .triggerableAnim("attack", ATTACK));
    }

    private PlayState movementPredicate(AnimationState<WitheredKnight> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    private PlayState idlePredicate(AnimationState<WitheredKnight> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.WITHER_SPORE.get(), 4, 0.8D, -0.01D);
        if (this.isCaptain()) {
            MobParticles.ambient(this, ParticleTypes.SOUL_FIRE_FLAME, 6, 0.5D, 0.02D);
        }
    }
}
