package com.netheryx.entity;

import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModItems;
import com.netheryx.registry.ModParticles;
import com.netheryx.util.MobParticles;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
 * Escargot de cristal : lent et paisible, sa coquille se couvre de cristaux d'onyx. Aux cisailles, il donne des
 * eclats d'onyx sans etre blesse ; les cristaux repoussent en quelques minutes.
 */
public class CrystalSnail extends Animal implements GeoEntity {
    private static final EntityDataAccessor<Boolean> CRYSTALS = SynchedEntityData.defineId(CrystalSnail.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = NetheryXAnimations.loop("crystal_snail", "idle");
    private static final RawAnimation WALK = NetheryXAnimations.loop("crystal_snail", "walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int regrowTime;

    public CrystalSnail(EntityType<? extends CrystalSnail> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CRYSTALS, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2D, Ingredient.of(ModTags.Items.CRYSTAL_SNAIL_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    public boolean hasCrystals() {
        return this.entityData.get(CRYSTALS);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.hasCrystals()) {
            MobParticles.ambient(this, ModParticles.ONYX_GLINT.get(), 14, 0.4D, 0.0D);
        }
        if (!this.level().isClientSide && !this.hasCrystals() && --this.regrowTime <= 0) {
            this.entityData.set(CRYSTALS, true);
            this.playSound(SoundEvents.AMETHYST_CLUSTER_PLACE, 0.6F, 1.2F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SHEARS) && this.hasCrystals() && !this.isBaby()) {
            if (!this.level().isClientSide) {
                this.level().playSound(null, this, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.entityData.set(CRYSTALS, false);
                this.regrowTime = 4800 + this.random.nextInt(2400);
                this.spawnAtLocation(new ItemStack(ModItems.ONYX_SHARD.get(), 1 + this.random.nextInt(3)));
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Crystals", this.hasCrystals());
        tag.putInt("RegrowTime", this.regrowTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Crystals")) {
            this.entityData.set(CRYSTALS, tag.getBoolean("Crystals"));
        }
        this.regrowTime = tag.getInt("RegrowTime");
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModTags.Items.CRYSTAL_SNAIL_FOOD);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.CRYSTAL_SNAIL.get().create(level);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.1F, 1.2F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
    }

    private PlayState movementPredicate(AnimationState<CrystalSnail> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
