package com.netheryx.entity;

import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Le Coeur du Nether : boss final, invoque avec le Coeur scelle (fabrique avec les trophees des trois boss de donjon).
 * Il flotte au-dessus de l'arene et enchaine :
 * <ul>
 *     <li>salves de boules de feu ;</li>
 *     <li>pulsation : onde de choc enflammee au contact ;</li>
 *     <li>pluie de meteores annoncee par un cercle de flammes ;</li>
 *     <li>a 66 % et 33 % : une garde (traqueurs, chevaliers, spectres) et un bouclier tant qu'elle vit ;</li>
 *     <li>sous 33 % : geysers de lave annonces par des cercles d'etincelles.</li>
 * </ul>
 */
public class NetherHeart extends Monster implements GeoEntity {
    private static final String NAME = "nether_heart";
    private static final EntityDataAccessor<Integer> AWAKEN = SynchedEntityData.defineId(NetherHeart.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation PULSE = NetheryXAnimations.once(NAME, "pulse");
    private static final RawAnimation CAST = NetheryXAnimations.once(NAME, "cast");
    private static final RawAnimation AWAKEN_ANIM = NetheryXAnimations.once(NAME, "awaken");
    public static final int AWAKEN_TICKS = 120;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Le boss reste cloisonne dans son arene (rayon 28 blocs autour de son point d'apparition). */
    private final com.netheryx.util.BossArena arena = new com.netheryx.util.BossArena(28.0D, 26.0D, 18.0D).noRegeneration();
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_12).setDarkenScreen(true).setCreateWorldFog(true);
    private final List<UUID> guards = new ArrayList<>();
    private int volleyCooldown = 40;
    private int pulseCooldown = 80;
    private int meteorCooldown = 120;
    private int meteorTicks;
    private Vec3 meteorTarget;
    private int geyserCooldown = 100;
    private int geyserTicks;
    private final List<Vec3> geysers = new ArrayList<>();
    private int guardWaves;

    public NetherHeart(EntityType<? extends NetherHeart> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AWAKEN, 0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Appele par le Coeur scelle : le boss se reveille lentement, invulnerable. */
    public void startAwakening() {
        this.entityData.set(AWAKEN, AWAKEN_TICKS);
        this.triggerAnim("attack", "awaken");
    }

    public int getAwakenTicks() {
        return this.entityData.get(AWAKEN);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.getAwakenTicks() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.shielded() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.1F;
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ModParticles.ONYX_GLINT.get(), this.getX(), this.getY(0.5D), this.getZ(), 20, 1.6D, 1.6D, 1.6D, 0.0D);
            }
            this.playSound(SoundEvents.AMETHYST_BLOCK_HIT, 1.5F, 0.5F);
        }
        return super.hurt(source, amount);
    }

    private boolean shielded() {
        if (!(this.level() instanceof ServerLevel server)) {
            return false;
        }
        this.guards.removeIf(id -> {
            Entity guard = server.getEntity(id);
            return guard == null || !guard.isAlive();
        });
        return !this.guards.isEmpty();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.arena.tick(this);
        float ratio = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(ratio);
        int awaken = this.getAwakenTicks();
        if (awaken > 0) {
            this.entityData.set(AWAKEN, awaken - 1);
            this.setDeltaMovement(0.0D, 0.02D, 0.0D);
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5D), this.getZ(), 10, 4.0D, 3.0D, 4.0D, -0.2D);
                if (awaken == 1) {
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(0.5D), this.getZ(), 1, 0, 0, 0, 0);
                    this.playSound(SoundEvents.WITHER_SPAWN, 3.0F, 0.6F);
                    this.knockbackAround(10.0D, 1.8D, 0.0F);
                }
            }
            return;
        }
        LivingEntity target = this.getTarget();
        this.hover(target);
        int expectedWaves = ratio < 0.33F ? 2 : ratio < 0.66F ? 1 : 0;
        if (this.guardWaves < expectedWaves) {
            this.guardWaves++;
            this.summonGuard();
        }
        if (target == null) {
            return;
        }
        if (--this.volleyCooldown <= 0) {
            this.volleyCooldown = ratio < 0.33F ? 25 : 40;
            this.volley(target);
        }
        if (--this.pulseCooldown <= 0 && this.distanceToSqr(target) < 64.0D) {
            this.pulseCooldown = 100;
            this.triggerAnim("attack", "pulse");
            this.knockbackAround(8.0D, 1.4D, 8.0F);
        }
        this.tickMeteors(target);
        if (ratio < 0.33F) {
            this.tickGeysers();
        }
    }

    /** Flotte a quelques blocs au-dessus de sa cible en tournant lentement autour. */
    private void hover(@Nullable LivingEntity target) {
        Vec3 anchor = target != null ? target.position() : this.position();
        double angle = this.tickCount * 0.02D;
        Vec3 wanted = anchor.add(Math.cos(angle) * 7.0D, 6.0D, Math.sin(angle) * 7.0D);
        Vec3 delta = wanted.subtract(this.position());
        if (delta.lengthSqr() > 0.25D) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D).add(delta.normalize().scale(0.06D)));
        }
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private void volley(LivingEntity target) {
        this.triggerAnim("attack", "cast");
        for (int i = 0; i < 3; i++) {
            Vec3 to = target.position().add(0, target.getBbHeight() / 2, 0).subtract(this.getX(), this.getY(0.5D), this.getZ());
            SmallFireball fireball = new SmallFireball(this.level(), this, to.x + this.random.triangle(0, 2), to.y, to.z + this.random.triangle(0, 2));
            fireball.setPos(this.getX(), this.getY(0.5D), this.getZ());
            this.level().addFreshEntity(fireball);
        }
        this.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, 0.5F);
    }

    private void knockbackAround(double radius, double strength, float damage) {
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius),
                e -> e != this && e.isAlive() && !(e instanceof Mob mob && this.guards.contains(mob.getUUID())))) {
            if (victim instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            Vec3 push = victim.position().subtract(this.position()).normalize().scale(strength);
            victim.push(push.x, 0.6D, push.z);
            victim.hurtMarked = true;
            if (damage > 0) {
                victim.hurt(this.damageSources().indirectMagic(this, this), damage);
                victim.setSecondsOnFire(5);
            }
        }
        if (this.level() instanceof ServerLevel server) {
            for (int i = 0; i < 64; i++) {
                double angle = i * Math.PI * 2.0D / 64.0D;
                server.sendParticles(ParticleTypes.FLAME, this.getX() + Math.cos(angle) * 3.0D, this.getY(0.5D),
                        this.getZ() + Math.sin(angle) * 3.0D, 2, 0.0D, 0.0D, 0.0D, 0.25D);
            }
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 0.6F);
    }

    private void tickMeteors(LivingEntity target) {
        if (this.meteorTicks > 0 && this.meteorTarget != null && this.level() instanceof ServerLevel server) {
            this.meteorTicks--;
            for (int i = 0; i < 20; i++) {
                double angle = i * Math.PI / 10.0D;
                server.sendParticles(ParticleTypes.FLAME, this.meteorTarget.x + Math.cos(angle) * 3.5D, this.meteorTarget.y + 0.1D,
                        this.meteorTarget.z + Math.sin(angle) * 3.5D, 1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
            if (this.meteorTicks % 4 == 0 && this.meteorTicks <= 28) {
                SmallFireball meteor = new SmallFireball(this.level(), this, 0.0D, -1.0D, 0.0D);
                meteor.setPos(this.meteorTarget.x + (this.random.nextDouble() - 0.5D) * 6.0D, this.meteorTarget.y + 14.0D,
                        this.meteorTarget.z + (this.random.nextDouble() - 0.5D) * 6.0D);
                this.level().addFreshEntity(meteor);
            }
            return;
        }
        if (--this.meteorCooldown <= 0) {
            this.meteorCooldown = 180;
            this.meteorTarget = target.position();
            this.meteorTicks = 50;
            this.triggerAnim("attack", "cast");
            this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 2.0F, 0.4F);
        }
    }

    /** Geysers de lave (derniere phase) : cercles d'etincelles puis colonne de flammes qui projette en l'air. */
    private void tickGeysers() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (this.geyserTicks > 0) {
            this.geyserTicks--;
            for (Vec3 point : this.geysers) {
                server.sendParticles(ModParticles.EMBER_SPARK.get(), point.x, point.y + 0.1D, point.z, 4, 0.8D, 0.0D, 0.8D, 0.0D);
                if (this.geyserTicks == 0) {
                    server.sendParticles(ParticleTypes.LAVA, point.x, point.y + 0.5D, point.z, 30, 0.4D, 2.0D, 0.4D, 0.1D);
                    server.sendParticles(ParticleTypes.FLAME, point.x, point.y + 2.0D, point.z, 60, 0.3D, 2.5D, 0.3D, 0.05D);
                    for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                            new net.minecraft.world.phys.AABB(point, point).inflate(1.3D, 3.0D, 1.3D), e -> e != this)) {
                        victim.hurt(this.damageSources().indirectMagic(this, this), 10.0F);
                        victim.setSecondsOnFire(6);
                        victim.push(0.0D, 1.2D, 0.0D);
                        victim.hurtMarked = true;
                    }
                }
            }
            if (this.geyserTicks == 0) {
                this.geysers.clear();
                this.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, 0.3F);
            }
            return;
        }
        if (--this.geyserCooldown <= 0) {
            this.geyserCooldown = 140;
            for (Player player : server.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32.0D),
                    p -> !p.isCreative() && !p.isSpectator())) {
                for (int i = 0; i < 3; i++) {
                    this.geysers.add(player.position().add((this.random.nextDouble() - 0.5D) * 6.0D, 0.0D, (this.random.nextDouble() - 0.5D) * 6.0D));
                }
            }
            this.geyserTicks = 30;
        }
    }

    private void summonGuard() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.triggerAnim("attack", "pulse");
        this.playSound(SoundEvents.WITHER_AMBIENT, 2.5F, 0.5F);
        List<EntityType<? extends Mob>> types = List.of(ModEntities.EMBER_STALKER.get(), ModEntities.WITHERED_KNIGHT.get(),
                ModEntities.ASH_WRAITH.get(), ModEntities.EMBER_STALKER.get(), ModEntities.WITHERED_KNIGHT.get());
        for (int i = 0; i < types.size(); i++) {
            Mob guard = types.get(i).create(server);
            if (guard == null) {
                continue;
            }
            double angle = i * Math.PI * 2.0D / types.size();
            BlockPosHelper.placeOnGround(server, guard, this.getX() + Math.cos(angle) * 5.0D, this.getY(), this.getZ() + Math.sin(angle) * 5.0D);
            guard.setTarget(this.getTarget());
            guard.setPersistenceRequired();
            server.addFreshEntity(guard);
            this.guards.add(guard.getUUID());
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, guard.getX(), guard.getY(0.5D), guard.getZ(), 30, 0.4D, 0.8D, 0.4D, 0.05D);
        }
    }

    /** Place une creature sur le premier sol solide sous un point (l'arene est sous le boss volant). */
    static final class BlockPosHelper {
        private BlockPosHelper() {
        }

        static void placeOnGround(ServerLevel level, Mob mob, double x, double y, double z) {
            net.minecraft.core.BlockPos.MutableBlockPos pos = net.minecraft.core.BlockPos.containing(x, y, z).mutable();
            for (int i = 0; i < 24 && level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty(); i++) {
                pos.move(0, -1, 0);
            }
            mob.moveTo(x, pos.getY(), z, mob.getRandom().nextFloat() * 360.0F, 0.0F);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ModParticles.EMBER_SPARK.get(), this.getRandomX(1.2D), this.getRandomY(), this.getRandomZ(1.2D),
                        0.0D, 0.03D, 0.0D);
            }
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.DRIPPING_LAVA, this.getRandomX(0.8D), this.getY(0.2D), this.getRandomZ(0.8D), 0, 0, 0);
            }
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
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
        this.arena.save(tag);
        tag.putInt("GuardWaves", this.guardWaves);
        tag.putInt("Awaken", this.getAwakenTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.arena.load(tag);
        this.guardWaves = tag.getInt("GuardWaves");
        this.entityData.set(AWAKEN, tag.getInt("Awaken"));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_HEARTBEAT;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, this::idlePredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("pulse", PULSE)
                .triggerableAnim("cast", CAST)
                .triggerableAnim("awaken", AWAKEN_ANIM));
    }

    private PlayState idlePredicate(AnimationState<NetherHeart> state) {
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
