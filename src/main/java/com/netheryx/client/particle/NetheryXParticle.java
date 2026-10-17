package com.netheryx.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Particule generique de NetheryX : un seul code, plusieurs comportements ({@link Style}).
 * La texture est animee sur la duree de vie (une image par etape du fichier {@code particles/<nom>.json}).
 */
public class NetheryXParticle extends TextureSheetParticle {
    public enum Style {
        EMBER, ASH, SOUL, GLINT, PUFF, POLLEN, SPORE
    }

    private static final int FULL_BRIGHT = 0xF000F0;

    private final SpriteSet sprites;
    private final Style style;
    private final float baseSize;
    private final float phase;

    protected NetheryXParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                               SpriteSet sprites, Style style) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.style = style;
        this.phase = this.random.nextFloat() * Mth.TWO_PI;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.hasPhysics = false;
        switch (style) {
            case EMBER -> {
                this.lifetime = 20 + this.random.nextInt(25);
                this.quadSize = 0.06F + this.random.nextFloat() * 0.05F;
                this.gravity = -0.015F;
                this.friction = 0.94F;
                this.xd += (this.random.nextDouble() - 0.5D) * 0.02D;
                this.zd += (this.random.nextDouble() - 0.5D) * 0.02D;
                this.yd += 0.01D + this.random.nextDouble() * 0.02D;
            }
            case ASH -> {
                this.lifetime = 60 + this.random.nextInt(50);
                this.quadSize = 0.05F + this.random.nextFloat() * 0.04F;
                this.gravity = 0.006F;
                this.friction = 0.96F;
                this.hasPhysics = true;
                float grey = 0.75F + this.random.nextFloat() * 0.25F;
                this.setColor(grey, grey, grey);
            }
            case SOUL -> {
                this.lifetime = 40 + this.random.nextInt(30);
                this.quadSize = 0.08F + this.random.nextFloat() * 0.06F;
                this.gravity = -0.004F;
                this.friction = 0.92F;
            }
            case GLINT -> {
                this.lifetime = 10 + this.random.nextInt(12);
                this.quadSize = 0.07F + this.random.nextFloat() * 0.05F;
                this.gravity = 0.0F;
                this.friction = 0.8F;
            }
            case PUFF -> {
                this.lifetime = 35 + this.random.nextInt(30);
                this.quadSize = 0.12F + this.random.nextFloat() * 0.08F;
                this.gravity = -0.008F;
                this.friction = 0.9F;
            }
            case POLLEN -> {
                this.lifetime = 80 + this.random.nextInt(60);
                this.quadSize = 0.03F + this.random.nextFloat() * 0.03F;
                this.gravity = 0.0F;
                this.friction = 0.97F;
                this.xd += (this.random.nextDouble() - 0.5D) * 0.01D;
                this.zd += (this.random.nextDouble() - 0.5D) * 0.01D;
            }
            case SPORE -> {
                this.lifetime = 60 + this.random.nextInt(40);
                this.quadSize = 0.04F + this.random.nextFloat() * 0.03F;
                this.gravity = 0.003F;
                this.friction = 0.96F;
                this.hasPhysics = true;
            }
        }
        this.baseSize = this.quadSize;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        this.setSpriteFromAge(this.sprites);
        float life = (float) this.age / (float) this.lifetime;
        float wave = Mth.sin(this.age * 0.15F + this.phase);
        switch (this.style) {
            case EMBER -> {
                this.xd += wave * 0.0015D;
                this.zd += Mth.cos(this.age * 0.15F + this.phase) * 0.0015D;
                this.alpha = 1.0F - life * life;
            }
            case ASH, SPORE -> {
                this.xd += wave * 0.002D;
                this.zd += Mth.cos(this.age * 0.11F + this.phase) * 0.002D;
                this.alpha = life > 0.8F ? (1.0F - life) * 5.0F : 1.0F;
            }
            case SOUL -> {
                this.xd += wave * 0.003D;
                this.zd += Mth.cos(this.age * 0.13F + this.phase) * 0.003D;
                this.alpha = Mth.sin(life * Mth.PI) * 0.9F;
            }
            case GLINT -> this.alpha = 1.0F;
            case PUFF -> this.alpha = 0.7F * (1.0F - life);
            case POLLEN -> {
                this.xd += wave * 0.0008D;
                this.yd += Mth.cos(this.age * 0.07F + this.phase) * 0.0006D;
                this.alpha = Mth.sin(life * Mth.PI);
            }
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float life = ((float) this.age + partialTick) / (float) this.lifetime;
        return switch (this.style) {
            case GLINT -> this.baseSize * Mth.sin(Mth.clamp(life, 0.0F, 1.0F) * Mth.PI);
            case PUFF -> this.baseSize * (1.0F + life * 1.5F);
            case EMBER -> this.baseSize * (1.0F - life * 0.6F);
            default -> this.baseSize;
        };
    }

    @Override
    public int getLightColor(float partialTick) {
        return switch (this.style) {
            case EMBER, SOUL, GLINT, POLLEN -> FULL_BRIGHT;
            default -> super.getLightColor(partialTick);
        };
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites, Style style) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new NetheryXParticle(level, x, y, z, dx, dy, dz, this.sprites, this.style);
        }
    }
}
