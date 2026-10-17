package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.util.GuardianSeals;
import com.netheryx.entity.ai.SentinelSlamGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
 * Sentinelle d'Onyx : boss gardien des Citadelles d'Onyx.
 * Elle frappe au corps a corps et declenche regulierement une onde de choc qui projette les joueurs.
 */
public class OnyxSentinel extends Monster implements GeoEntity {
    private static final String NAME = "onyx_sentinel";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation PUNCH = NetheryXAnimations.once(NAME, "punch");
    private static final RawAnimation SLAM = NetheryXAnimations.once(NAME, "slam");

    private static final double SLAM_RADIUS = 5.0D;
    private static final float SLAM_DAMAGE = 10.0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);
    private int slamCooldown = 60;
    private int eruptionCooldown = 80;
    private boolean enraged;
    private static final String TAG_ENRAGED = "Enraged";
    private static final java.util.UUID RAGE_SPEED_ID = java.util.UUID.fromString("6f1b5d2a-3c1e-4c8b-9a51-2f6c7d9e1a01");

    public OnyxSentinel(EntityType<? extends OnyxSentinel> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SentinelSlamGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
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
            maxHealth.setBaseValue(NetheryXConfig.SENTINEL_MAX_HEALTH.get());
            this.setHealth(this.getMaxHealth());
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    // --- Onde de choc ------------------------------------------------------------------------

    public boolean canSlam() {
        return this.slamCooldown <= 0;
    }

    public void resetSlamCooldown() {
        this.slamCooldown = 120 + this.random.nextInt(60);
    }

    public void startSlamAnimation() {
        this.triggerAnim("attack", "slam");
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.6F);
    }

    public void performSlam() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SLAM_RADIUS, 1.0D, SLAM_RADIUS),
                entity -> entity != this && entity.isAlive() && !(entity instanceof OnyxSentinel))) {
            if (victim instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), SLAM_DAMAGE);
            Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.2D);
            victim.push(push.x, 0.7D, push.z);
            victim.hurtMarked = true;
        }
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLACKSTONE.defaultBlockState()),
                this.getX(), this.getY() + 0.1D, this.getZ(), 80, SLAM_RADIUS / 2.0D, 0.1D, SLAM_RADIUS / 2.0D, 0.15D);
        server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 3, 1.0D, 0.2D, 1.0D, 0.0D);
        this.playSound(SoundEvents.GENERIC_EXPLODE, 1.2F, 0.7F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim("attack", "punch");
        return super.doHurtTarget(target);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (!this.enraged && this.getHealth() < this.getMaxHealth() / 2.0F) {
            this.enterRage();
        }
        if (this.enraged && this.getTarget() != null && --this.eruptionCooldown <= 0) {
            this.crystalEruption(this.getTarget());
            this.eruptionCooldown = 90 + this.random.nextInt(40);
        }
    }

    /** Phase 2 : la Sentinelle accelere et fait jaillir des cristaux sous les pieds des joueurs. */
    private void enterRage() {
        this.enraged = true;
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(RAGE_SPEED_ID) == null) {
            speed.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(RAGE_SPEED_ID,
                    "Sentinel rage", 0.35D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE));
        }
        this.playSound(SoundEvents.WARDEN_ROAR, 2.0F, 0.8F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(1.0D), this.getZ(), 120, 1.0D, 1.5D, 1.0D, 0.2D);
        }
    }

    /** Une ligne de crocs de cristal (crocs d'evocateur) part de la Sentinelle vers sa cible. */
    private void crystalEruption(LivingEntity target) {
        this.triggerAnim("attack", "slam");
        Vec3 direction = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        float yaw = (float) Math.atan2(direction.z, direction.x);
        for (int i = 1; i <= 12; i++) {
            Vec3 point = this.position().add(direction.scale(1.25D * i));
            this.level().addFreshEntity(new net.minecraft.world.entity.projectile.EvokerFangs(this.level(),
                    point.x, Math.floor(this.getY()), point.z, yaw, i * 2, this));
        }
        this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.6F);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel server) {
            GuardianSeals.breakSealsAround(server, this.blockPosition());
        }
    }

    // --- Barre de boss -------------------------------------------------------------------------

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
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(TAG_ENRAGED, this.enraged);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.enraged = tag.getBoolean(TAG_ENRAGED);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.6F);
    }

    // --- GeckoLib -----------------------------------------------------------------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 6, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, this::idlePredicate)
                .triggerableAnim("punch", PUNCH)
                .triggerableAnim("slam", SLAM));
    }

    private PlayState movementPredicate(AnimationState<OnyxSentinel> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    private PlayState idlePredicate(AnimationState<OnyxSentinel> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.ONYX_GLINT.get(), 2, 1.0D, 0.0D);
    }
}
