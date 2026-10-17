package com.netheryx.entity;

import com.netheryx.config.NetheryXConfig;
import com.netheryx.util.GuardianSeals;
import com.netheryx.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Sorcier infernal : mini-boss du Sanctuaire infernal. Lance des salves de boules de feu,
 * se teleporte quand on le frappe et invoque des Traqueurs de braise a mi-vie.
 */
public class InfernalWarlock extends Monster implements RangedAttackMob, GeoEntity {
    private static final String NAME = "infernal_warlock";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation CAST = NetheryXAnimations.once(NAME, "cast");
    private static final RawAnimation SUMMON = NetheryXAnimations.once(NAME, "summon");
    private static final String TAG_SUMMONED = "HasSummoned";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS).setCreateWorldFog(true);
    private boolean hasSummoned;
    private int novaCooldown = 60;

    public InfernalWarlock(EntityType<? extends InfernalWarlock> type, Level level) {
        super(type, level);
        this.xpReward = 60;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 30, 50, 18.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(NetheryXConfig.WARLOCK_MAX_HEALTH.get());
            this.setHealth(this.getMaxHealth());
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        this.triggerAnim("attack", "cast");
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getY(0.5D);
        double dz = target.getZ() - this.getZ();
        for (int i = 0; i < 3; i++) {
            SmallFireball fireball = new SmallFireball(this.level(), this,
                    dx + this.random.triangle(0.0D, 1.5D), dy, dz + this.random.triangle(0.0D, 1.5D));
            fireball.setPos(this.getX(), this.getY(0.5D) + 0.6D, this.getZ());
            this.level().addFreshEntity(fireball);
        }
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.2F, 0.7F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive() && source.getEntity() != null && this.random.nextFloat() < 0.25F) {
            this.teleportAway();
        }
        return hurt;
    }

    /** Nova de flammes : repousse et enflamme tout ce qui colle le Sorcier. */
    private void flameNova() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.triggerAnim("attack", "summon");
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D),
                entity -> entity != this && entity.isAlive() && !(entity instanceof EmberStalker))) {
            victim.hurt(this.damageSources().indirectMagic(this, this), 6.0F);
            victim.setSecondsOnFire(5);
            net.minecraft.world.phys.Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.4D);
            victim.push(push.x, 0.5D, push.z);
            victim.hurtMarked = true;
        }
        for (int i = 0; i < 48; i++) {
            double angle = i * Math.PI * 2.0D / 48.0D;
            server.sendParticles(ParticleTypes.FLAME, this.getX() + Math.cos(angle) * 2.5D, this.getY() + 0.3D,
                    this.getZ() + Math.sin(angle) * 2.5D, 3, 0.1D, 0.1D, 0.1D, 0.08D);
        }
        this.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, 0.5F);
    }

    private void teleportAway() {
        for (int attempt = 0; attempt < 10; attempt++) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * 14.0D;
            double y = this.getY() + this.random.nextInt(5) - 2;
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 14.0D;
            if (this.randomTeleport(x, y, z, true)) {
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.6F);
                return;
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        LivingEntity target = this.getTarget();
        if (--this.novaCooldown <= 0 && target != null && this.distanceToSqr(target) < 20.0D) {
            this.flameNova();
            this.novaCooldown = 80 + this.random.nextInt(40);
        }
        if (!this.hasSummoned && this.getHealth() < this.getMaxHealth() / 2.0F && this.level() instanceof ServerLevel server) {
            this.hasSummoned = true;
            this.triggerAnim("attack", "summon");
            for (int i = 0; i < 2; i++) {
                EmberStalker minion = ModEntities.EMBER_STALKER.get().create(server);
                if (minion != null) {
                    minion.moveTo(this.getX() + (i == 0 ? 2 : -2), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    minion.setTarget(this.getTarget());
                    server.addFreshEntity(minion);
                    server.sendParticles(ParticleTypes.FLAME, minion.getX(), minion.getY(0.5D), minion.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.05D);
                }
            }
            this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.8F);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel server) {
            GuardianSeals.breakSealsAround(server, this.blockPosition());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(TAG_SUMMONED, this.hasSummoned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hasSummoned = tag.getBoolean(TAG_SUMMONED);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide && this.random.nextInt(2) == 0) {
            this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.EVOKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, this::idlePredicate)
                .triggerableAnim("cast", CAST)
                .triggerableAnim("summon", SUMMON));
    }

    private PlayState movementPredicate(AnimationState<InfernalWarlock> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    private PlayState idlePredicate(AnimationState<InfernalWarlock> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
