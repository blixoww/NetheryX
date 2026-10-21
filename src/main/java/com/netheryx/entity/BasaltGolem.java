package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
 * Golem de basalte : mini-boss gardien de la Forge de basalte. Lent mais massif, chaque attaque est annoncee et se
 * contre :
 * <ul>
 * <li>Frappe au sol : une onde de choc part en anneau et se propage ; on l'esquive en sautant par-dessus.</li>
 * <li>Charge : il se cabre et rugit, puis fonce en ligne droite. S'il percute un mur, il reste etourdi et encaisse
 * davantage de degats : c'est le moment de frapper.</li>
 * <li>Eruptions : des cercles de magma bouillonnent sous ses cibles, puis des colonnes de feu en jaillissent.</li>
 * <li>Eclats de magma lances sur les cibles lointaines.</li>
 * </ul>
 * Il resiste aux projectiles. Sous la moitie de sa vie, son coeur entre en fusion : plus rapide, il enchaine plus
 * vite, ses eruptions sont plus nombreuses et il enflamme ceux qui le frappent au corps a corps.
 */
public class BasaltGolem extends Monster implements GeoEntity {
    private static final String NAME = "basalt_golem";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation SLAM = NetheryXAnimations.once(NAME, "slam");
    private static final RawAnimation THROW = NetheryXAnimations.once(NAME, "throw");
    private static final RawAnimation PUNCH = NetheryXAnimations.once(NAME, "punch");
    private static final RawAnimation CHARGE = NetheryXAnimations.once(NAME, "charge");
    private static final RawAnimation STUN = NetheryXAnimations.once(NAME, "stun");
    private static final RawAnimation SUMMON = NetheryXAnimations.once(NAME, "summon");
    private static final int CHARGE_WINDUP = 20;
    private static final int CHARGE_TICKS = 30;
    private static final int STUN_TICKS = 50;
    private static final int ERUPTION_DELAY = 26;
    private static final double WAVE_SPEED = 0.55D;
    private static final double WAVE_MAX = 10.0D;
    private static final java.util.UUID MOLTEN_SPEED_ID = java.util.UUID.fromString("0d9b6c3e-71b4-4f5e-8f3a-6b2c1e9a7d42");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
    private int slamCooldown = 80;
    private int slamWindup = -1;
    private int throwCooldown = 60;
    private int chargeCooldown = 140;
    private int eruptionCooldown = 120;
    /** Onde de choc en cours : rayon actuel (negatif si aucune) et victimes deja touchees par cette onde. */
    private double waveRadius = -1.0D;
    private final java.util.Set<java.util.UUID> waveHit = new java.util.HashSet<>();
    /** Charge : preparation (> 0), course (< 0, decompte jusqu'a 0), direction verrouillee. */
    private int chargeWindup;
    private int chargeTicks;
    private Vec3 chargeDir = Vec3.ZERO;
    private final java.util.Set<java.util.UUID> chargeHit = new java.util.HashSet<>();
    private int stunTicks;
    /** Eruptions annoncees : position au sol et decompte. */
    private final java.util.List<Eruption> eruptions = new java.util.ArrayList<>();
    private boolean molten;

    private static final class Eruption {
        final Vec3 pos;
        int timer;

        Eruption(Vec3 pos, int timer) {
            this.pos = pos;
            this.timer = timer;
        }
    }
    /** Le golem garde sa forge : il reste dans un rayon de 12 blocs autour de son point d'apparition. */
    private final com.netheryx.util.BossArena arena = new com.netheryx.util.BossArena(12.0D, 8.0D);

    public BasaltGolem(EntityType<? extends BasaltGolem> type, Level level) {
        super(type, level);
        this.xpReward = 40;
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.arena.tick(this);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (!this.molten && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.becomeMolten();
        }
        ServerLevel level = (ServerLevel) this.level();
        this.tickWave(level);
        this.tickEruptions(level);
        if (this.busy()) {
            this.holdStill();
        }
        if (this.stunTicks > 0) {
            this.stunTicks--;
            if (this.tickCount % 4 == 0) {
                level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + this.getBbHeight() + 0.3D, this.getZ(), 3, 0.4D, 0.1D, 0.4D, 0.1D);
            }
            return;
        }
        if (this.chargeWindup > 0 || this.chargeTicks > 0) {
            this.tickCharge(level);
            return;
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        if (this.throwCooldown > 0) {
            this.throwCooldown--;
        }
        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;
        }
        if (this.eruptionCooldown > 0) {
            this.eruptionCooldown--;
        }
        if (this.slamWindup >= 0) {
            // Annonce : poussiere de basalte autour du golem avant l'impact
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    this.getX(), this.getY() + 0.1D, this.getZ(), 8, 2.5D, 0.1D, 2.5D, 0.05D);
            if (--this.slamWindup < 0) {
                this.slam(level);
            }
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        double distance = this.distanceToSqr(target);
        if (this.slamCooldown <= 0 && distance < 25.0D && this.onGround()) {
            this.slamWindup = 14;
            this.slamCooldown = (this.molten ? 80 : 120) + this.random.nextInt(40);
            this.triggerAnim("attack", "slam");
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.6F);
        } else if (this.chargeCooldown <= 0 && distance > 36.0D && distance < 400.0D && this.onGround() && this.hasLineOfSight(target)) {
            this.chargeWindup = CHARGE_WINDUP;
            this.chargeCooldown = (this.molten ? 140 : 200) + this.random.nextInt(60);
            this.triggerAnim("attack", "charge");
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.5F);
        } else if (this.eruptionCooldown <= 0 && distance < 576.0D) {
            this.startEruptions(level, target);
        } else if (this.throwCooldown <= 0 && distance > 49.0D && distance < 576.0D && this.hasLineOfSight(target)) {
            this.throwCooldown = (this.molten ? 50 : 80) + this.random.nextInt(30);
            this.triggerAnim("attack", "throw");
            this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.6F);
            Vec3 eye = this.getEyePosition();
            for (int i = 0; i < (this.molten ? 5 : 3); i++) {
                double dx = target.getX() - eye.x + this.random.nextGaussian() * 1.5D;
                double dy = target.getY(0.5D) - eye.y;
                double dz = target.getZ() - eye.z + this.random.nextGaussian() * 1.5D;
                SmallFireball chunk = new SmallFireball(level, this, dx, dy, dz);
                chunk.setPos(eye.x, eye.y, eye.z);
                level.addFreshEntity(chunk);
            }
        }
    }

    /** Pendant une attaque speciale ou l'etourdissement, il ne marche plus vers sa cible (la charge gere son propre elan). */
    private boolean busy() {
        return this.stunTicks > 0 || this.chargeWindup > 0 || this.chargeTicks > 0 || this.slamWindup >= 0;
    }

    private void holdStill() {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
    }

    // --- Frappe au sol : impact puis onde de choc ----------------------------------------------------

    private void slam(ServerLevel level) {
        this.playSound(SoundEvents.GENERIC_EXPLODE, 1.4F, 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3D, this.getZ(), 3, 1.5D, 0.1D, 1.5D, 0.0D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                this.getX(), this.getY() + 0.2D, this.getZ(), 60, 3.0D, 0.2D, 3.0D, 0.2D);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D, 2.0D, 2.5D),
                entity -> entity != this && !(entity instanceof BasaltGolem))) {
            if (victim.distanceTo(this) > 3.0D) {
                continue;
            }
            this.shockHit(victim, 12.0F, 1.1D);
        }
        // L'onde de choc se propage ensuite en anneau : sauter par-dessus permet de l'eviter
        this.waveRadius = 2.0D;
        this.waveHit.clear();
    }

    private void tickWave(ServerLevel level) {
        if (this.waveRadius < 0.0D) {
            return;
        }
        double inner = this.waveRadius;
        this.waveRadius += WAVE_SPEED;
        double outer = this.waveRadius;
        int points = (int) (outer * 7.0D);
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points + this.random.nextDouble() * 0.1D;
            double x = this.getX() + Math.cos(angle) * outer;
            double z = this.getZ() + Math.sin(angle) * outer;
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    x, this.getY() + 0.1D, z, 2, 0.1D, 0.05D, 0.1D, 0.15D);
            if (this.molten && i % 3 == 0) {
                level.sendParticles(ParticleTypes.FLAME, x, this.getY() + 0.2D, z, 1, 0.05D, 0.1D, 0.05D, 0.02D);
            }
        }
        if (this.tickCount % 3 == 0) {
            this.playSound(SoundEvents.BASALT_BREAK, 1.0F, 0.6F);
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(outer + 1.0D, 1.5D, outer + 1.0D),
                entity -> entity != this && !(entity instanceof BasaltGolem) && !this.waveHit.contains(entity.getUUID()))) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            // Seules les creatures au sol sont touchees : en l'air, l'onde passe sous vos pieds
            if (dist >= inner - 0.6D && dist <= outer + 0.6D && victim.onGround() && Math.abs(victim.getY() - this.getY()) < 1.5D) {
                this.waveHit.add(victim.getUUID());
                this.shockHit(victim, 8.0F, 0.7D);
            }
        }
        if (this.waveRadius > WAVE_MAX) {
            this.waveRadius = -1.0D;
        }
    }

    private void shockHit(LivingEntity victim, float damage, double push) {
        victim.hurt(this.damageSources().mobAttack(this), damage);
        Vec3 away = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(push);
        victim.setDeltaMovement(away.x, 0.6D, away.z);
        victim.hurtMarked = true;
        if (this.molten) {
            victim.setSecondsOnFire(4);
        }
    }

    // --- Charge -------------------------------------------------------------------------------------

    private void tickCharge(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (this.chargeWindup > 0) {
            // Il se cabre en fixant sa cible, la direction est verrouillee a la fin de la preparation
            if (target != null) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                Vec3 to = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                if (to.lengthSqr() > 0.01D) {
                    this.chargeDir = to.normalize();
                }
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    this.getX(), this.getY() + 0.1D, this.getZ(), 4, 0.8D, 0.05D, 0.8D, 0.1D);
            if (--this.chargeWindup == 0) {
                this.chargeTicks = CHARGE_TICKS;
                this.chargeHit.clear();
                this.playSound(SoundEvents.RAVAGER_STEP, 1.5F, 0.6F);
            }
            return;
        }
        this.chargeTicks--;
        float yaw = (float) (Math.atan2(this.chargeDir.z, this.chargeDir.x) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        double speed = this.molten ? 0.85D : 0.7D;
        this.setDeltaMovement(this.chargeDir.x * speed, this.getDeltaMovement().y, this.chargeDir.z * speed);
        if (this.tickCount % 2 == 0) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    this.getX(), this.getY() + 0.1D, this.getZ(), 6, 0.6D, 0.05D, 0.6D, 0.2D);
            this.playSound(SoundEvents.RAVAGER_STEP, 1.0F, 0.7F);
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.6D),
                entity -> entity != this && !(entity instanceof BasaltGolem) && !this.chargeHit.contains(entity.getUUID()))) {
            this.chargeHit.add(victim.getUUID());
            victim.hurt(this.damageSources().mobAttack(this), this.molten ? 14.0F : 11.0F);
            Vec3 fling = this.chargeDir.scale(1.6D);
            victim.setDeltaMovement(fling.x, 0.7D, fling.z);
            victim.hurtMarked = true;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.2F, 0.6F);
        }
        if (this.horizontalCollision) {
            // Il percute un mur : sonne, il baisse sa garde
            this.chargeTicks = 0;
            this.stunTicks = STUN_TICKS;
            this.setDeltaMovement(this.chargeDir.scale(-0.3D).add(0.0D, 0.2D, 0.0D));
            this.triggerAnim("attack", "stun");
            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.2F);
            this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.5F);
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX() + this.chargeDir.x, this.getY() + 1.2D, this.getZ() + this.chargeDir.z,
                    2, 0.4D, 0.4D, 0.4D, 0.0D);
        } else if (this.chargeTicks <= 0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2D, 1.0D, 0.2D));
        }
    }

    // --- Eruptions ----------------------------------------------------------------------------------

    private void startEruptions(ServerLevel level, LivingEntity target) {
        this.eruptionCooldown = (this.molten ? 110 : 170) + this.random.nextInt(40);
        this.triggerAnim("attack", "summon");
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 0.8F);
        // Une eruption sous chaque combattant proche, les autres autour de la cible
        java.util.List<LivingEntity> victims = new java.util.ArrayList<>(level.getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(16.0D), player -> player.isAlive() && !player.isSpectator() && !player.isCreative()));
        if (!victims.contains(target)) {
            victims.add(0, target);
        }
        int count = this.molten ? 5 : 3;
        for (LivingEntity victim : victims) {
            this.eruptions.add(new Eruption(victim.position(), ERUPTION_DELAY));
            count--;
        }
        for (int i = 0; i < count; i++) {
            Vec3 around = target.position().add(this.random.nextGaussian() * 3.5D, 0.0D, this.random.nextGaussian() * 3.5D);
            this.eruptions.add(new Eruption(around, ERUPTION_DELAY + 4 + i * 5));
        }
    }

    private void tickEruptions(ServerLevel level) {
        java.util.Iterator<Eruption> it = this.eruptions.iterator();
        while (it.hasNext()) {
            Eruption eruption = it.next();
            Vec3 p = eruption.pos;
            eruption.timer--;
            if (eruption.timer > 0) {
                // Annonce : cercle de magma qui bouillonne, de plus en plus vif
                for (int i = 0; i < 6; i++) {
                    double angle = this.random.nextDouble() * Math.PI * 2.0D;
                    level.sendParticles(ParticleTypes.FLAME, p.x + Math.cos(angle) * 1.2D, p.y + 0.1D, p.z + Math.sin(angle) * 1.2D,
                            1, 0.0D, 0.02D, 0.0D, 0.0D);
                }
                if (eruption.timer % 4 == 0) {
                    level.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.1D, p.z, 2, 0.5D, 0.0D, 0.5D, 0.0D);
                }
                continue;
            }
            it.remove();
            // Colonne de feu qui jaillit du sol
            for (int i = 0; i < 14; i++) {
                level.sendParticles(ModParticles.DRAGON_FLAME.get(), p.x + this.random.nextGaussian() * 0.25D, p.y + 0.2D,
                        p.z + this.random.nextGaussian() * 0.25D, 0, this.random.nextGaussian() * 0.05D, 1.0D, this.random.nextGaussian() * 0.05D,
                        0.6D + this.random.nextDouble() * 0.5D);
            }
            level.sendParticles(ModParticles.DRAGON_SMOKE.get(), p.x, p.y + 3.0D, p.z, 0, 0.0D, 1.0D, 0.0D, 0.1D);
            level.playSound(null, p.x, p.y, p.z, SoundEvents.FIRECHARGE_USE, this.getSoundSource(), 1.2F, 0.6F);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(p.x - 1.3D, p.y - 0.5D,
                    p.z - 1.3D, p.x + 1.3D, p.y + 4.0D, p.z + 1.3D), entity -> entity != this && !(entity instanceof BasaltGolem))) {
                victim.hurt(this.damageSources().mobAttack(this), this.molten ? 10.0F : 8.0F);
                victim.setSecondsOnFire(4);
                victim.setDeltaMovement(victim.getDeltaMovement().x * 0.3D, 0.9D, victim.getDeltaMovement().z * 0.3D);
                victim.hurtMarked = true;
            }
        }
    }

    private void becomeMolten() {
        this.molten = true;
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.5F);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(MOLTEN_SPEED_ID) == null) {
            speed.addPermanentModifier(new AttributeModifier(MOLTEN_SPEED_ID, "Molten core", 0.3D, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, this.molten ? ParticleTypes.LAVA : ModParticles.ASH_FLAKE.get(), this.molten ? 4 : 10, 1.0D, 0.02D);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.busy()) {
            return false;
        }
        this.triggerAnim("attack", "punch");
        boolean hit = super.doHurtTarget(target);
        if (hit && this.molten) {
            target.setSecondsOnFire(5);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALLING_BLOCK)) {
            return false;
        }
        if (source.getDirectEntity() != source.getEntity() && source.getDirectEntity() != null) {
            amount *= 0.5F; // Projectiles : la roche absorbe la moitie
        }
        if (this.stunTicks > 0) {
            amount *= 1.5F; // Etourdi apres une charge ratee : il baisse sa garde
        }
        if (this.molten && source.getDirectEntity() instanceof LivingEntity attacker && attacker.distanceTo(this) < 4.0F) {
            attacker.setSecondsOnFire(3);
        }
        return super.hurt(source, amount);
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
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Molten", this.molten);
        this.arena.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.molten = tag.getBoolean("Molten");
        this.arena.load(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("throw", THROW)
                .triggerableAnim("punch", PUNCH)
                .triggerableAnim("charge", CHARGE)
                .triggerableAnim("stun", STUN)
                .triggerableAnim("summon", SUMMON));
    }

    private PlayState movementPredicate(AnimationState<BasaltGolem> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
