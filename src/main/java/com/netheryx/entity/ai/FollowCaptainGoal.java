package com.netheryx.entity.ai;

import com.netheryx.entity.WitheredKnight;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Chevalier d'une patrouille : garde sa place dans la formation, en arc de cercle derriere son capitaine.
 */
public class FollowCaptainGoal extends Goal {
    private static final double SLOT_DISTANCE = 3.0D;

    private final WitheredKnight knight;
    private WitheredKnight captain;

    public FollowCaptainGoal(WitheredKnight knight) {
        this.knight = knight;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.knight.getTarget() != null) {
            return false;
        }
        this.captain = this.knight.findCaptain();
        return this.captain != null && this.knight.distanceToSqr(this.slot()) > 4.0D;
    }

    @Override
    public boolean canContinueToUse() {
        return this.captain != null && this.captain.isAlive() && this.knight.getTarget() == null
                && this.knight.distanceToSqr(this.slot()) > 2.0D;
    }

    @Override
    public void stop() {
        this.captain = null;
        this.knight.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.knight.tickCount % 10 == 0) {
            Vec3 slot = this.slot();
            double speed = this.knight.distanceToSqr(slot) > 64.0D ? 1.1D : 0.8D;
            this.knight.getNavigation().moveTo(slot.x, slot.y, slot.z, speed);
        }
    }

    /** Place du chevalier : en arc derriere le capitaine, selon un rang tire de son identifiant. */
    private Vec3 slot() {
        int rank = Math.floorMod(this.knight.getUUID().hashCode(), 5) - 2;
        double yaw = Math.toRadians(this.captain.getYRot()) + rank * 0.6D;
        // Derriere le capitaine : oppose a son regard
        double x = this.captain.getX() + Math.sin(yaw) * SLOT_DISTANCE;
        double z = this.captain.getZ() - Math.cos(yaw) * SLOT_DISTANCE;
        return new Vec3(x, this.captain.getY(), z);
    }
}
