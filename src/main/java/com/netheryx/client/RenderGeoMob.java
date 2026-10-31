package com.netheryx.client;

import com.netheryx.NetheryX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Rendu d'une creature a modele converti : texture, zones lumineuses (calque additif), echelle, barre de boss. */
public class RenderGeoMob extends RenderLiving {
    private final ResourceLocation texture;
    private final ResourceLocation glow;
    private final float scale;

    public RenderGeoMob(String name, float shadow, float scale, boolean glow) {
        super(new GeoModel(name), shadow * scale);
        this.texture = new ResourceLocation(NetheryX.MOD_ID, "textures/entity/" + name + ".png");
        this.glow = glow ? new ResourceLocation(NetheryX.MOD_ID, "textures/entity/" + name + "_glowmask.png") : null;
        this.scale = scale;
        setRenderPassModel(this.mainModel);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return this.texture;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        float s = this.scale * (entity.isChild() ? 0.5F : 1.0F);
        GL11.glScalef(s, s, s);
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int pass, float partialTicks) {
        if (pass != 0 || this.glow == null) {
            return -1;
        }
        bindTexture(this.glow);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glDepthMask(!entity.isInvisible());
        int light = 61680;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        return 1;
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
        if (entity instanceof IBossDisplayData) {
            BossStatus.setBossStatus((IBossDisplayData) entity, true);
        }
        super.doRender(entity, x, y, z, yaw, partialTicks);
    }
}
