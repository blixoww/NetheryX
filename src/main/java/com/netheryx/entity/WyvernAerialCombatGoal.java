package com.netheryx.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Combat aerien de la Wyverne de cendre (sans cavalier) : elle decolle seule et tourne autour de la cible, puis
 * varie ses attaques selon la distance : jet de flammes, grande boule de feu de loin, rafale d'ailes et coup de queue
 * de pres, piques pour mordre. Elle revient se poser quand le combat est fini.
 */
class WyvernAerialCombatGoal extends Goal {
    private enum Phase { CIRCLE, BREATH, DIVE }

    private final AshWyvern wyvern;
    private Phase phase = Phase.CIRCLE;
    private int phaseTicks;
    private int diveCooldown;
    private float angle;

    WyvernAerialCombatGoal(AshWyvern wyvern) {
        this.wyvern = wyvern;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.wyvern.getTarget();
        return target != null && target.isAlive() && !this.wyvern.isVehicle() && !this.wyvern.isOrderedToSit()
                && this.wyvern.distanceToSqr(target) < 48.0D * 48.0D;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.wyvern.setAerial(true);
        this.wyvern.getNavigation().stop();
        this.phase = Phase.CIRCLE;
        this.phaseTicks = 0;
        this.diveCooldown = 80;
        this.angle = this.wyvern.getRandom().nextFloat() * Mth.TWO_PI;
        // Envol : un premier coup d'ailes vers le haut
        this.wyvern.setDeltaMovement(this.wyvern.getDeltaMovement().add(0.0D, 0.6D, 0.0D));
        this.wyvern.triggerAnim("action", "roar");
    }

    @Override
    public void stop() {
        // Fin du combat : elle redescend en planant (la gravite reprend, la plane ralentit la chute)
        this.wyvern.setAerial(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.wyvern.getTarget();
        if (target == null) {
            return;
        }
        this.phaseTicks++;
        if (this.diveCooldown > 0) {
            this.diveCooldown--;
        }
        double distance = this.wyvern.distanceTo(target);
        Vec3 desired;
        double speed;
        switch (this.phase) {
            case BREATH -> {
                // Vol stationnaire face a la cible, un peu au-dessus
                desired = target.position().add(this.wyvern.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D)
                        .normalize().scale(7.0D)).add(0.0D, 4.0D, 0.0D);
                speed = 0.25D;
                if (!this.wyvern.isBusyAttacking() && this.phaseTicks > 5) {
                    this.switchTo(Phase.CIRCLE);
                }
            }
            case DIVE -> {
                desired = target.getEyePosition();
                speed = 0.95D;
                if (distance < 2.8D) {
                    this.wyvern.diveHit(target);
                    this.diveCooldown = 110 + this.wyvern.getRandom().nextInt(40);
                    this.switchTo(Phase.CIRCLE);
                } else if (this.phaseTicks > 45) {
                    this.diveCooldown = 60;
                    this.switchTo(Phase.CIRCLE);
                }
            }
            default -> {
                this.angle += 0.05F;
                desired = target.position().add(Mth.cos(this.angle) * 8.0D, 5.0D + Mth.sin(this.angle * 2.0F) * 1.5D,
                        Mth.sin(this.angle) * 8.0D);
                speed = 0.6D;
                boolean sight = this.wyvern.hasLineOfSight(target);
                if (distance < 5.5D && this.wyvern.canTailSweep()) {
                    this.wyvern.startTailSweep();
                } else if (distance < 8.0D && sight && this.wyvern.canGust()) {
                    this.wyvern.startGust();
                    this.switchTo(Phase.BREATH);
                } else if (this.wyvern.canBreath() && distance < 14.0D && sight) {
                    this.wyvern.startBreath();
                    this.switchTo(Phase.BREATH);
                } else if (distance >= 12.0D && sight && this.wyvern.canFireball()) {
                    this.wyvern.startFireball();
                    this.switchTo(Phase.BREATH);
                } else if (this.diveCooldown <= 0 && this.wyvern.hasLineOfSight(target)) {
                    this.switchTo(Phase.DIVE);
                }
            }
        }
        Vec3 to = desired.subtract(this.wyvern.position());
        double length = to.length();
        Vec3 wanted = length > 0.01D ? to.scale(Math.min(speed, length * 0.15D) / length) : Vec3.ZERO;
        this.wyvern.setDeltaMovement(this.wyvern.getDeltaMovement().scale(0.8D).add(wanted.scale(0.2D)));
        // Orientation : le corps suit la trajectoire, la tete fixe la cible
        Vec3 motion = this.wyvern.getDeltaMovement();
        if (this.phase == Phase.BREATH || motion.horizontalDistanceSqr() < 0.0025D) {
            Vec3 face = target.position().subtract(this.wyvern.position());
            this.setYaw((float) (Mth.atan2(face.z, face.x) * Mth.RAD_TO_DEG) - 90.0F);
        } else {
            this.setYaw((float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0F);
        }
        this.wyvern.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private void setYaw(float yaw) {
        float current = this.wyvern.getYRot();
        float next = current + Mth.clamp(Mth.wrapDegrees(yaw - current), -12.0F, 12.0F);
        this.wyvern.setYRot(next);
        this.wyvern.yBodyRot = next;
    }

    private void switchTo(Phase phase) {
        this.phase = phase;
        this.phaseTicks = 0;
    }
}
