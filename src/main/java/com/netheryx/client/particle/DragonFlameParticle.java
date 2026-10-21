package com.netheryx.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Jet de flammes de la wyverne : de grandes bouffees de feu qui jaillissent de la gueule, gonflent, tournoient et se
 * dechirent en rougissant, sur un coeur blanc incandescent, puis laissent une fumee qui monte. Les flammes sont dessinees
 * en melange additif : elles s'ajoutent a la lumiere de la scene et brillent comme un vrai brasier.
 */
public class DragonFlameParticle extends TextureSheetParticle {
    public enum Kind {
        FLAME, CORE, SMOKE
    }

    private static final int FULL_BRIGHT = 0xF000F0;

    /** Melange additif (source * alpha + destination), sans ecriture de profondeur. */
    public static final ParticleRenderType ADDITIVE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textures) {
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "NETHERYX_ADDITIVE";
        }
    };

    private final SpriteSet sprites;
    private final Kind kind;
    private final float startSize;
    private final float endSize;
    private final float spin;

    protected DragonFlameParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                  SpriteSet sprites, Kind kind) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.kind = kind;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.spin = (this.random.nextFloat() - 0.5F) * 0.3F;
        switch (kind) {
            case FLAME -> {
                this.lifetime = 16 + this.random.nextInt(9);
                this.startSize = 0.3F + this.random.nextFloat() * 0.2F;
                this.endSize = 1.9F + this.random.nextFloat() * 0.9F;
                this.friction = 0.9F;
                this.gravity = -0.25F;
                this.hasPhysics = true;
                float tint = 0.85F + this.random.nextFloat() * 0.15F;
                this.setColor(1.0F, tint, tint * 0.9F);
            }
            case CORE -> {
                this.lifetime = 6 + this.random.nextInt(4);
                this.startSize = 0.25F + this.random.nextFloat() * 0.1F;
                this.endSize = 0.7F + this.random.nextFloat() * 0.3F;
                this.friction = 0.88F;
                this.gravity = 0.0F;
                this.hasPhysics = true;
            }
            default -> {
                this.lifetime = 30 + this.random.nextInt(18);
                this.startSize = 0.6F + this.random.nextFloat() * 0.3F;
                this.endSize = 2.6F + this.random.nextFloat() * 1.2F;
                this.friction = 0.86F;
                this.gravity = -0.45F;
                this.hasPhysics = true;
            }
        }
        this.quadSize = this.startSize;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        if (this.removed) {
            return;
        }
        this.setSpriteFromAge(this.sprites);
        float life = (float) this.age / (float) this.lifetime;
        this.roll += this.spin * (1.0F - life * 0.5F);
        switch (this.kind) {
            case FLAME -> this.alpha = life < 0.6F ? 1.0F : (1.0F - life) / 0.4F;
            case CORE -> this.alpha = 1.0F - life * life;
            default -> this.alpha = 0.6F * Mth.sin(Mth.clamp(life, 0.0F, 1.0F) * Mth.PI);
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float life = Mth.clamp(((float) this.age + partialTick) / (float) this.lifetime, 0.0F, 1.0F);
        // Gonfle vite a la sortie de la gueule, puis de plus en plus lentement
        return Mth.lerp((float) Math.sqrt(life), this.startSize, this.endSize);
    }

    @Override
    public int getLightColor(float partialTick) {
        return this.kind == Kind.SMOKE ? super.getLightColor(partialTick) : FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.kind == Kind.SMOKE ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ADDITIVE;
    }

    public record Provider(SpriteSet sprites, Kind kind) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new DragonFlameParticle(level, x, y, z, dx, dy, dz, this.sprites, this.kind);
        }
    }
}
