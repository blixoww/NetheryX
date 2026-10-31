package com.netheryx.entity.ai;

import com.netheryx.entity.WitheredKnight;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Capitaine fletri : mene sa patrouille d'un point a l'autre, a pas lents pour que ses chevaliers suivent, et marque
 * une pause a chaque etape.
 */
public class KnightPatrolGoal extends Goal {
    private final WitheredKnight captain;
    private Vec3 destination;
    private int pause;

    public KnightPatrolGoal(WitheredKnight captain) {
        this.captain = captain;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.captain.isCaptain() || this.captain.getTarget() != null || this.captain.isVehicle()) {
            return false;
        }
        if (this.pause > 0) {
            this.pause--;
            return false;
        }
        this.destination = LandRandomPos.getPos(this.captain, 24, 6);
        return this.destination != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.captain.getTarget() == null && !this.captain.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.captain.getNavigation().moveTo(this.destination.x, this.destination.y, this.destination.z, 0.65D);
    }

    @Override
    public void stop() {
        this.pause = 80 + this.captain.getRandom().nextInt(80);
    }
}
