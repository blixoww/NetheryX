package com.netheryx.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Le predateur tourne autour de sa proie en resserrant le cercle, puis bondit sur elle. Apres le bond, l'attaque au
 * corps a corps prend le relais jusqu'a la fin de la recharge, et la chasse reprend. Touche pendant qu'il tourne, il
 * bondit aussitot ({@link #provoke()}).
 */
public class StalkerCircleGoal extends Goal {
    private static final double MIN_DISTANCE = 3.5D;
    private static final double MAX_DISTANCE = 16.0D;
    private static final double START_RADIUS = 7.0D;
    private static final double END_RADIUS = 3.5D;
    private static final int COOLDOWN = 140;

    private final PathfinderMob mob;
    private final double speed;
    private LivingEntity target;
    private int duration;
    private int ticks;
    private double angle;
    private double direction;
    private int cooldown;
    private boolean provoked;

    public StalkerCircleGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    /** Appele quand le predateur est blesse : s'il est en train de tourner, il bondit au tick suivant. */
    public void provoke() {
        this.provoked = true;
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        LivingEntity candidate = this.mob.getTarget();
        if (candidate == null || !candidate.isAlive()) {
            return false;
        }
        double distance = this.mob.distanceTo(candidate);
        return distance >= MIN_DISTANCE && distance <= MAX_DISTANCE && this.mob.hasLineOfSight(candidate);
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && this.ticks < this.duration + 20
                && this.mob.getTarget() == this.target && this.mob.distanceTo(this.target) <= MAX_DISTANCE + 4.0D;
    }

    @Override
    public void start() {
        this.target = this.mob.getTarget();
        this.ticks = 0;
        this.provoked = false;
        this.duration = 50 + this.mob.getRandom().nextInt(50);
        Vec3 offset = this.mob.position().subtract(this.target.position());
        this.angle = Math.atan2(offset.z, offset.x);
        this.direction = this.mob.getRandom().nextBoolean() ? 1.0D : -1.0D;
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
        this.mob.setShiftKeyDown(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (this.ticks >= this.duration || this.provoked) {
            this.pounce();
            return;
        }
        // Cercle qui se resserre : le predateur se rapproche a chaque tour, tapi (accroupi)
        double progress = (double) this.ticks / this.duration;
        double radius = Mth.lerp(progress, START_RADIUS, END_RADIUS);
        this.angle += this.direction * 0.09D;
        double x = this.target.getX() + Math.cos(this.angle) * radius;
        double z = this.target.getZ() + Math.sin(this.angle) * radius;
        this.mob.setShiftKeyDown(true);
        if (this.ticks % 5 == 0) {
            this.mob.getNavigation().moveTo(x, this.target.getY(), z, this.speed);
        }
        if (this.ticks % 30 == 0) {
            this.mob.playSound(SoundEvents.WOLF_GROWL, 0.6F, 0.6F);
        }
    }

    private void pounce() {
        this.mob.setShiftKeyDown(false);
        if (this.mob.onGround() && this.mob.hasLineOfSight(this.target)) {
            Vec3 toTarget = this.target.position().subtract(this.mob.position());
            Vec3 horizontal = new Vec3(toTarget.x, 0.0D, toTarget.z);
            double length = horizontal.length();
            if (length > 0.01D) {
                double strength = Mth.clamp(length / 6.0D, 0.6D, 1.3D);
                Vec3 leap = horizontal.scale(strength / length);
                this.mob.setDeltaMovement(leap.x, 0.42D + Math.min(0.25D, length * 0.03D), leap.z);
                this.mob.playSound(SoundEvents.RAVAGER_ROAR, 0.5F, 1.6F);
            }
        }
        this.cooldown = COOLDOWN;
        this.ticks = this.duration + 20;
    }
}
