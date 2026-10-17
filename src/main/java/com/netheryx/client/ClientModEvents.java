package com.netheryx.client;

import com.netheryx.NetheryX;
import com.netheryx.client.renderer.FireGhastRenderer;
import com.netheryx.client.renderer.NetheryXGeoRenderer;
import com.netheryx.client.renderer.OnyxCubeRenderer;
import com.netheryx.entity.AshWraith;
import com.netheryx.entity.BasaltBeetle;
import com.netheryx.entity.CinderHen;
import com.netheryx.entity.EmberCow;
import com.netheryx.entity.InfernalWarlock;
import com.netheryx.entity.SoulMoth;
import com.netheryx.entity.WitheredChampion;
import com.netheryx.entity.WitheredKnight;
import com.netheryx.entity.EmberStalker;
import com.netheryx.entity.MagmaCrab;
import com.netheryx.entity.OnyxSentinel;
import com.netheryx.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import com.netheryx.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Inscriptions cote client uniquement (rendus). */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Animation de tension de l'arc en onyx (memes predicats que l'arc vanilla)
            ItemProperties.register(ModItems.ONYX_BOW.get(), new ResourceLocation("pull"), (stack, level, entity, seed) ->
                    entity == null || entity.getUseItem() != stack ? 0.0F
                            : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
            ItemProperties.register(ModItems.ONYX_BOW.get(), new ResourceLocation("pulling"), (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EMBER_STALKER.get(),
                context -> new NetheryXGeoRenderer<EmberStalker>(context, "ember_stalker", 0.6F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.ASH_WRAITH.get(),
                context -> new NetheryXGeoRenderer<AshWraith>(context, "ash_wraith", 0.4F, 1.0F).translucent());
        event.registerEntityRenderer(ModEntities.ONYX_SENTINEL.get(),
                context -> new NetheryXGeoRenderer<OnyxSentinel>(context, "onyx_sentinel", 1.2F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.MAGMA_CRAB.get(),
                context -> new NetheryXGeoRenderer<MagmaCrab>(context, "magma_crab", 0.5F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.SOUL_MOTH.get(),
                context -> new NetheryXGeoRenderer<SoulMoth>(context, "soul_moth", 0.2F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.WITHERED_KNIGHT.get(),
                context -> new NetheryXGeoRenderer<WitheredKnight>(context, "withered_knight", 0.5F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.BASALT_BEETLE.get(),
                context -> new NetheryXGeoRenderer<BasaltBeetle>(context, "basalt_beetle", 0.7F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.INFERNAL_WARLOCK.get(),
                context -> new NetheryXGeoRenderer<InfernalWarlock>(context, "infernal_warlock", 0.5F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.WITHERED_CHAMPION.get(),
                context -> new NetheryXGeoRenderer<WitheredChampion>(context, "withered_champion", 0.9F, 1.4F).glowing());
        event.registerEntityRenderer(ModEntities.EMBER_COW.get(),
                context -> new NetheryXGeoRenderer<EmberCow>(context, "ember_cow", 0.7F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.CINDER_HEN.get(),
                context -> new NetheryXGeoRenderer<CinderHen>(context, "cinder_hen", 0.3F, 1.0F).glowing());
        event.registerEntityRenderer(ModEntities.FIRE_GHAST.get(), FireGhastRenderer::new);
        event.registerEntityRenderer(ModEntities.ONYX_CUBE.get(), OnyxCubeRenderer::new);
        event.registerEntityRenderer(ModEntities.SULFUR_BOMB.get(), ThrownItemRenderer::new);
    }
}
