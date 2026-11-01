package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.entity.boss.BossEncounter;
import com.netheryx.entity.boss.BossFx;
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
 * Phase 1 : coups, frappe annoncee au sol, bond ecrasant sur les cibles eloignees, pluie de cristaux.
 * Phase 2 (66 %) : rage, ondes de choc a sauter, lignes de crocs de cristal, faisceau d'onyx (les piliers de l'arene
 * l'arretent) et carapace (degats reduits de 75 %, mais six coups la brisent et etourdissent la Sentinelle).
 * Phase 3 (33 %) : appelle des cubes d'onyx, pluie plus dense.
 */
public class OnyxSentinel extends Monster implements com.netheryx.entity.boss.BossEntity, GeoEntity {
    private static final String NAME = "onyx_sentinel";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation PUNCH = NetheryXAnimations.once(NAME, "punch");
    private static final RawAnimation SLAM = NetheryXAnimations.once(NAME, "slam");
    private static final RawAnimation LEAP = NetheryXAnimations.once(NAME, "leap");
    private static final RawAnimation SHELL = NetheryXAnimations.once(NAME, "shell");
    private static final RawAnimation ROAR = NetheryXAnimations.once(NAME, "roar");

    private static final double SLAM_RADIUS = 5.0D;
    private static final float SLAM_DAMAGE = 10.0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Le boss reste cloisonne dans son arene (rayon 11 blocs autour de son point d'apparition). */
    private final com.netheryx.util.BossArena arena = new com.netheryx.util.BossArena(15.0D, 10.0D);
    private final BossEncounter encounter = new BossEncounter(this, BossFx.ONYX, new float[]{0.66F, 0.33F}, this::onPhase);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);
    private int slamCooldown = 60;
    private int eruptionCooldown = 80;
    private boolean enraged;
    private boolean lastStand;
    private int leapCooldown = 120;
    private boolean leaping;
    private int leapAirTicks;
    private int rainCooldown = 160;
    private int rainTicks;
    private final java.util.List<Vec3> rainTargets = new java.util.ArrayList<>();
    private int shellCooldown = 300;
    private int shellTicks;
    private int shellHits;
    private int stunTicks;
    private int beamCooldown = 120;
    private int beamTicks;
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
        return this.slamCooldown <= 0 && !this.isBusy();
    }

    /** Transition de phase, etourdissement ou faisceau : la Sentinelle ne marche ni ne frappe. */
    private boolean isBusy() {
        return this.encounter.inTransition() || this.stunTicks > 0 || this.beamTicks > 0;
    }

    /** Reste sur place sans couper l'IA (isImmobile() de Minecraft arreterait aussi le code du boss). */
    private void holdStill() {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setZza(0.0F);
    }

    public void resetSlamCooldown() {
        this.slamCooldown = 120 + this.random.nextInt(60);
    }

    public void startSlamAnimation() {
        this.triggerAnim("attack", "slam");
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.6F);
        if (this.level() instanceof ServerLevel server) {
            BossFx.telegraph(server, BossFx.onGround(server, this.position()), SLAM_RADIUS + 1.0D, 14, BossFx.ONYX);
        }
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
        BossFx.shake(server, this.position(), 28.0D, 0.7F, 12);
        BossFx.shockwave(server, BossFx.onGround(server, this.position()), SLAM_RADIUS + 1.0D, 8, BossFx.ONYX);
        if (this.encounter.phase() >= 1) {
            // En rage, la frappe se prolonge en onde : il faut sauter par-dessus
            this.encounter.hazards().wave(server, this.position(), 14.0D, 0.6D, 7.0F, BossFx.ONYX);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.isBusy()) {
            return false;
        }
        this.triggerAnim("attack", "punch");
        return super.doHurtTarget(target);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.arena.tick(this);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.encounter.tick(server);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isBusy()) {
            this.holdStill();
        }
        if (this.encounter.inTransition()) {
            return;
        }
        if (this.stunTicks > 0) {
            this.stunTicks--;
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + this.getBbHeight() + 0.2D, this.getZ(), 2, 0.5D, 0.1D, 0.5D, 0.1D);
            return;
        }
        if (this.beamTicks > 0) {
            this.beamTicks--;
            BossFx.runes(server, BossFx.onGround(server, this.position()), 2.2D, 3, this.tickCount);
            return;
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        if (this.enraged && this.getTarget() != null && --this.eruptionCooldown <= 0) {
            this.crystalEruption(this.getTarget());
            this.eruptionCooldown = 110 + this.random.nextInt(50);
        }
        this.tickLeap();
        this.tickCrystalRain();
        this.tickShell();
        this.tickBeam(server);
    }

    private void onPhase(int phase) {
        if (phase == 1) {
            this.enterRage();
        } else if (phase == 2) {
            this.enterLastStand();
        }
    }

    // --- Faisceau d'onyx : balaie l'arene ; on tourne avec lui ou on s'abrite derriere un pilier --------

    private void tickBeam(ServerLevel server) {
        LivingEntity target = this.getTarget();
        if (this.encounter.phase() < 1 || --this.beamCooldown > 0 || target == null || this.shellTicks > 0) {
            return;
        }
        this.beamCooldown = 320 + this.random.nextInt(100);
        int windup = 35;
        int duration = 110;
        this.beamTicks = windup + duration;
        this.getNavigation().stop();
        this.triggerAnim("attack", "roar");
        this.playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 0.6F);
        Vec3 toward = target.position().subtract(this.position());
        double side = this.random.nextBoolean() ? 1.0D : -1.0D;
        double start = Math.atan2(toward.z, toward.x) - side * Math.toRadians(35.0D);
        this.encounter.hazards().beam(server, this.position().add(0.0D, 1.0D, 0.0D), start, side * 2.6D, windup, duration, 22.0D, 6.0F, BossFx.ONYX);
    }

    // --- Bond ecrasant : saute sur une cible eloignee et provoque une onde de choc a la reception -----

    private void tickLeap() {
        LivingEntity target = this.getTarget();
        if (this.leaping) {
            this.leapAirTicks++;
            if (this.onGround() && this.leapAirTicks > 6) {
                this.leaping = false;
                this.performSlam();
            }
            return;
        }
        if (--this.leapCooldown > 0 || target == null || !this.onGround()) {
            return;
        }
        double distance = this.distanceToSqr(target);
        if (distance > 49.0D && distance < 400.0D) {
            this.triggerAnim("attack", "leap");
            if (this.level() instanceof ServerLevel server) {
                BossFx.telegraph(server, BossFx.onGround(server, target.position()), SLAM_RADIUS + 1.0D, 22, BossFx.ONYX);
            }
            Vec3 toward = target.position().subtract(this.position());
            this.setDeltaMovement(toward.x * 0.16D, 1.1D, toward.z * 0.16D);
            this.hurtMarked = true;
            this.leaping = true;
            this.leapAirTicks = 0;
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.5F);
        }
        this.leapCooldown = (this.enraged ? 100 : 160) + this.random.nextInt(60);
    }

    // --- Pluie de cristaux : cercles au sol sous chaque joueur, puis impact ------------------------------

    private void tickCrystalRain() {
        if (--this.rainCooldown > 0 || this.getTarget() == null || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        int perPlayer = 2 + this.encounter.phase();
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24.0D),
                player -> !player.isCreative() && !player.isSpectator())) {
            this.encounter.hazards().circle(server, player.position(), 2.6D, 38, 8.0F, BossFx.ONYX);
            for (int i = 1; i < perPlayer; i++) {
                Vec3 around = player.position().add(this.random.nextGaussian() * 4.5D, 0.0D, this.random.nextGaussian() * 4.5D);
                this.encounter.hazards().circle(server, around, 2.6D, 38 + i * 9, 8.0F, BossFx.ONYX);
            }
        }
        this.triggerAnim("attack", "slam");
        this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0F, 0.5F);
        this.rainCooldown = 190 + this.random.nextInt(80);
    }

    // --- Carapace d'onyx : reduit fortement les degats pendant quelques secondes -----------------------

    private void tickShell() {
        if (this.shellTicks > 0) {
            this.shellTicks--;
            if (this.level() instanceof ServerLevel server && this.tickCount % 4 == 0) {
                server.sendParticles(ModParticles.ONYX_GLINT.get(), this.getX(), this.getY(1.0D), this.getZ(), 6, 1.2D, 1.6D, 1.2D, 0.0D);
            }
            return;
        }
        if (this.enraged && --this.shellCooldown <= 0) {
            this.shellTicks = 100;
            this.shellHits = 0;
            this.shellCooldown = 420 + this.random.nextInt(120);
            this.triggerAnim("attack", "shell");
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.4F);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean bypass = source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (this.encounter.inTransition() && !bypass) {
            this.playSound(SoundEvents.ANVIL_LAND, 0.5F, 1.8F);
            return false;
        }
        if (this.stunTicks > 0) {
            amount *= 1.4F; // Carapace brisee : la Sentinelle baisse sa garde
        }
        if (this.shellTicks > 0 && !bypass) {
            amount *= 0.25F;
            this.playSound(SoundEvents.AMETHYST_BLOCK_HIT, 1.5F, 0.6F);
            if (source.getEntity() instanceof Player && ++this.shellHits >= 6) {
                this.shatterShell();
            }
        }
        return super.hurt(source, amount);
    }

    /** Six coups sur la carapace la brisent : la Sentinelle reste etourdie et encaisse davantage. */
    private void shatterShell() {
        this.shellTicks = 0;
        this.stunTicks = 70;
        this.getNavigation().stop();
        this.playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 2.0F, 0.5F);
        this.playSound(SoundEvents.ANVIL_BREAK, 1.0F, 0.6F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ModParticles.ONYX_GLINT.get(), this.getX(), this.getY(1.0D), this.getZ(), 80, 1.4D, 1.6D, 1.4D, 0.1D);
            BossFx.shockwave(server, BossFx.onGround(server, this.position()), 5.0D, 10, BossFx.ONYX);
            BossFx.shake(server, this.position(), 24.0D, 0.6F, 10);
        }
    }

    /** Derniere phase : appelle des cubes d'onyx et reste en rage. */
    private void enterLastStand() {
        this.lastStand = true;
        this.triggerAnim("attack", "roar");
        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.5F, 0.6F);
        if (this.level() instanceof ServerLevel server) {
            for (int i = 0; i < 3; i++) {
                OnyxCube cube = com.netheryx.registry.ModEntities.ONYX_CUBE.get().create(server);
                if (cube != null) {
                    cube.moveTo(this.getX() + (i - 1) * 2.5D, this.getY() + 0.5D, this.getZ() + 2.0D, this.getYRot(), 0.0F);
                    cube.setTarget(this.getTarget());
                    server.addFreshEntity(cube);
                }
            }
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
        this.triggerAnim("attack", "roar");
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(1.0D), this.getZ(), 120, 1.0D, 1.5D, 1.0D, 0.2D);
        }
    }

    /** Une ligne de crocs de cristal (crocs d'evocateur) part de la Sentinelle vers sa cible. */
    private void crystalEruption(LivingEntity target) {
        this.triggerAnim("attack", "slam");
        Vec3 direction = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        if (this.level() instanceof ServerLevel server) {
            for (int i = 1; i <= 12; i += 2) {
                Vec3 mark = this.position().add(direction.scale(1.25D * i));
                server.sendParticles(ModParticles.ONYX_GLINT.get(), mark.x, this.getY() + 0.15D, mark.z, 6, 0.3D, 0.0D, 0.3D, 0.0D);
            }
        }
        float yaw = (float) Math.atan2(direction.z, direction.x);
        for (int i = 1; i <= 12; i++) {
            Vec3 point = this.position().add(direction.scale(1.25D * i));
            this.level().addFreshEntity(new net.minecraft.world.entity.projectile.EvokerFangs(this.level(),
                    point.x, Math.floor(this.getY()), point.z, yaw, i * 2, this));
        }
        this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.6F);
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
        this.arena.save(tag);
        this.encounter.save(tag);
        tag.putBoolean(TAG_ENRAGED, this.enraged);
        tag.putBoolean("LastStand", this.lastStand);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.arena.load(tag);
        this.encounter.load(tag);
        this.enraged = tag.getBoolean(TAG_ENRAGED);
        this.lastStand = tag.getBoolean("LastStand");
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
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("leap", LEAP)
                .triggerableAnim("shell", SHELL)
                .triggerableAnim("roar", ROAR));
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
