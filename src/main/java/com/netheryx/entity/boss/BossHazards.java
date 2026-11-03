package com.netheryx.entity.boss;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Dangers annonces d'un combat de boss. Tous se lisent a l'avance et se contrent :
 * <ul>
 *     <li>{@link #circle} : on sort de la zone avant la fin du remplissage ;</li>
 *     <li>{@link #wave} : on saute par-dessus l'anneau ;</li>
 *     <li>{@link #line} : on quitte la ligne ;</li>
 *     <li>{@link #beam} : on tourne avec le faisceau, ou on s'abrite derriere un pilier (les blocs l'arretent).</li>
 * </ul>
 * Les degats sont magiques (ils traversent l'armure) : ces attaques se gagnent a l'esquive, pas a l'equipement.
 */
public final class BossHazards {
    private interface Hazard {
        /** Renvoie true quand le danger est termine. */
        boolean tick(ServerLevel level);
    }

    private final Mob boss;
    private Entity anchor;
    private final List<Hazard> active = new ArrayList<>();
    private final Map<String, Integer> started = new HashMap<>();

    public BossHazards(Mob boss) {
        this.boss = boss;
        this.anchor = boss;
    }

    /** Sans boss : les memes dangers, lances par un mecanisme de donjon. */
    public BossHazards() {
        this.boss = null;
    }

    /** Entite de reference des tests de vue du faisceau, quand il n'y a pas de boss pour tenir ce role. */
    public void anchor(Entity entity) {
        this.anchor = entity;
    }

    private SoundSource sounds() {
        return this.boss != null ? this.boss.getSoundSource() : SoundSource.HOSTILE;
    }

    public void tick(ServerLevel level) {
        this.active.removeIf(hazard -> hazard.tick(level));
    }

    public boolean isEmpty() {
        return this.active.isEmpty();
    }

    /** Nombre de dangers de chaque sorte lances depuis le debut du combat (circle, wave, line, beam). */
    public Map<String, Integer> started() {
        return this.started;
    }

    private static ParticleOptions flash(int color) {
        return switch (color) {
            case BossFx.ONYX -> ParticleTypes.REVERSE_PORTAL;
            case BossFx.SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
            default -> ParticleTypes.FLAME;
        };
    }

    private static ParticleOptions debris(int color) {
        return new BlockParticleOption(ParticleTypes.BLOCK, switch (color) {
            case BossFx.ONYX -> Blocks.BLACKSTONE.defaultBlockState();
            case BossFx.SOUL -> Blocks.SOUL_SOIL.defaultBlockState();
            case BossFx.GOLD -> Blocks.BASALT.defaultBlockState();
            default -> Blocks.MAGMA_BLOCK.defaultBlockState();
        });
    }

    private void hurt(Player victim, float damage, int color, double pushX, double pushY, double pushZ) {
        DamageSource source = this.boss != null ? this.boss.damageSources().indirectMagic(this.boss, this.boss) : victim.damageSources().magic();
        if (victim.hurt(source, damage)) {
            if (color == BossFx.FIRE) {
                victim.setSecondsOnFire(4);
            }
            victim.push(pushX, pushY, pushZ);
            victim.hurtMarked = true;
        }
    }

    private List<Player> playersIn(ServerLevel level, AABB box) {
        return level.getEntitiesOfClass(Player.class, box, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    }

    /** Zone circulaire : remplissage pendant delay ticks, puis impact. */
    public void circle(ServerLevel level, Vec3 point, double radius, int delay, float damage, int color) {
        this.started.merge("circle", 1, Integer::sum);
        Vec3 center = BossFx.onGround(level, point);
        BossFx.telegraph(level, center, radius, delay, color);
        this.active.add(new Hazard() {
            private int age;

            @Override
            public boolean tick(ServerLevel server) {
                if (++this.age < delay) {
                    return false;
                }
                server.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(debris(color), center.x, center.y + 0.2D, center.z, (int) (radius * 18.0D), radius / 2.0D, 0.1D, radius / 2.0D, 0.15D);
                BossFx.ring(server, flash(color), center.add(0.0D, 0.15D, 0.0D), radius * 0.6D, 18, 0.14D);
                BossFx.column(server, flash(color), center, 3.5D, 12, radius * 0.2D);
                BossFx.shockwave(server, center, radius * 1.15D, 7, color);
                server.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, BossHazards.this.sounds(), 0.8F, 1.2F);
                for (Player victim : BossHazards.this.playersIn(server, new AABB(center, center).inflate(radius, 3.0D, radius))) {
                    double dx = victim.getX() - center.x;
                    double dz = victim.getZ() - center.z;
                    if (dx * dx + dz * dz <= radius * radius) {
                        BossHazards.this.hurt(victim, damage, color, dx * 0.12D, 0.55D, dz * 0.12D);
                    }
                }
                return true;
            }
        });
    }

    /** Onde : un anneau part du centre et s'etend au sol ; il suffit d'etre en l'air quand il passe. */
    public void wave(ServerLevel level, Vec3 point, double maxRadius, double speed, float damage, int color) {
        this.started.merge("wave", 1, Integer::sum);
        Vec3 center = BossFx.onGround(level, point);
        BossFx.shockwave(level, center, maxRadius, (int) (maxRadius / speed), color);
        this.active.add(new Hazard() {
            private final Set<UUID> struck = new HashSet<>();
            private double radius;

            @Override
            public boolean tick(ServerLevel server) {
                this.radius += speed;
                int count = Math.min(40, (int) (this.radius * 4.0D) + 6);
                BossFx.ring(server, debris(color), center.add(0.0D, 0.1D, 0.0D), this.radius, count, 0.05D);
                BossFx.ring(server, flash(color), center.add(0.0D, 0.2D, 0.0D), this.radius, count / 2, 0.01D);
                for (Player victim : BossHazards.this.playersIn(server, new AABB(center, center).inflate(this.radius + 1.5D, 1.2D, this.radius + 1.5D))) {
                    double dx = victim.getX() - center.x;
                    double dz = victim.getZ() - center.z;
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    boolean airborne = victim.getY() - center.y > 0.75D;
                    if (!airborne && Math.abs(distance - this.radius) < 1.2D && this.struck.add(victim.getUUID())) {
                        double norm = Math.max(0.1D, distance);
                        BossHazards.this.hurt(victim, damage, color, dx / norm * 0.8D, 0.45D, dz / norm * 0.8D);
                    }
                }
                return this.radius >= maxRadius;
            }
        });
    }

    /** Ligne droite : balisee pendant delay ticks, puis elle se dechaine d'un bout a l'autre. */
    public void line(ServerLevel level, Vec3 start, Vec3 direction, double length, double width, int delay, float damage, int color) {
        this.started.merge("line", 1, Integer::sum);
        Vec3 from = BossFx.onGround(level, start);
        Vec3 dir = new Vec3(direction.x, 0.0D, direction.z).normalize();
        this.active.add(new Hazard() {
            private int age;

            @Override
            public boolean tick(ServerLevel server) {
                this.age++;
                if (this.age < delay) {
                    if (this.age % 4 == 1) {
                        for (double d = 0.0D; d < length; d += 1.0D) {
                            Vec3 at = from.add(dir.scale(d));
                            server.sendParticles(flash(color), at.x, at.y + 0.1D, at.z, 1, width * 0.3D, 0.0D, width * 0.3D, 0.0D);
                        }
                    }
                    return false;
                }
                for (double d = 0.0D; d < length; d += 1.0D) {
                    Vec3 at = from.add(dir.scale(d));
                    server.sendParticles(debris(color), at.x, at.y + 0.2D, at.z, 5, width * 0.4D, 0.1D, width * 0.4D, 0.15D);
                    BossFx.column(server, flash(color), at, 2.0D, 3, 0.15D);
                }
                server.playSound(null, from.x, from.y, from.z, SoundEvents.BLAZE_SHOOT, BossHazards.this.sounds(), 1.2F, 0.5F);
                Vec3 end = from.add(dir.scale(length));
                for (Player victim : BossHazards.this.playersIn(server, new AABB(from, end).inflate(width + 0.5D, 2.5D, width + 0.5D))) {
                    if (distanceToSegment(victim.position().multiply(1.0D, 0.0D, 1.0D), from.multiply(1.0D, 0.0D, 1.0D),
                            end.multiply(1.0D, 0.0D, 1.0D)) <= width) {
                        BossHazards.this.hurt(victim, damage, color, 0.0D, 0.5D, 0.0D);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Faisceau tournant autour d'un point : montre sa direction pendant windup ticks, puis balaie l'arene. Les blocs
     * l'arretent : un pilier est un abri.
     */
    public void beam(ServerLevel level, Vec3 origin, double startAngle, double degreesPerTick, int windup, int duration,
                     double length, float damage, int color) {
        this.started.merge("beam", 1, Integer::sum);
        this.active.add(new Hazard() {
            private final Map<UUID, Integer> lastHit = new HashMap<>();
            private int age;

            @Override
            public boolean tick(ServerLevel server) {
                this.age++;
                int moving = Math.max(0, this.age - windup);
                double angle = startAngle + Math.toRadians(degreesPerTick * moving);
                Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                var hit = server.clip(new ClipContext(origin, origin.add(dir.scale(length)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, BossHazards.this.anchor));
                Vec3 end = hit.getType() == HitResult.Type.MISS ? origin.add(dir.scale(length)) : hit.getLocation();
                double reach = end.distanceTo(origin);
                if (this.age <= windup) {
                    if (this.age % 3 == 0) {
                        for (double d = 1.0D; d < reach; d += 1.0D) {
                            Vec3 at = origin.add(dir.scale(d));
                            server.sendParticles(flash(color), at.x, at.y, at.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
                        }
                    }
                    return false;
                }
                for (double d = 1.0D; d < reach; d += 0.6D) {
                    Vec3 at = origin.add(dir.scale(d));
                    server.sendParticles(flash(color), at.x, at.y, at.z, 1, 0.08D, 0.08D, 0.08D, 0.01D);
                }
                server.sendParticles(debris(color), end.x, end.y, end.z, 4, 0.2D, 0.2D, 0.2D, 0.1D);
                if (moving % 8 == 0) {
                    server.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BEACON_AMBIENT, BossHazards.this.sounds(), 1.5F, 1.6F);
                }
                for (Player victim : BossHazards.this.playersIn(server, new AABB(origin, end).inflate(1.5D, 3.0D, 1.5D))) {
                    Vec3 chest = victim.position().add(0.0D, 0.9D, 0.0D);
                    if (distanceToSegment(chest, origin, end) < 1.1D
                            && this.age - this.lastHit.getOrDefault(victim.getUUID(), -100) >= 12) {
                        this.lastHit.put(victim.getUUID(), this.age);
                        BossHazards.this.hurt(victim, damage, color, dir.x * 0.2D, 0.2D, dir.z * 0.2D);
                    }
                }
                return moving >= duration;
            }
        });
    }

    static double distanceToSegment(Vec3 point, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double lengthSqr = ab.lengthSqr();
        double t = lengthSqr < 1.0E-6D ? 0.0D : Math.max(0.0D, Math.min(1.0D, point.subtract(a).dot(ab) / lengthSqr));
        return point.distanceTo(a.add(ab.scale(t)));
    }
}
