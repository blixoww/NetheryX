package com.netheryx.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Decalcomanie plaquee au sol, le langage visuel des attaques de boss. La « vitesse » de la particule transporte ses
 * reglages : x = rayon, y = duree en ticks, z = teinte (+ 10 pour une onde qui s'etend et s'efface).
 * <ul>
 *     <li>contour : pulse sur place pendant toute l'annonce ;</li>
 *     <li>remplissage : grandit de 0 au rayon, l'impact tombe quand il touche le contour.</li>
 * </ul>
 */
public class GroundDecalParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float[][] TINTS = {
            {1.0F, 0.32F, 0.08F}, // feu
            {0.68F, 0.36F, 1.0F}, // onyx
            {0.30F, 0.92F, 1.0F}, // ames
            {1.0F, 0.78F, 0.25F}  // or
    };

    private final float radius;
    private final boolean fill;
    private final boolean wave;

    protected GroundDecalParticle(ClientLevel level, double x, double y, double z, double radius, double ticks, double code,
                                  SpriteSet sprites, boolean fill) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.radius = (float) radius;
        this.lifetime = Math.max(1, (int) Math.round(ticks));
        this.fill = fill;
        int value = (int) Math.round(code);
        this.wave = value >= 10;
        float[] tint = TINTS[Mth.clamp(value % 10, 0, TINTS.length - 1)];
        this.setColor(tint[0], tint[1], tint[2]);
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        float progress = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        float size;
        float opacity;
        if (this.wave) {
            size = this.radius * progress;
            opacity = 1.0F - progress * progress;
        } else if (this.fill) {
            size = this.radius * progress;
            opacity = 0.25F + progress * 0.4F;
        } else {
            size = this.radius;
            // Le contour bat de plus en plus vite a l'approche de l'impact
            opacity = 0.65F + 0.35F * Mth.sin((this.age + partialTick) * (0.35F + progress * 0.9F));
        }
        Vec3 cam = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y);
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        // Quad horizontal, dans les deux sens pour rester visible d'en dessous
        this.vertex(consumer, x - size, y, z - size, u0, v0, opacity);
        this.vertex(consumer, x - size, y, z + size, u0, v1, opacity);
        this.vertex(consumer, x + size, y, z + size, u1, v1, opacity);
        this.vertex(consumer, x + size, y, z - size, u1, v0, opacity);
        this.vertex(consumer, x + size, y, z - size, u1, v0, opacity);
        this.vertex(consumer, x + size, y, z + size, u1, v1, opacity);
        this.vertex(consumer, x - size, y, z + size, u0, v1, opacity);
        this.vertex(consumer, x - size, y, z - size, u0, v0, opacity);
    }

    private void vertex(VertexConsumer consumer, float x, float y, float z, float u, float v, float opacity) {
        consumer.vertex(x, y, z).uv(u, v).color(this.rCol, this.gCol, this.bCol, opacity).uv2(FULL_BRIGHT).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites, boolean fill) implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double radius, double ticks, double code) {
            return new GroundDecalParticle(level, x, y, z, radius, ticks, code, this.sprites, this.fill);
        }
    }
}
