package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.entity.boss.BossEncounter;
import com.netheryx.entity.boss.BossFx;
import com.netheryx.registry.ModEntities;
import com.netheryx.util.GuardianSeals;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Champion fletri : boss des Catacombes fletries. Version geante du Chevalier fletri, un duelliste :
 * balayage spectral annonce (on passe derriere lui), ruee des ames dans une direction fixee a l'avance (on s'ecarte),
 * chaines d'ames qui attirent les fuyards.
 * Phase 2 (66 %) : ruees enchainees, ames errantes (cercles au sol), anneaux de pointes d'os et onde a sauter.
 * Phase 3 (33 %) : danse macabre, il disparait et reapparait dans le dos de sa cible, dans l'obscurite.
 * Renforts a chaque phase. Sa mort brise les sceaux de la salle au tresor.
 */
public class WitheredChampion extends WitheredKnight implements com.netheryx.entity.boss.BossEntity {
    /** Le boss reste cloisonne dans son arene (rayon 7 blocs autour de son point d\'apparition). */
    private final com.netheryx.util.BossArena arena = new com.netheryx.util.BossArena(14.0D, 5.0D);
    private final BossEncounter encounter = new BossEncounter(this, BossFx.SOUL, new float[]{0.66F, 0.33F}, this::onPhase);
    private static final String TAG_WAVES = "SummonWaves";

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_6).setDarkenScreen(true);
    private int summonWaves;
    private int dashCooldown = 100;
    private int dashTicks;
    private int sweepCooldown = 60;
    private int chainsCooldown = 140;
    private int spikesCooldown = 100;
    private int spikesTicks;
    private int sweepWindup;
    private int dashWindup;
    private int dashChain;
    private net.minecraft.world.phys.Vec3 dashDirection = net.minecraft.world.phys.Vec3.ZERO;
    private int soulsCooldown = 60;
    private int danceCooldown = 80;
    private int danceWindup;
    private net.minecraft.world.phys.Vec3 danceSpot = net.minecraft.world.phys.Vec3.ZERO;

    public WitheredChampion(EntityType<? extends WitheredChampion> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 13.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(NetheryXConfig.CHAMPION_MAX_HEALTH.get());
            this.setHealth(this.getMaxHealth());
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    /** Transition de phase ou attaque en preparation : il reste sur place, ce qui laisse le temps de reagir. */
    private boolean isBusy() {
        return this.encounter.inTransition() || this.sweepWindup > 0 || this.dashWindup > 0 || this.danceWindup > 0;
    }

    /** Reste sur place sans couper l'IA (isImmobile() de Minecraft arreterait aussi le code du boss). */
    private void holdStill() {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setZza(0.0F);
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
        if (this.danceWindup > 0) {
            this.tickDanceWindup(server);
            return;
        }
        this.tickSoulDash();
        this.tickSweep();
        this.tickChains();
        this.tickSpikes();
        this.tickSouls(server);
        this.tickDance(server);
    }

    private void onPhase(int phase) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.summonWaves++;
        this.triggerAnim("attack", "attack");
        for (int i = 0; i < 2; i++) {
            WitheredKnight knight = ModEntities.WITHERED_KNIGHT.get().create(server);
            if (knight != null) {
                knight.moveTo(this.getX() + (i == 0 ? 2.5D : -2.5D), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                knight.setTarget(this.getTarget());
                server.addFreshEntity(knight);
                server.sendParticles(ParticleTypes.SOUL, knight.getX(), knight.getY(0.5D), knight.getZ(), 30, 0.4D, 0.8D, 0.4D, 0.05D);
            }
        }
        this.playSound(SoundEvents.WITHER_AMBIENT, 1.5F, 0.7F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.encounter.inTransition() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.playSound(SoundEvents.SOUL_ESCAPE, 1.0F, 1.5F);
            return false;
        }
        return super.hurt(source, amount);
    }

    // --- Ames errantes : cercles sous chaque joueur -----------------------------------------------

    private void tickSouls(ServerLevel server) {
        if (this.encounter.phase() < 1 || --this.soulsCooldown > 0 || this.getTarget() == null) {
            return;
        }
        this.soulsCooldown = 170 + this.random.nextInt(60);
        this.triggerAnim("boss", "spikes");
        this.playSound(SoundEvents.SOUL_ESCAPE, 2.0F, 0.5F);
        for (net.minecraft.world.entity.player.Player player : server.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,
                this.getBoundingBox().inflate(24.0D), p -> !p.isCreative() && !p.isSpectator())) {
            this.encounter.hazards().circle(server, player.position(), 2.6D, 32, 8.0F, BossFx.SOUL);
            for (int i = 1; i < 3; i++) {
                net.minecraft.world.phys.Vec3 around = player.position().add(this.random.nextGaussian() * 4.0D, 0.0D, this.random.nextGaussian() * 4.0D);
                this.encounter.hazards().circle(server, around, 2.6D, 32 + i * 8, 8.0F, BossFx.SOUL);
            }
        }
    }

    // --- Danse macabre : un cercle marque le dos de la cible, puis il y surgit et balaie -------------

    private void tickDance(ServerLevel server) {
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (this.encounter.phase() < 2 || --this.danceCooldown > 0 || target == null) {
            return;
        }
        this.danceCooldown = 220 + this.random.nextInt(80);
        this.danceSpot = target.position().subtract(target.getLookAngle().multiply(2.5D, 0.0D, 2.5D));
        this.danceWindup = 18;
        this.getNavigation().stop();
        this.playSound(SoundEvents.WITHER_AMBIENT, 2.0F, 0.5F);
        BossFx.telegraph(server, BossFx.onGround(server, this.danceSpot), 4.5D, 18, BossFx.SOUL);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 80, 0), this);
    }

    private void tickDanceWindup(ServerLevel server) {
        server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.5D), this.getZ(), 6, 0.4D, 0.8D, 0.4D, 0.03D);
        if (--this.danceWindup > 0) {
            return;
        }
        if (this.randomTeleport(this.danceSpot.x, this.danceSpot.y, this.danceSpot.z, true)) {
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.2F, 0.5F);
        }
        this.encounter.hazards().circle(server, this.position(), 4.5D, 1, 9.0F, BossFx.SOUL);
        this.sweepCooldown = 30;
    }

    /** Ruee des ames : la ligne s'affiche, la direction est figee, puis le Champion fonce. On s'ecarte de la ligne. */
    private void tickSoulDash() {
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (this.dashWindup > 0) {
            if (--this.dashWindup == 0) {
                this.setDeltaMovement(this.dashDirection.x * 1.6D, 0.3D, this.dashDirection.z * 1.6D);
                this.hurtMarked = true;
                this.dashTicks = 12;
                this.playSound(SoundEvents.SOUL_ESCAPE, 2.0F, 0.6F);
            }
            return;
        }
        if (this.dashTicks > 0) {
            this.dashTicks--;
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.5D), this.getZ(), 6, 0.3D, 0.5D, 0.3D, 0.01D);
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.2D, this.getZ(), 4, 0.3D, 0.1D, 0.3D, 0.01D);
            }
            if (target != null && this.distanceToSqr(target) < 6.0D) {
                this.doHurtTarget(target);
                this.dashTicks = 0;
            }
            return;
        }
        if (target != null && --this.dashCooldown <= 0) {
            double distance = this.distanceToSqr(target);
            if (distance > 25.0D && distance < 400.0D && this.level() instanceof ServerLevel server) {
                this.dashDirection = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                this.dashWindup = 12;
                this.getNavigation().stop();
                this.encounter.hazards().line(server, this.position(), this.dashDirection, 16.0D, 1.2D, 12, 6.0F, BossFx.SOUL);
                this.dashChain = this.dashChain > 0 ? this.dashChain - 1 : this.encounter.phase();
            }
            // Ruees enchainees en phase 2 et 3
            this.dashCooldown = this.dashChain > 0 ? 24 : 110 + this.random.nextInt(60);
        }
    }

    /** Chaque coup du Champion affaiblit : lenteur, faiblesse et cecite. */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (this.isBusy()) {
            return false;
        }
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof net.minecraft.world.entity.LivingEntity living) {
            applyCurses(living, 1.0F);
        }
        return hit;
    }

    private void applyCurses(net.minecraft.world.entity.LivingEntity victim, float strength) {
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                (int) (80 * strength), 1), this);
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,
                (int) (100 * strength), 0), this);
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS,
                (int) (40 * strength), 0), this);
    }

    // --- Balayage spectral : arc devant le Champion, fletrissement ----------------------------------

    private void tickSweep() {
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (this.sweepWindup > 0) {
            if (target != null) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (--this.sweepWindup == 0) {
                this.performSweep();
            }
            return;
        }
        if (--this.sweepCooldown > 0 || target == null || this.distanceToSqr(target) > 16.0D) {
            return;
        }
        this.sweepCooldown = 70 + this.random.nextInt(40);
        this.sweepWindup = 12;
        this.getNavigation().stop();
        this.triggerAnim("boss", "sweep");
        this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 0.6F);
        if (this.level() instanceof ServerLevel server) {
            BossFx.telegraph(server, BossFx.onGround(server, this.position()), 4.5D, 12, BossFx.SOUL);
        }
    }

    private void performSweep() {
        net.minecraft.world.phys.Vec3 facing = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        for (net.minecraft.world.entity.LivingEntity victim : this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                this.getBoundingBox().inflate(4.5D, 1.0D, 4.5D), e -> e != this && e.isAlive() && !(e instanceof WitheredKnight))) {
            net.minecraft.world.phys.Vec3 toVictim = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
            if (toVictim.dot(facing) < 0.0D) {
                continue;
            }
            victim.hurt(this.damageSources().indirectMagic(this, this), 7.0F);
            victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 80, 1));
            this.applyCurses(victim, 1.0F);
            victim.push(toVictim.x * 0.8D, 0.3D, toVictim.z * 0.8D);
            victim.hurtMarked = true;
        }
        if (this.level() instanceof ServerLevel server) {
            for (int i = -6; i <= 6; i++) {
                double angle = Math.atan2(facing.z, facing.x) + i * 0.26D;
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(angle) * 2.8D, this.getY(1.0D),
                        this.getZ() + Math.sin(angle) * 2.8D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + Math.cos(angle) * 4.0D, this.getY(0.6D),
                        this.getZ() + Math.sin(angle) * 4.0D, 2, 0.1D, 0.2D, 0.1D, 0.02D);
            }
            BossFx.shake(server, this.position(), 16.0D, 0.4F, 6);
        }
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.6F);
    }

    // --- Chaines d'ames : attire une cible lointaine et la ralentit ----------------------------------

    private void tickChains() {
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (--this.chainsCooldown > 0 || target == null) {
            return;
        }
        double distance = this.distanceToSqr(target);
        if (distance < 49.0D || distance > 324.0D) {
            return;
        }
        this.chainsCooldown = 180 + this.random.nextInt(80);
        this.triggerAnim("boss", "chains");
        net.minecraft.world.phys.Vec3 pull = this.position().subtract(target.position()).normalize();
        target.setDeltaMovement(pull.x * 1.6D, 0.45D, pull.z * 1.6D);
        target.hurtMarked = true;
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        if (this.level() instanceof ServerLevel server) {
            net.minecraft.world.phys.Vec3 step = target.position().subtract(this.position()).scale(1.0D / 16.0D);
            for (int i = 0; i <= 16; i++) {
                server.sendParticles(ParticleTypes.SOUL, this.getX() + step.x * i, this.getY(0.6D) + step.y * i, this.getZ() + step.z * i,
                        2, 0.05D, 0.05D, 0.05D, 0.0D);
            }
        }
        this.playSound(SoundEvents.CHAIN_BREAK, 2.0F, 0.5F);
    }

    // --- Pointes d'os (sous 50 %) : deux anneaux de crocs successifs autour du Champion --------------

    private void tickSpikes() {
        if (this.spikesTicks > 0) {
            this.spikesTicks--;
            if (this.spikesTicks == 10 || this.spikesTicks == 0) {
                double radius = this.spikesTicks == 10 ? 3.0D : 5.5D;
                int count = this.spikesTicks == 10 ? 12 : 20;
                for (int i = 0; i < count; i++) {
                    double angle = i * Math.PI * 2.0D / count;
                    this.level().addFreshEntity(new net.minecraft.world.entity.projectile.EvokerFangs(this.level(),
                            this.getX() + Math.cos(angle) * radius, this.getY(), this.getZ() + Math.sin(angle) * radius,
                            (float) angle, 0, this));
                }
            }
            return;
        }
        if (this.encounter.phase() >= 1 && this.getTarget() != null && --this.spikesCooldown <= 0) {
            this.spikesCooldown = 170 + this.random.nextInt(60);
            this.spikesTicks = 20;
            this.triggerAnim("boss", "spikes");
            this.playSound(SoundEvents.SKELETON_HORSE_DEATH, 1.5F, 0.5F);
            if (this.level() instanceof ServerLevel server) {
                BossFx.telegraph(server, BossFx.onGround(server, this.position()), 6.0D, 20, BossFx.SOUL);
                this.encounter.hazards().wave(server, this.position(), 13.0D, 0.55D, 7.0F, BossFx.SOUL);
            }
        }
    }

    @Override
    public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(new software.bernie.geckolib.core.animation.AnimationController<>(this, "boss", 0,
                state -> software.bernie.geckolib.core.object.PlayState.STOP)
                .triggerableAnim("sweep", NetheryXAnimations.once("withered_knight", "sweep"))
                .triggerableAnim("chains", NetheryXAnimations.once("withered_knight", "chains"))
                .triggerableAnim("spikes", NetheryXAnimations.once("withered_knight", "spikes")));
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
        tag.putInt(TAG_WAVES, this.summonWaves);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.arena.load(tag);
        this.encounter.load(tag);
        this.summonWaves = tag.getInt(TAG_WAVES);
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
    public float getVoicePitch() {
        return 0.6F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.WITHER_SPORE.get(), 2, 1.0D, -0.01D);
        MobParticles.ambient(this, ModParticles.SOUL_WISP.get(), 6, 0.8D, 0.02D);
    }
}
