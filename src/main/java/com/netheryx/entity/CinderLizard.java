package com.netheryx.entity;

import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
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
 * Lezard de cendre : monture du Nether. On l'apprivoise avec des baies de feu, on le selle, puis on le dirige
 * comme un cheval. Il marche sur la lave et court encore plus vite sur le basalte et la pierre noire.
 */
public class CinderLizard extends Animal implements Saddleable, GeoEntity {
    private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(CinderLizard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(CinderLizard.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = NetheryXAnimations.loop("cinder_lizard", "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop("cinder_lizard", "walk");
    private static final RawAnimation RUN = NetheryXAnimations.loop("cinder_lizard", "run");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CinderLizard(EntityType<? extends CinderLizard> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TAMED, false);
        this.entityData.define(SADDLED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.3D, Ingredient.of(ModTags.Items.CINDER_LIZARD_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.2D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public boolean isTamed() {
        return this.entityData.get(TAMED);
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby() && this.isTamed();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource source) {
        this.entityData.set(SADDLED, true);
        if (source != null) {
            this.level().playSound(null, this, SoundEvents.HORSE_SADDLE, source, 0.5F, 1.0F);
        }
    }

    @Override
    public boolean isSaddled() {
        return this.entityData.get(SADDLED);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModTags.Items.CINDER_LIZARD_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTamed() && this.isFood(stack) && !this.isBaby()) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (this.random.nextInt(4) == 0) {
                    this.entityData.set(TAMED, true);
                    this.setPersistenceRequired();
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (stack.is(Items.SADDLE) && this.isSaddleable() && !this.isSaddled()) {
            if (!this.level().isClientSide) {
                this.equipSaddle(SoundSource.NEUTRAL);
                this.gameEvent(GameEvent.EQUIP, player);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isSaddled() && !this.isVehicle() && !player.isSecondaryUseActive() && !this.isFood(stack)) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 6 || id == 7) {
            for (int i = 0; i < 7; i++) {
                this.level().addParticle(id == 7 ? ParticleTypes.HEART : ParticleTypes.SMOKE, this.getRandomX(1.0D), this.getRandomY() + 0.5D,
                        this.getRandomZ(1.0D), 0.0D, 0.02D, 0.0D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // --- Monture ----------------------------------------------------------------------------------

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return this.isSaddled() && this.getFirstPassenger() instanceof Player player ? player : null;
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
            forward *= 0.25F;
        }
        return new Vec3(player.xxa * 0.5F, 0.0D, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.onFastGround() ? 1.6F : 1.15F);
    }

    /** Basalte, pierre noire et lave : terrain de predilection, vitesse accrue. */
    private boolean onFastGround() {
        BlockState below = this.getBlockStateOn();
        return below.is(ModTags.Blocks.CINDER_LIZARD_FAST_GROUND) || this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)
                || this.level().getFluidState(this.blockPosition().below()).is(FluidTags.LAVA);
    }

    @Override
    public boolean canStandOnFluid(FluidState fluid) {
        return fluid.is(FluidTags.LAVA);
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getBbHeight() * 0.85D;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        MobParticles.ambient(this, ModParticles.EMBER_SPARK.get(), this.isVehicle() ? 2 : 12, 0.9D, 0.02D);
        if (!this.level().isClientSide && this.isInLava()) {
            // Remonte a la surface comme un arpenteur
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.08D, 0.0D));
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).is(ModTags.Blocks.CINDER_LIZARD_FAST_GROUND) ? 10.0F : 0.0F;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        CinderLizard baby = ModEntities.CINDER_LIZARD.get().create(level);
        if (baby != null && this.isTamed()) {
            baby.entityData.set(TAMED, true);
        }
        return baby;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Tamed", this.isTamed());
        tag.putBoolean("Saddled", this.isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TAMED, tag.getBoolean("Tamed"));
        this.entityData.set(SADDLED, tag.getBoolean("Saddled"));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.isTamed() && super.removeWhenFarAway(distance);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.STRIDER_STEP, 0.3F, 1.3F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 3, this::predicate));
    }

    private PlayState predicate(AnimationState<CinderLizard> state) {
        if (!state.isMoving()) {
            return state.setAndContinue(IDLE);
        }
        return state.setAndContinue(this.isVehicle() ? RUN : WALK);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
