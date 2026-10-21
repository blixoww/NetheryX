package com.netheryx.entity;

import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
 * Wyverne de cendre : monture volante, eclose d'un oeuf laisse par le Coeur du Nether. Elle appartient au joueur
 * qui l'a fait eclore : clic droit pour la monter, accroupi + clic droit pour la faire attendre, viande pour la soigner.
 * En vol, elle suit le regard du cavalier (regarder vers le haut en avancant pour decoller, vers le bas pour descendre).
 */
public class AshWyvern extends TamableAnimal implements GeoEntity {
    private static final String NAME = "ash_wyvern";
    private static final RawAnimation IDLE = NetheryXAnimations.loop(NAME, "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop(NAME, "walk");
    private static final RawAnimation FLY = NetheryXAnimations.loop(NAME, "fly");
    private static final RawAnimation SIT = NetheryXAnimations.loop(NAME, "sit");
    private static final RawAnimation GLIDE = NetheryXAnimations.loop(NAME, "glide");
    private static final RawAnimation ROAR = NetheryXAnimations.once(NAME, "roar");
    private static final RawAnimation BREATH = NetheryXAnimations.once(NAME, "breath");
    private static final RawAnimation DIVE = NetheryXAnimations.once(NAME, "dive");
    /** En combat aerien (vole seule, sans cavalier) : synchronise pour que le client sache qu'elle ne tombe pas. */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> AERIAL =
            net.minecraft.network.syncher.SynchedEntityData.defineId(AshWyvern.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final double BREATH_RANGE = 11.0D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int breathTicks;
    private int breathCooldown = 40;

    public AshWyvern(EntityType<? extends AshWyvern> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.07D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new WyvernAerialCombatGoal(this));
        this.goalSelector.addGoal(5, new WyvernFollowOwnerGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, 10,
                true, false, target -> this.isNestGuardian() && !this.isTame()));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AERIAL, false);
    }

    // --- Compagnon : sante, registre, oeuf a la mort ---------------------------------------------------

    /** Gardien d'un nid de wyverne : agressif, impossible a apprivoiser, il laisse son oeuf quand on le bat. */
    private boolean nestGuardian;
    private final com.netheryx.util.BossArena nestArena = new com.netheryx.util.BossArena(14.0D, 20.0D, 20.0D);

    public boolean isNestGuardian() {
        return this.nestGuardian;
    }

    /** Vrai (et previent le joueur) s'il possede deja une wyverne : une seule par joueur. */
    public static boolean hasWyvern(net.minecraft.server.level.ServerPlayer player) {
        if (com.netheryx.util.WyvernRegistry.get(player.server).ownedBy(player.getUUID()).isEmpty()) {
            return false;
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.netheryx.wyvern_already")
                .withStyle(net.minecraft.ChatFormatting.RED), true);
        return true;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("NestGuardian", this.nestGuardian);
        this.nestArena.save(tag);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nestGuardian = tag.getBoolean("NestGuardian");
        this.nestArena.load(tag);
        if (this.nestGuardian && !this.isTame()) {
            var health = this.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(120.0D);
            }
            if (!tag.contains("Health")) {
                this.setHealth(this.getMaxHealth());
            }
        }
    }

    public static final double WILD_HEALTH = 80.0D;
    public static final double TAMED_HEALTH = 160.0D;

    @Override
    public void setTame(boolean tame) {
        super.setTame(tame);
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(tame ? TAMED_HEALTH : WILD_HEALTH);
        }
    }

    @Override
    public void tame(Player player) {
        super.tame(player);
        this.setHealth(this.getMaxHealth());
        if (this.level() instanceof ServerLevel server) {
            this.updateRegistry(server);
        }
    }

    private void updateRegistry(ServerLevel server) {
        java.util.UUID owner = this.getOwnerUUID();
        if (owner != null) {
            com.netheryx.util.WyvernRegistry.get(server.getServer()).update(this.getUUID(), owner, server.dimension(), this.blockPosition());
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel server && this.nestGuardian && !this.isTame()) {
            ItemStack egg = new ItemStack(com.netheryx.registry.ModItems.ASH_WYVERN_EGG.get());
            net.minecraft.world.entity.item.ItemEntity drop = this.spawnAtLocation(egg, 1.0F);
            if (drop != null) {
                drop.setUnlimitedLifetime();
                drop.setGlowingTag(true);
            }
            server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY(1.0D), this.getZ(), 60, 1.0D, 1.0D, 1.0D, 0.3D);
        }
        if (this.level() instanceof ServerLevel server && this.isTame()) {
            // Elle ne disparait jamais vraiment : son oeuf revient a son maitre (ou tombe a ses pieds), avec son nom
            ItemStack egg = new ItemStack(com.netheryx.registry.ModItems.ASH_WYVERN_EGG.get());
            if (this.hasCustomName()) {
                egg.setHoverName(this.getCustomName());
            }
            LivingEntity owner = this.getOwner();
            if (owner instanceof Player player) {
                if (!player.getInventory().add(egg)) {
                    player.drop(egg, false);
                }
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.netheryx.wyvern_fallen")
                        .withStyle(net.minecraft.ChatFormatting.GOLD), false);
            } else {
                this.spawnAtLocation(egg);
            }
            com.netheryx.util.WyvernRegistry.get(server.getServer()).remove(this.getUUID());
        }
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED && this.level() instanceof ServerLevel server) {
            com.netheryx.util.WyvernRegistry.get(server.getServer()).remove(this.getUUID());
        }
        super.remove(reason);
    }

    public boolean isAerial() {
        return this.entityData.get(AERIAL);
    }

    void setAerial(boolean aerial) {
        this.entityData.set(AERIAL, aerial);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame()) {
            if (this.isNestGuardian()) {
                return InteractionResult.PASS; // le gardien du nid ne se laisse pas apprivoiser : il faut le vaincre
            }
            // Wyverne sauvage : se laisse apprivoiser avec de la viande (une seule wyverne par joueur)
            if (this.isFood(stack)) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && hasWyvern(serverPlayer)) {
                    return InteractionResult.FAIL;
                }
                if (!this.level().isClientSide) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return InteractionResult.PASS;
        }
        if (!this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }
        if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide) {
                this.heal(10.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (player.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.navigation.stop();
                this.setTarget(null);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (!this.isVehicle()) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
                player.startRiding(this);
                this.triggerAnim("action", "roar");
                this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 0.5F, 1.4F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModTags.Items.ASH_WYVERN_FOOD);
    }

    // --- Vol monte --------------------------------------------------------------------------------

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player && this.isOwnedBy(player) ? player : null;
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        float forward = player.zza;
        if (forward <= 0.0F) {
            forward *= 0.3F;
        }
        // Monter ou descendre en suivant le regard du cavalier
        double vertical = forward > 0.0F ? -Math.sin(Math.toRadians(player.getXRot())) * forward : 0.0D;
        return new Vec3(player.xxa * 0.5F, vertical, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player && this.isControlledByLocalInstance()
                && (!this.onGround() || input.y > 0.15D)) {
            float speed = (float) this.getAttributeValue(Attributes.FLYING_SPEED);
            this.moveRelative(speed, new Vec3(input.x, 0.0D, input.z));
            Vec3 motion = this.getDeltaMovement();
            double lift = input.y * speed * 0.9D - (input.z == 0.0D ? 0.005D : 0.0D);
            this.setDeltaMovement(motion.x, motion.y * 0.9D + lift, motion.z);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.91D, 0.91D, 0.91D));
            this.calculateEntityAnimation(true);
            return;
        }
        super.travel(input);
    }

    @Override
    public void tick() {
        this.setNoGravity((this.isVehicle() && !this.onGround()) || this.isAerial());
        super.tick();
        if (this.level() instanceof ServerLevel server) {
            if (this.nestGuardian && !this.isTame()) {
                this.nestArena.tick(this);
            }
            if (this.isTame() && this.tickCount % 100 == 0) {
                this.updateRegistry(server);
            }
            // Regeneration lente hors combat
            if (this.isTame() && this.getTarget() == null && this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
            }
            if (this.breathCooldown > 0) {
                this.breathCooldown--;
            }
            LivingEntity target = this.getTarget();
            // Montee : elle crache du feu sur ce que son cavalier attaque, s'il est devant elle
            if (this.isVehicle() && target != null && target.isAlive() && this.breathCooldown <= 0 && this.breathTicks <= 0
                    && this.distanceTo(target) < BREATH_RANGE + 3.0D && this.isInFront(target)) {
                this.startBreath();
            }
            if (this.breathTicks > 0) {
                this.breathTicks--;
                this.tickBreath(server);
            }
        }
    }

    // --- Attaques ---------------------------------------------------------------------------------

    boolean canBreath() {
        return this.breathCooldown <= 0 && this.breathTicks <= 0;
    }

    void startBreath() {
        this.breathTicks = 30;
        this.breathCooldown = 70 + this.random.nextInt(30);
        this.triggerAnim("action", "breath");
        this.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.2F, 1.2F);
    }

    boolean isBreathing() {
        return this.breathTicks > 0;
    }

    private boolean isInFront(Entity entity) {
        Vec3 look = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 to = entity.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        return look.dot(to) > 0.5D;
    }

    /** Souffle de feu : un cone devant la gueule. Il embrase les creatures, jamais les blocs. */
    private void tickBreath(ServerLevel level) {
        LivingEntity target = this.getTarget();
        Vec3 mouth = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(2.6D)).add(0.0D, 1.25D, 0.0D);
        Vec3 dir = target != null ? target.getEyePosition().subtract(mouth).normalize()
                : Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        // Jet de flammes : chaque particule part de la gueule avec une vitesse propre, en cone, comme un vrai crachat
        for (int i = 0; i < 18; i++) {
            Vec3 jet = dir.add(this.random.nextGaussian() * 0.08D, this.random.nextGaussian() * 0.08D, this.random.nextGaussian() * 0.08D)
                    .normalize();
            double speed = 0.55D + this.random.nextDouble() * 0.45D;
            level.sendParticles(i % 6 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, mouth.x, mouth.y, mouth.z, 0,
                    jet.x, jet.y, jet.z, speed);
        }
        for (int i = 0; i < 4; i++) {
            Vec3 jet = dir.add(this.random.nextGaussian() * 0.15D, this.random.nextGaussian() * 0.1D, this.random.nextGaussian() * 0.15D);
            level.sendParticles(ModParticles.EMBER_SPARK.get(), mouth.x, mouth.y, mouth.z, 0, jet.x, jet.y, jet.z, 0.7D);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y, mouth.z, 0, dir.x, dir.y + 0.1D, dir.z, 0.25D);
        // Impact : gerbe de flammes la ou le souffle touche
        Vec3 impact = mouth.add(dir.scale(BREATH_RANGE * 0.85D));
        level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y, impact.z, 6, 0.6D, 0.4D, 0.6D, 0.04D);
        if (this.breathTicks % 6 == 0) {
            this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.6F + this.random.nextFloat() * 0.2F);
        }
        if (this.breathTicks % 4 != 0) {
            return;
        }
        LivingEntity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BREATH_RANGE + 2.0D))) {
            if (victim == this || victim == owner || victim.isPassengerOfSameVehicle(this) || this.hasPassenger(victim)
                    || (victim instanceof TamableAnimal pet && owner != null && pet.isOwnedBy(owner))
                    || (victim instanceof Player && victim != target) || victim.isAlliedTo(this)) {
                continue;
            }
            Vec3 to = victim.getBoundingBox().getCenter().subtract(mouth);
            double distance = to.length();
            if (distance > BREATH_RANGE || to.normalize().dot(dir) < 0.8D) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), 4.0F);
            victim.setSecondsOnFire(5);
        }
    }

    void diveHit(LivingEntity target) {
        this.triggerAnim("action", "dive");
        target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25F);
        Vec3 push = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(1.2D);
        target.setDeltaMovement(push.x, 0.5D, push.z);
        target.hurtMarked = true;
        this.playSound(SoundEvents.PHANTOM_BITE, 1.2F, 0.7F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.ASH_FLAKE.get(), this.onGround() ? 10 : 2, 1.2D, -0.02D);
        Vec3 motion = this.getDeltaMovement();
        if (!this.isVehicle() && !this.isAerial() && !this.onGround() && motion.y < 0.0D) {
            this.setDeltaMovement(motion.multiply(1.0D, 0.6D, 1.0D)); // plane
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.onGround() && passenger instanceof LivingEntity living && !this.level().isClientSide) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0, false, false, true));
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.45D; // selle posee sur le dos du grand modele
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            for (int i = 0; i < 7; i++) {
                this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.02D, 0.0D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal other) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("roar", ROAR).triggerableAnim("breath", BREATH).triggerableAnim("dive", DIVE));
    }

    private PlayState movementPredicate(AnimationState<AshWyvern> state) {
        if (this.isInSittingPose()) {
            return state.setAndContinue(SIT);
        }
        if (!this.onGround()) {
            // Plane en descente, bat des ailes pour monter ou se maintenir
            Vec3 motion = this.getDeltaMovement();
            boolean gliding = motion.y < -0.04D && motion.horizontalDistanceSqr() > 0.01D;
            return state.setAndContinue(gliding ? GLIDE : FLY);
        }
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
