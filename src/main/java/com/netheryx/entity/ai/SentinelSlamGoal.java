package com.netheryx.entity.ai;

import com.netheryx.entity.OnyxSentinel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Onde de choc de la Sentinelle : courte preparation (animation) puis degats de zone. */
public class SentinelSlamGoal extends Goal {
    private static final int DURATION = 30;
    private static final int IMPACT_TICK = 14;
    private static final double TRIGGER_DISTANCE_SQR = 36.0D;

    private final OnyxSentinel sentinel;
    private int timer;

    public SentinelSlamGoal(OnyxSentinel sentinel) {
        this.sentinel = sentinel;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.sentinel.getTarget();
        return target != null && target.isAlive() && this.sentinel.canSlam()
                && this.sentinel.distanceToSqr(target) < TRIGGER_DISTANCE_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        return this.timer < DURATION && this.sentinel.isAlive();
    }

    @Override
    public void start() {
        this.timer = 0;
        this.sentinel.getNavigation().stop();
        this.sentinel.startSlamAnimation();
    }

    @Override
    public void tick() {
        this.timer++;
        LivingEntity target = this.sentinel.getTarget();
        if (target != null) {
            this.sentinel.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (this.timer == IMPACT_TICK) {
            this.sentinel.performSlam();
        }
    }

    @Override
    public void stop() {
        this.sentinel.resetSlamCooldown();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
