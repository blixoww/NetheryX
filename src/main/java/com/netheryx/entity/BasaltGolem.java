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
 * Golem de basalte : mini-boss des Failles de magma. Lent mais massif, il frappe le sol (onde de choc annoncee),
 * crache des eclats de magma sur les cibles lointaines et resiste aux projectiles. Sous la moitie de sa vie, son
 * coeur entre en fusion : plus rapide, il enflamme ceux qui le frappent au corps a corps.
 */
public class BasaltGolem extends Monster implements GeoEntity {
    private static final String NAME = "basalt_golem";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation SLAM = NetheryXAnimations.once(NAME, "slam");
    private static final RawAnimation THROW = NetheryXAnimations.once(NAME, "throw");
    private static final RawAnimation PUNCH = NetheryXAnimations.once(NAME, "punch");
    private static final java.util.UUID MOLTEN_SPEED_ID = java.util.UUID.fromString("0d9b6c3e-71b4-4f5e-8f3a-6b2c1e9a7d42");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
    private int slamCooldown = 80;
    private int slamWindup = -1;
    private int throwCooldown = 60;
    private boolean molten;

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
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        LivingEntity target = this.getTarget();
        if (!this.molten && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.becomeMolten();
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        if (this.throwCooldown > 0) {
            this.throwCooldown--;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (this.slamWindup >= 0) {
            // Annonce : poussiere de basalte autour du golem avant l'impact
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    this.getX(), this.getY() + 0.1D, this.getZ(), 8, 2.5D, 0.1D, 2.5D, 0.05D);
            if (--this.slamWindup < 0) {
                this.slam(level);
            }
            return;
        }
        if (target == null || !target.isAlive()) {
            return;
        }
        double distance = this.distanceToSqr(target);
        if (this.slamCooldown <= 0 && distance < 25.0D && this.onGround()) {
            this.slamWindup = 14;
            this.slamCooldown = (this.molten ? 80 : 120) + this.random.nextInt(40);
            this.triggerAnim("attack", "slam");
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.6F);
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

    private void slam(ServerLevel level) {
        this.playSound(SoundEvents.GENERIC_EXPLODE, 1.4F, 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3D, this.getZ(), 3, 1.5D, 0.1D, 1.5D, 0.0D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                this.getX(), this.getY() + 0.2D, this.getZ(), 60, 3.0D, 0.2D, 3.0D, 0.2D);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D, 2.0D, 5.0D),
                entity -> entity != this && !(entity instanceof BasaltGolem))) {
            double dist = victim.distanceTo(this);
            if (dist > 5.5D) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), (float) (14.0D - dist * 1.2D));
            Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.1D);
            victim.setDeltaMovement(push.x, 0.6D, push.z);
            victim.hurtMarked = true;
            if (this.molten) {
                victim.setSecondsOnFire(4);
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.molten = tag.getBoolean("Molten");
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
                .triggerableAnim("punch", PUNCH));
    }

    private PlayState movementPredicate(AnimationState<BasaltGolem> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
