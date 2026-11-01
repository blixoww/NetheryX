package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.entity.boss.BossEncounter;
import com.netheryx.entity.boss.BossFx;
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
import net.minecraft.world.phys.Vec3;
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
 * Sorcier infernal : boss du Sanctuaire infernal. Salves de boules de feu, nova annoncee au contact, pluie de meteores
 * (cercles au sol), clignement derriere les cibles eloignees.
 * Phase 2 (66 %) : murs de flammes qui avancent dans la nef, avec une breche a trouver.
 * Phase 3 (33 %) : spirale de feu, immobile au centre.
 * A chaque phase il invoque des Traqueurs de braise : tant qu'ils vivent, son bouclier absorbe 70 % des degats.
 */
public class InfernalWarlock extends Monster implements com.netheryx.entity.boss.BossEntity, RangedAttackMob, GeoEntity {
    private static final String NAME = "infernal_warlock";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation CAST = NetheryXAnimations.once(NAME, "cast");
    private static final RawAnimation SUMMON = NetheryXAnimations.once(NAME, "summon");
    private static final RawAnimation METEOR = NetheryXAnimations.once(NAME, "meteor");
    private static final RawAnimation BLINK = NetheryXAnimations.once(NAME, "blink");
    private static final String TAG_SUMMONED = "HasSummoned";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Le boss reste cloisonne dans son arene (rayon 13 blocs autour de son point d'apparition). */
    private final com.netheryx.util.BossArena arena = new com.netheryx.util.BossArena(15.0D, 7.0D);
    private final BossEncounter encounter = new BossEncounter(this, BossFx.FIRE, new float[]{0.66F, 0.33F}, this::onPhase);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS).setCreateWorldFog(true);
    private boolean hasSummoned;
    private int novaCooldown = 60;
    private int meteorCooldown = 120;
    private int wallCooldown = 80;
    private int spiralCooldown = 60;
    private int spiralTicks;
    private int teleportCooldown;
    private int blinkCooldown = 160;
    private int summonWaves;
    private final java.util.List<java.util.UUID> minions = new java.util.ArrayList<>();

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
        if (this.isBusy()) {
            return;
        }
        this.encounter.hazards().started().merge("volley", 1, Integer::sum);
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

    /** Bouclier de braise : tant que ses serviteurs vivent, le Sorcier ne subit que 30 % des degats. */
    private boolean shielded() {
        if (!(this.level() instanceof ServerLevel server)) {
            return false;
        }
        this.minions.removeIf(id -> {
            net.minecraft.world.entity.Entity minion = server.getEntity(id);
            return minion == null || !minion.isAlive() || minion.distanceToSqr(this) > 24.0D * 24.0D;
        });
        return !this.minions.isEmpty();
    }

    /** Transition de phase ou spirale de feu : il reste sur place. */
    private boolean isBusy() {
        return this.encounter.inTransition() || this.spiralTicks > 0;
    }

    /** Reste sur place sans couper l'IA (isImmobile() de Minecraft arreterait aussi le code du boss). */
    private void holdStill() {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setZza(0.0F);
    }

    // --- Pluie de meteores : cercles qui se remplissent, l'impact tombe quand ils sont pleins -----------

    private void tickMeteor(ServerLevel server, @Nullable LivingEntity target) {
        if (--this.meteorCooldown > 0 || target == null) {
            return;
        }
        this.meteorCooldown = 170 + this.random.nextInt(60);
        this.triggerAnim("attack", "meteor");
        this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.5F, 0.6F);
        this.encounter.hazards().circle(server, target.position(), 2.8D, 34, 8.0F, BossFx.FIRE);
        int extra = 3 + this.encounter.phase();
        for (int i = 0; i < extra; i++) {
            Vec3 around = target.position().add(this.random.nextGaussian() * 5.0D, 0.0D, this.random.nextGaussian() * 5.0D);
            this.encounter.hazards().circle(server, around, 2.8D, 40 + i * 7, 8.0F, BossFx.FIRE);
        }
    }

    // --- Mur de flammes : des rangees avancent depuis le Sorcier, une breche permet de les traverser ----

    private void tickFlameWall(ServerLevel server, @Nullable LivingEntity target) {
        if (this.encounter.phase() < 1 || --this.wallCooldown > 0 || target == null) {
            return;
        }
        this.wallCooldown = 260 + this.random.nextInt(80);
        this.triggerAnim("attack", "cast");
        this.playSound(SoundEvents.BLAZE_AMBIENT, 2.0F, 0.5F);
        Vec3 forward = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 across = new Vec3(-forward.z, 0.0D, forward.x);
        double gap = this.random.nextDouble() * 14.0D - 7.0D;
        double halfGap = 2.5D;
        double span = 15.0D;
        for (int row = 0; row < 7; row++) {
            Vec3 center = this.position().add(forward.scale(3.0D + row * 3.5D));
            int delay = 26 + row * 11;
            Vec3 left = center.add(across.scale(-span));
            this.encounter.hazards().line(server, left, across, span + gap - halfGap, 1.3D, delay, 7.0F, BossFx.FIRE);
            Vec3 right = center.add(across.scale(gap + halfGap));
            this.encounter.hazards().line(server, right, across, span - gap - halfGap, 1.3D, delay, 7.0F, BossFx.FIRE);
        }
    }

    // --- Spirale de feu : deux bras de boules de feu tournent autour du Sorcier immobile (vulnerable) ---

    private void tickSpiral(ServerLevel server, @Nullable LivingEntity target) {
        if (this.spiralTicks > 0) {
            this.spiralTicks--;
            BossFx.runes(server, BossFx.onGround(server, this.position()), 2.0D, 3, this.tickCount);
            if (this.spiralTicks % 3 == 0) {
                double angle = this.spiralTicks * 0.22D;
                for (int arm = 0; arm < 2; arm++) {
                    double a = angle + arm * Math.PI;
                    SmallFireball fireball = new SmallFireball(server, this, Math.cos(a), 0.0D, Math.sin(a));
                    fireball.setPos(this.getX() + Math.cos(a), this.getY(0.5D), this.getZ() + Math.sin(a));
                    server.addFreshEntity(fireball);
                }
                this.playSound(SoundEvents.BLAZE_SHOOT, 0.6F, 1.2F);
            }
            return;
        }
        if (this.encounter.phase() >= 2 && --this.spiralCooldown <= 0 && target != null) {
            this.spiralCooldown = 260 + this.random.nextInt(80);
            this.spiralTicks = 72;
            this.triggerAnim("attack", "summon");
            this.getNavigation().stop();
            this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.8F, 0.6F);
            BossFx.telegraph(server, BossFx.onGround(server, this.position()), 4.0D, 16, BossFx.FIRE);
        }
    }

    private void onPhase(int phase) {
        if (this.level() instanceof ServerLevel server) {
            this.summonMinions(server, 1 + phase);
        }
    }

    private void summonMinions(ServerLevel server, int count) {
        this.summonWaves++;
        this.hasSummoned = true;
        this.triggerAnim("attack", "summon");
        for (int i = 0; i < count; i++) {
            EmberStalker minion = ModEntities.EMBER_STALKER.get().create(server);
            if (minion != null) {
                double angle = i * Math.PI * 2.0D / count;
                minion.moveTo(this.getX() + Math.cos(angle) * 3.0D, this.getY(), this.getZ() + Math.sin(angle) * 3.0D, this.getYRot(), 0.0F);
                minion.setTarget(this.getTarget());
                server.addFreshEntity(minion);
                this.minions.add(minion.getUUID());
                server.sendParticles(ParticleTypes.FLAME, minion.getX(), minion.getY(0.5D), minion.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.05D);
            }
        }
        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.8F);
    }

    // --- Clignement : reapparait derriere une cible eloignee et explose en nova ------------------------

    private void tickBlink(@Nullable LivingEntity target) {
        if (--this.blinkCooldown > 0 || target == null || this.distanceToSqr(target) < 64.0D) {
            return;
        }
        this.blinkCooldown = 200 + this.random.nextInt(80);
        Vec3 behind = target.position().subtract(target.getLookAngle().multiply(2.5D, 0.0D, 2.5D));
        this.triggerAnim("attack", "blink");
        if (this.randomTeleport(behind.x, target.getY(), behind.z, true)) {
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.2F, 0.5F);
            this.novaCooldown = 10;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.encounter.inTransition() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.6F, 1.4F);
            return false;
        }
        if (this.shielded() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.3F;
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(1.0D), this.getZ(), 20, 0.8D, 1.0D, 0.8D, 0.02D);
            }
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive() && source.getEntity() != null && this.teleportCooldown <= 0
                && this.spiralTicks <= 0 && this.random.nextFloat() < 0.2F) {
            this.teleportCooldown = 80;
            this.teleportAway();
        }
        return hurt;
    }

    /** Nova de flammes : annoncee au sol, elle repousse et enflamme ceux qui restent colles au Sorcier. */
    private void flameNova() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.triggerAnim("attack", "summon");
        this.encounter.hazards().circle(server, this.position(), 5.5D, 16, 7.0F, BossFx.FIRE);
        this.playSound(SoundEvents.BLAZE_AMBIENT, 2.0F, 0.5F);
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
        this.arena.tick(this);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.encounter.tick(server);
        if (this.isBusy()) {
            this.holdStill();
        }
        if (this.encounter.inTransition()) {
            return;
        }
        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }
        LivingEntity target = this.getTarget();
        this.tickSpiral(server, target);
        if (this.spiralTicks > 0) {
            return;
        }
        if (--this.novaCooldown <= 0 && target != null && this.distanceToSqr(target) < 20.0D) {
            this.flameNova();
            this.novaCooldown = 90 + this.random.nextInt(40);
        }
        this.tickMeteor(server, target);
        this.tickFlameWall(server, target);
        this.tickBlink(target);
    }

    @Override
    public com.netheryx.entity.boss.BossEncounter encounter() {
        return this.encounter;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel server) {
            GuardianSeals.breakSealsAround(server, this.blockPosition());
            this.encounter.deathBurst(server);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.arena.save(tag);
        this.encounter.save(tag);
        tag.putBoolean(TAG_SUMMONED, this.hasSummoned);
        tag.putInt("SummonWaves", this.summonWaves);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.arena.load(tag);
        this.encounter.load(tag);
        this.hasSummoned = tag.getBoolean(TAG_SUMMONED);
        this.summonWaves = tag.contains("SummonWaves") ? tag.getInt("SummonWaves") : (this.hasSummoned ? 1 : 0);
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
        MobParticles.ambient(this, ModParticles.EMBER_SPARK.get(), 2, 0.8D, 0.03D);
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
                .triggerableAnim("summon", SUMMON)
                .triggerableAnim("meteor", METEOR)
                .triggerableAnim("blink", BLINK));
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
