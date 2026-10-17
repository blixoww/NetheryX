package com.netheryx.entity;

import com.netheryx.config.NetheryXConfig;
import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Fouisseur de basalte : grand ver de roche, neutre. Il creuse de longs tunnels a travers la netherrack,
 * le basalte et la pierre noire (pratique pour explorer) et digere les minerais : a sa mort il rend des
 * pepites, du quartz, parfois de l'onyx, ainsi qu'un croc qui sert a l'infusion de portee.
 */
public class BasaltBurrower extends Monster implements GeoEntity {
    private static final RawAnimation CRAWL = NetheryXAnimations.loop("basalt_burrower", "crawl");
    private static final RawAnimation IDLE = NetheryXAnimations.loop("basalt_burrower", "idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BasaltBurrower(EntityType<? extends BasaltBurrower> type, Level level) {
        super(type, level);
        this.xpReward = 8;
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new BurrowGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** Creuse un tunnel en ligne droite (2 blocs de haut) dans une direction choisie au hasard. */
    static class BurrowGoal extends Goal {
        private final BasaltBurrower worm;
        private Direction direction = Direction.NORTH;
        private int remaining;
        private int cooldown;

        BurrowGoal(BasaltBurrower worm) {
            this.worm = worm;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || this.worm.getTarget() != null || !(this.worm.level() instanceof ServerLevel level)
                    || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || !NetheryXConfig.BURROWER_DIGS.get()) {
                return false;
            }
            return this.worm.getRandom().nextInt(80) == 0;
        }

        @Override
        public void start() {
            this.direction = Direction.Plane.HORIZONTAL.getRandomDirection(this.worm.getRandom());
            this.remaining = 6 + this.worm.getRandom().nextInt(18);
        }

        @Override
        public boolean canContinueToUse() {
            return this.remaining > 0 && this.worm.getTarget() == null;
        }

        @Override
        public void stop() {
            this.cooldown = 200 + this.worm.getRandom().nextInt(400);
        }

        @Override
        public void tick() {
            if (this.worm.tickCount % 12 != 0) {
                this.worm.getNavigation().moveTo(this.worm.getX() + this.direction.getStepX() * 2.0D, this.worm.getY(),
                        this.worm.getZ() + this.direction.getStepZ() * 2.0D, 0.8D);
                return;
            }
            BlockPos front = this.worm.blockPosition().relative(this.direction);
            boolean dug = false;
            for (int dy = 0; dy <= 1; dy++) {
                BlockPos pos = front.above(dy);
                BlockState state = this.worm.level().getBlockState(pos);
                if (state.is(ModTags.Blocks.BURROWER_DIGGABLE)) {
                    this.worm.level().destroyBlock(pos, false, this.worm);
                    ((ServerLevel) this.worm.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12, 0.3D, 0.3D, 0.3D, 0.05D);
                    dug = true;
                }
            }
            if (!dug && !this.worm.level().getBlockState(front).isAir()) {
                this.remaining = 0;
                return;
            }
            Vec3 step = Vec3.atBottomCenterOf(front);
            this.worm.getNavigation().moveTo(step.x, step.y, step.z, 0.8D);
            this.remaining--;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    public float getVoicePitch() {
        return 0.4F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BASALT_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BASALT_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.3F, 0.5F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<BasaltBurrower> state) {
        return state.setAndContinue(state.isMoving() ? CRAWL : IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
