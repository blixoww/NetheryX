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
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
    private static final RawAnimation FIREBALL = NetheryXAnimations.once(NAME, "fireball");
    private static final RawAnimation GUST = NetheryXAnimations.once(NAME, "gust");
    private static final RawAnimation TAIL_SWEEP = NetheryXAnimations.once(NAME, "tail_sweep");
    private static final int FIREBALL_CHARGE = 16;
    private static final int GUST_TICKS = 24;
    private static final double GUST_RANGE = 9.0D;
    private static final double SWEEP_RANGE = 4.5D;
    private static final RawAnimation DIVE = NetheryXAnimations.once(NAME, "dive");
    /** En combat aerien (vole seule, sans cavalier) : synchronise pour que le client sache qu'elle ne tombe pas. */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> AERIAL =
            net.minecraft.network.syncher.SynchedEntityData.defineId(AshWyvern.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final double BREATH_RANGE = 11.0D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int breathTicks;
    private int breathCooldown = 40;
    private int fireballCharge;
    private int fireballCooldown = 100;
    private int gustTicks;
    private int gustCooldown = 80;
    private int sweepTicks;
    private int sweepCooldown = 40;

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
    /** Vrai pendant qu'elle est rangee dans un cristal : son inscription au registre est conservee. */
    private boolean storing;
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
        if (reason == RemovalReason.DISCARDED && !this.storing && this.level() instanceof ServerLevel server) {
            com.netheryx.util.WyvernRegistry.get(server.getServer()).remove(this.getUUID());
        }
        super.remove(reason);
    }

    /**
     * Range la wyverne dans un cristal : renvoie ses donnees completes (nom, vie, maitre, identifiant) et la retire du
     * monde, en gardant son inscription au registre (elle reste la wyverne de son maitre).
     */
    public net.minecraft.nbt.CompoundTag store() {
        this.ejectPassengers();
        this.setAerial(false);
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        this.saveWithoutId(data);
        data.putString("id", EntityType.getKey(this.getType()).toString());
        if (this.level() instanceof ServerLevel server) {
            com.netheryx.util.WyvernRegistry.get(server.getServer()).markStored(this.getUUID());
        }
        this.storing = true;
        this.discard();
        return data;
    }

    /** Liberee d'un cristal : elle reprend sa place au registre a sa nouvelle position. */
    public void onReleased() {
        if (this.level() instanceof ServerLevel server) {
            this.updateRegistry(server);
        }
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
        if (stack.getItem() instanceof com.netheryx.item.WyvernCrystalItem) {
            return InteractionResult.PASS; // le cristal gere lui-meme l'interaction (ranger la wyverne)
        }
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
            // Grande et lourde, elle peut se retrouver coincee dans un bloc : elle s'en degage vers le haut
            if (this.tickCount % 10 == 0 && this.isInWall()) {
                this.setPos(this.getX(), Math.floor(this.getY()) + 1.0D, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
            }
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
            if (this.fireballCooldown > 0) {
                this.fireballCooldown--;
            }
            if (this.gustCooldown > 0) {
                this.gustCooldown--;
            }
            if (this.sweepCooldown > 0) {
                this.sweepCooldown--;
            }
            LivingEntity target = this.getTarget();
            // Montee : elle crache du feu sur ce que son cavalier attaque, s'il est devant elle
            if (this.isVehicle() && target != null && target.isAlive() && this.breathCooldown <= 0 && this.breathTicks <= 0
                    && this.distanceTo(target) < BREATH_RANGE + 3.0D && this.isInFront(target)) {
                this.startBreath();
            } else if (this.isVehicle() && target != null && target.isAlive() && this.canFireball()
                    && this.distanceTo(target) >= BREATH_RANGE + 3.0D && this.distanceTo(target) < 40.0D && this.isInFront(target)) {
                // Cible trop lointaine pour le souffle : grande boule de feu
                this.startFireball();
            } else if (this.isVehicle() && target != null && target.isAlive() && this.canTailSweep() && this.distanceTo(target) < 5.0D
                    && !this.isInFront(target)) {
                // Cible sur le cote ou derriere : coup de queue
                this.startTailSweep();
            }
            if (this.fireballCharge > 0) {
                this.tickFireball(server);
            }
            if (this.gustTicks > 0) {
                this.tickGust(server);
            }
            if (this.sweepTicks > 0) {
                this.tickSweep(server);
            }
            if (this.breathTicks > 0) {
                this.breathTicks--;
                this.tickBreath(server);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Jamais blessee par un mur ou un sol brulant : elle s'en degage seule (voir tick)
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.HOT_FLOOR)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    // --- Attaques ---------------------------------------------------------------------------------

    boolean canBreath() {
        return this.breathCooldown <= 0 && !this.isBusyAttacking();
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
        Vec3 mouth = this.mouthPosition();
        Vec3 dir = target != null ? target.getEyePosition().subtract(mouth).normalize()
                : Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        // Jet de flammes : un flot continu de grandes bouffees de feu qui gonflent en avancant, sur un coeur blanc
        // incandescent ; la fumee monte au bout du jet. Les particules ralentissent et s'ecrasent contre les obstacles.
        for (int i = 0; i < 9; i++) {
            Vec3 jet = this.spread(dir, 0.055D);
            level.sendParticles(ModParticles.DRAGON_FLAME.get(), mouth.x, mouth.y, mouth.z, 0, jet.x, jet.y, jet.z,
                    0.95D + this.random.nextDouble() * 0.4D);
        }
        for (int i = 0; i < 3; i++) {
            Vec3 jet = this.spread(dir, 0.025D);
            level.sendParticles(ModParticles.DRAGON_FLAME_CORE.get(), mouth.x, mouth.y, mouth.z, 0, jet.x, jet.y, jet.z,
                    0.7D + this.random.nextDouble() * 0.25D);
        }
        for (int i = 0; i < 3; i++) {
            Vec3 jet = this.spread(dir, 0.18D);
            level.sendParticles(ModParticles.EMBER_SPARK.get(), mouth.x, mouth.y, mouth.z, 0, jet.x, jet.y, jet.z, 0.8D);
        }
        // Impact : la ou le jet touche un bloc ou la cible, le feu se brise en gerbe et fume
        Vec3 end = mouth.add(dir.scale(BREATH_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(mouth, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 impact = hit.getType() == HitResult.Type.MISS ? (target != null && target.distanceTo(this) < BREATH_RANGE + 2.0D
                ? target.getBoundingBox().getCenter() : end) : hit.getLocation().subtract(dir.scale(0.4D));
        for (int i = 0; i < 3; i++) {
            Vec3 burst = new Vec3(this.random.nextGaussian(), this.random.nextDouble() * 0.8D, this.random.nextGaussian()).normalize();
            level.sendParticles(ModParticles.DRAGON_FLAME.get(), impact.x, impact.y, impact.z, 0, burst.x, burst.y, burst.z, 0.25D);
        }
        if (this.breathTicks % 2 == 0) {
            level.sendParticles(ModParticles.DRAGON_SMOKE.get(), impact.x, impact.y + 0.5D, impact.z, 0, 0.0D, 1.0D, 0.0D, 0.06D);
            Vec3 tail = mouth.add(dir.scale(BREATH_RANGE * 0.6D));
            level.sendParticles(ModParticles.DRAGON_SMOKE.get(), tail.x, tail.y, tail.z, 0, dir.x, dir.y + 0.4D, dir.z, 0.12D);
        }
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

    /** Position de la gueule (devant et au-dessus du corps). */
    Vec3 mouthPosition() {
        return this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(2.6D)).add(0.0D, 1.25D, 0.0D);
    }

    /** Ce qu'une attaque de zone doit epargner : elle-meme, son cavalier, son maitre, les compagnons du maitre. */
    private boolean spares(LivingEntity victim) {
        LivingEntity owner = this.getOwner();
        return victim == this || victim == owner || this.hasPassenger(victim) || victim.isAlliedTo(this)
                || (victim instanceof TamableAnimal pet && owner != null && pet.isOwnedBy(owner))
                || (victim instanceof Player && victim != this.getTarget() && this.isTame());
    }

    // --- Grande boule de feu : elle prend son souffle (flammes aspirees vers la gueule) puis crache une sphere de feu

    boolean canFireball() {
        return this.fireballCooldown <= 0 && this.fireballCharge <= 0 && this.breathTicks <= 0;
    }

    void startFireball() {
        this.fireballCharge = FIREBALL_CHARGE;
        this.fireballCooldown = 140 + this.random.nextInt(60);
        this.triggerAnim("action", "fireball");
        this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.4F);
    }

    boolean isBusyAttacking() {
        return this.breathTicks > 0 || this.fireballCharge > 0 || this.gustTicks > 0 || this.sweepTicks > 0;
    }

    private void tickFireball(ServerLevel level) {
        this.fireballCharge--;
        Vec3 mouth = this.mouthPosition();
        for (int i = 0; i < 4; i++) {
            Vec3 from = mouth.add(this.random.nextGaussian() * 1.6D, this.random.nextGaussian() * 1.2D, this.random.nextGaussian() * 1.6D);
            Vec3 pull = mouth.subtract(from);
            level.sendParticles(ParticleTypes.FLAME, from.x, from.y, from.z, 0, pull.x, pull.y, pull.z, 0.12D);
        }
        level.sendParticles(ModParticles.DRAGON_FLAME_CORE.get(), mouth.x, mouth.y, mouth.z, 0, 0.0D, 0.0D, 0.0D, 0.0D);
        if (this.fireballCharge > 0) {
            return;
        }
        LivingEntity target = this.getTarget();
        Vec3 dir = target != null ? target.getBoundingBox().getCenter().subtract(mouth).normalize()
                : Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        WyvernFireball fireball = new WyvernFireball(level, this, dir);
        fireball.setPos(mouth.x, mouth.y - 0.5D, mouth.z);
        fireball.setDeltaMovement(dir.scale(0.6D));
        level.addFreshEntity(fireball);
        this.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.6F, 0.7F);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.2F, 0.5F);
    }

    // --- Rafale d'ailes : deux battements immenses qui repoussent tout ce qui est devant elle, dans un nuage de cendres

    boolean canGust() {
        return this.gustCooldown <= 0 && !this.isBusyAttacking();
    }

    void startGust() {
        this.gustTicks = GUST_TICKS;
        this.gustCooldown = 160 + this.random.nextInt(60);
        this.triggerAnim("action", "gust");
        this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 2.0F, 0.6F);
    }

    private void tickGust(ServerLevel level) {
        this.gustTicks--;
        int beat = GUST_TICKS - this.gustTicks;
        if (beat != 6 && beat != 16) {
            return;
        }
        this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 2.5F, 0.5F);
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 origin = this.position().add(0.0D, 1.0D, 0.0D);
        for (int i = 0; i < 30; i++) {
            Vec3 dir = forward.add(this.random.nextGaussian() * 0.35D, this.random.nextGaussian() * 0.2D, this.random.nextGaussian() * 0.35D);
            level.sendParticles(ModParticles.ASH_FLAKE.get(), origin.x, origin.y, origin.z, 0, dir.x, dir.y, dir.z, 0.9D);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.CLOUD, origin.x, origin.y, origin.z, 0, dir.x, dir.y, dir.z, 0.6D);
            }
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(GUST_RANGE))) {
            if (this.spares(victim)) {
                continue;
            }
            Vec3 to = victim.position().subtract(this.position());
            double distance = to.length();
            if (distance > GUST_RANGE || to.multiply(1.0D, 0.0D, 1.0D).normalize().dot(forward) < 0.3D) {
                continue;
            }
            double strength = 1.6D * (1.0D - distance / (GUST_RANGE + 2.0D));
            Vec3 push = to.multiply(1.0D, 0.0D, 1.0D).normalize().scale(strength);
            victim.setDeltaMovement(push.x, 0.35D + strength * 0.2D, push.z);
            victim.hurtMarked = true;
            victim.hurt(this.damageSources().mobAttack(this), 3.0F);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            victim.clearFire();
        }
    }

    // --- Coup de queue : un tour complet sur elle-meme qui balaie tout autour

    boolean canTailSweep() {
        return this.sweepCooldown <= 0 && !this.isBusyAttacking();
    }

    void startTailSweep() {
        this.sweepTicks = 16;
        this.sweepCooldown = 70 + this.random.nextInt(30);
        this.triggerAnim("action", "tail_sweep");
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 0.5F);
    }

    private void tickSweep(ServerLevel level) {
        this.sweepTicks--;
        if (this.sweepTicks != 9) {
            return;
        }
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.6F);
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(angle) * 3.0D, this.getY() + 0.8D,
                    this.getZ() + Math.sin(angle) * 3.0D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SWEEP_RANGE, 1.5D, SWEEP_RANGE))) {
            if (this.spares(victim) || victim.distanceTo(this) > SWEEP_RANGE + 1.5D) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), 8.0F);
            Vec3 push = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(1.3D);
            victim.setDeltaMovement(push.x, 0.5D, push.z);
            victim.hurtMarked = true;
        }
    }

    private Vec3 spread(Vec3 dir, double amount) {
        return dir.add(this.random.nextGaussian() * amount, this.random.nextGaussian() * amount, this.random.nextGaussian() * amount)
                .normalize();
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
                .triggerableAnim("roar", ROAR).triggerableAnim("breath", BREATH).triggerableAnim("dive", DIVE)
                .triggerableAnim("fireball", FIREBALL).triggerableAnim("gust", GUST).triggerableAnim("tail_sweep", TAIL_SWEEP));
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
