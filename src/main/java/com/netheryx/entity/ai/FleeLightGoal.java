package com.netheryx.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LightLayer;

import java.util.EnumSet;

/**
 * Creature volante qui fuit la lumiere : tant que la lumiere des blocs a sa position atteint {@code threshold}, elle
 * gagne la position la plus sombre des environs (sans attaquer). Lave, lanternes, porteurs de lanterne : la lumiere
 * devient une defense.
 */
public class FleeLightGoal extends Goal {
    private static final int SAMPLES = 16;
    private static final int RANGE = 10;

    private final PathfinderMob mob;
    private final int threshold;
    private final double speed;
    private BlockPos refuge;
    private int retry;

    public FleeLightGoal(PathfinderMob mob, int threshold, double speed) {
        this.mob = mob;
        this.threshold = threshold;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private int lightAt(BlockPos pos) {
        return this.mob.level().getBrightness(LightLayer.BLOCK, pos);
    }

    @Override
    public boolean canUse() {
        if (this.mob.tickCount % 10 != 0 || this.lightAt(this.mob.blockPosition()) < this.threshold) {
            return false;
        }
        this.refuge = this.findRefuge();
        return this.refuge != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.refuge != null && this.lightAt(this.mob.blockPosition()) >= this.threshold - 2
                && this.mob.distanceToSqr(this.refuge.getCenter()) > 2.0D;
    }

    @Override
    public void start() {
        this.retry = 0;
        this.moveToRefuge();
    }

    @Override
    public void stop() {
        this.refuge = null;
    }

    @Override
    public void tick() {
        // La cible peut bouger avec sa lumiere (porteur de lanterne) : on recherche un refuge de temps en temps
        if (++this.retry % 20 == 0 || this.mob.getNavigation().isDone()) {
            BlockPos better = this.findRefuge();
            if (better != null) {
                this.refuge = better;
            }
            this.moveToRefuge();
        }
    }

    private void moveToRefuge() {
        this.mob.getNavigation().moveTo(this.refuge.getX() + 0.5D, this.refuge.getY() + 0.5D, this.refuge.getZ() + 0.5D, this.speed);
    }

    /** Position d'air la plus sombre parmi quelques tirages, si elle est nettement plus sombre qu'ici. */
    private BlockPos findRefuge() {
        BlockPos origin = this.mob.blockPosition();
        int here = this.lightAt(origin);
        BlockPos best = null;
        int bestLight = here - 3;
        for (int i = 0; i < SAMPLES; i++) {
            BlockPos pos = origin.offset(this.mob.getRandom().nextInt(RANGE * 2 + 1) - RANGE,
                    this.mob.getRandom().nextInt(9) - 4, this.mob.getRandom().nextInt(RANGE * 2 + 1) - RANGE);
            if (!this.mob.level().isEmptyBlock(pos) || !this.mob.level().isEmptyBlock(pos.above())) {
                continue;
            }
            int light = this.lightAt(pos);
            if (light < bestLight) {
                best = pos;
                bestLight = light;
            }
        }
        return best;
    }
}
