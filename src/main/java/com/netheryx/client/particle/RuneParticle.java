package com.netheryx.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

/** Glyphe lumineux qui monte en s'estompant : un boss se prepare, un sceau reagit. */
public class RuneParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 0xF000F0;

    protected RuneParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.lifetime = 22 + this.random.nextInt(12);
        this.quadSize = 0.16F + this.random.nextFloat() * 0.08F;
        this.gravity = -0.012F;
        this.friction = 0.96F;
        this.hasPhysics = false;
        this.xd = 0.0D;
        this.yd = 0.015D;
        this.zd = 0.0D;
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        super.tick();
        float progress = (float) this.age / this.lifetime;
        this.alpha = progress < 0.2F ? progress * 5.0F : 1.0F - (progress - 0.2F) / 0.8F;
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new RuneParticle(level, x, y, z, this.sprites);
        }
    }
}
