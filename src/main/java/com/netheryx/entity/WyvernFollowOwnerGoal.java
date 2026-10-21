package com.netheryx.entity;

import com.netheryx.util.WyvernCompanions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Suivi du maitre : a pied quand il est proche et au sol, en vol quand il s'eloigne, prend de la hauteur ou vole
 * lui-meme ; teleportee a ses cotes s'il est trop loin. Elle ne se perd jamais en route, meme si elle est trop grande
 * pour passer par certains couloirs.
 */
class WyvernFollowOwnerGoal extends Goal {
    private static final double START = 9.0D;
    private static final double STOP = 4.5D;
    private static final double TELEPORT = 40.0D;

    private final AshWyvern wyvern;
    private boolean flying;

    WyvernFollowOwnerGoal(AshWyvern wyvern) {
        this.wyvern = wyvern;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private LivingEntity owner() {
        return this.wyvern.getOwner();
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.owner();
        return owner != null && owner.isAlive() && !owner.isSpectator() && this.wyvern.isTame() && !this.wyvern.isOrderedToSit()
                && !this.wyvern.isVehicle() && !this.wyvern.isLeashed() && owner.level() == this.wyvern.level()
                && (this.wyvern.distanceTo(owner) > START || !owner.onGround() && this.wyvern.distanceTo(owner) > STOP + 2.0D);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity owner = this.owner();
        if (owner == null || !owner.isAlive() || owner.level() != this.wyvern.level() || this.wyvern.isOrderedToSit()
                || this.wyvern.isVehicle()) {
            return false;
        }
        return this.wyvern.distanceTo(owner) > STOP || !owner.onGround();
    }

    @Override
    public void stop() {
        if (this.flying) {
            this.wyvern.setAerial(false);
            this.flying = false;
        }
        this.wyvern.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity owner = this.owner();
        if (owner == null) {
            return;
        }
        double distance = this.wyvern.distanceTo(owner);
        this.wyvern.getLookControl().setLookAt(owner, 20.0F, 20.0F);
        if (distance > TELEPORT && owner instanceof ServerPlayer player) {
            WyvernCompanions.bring(this.wyvern, player);
            return;
        }
        boolean shouldFly = !owner.onGround() || owner.getY() - this.wyvern.getY() > 2.5D || distance > 16.0D
                || (this.wyvern.getNavigation().isDone() && distance > START + 2.0D);
        if (shouldFly != this.flying) {
            this.flying = shouldFly;
            this.wyvern.setAerial(shouldFly);
            if (shouldFly) {
                this.wyvern.getNavigation().stop();
                this.wyvern.setDeltaMovement(this.wyvern.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
            }
        }
        if (this.flying) {
            // Vole un peu derriere et au-dessus du maitre
            Vec3 behind = owner.position().subtract(owner.getLookAngle().multiply(4.0D, 0.0D, 4.0D)).add(0.0D, owner.onGround() ? 2.0D : 3.5D, 0.0D);
            Vec3 to = behind.subtract(this.wyvern.position());
            double length = to.length();
            double speed = Math.min(0.9D, 0.25D + length * 0.04D);
            Vec3 wanted = length > 0.01D ? to.scale(Math.min(speed, length * 0.2D) / length) : Vec3.ZERO;
            this.wyvern.setDeltaMovement(this.wyvern.getDeltaMovement().scale(0.8D).add(wanted.scale(0.2D)));
            Vec3 motion = this.wyvern.getDeltaMovement();
            if (motion.horizontalDistanceSqr() > 0.003D) {
                float yaw = (float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0F;
                float next = this.wyvern.getYRot() + Mth.clamp(Mth.wrapDegrees(yaw - this.wyvern.getYRot()), -15.0F, 15.0F);
                this.wyvern.setYRot(next);
                this.wyvern.yBodyRot = next;
            }
        } else if (this.wyvern.tickCount % 10 == 0) {
            this.wyvern.getNavigation().moveTo(owner, 1.3D);
        }
    }
}
