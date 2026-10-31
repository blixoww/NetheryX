package com.netheryx.proxy;

import com.netheryx.NetheryX;
import com.netheryx.client.RenderGeoMob;
import com.netheryx.entity.*;
import com.netheryx.registry.ModItems;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Rendus des creatures (modeles convertis depuis la 1.20.1) et ecrans propres au client. */
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        geo(EntityEmberStalker.class, "ember_stalker", 0.6F, 1.0F, true);
        geo(EntityAshWraith.class, "ash_wraith", 0.4F, 1.0F, false);
        geo(EntityWitheredKnight.class, "withered_knight", 0.5F, 1.0F, true);
        geo(EntityBasaltBurrower.class, "basalt_burrower", 0.6F, 1.0F, true);
        geo(EntityMimic.class, "mimic", 0.5F, 1.0F, true);
        geo(EntityEmberCow.class, "ember_cow", 0.7F, 1.0F, true);
        geo(EntityCinderHen.class, "cinder_hen", 0.3F, 1.0F, true);
        geo(EntityBlazePig.class, "blaze_pig", 0.5F, 1.0F, true);
        geo(EntityCrystalSnail.class, "crystal_snail", 0.4F, 1.0F, true);
        geo(EntityMagmaCrab.class, "magma_crab", 0.5F, 1.0F, true);
        geo(EntityBasaltBeetle.class, "basalt_beetle", 0.6F, 1.0F, true);
        geo(EntityMagmafish.class, "magmafish", 0.3F, 1.0F, true);
        geo(EntityLanternBearer.class, "lantern_bearer", 0.4F, 1.0F, true);
        geo(EntityCinderLizard.class, "cinder_lizard", 0.7F, 1.0F, true);
        geo(EntityPiglinMerchant.class, "piglin_merchant", 0.5F, 1.0F, true);
        geo(EntityEmberFirefly.class, "ember_firefly", 0.0F, 1.1F, true);
        geo(EntitySoulFirefly.class, "soul_firefly", 0.0F, 1.1F, true);
        geo(EntitySoulMoth.class, "soul_moth", 0.0F, 1.0F, true);
        geo(EntityAshWyvern.class, "ash_wyvern", 1.6F, 1.0F, true);
        geo(EntityOnyxSentinel.class, "onyx_sentinel", 1.0F, 1.0F, true);
        geo(EntityInfernalWarlock.class, "infernal_warlock", 0.6F, 1.0F, true);
        geo(EntityWitheredChampion.class, "withered_champion", 0.6F, 1.0F, true);
        geo(EntityBasaltGolem.class, "basalt_golem", 1.0F, 1.0F, true);
        geo(EntityNetherHeart.class, "nether_heart", 1.2F, 1.0F, true);
        RenderingRegistry.registerEntityRenderingHandler(EntityFireGhast.class, new RenderGhast() {
            private final ResourceLocation idle = new ResourceLocation(NetheryX.MOD_ID, "textures/entity/fire_ghast.png");
            private final ResourceLocation shooting = new ResourceLocation(NetheryX.MOD_ID, "textures/entity/fire_ghast_shooting.png");

            @Override
            protected ResourceLocation getEntityTexture(Entity entity) {
                return ((EntityGhast) entity).func_110182_bF() ? this.shooting : this.idle;
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityOnyxCube.class, new RenderSlime(new ModelSlime(16), new ModelSlime(0), 0.25F) {
            private final ResourceLocation texture = new ResourceLocation(NetheryX.MOD_ID, "textures/entity/onyx_cube.png");

            @Override
            protected ResourceLocation getEntityTexture(Entity entity) {
                return this.texture;
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntitySulfurBomb.class, new RenderSnowball(ModItems.sulfurBomb));
    }

    private static void geo(Class<? extends Entity> type, String name, float shadow, float scale, boolean glow) {
        Render render = new RenderGeoMob(name, shadow, scale, glow);
        RenderingRegistry.registerEntityRenderingHandler(type, render);
    }

    @Override
    public void openCodex(EntityPlayer player, ItemStack book) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBook(player, book, false));
    }
}
