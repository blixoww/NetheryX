package com.netheryx.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Cloisonne un boss dans son arene : la position ou il apparait devient le centre de l'arene. S'il en sort, il y
 * retourne ; s'il s'en eloigne trop (bond, teleportation, recul), il y est ramene aussitot. Une cible qui quitte
 * l'arene est abandonnee, et le boss se soigne lentement tant que personne ne le combat.
 */
public final class BossArena {
    private static final String TAG = "ArenaHome";

    private final double radius;
    private final double height;
    private final double depth;
    private BlockPos home;

    public BossArena(double radius, double height) {
        this(radius, height, 4.0D);
    }

    /** depth : tolerance sous le point d'apparition (utile pour un boss volant invoque au-dessus du joueur). */
    public BossArena(double radius, double height, double depth) {
        this.radius = radius;
        this.height = height;
        this.depth = depth;
    }

    public boolean contains(Entity entity) {
        if (this.home == null) {
            return true;
        }
        double dx = entity.getX() - (this.home.getX() + 0.5D);
        double dz = entity.getZ() - (this.home.getZ() + 0.5D);
        double dy = entity.getY() - this.home.getY();
        return dx * dx + dz * dz <= (this.radius + 2.0D) * (this.radius + 2.0D) && dy > -this.depth && dy < this.height + 4.0D;
    }

    public void tick(Mob boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.home == null) {
            this.home = boss.blockPosition();
        }
        boss.restrictTo(this.home, (int) this.radius);
        LivingEntity target = boss.getTarget();
        if (target != null && !this.contains(target)) {
            boss.setTarget(null);
            target = null;
        }
        double dx = boss.getX() - (this.home.getX() + 0.5D);
        double dz = boss.getZ() - (this.home.getZ() + 0.5D);
        double dy = boss.getY() - this.home.getY();
        double distSqr = dx * dx + dz * dz;
        if (distSqr > (this.radius + 4.0D) * (this.radius + 4.0D) || dy < -this.depth - 2.0D || dy > this.height + 6.0D) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, boss.getX(), boss.getY(0.5D), boss.getZ(), 30, 0.6D, 1.0D, 0.6D, 0.05D);
            boss.getNavigation().stop();
            boss.teleportTo(this.home.getX() + 0.5D, this.home.getY(), this.home.getZ() + 0.5D);
            boss.setDeltaMovement(0.0D, 0.0D, 0.0D);
            boss.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.6F);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, boss.getX(), boss.getY(0.5D), boss.getZ(), 30, 0.6D, 1.0D, 0.6D, 0.05D);
        } else if (distSqr > this.radius * this.radius && boss.tickCount % 10 == 0) {
            boss.getNavigation().moveTo(this.home.getX() + 0.5D, this.home.getY(), this.home.getZ() + 0.5D, 1.2D);
        }
        if (target == null && boss.tickCount % 20 == 0 && boss.getHealth() < boss.getMaxHealth()) {
            boss.heal(boss.getMaxHealth() * 0.01F);
        }
    }

    public void save(CompoundTag tag) {
        if (this.home != null) {
            tag.put(TAG, NbtUtils.writeBlockPos(this.home));
        }
    }

    public void load(CompoundTag tag) {
        if (tag.contains(TAG)) {
            this.home = NbtUtils.readBlockPos(tag.getCompound(TAG));
        }
    }
}
