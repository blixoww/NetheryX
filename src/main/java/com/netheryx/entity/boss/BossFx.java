package com.netheryx.entity.boss;

import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.ScreenShakePacket;
import com.netheryx.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

/** Effets visuels des combats de boss : annonces plaquees au sol, anneaux, colonnes, tremblements de camera. */
public final class BossFx {
    /** Teintes des annonces au sol (lues par GroundDecalParticle). */
    public static final int FIRE = 0;
    public static final int ONYX = 1;
    public static final int SOUL = 2;
    public static final int GOLD = 3;

    /** Portee d'envoi : au-dela, la limite du client (32 blocs) ne s'applique pas grace au mode force. */
    private static final double SEND_RANGE = 64.0D;

    private BossFx() {
    }

    /** Toujours visible, meme avec le reglage « particules minimales » : une annonce d'attaque ne doit jamais manquer. */
    private static void forced(ServerLevel level, ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) < SEND_RANGE * SEND_RANGE) {
                level.sendParticles(player, particle, true, x, y, z, 0, vx, vy, vz, 1.0D);
            }
        }
    }

    /** Annonce au sol : contour fixe + remplissage qui grandit jusqu'a l'impact (le joueur lit le temps restant). */
    public static void telegraph(ServerLevel level, Vec3 center, double radius, int ticks, int color) {
        forced(level, ModParticles.TELEGRAPH_RING.get(), center.x, center.y + 0.06D, center.z, radius, ticks, color);
        forced(level, ModParticles.TELEGRAPH_FILL.get(), center.x, center.y + 0.05D, center.z, radius, ticks, color);
    }

    /** Onde : anneau plaque au sol qui s'etend de 0 a radius en ticks et s'efface. */
    public static void shockwave(ServerLevel level, Vec3 center, double radius, int ticks, int color) {
        forced(level, ModParticles.TELEGRAPH_RING.get(), center.x, center.y + 0.08D, center.z, radius, ticks, color + 10);
    }

    public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2.0D / count;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            level.sendParticles(particle, center.x + cos * radius, center.y, center.z + sin * radius, 0, cos, 0.0D, sin, speed);
        }
    }

    public static void column(ServerLevel level, ParticleOptions particle, Vec3 base, double height, int count, double spread) {
        for (int i = 0; i < count; i++) {
            level.sendParticles(particle, base.x, base.y + height * i / count, base.z, 1, spread, 0.1D, spread, 0.01D);
        }
    }

    /** Glyphes qui tournent et montent autour d'un point : le boss prepare quelque chose. */
    public static void runes(ServerLevel level, Vec3 center, double radius, int count, int tick) {
        for (int i = 0; i < count; i++) {
            double angle = tick * 0.15D + i * Math.PI * 2.0D / count;
            level.sendParticles(ModParticles.RUNE_GLYPH.get(), center.x + Math.cos(angle) * radius, center.y + 0.2D,
                    center.z + Math.sin(angle) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void shake(ServerLevel level, Vec3 center, double range, float intensity, int ticks) {
        for (ServerPlayer player : level.players()) {
            double distance = Math.sqrt(player.distanceToSqr(center));
            // Un joueur factice (machine d'un autre mod) n'a pas de connexion reseau
            if (distance < range && !(player instanceof FakePlayer)) {
                float scaled = intensity * (float) (1.0D - distance / range * 0.6D);
                NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ScreenShakePacket(scaled, ticks));
            }
        }
    }

    /** Sol le plus proche sous (ou juste au-dessus d') un point : les annonces restent plaquees sur l'arene. */
    public static Vec3 onGround(ServerLevel level, Vec3 point) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(point).mutable();
        for (int i = 0; i < 3 && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty(); i++) {
            pos.move(0, 1, 0);
        }
        for (int i = 0; i < 16; i++) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return new Vec3(point.x, pos.getY(), point.z);
            }
            pos.move(0, -1, 0);
        }
        return point;
    }
}
