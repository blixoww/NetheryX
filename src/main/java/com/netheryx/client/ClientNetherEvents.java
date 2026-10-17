package com.netheryx.client;

import com.netheryx.NetheryX;
import com.netheryx.item.SetBonusArmorItem;
import com.netheryx.registry.ModArmorMaterials;
import com.netheryx.registry.ModEffects;
import com.netheryx.registry.ModParticles;
import com.netheryx.world.NetherEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Rendu cote client des evenements du Nether (brouillard, couleur du ciel, particules autour de la camera)
 * et de la vision de lave.
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, value = Dist.CLIENT)
public final class ClientNetherEvents {
    private static NetherEventType current = NetherEventType.NONE;
    /** Intensite lissee de 0 a 1 pour des transitions douces. */
    private static float intensity;
    private static float intensityO;
    private static boolean pendingEnd;

    private ClientNetherEvents() {
    }

    public static void setCurrent(NetherEventType type) {
        if (type != NetherEventType.NONE) {
            current = type;
        } else if (current != NetherEventType.NONE) {
            // L'intensite redescend avant que l'evenement ne disparaisse completement
            pendingEnd = true;
        }
    }

    private static boolean inNether() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.level.dimension() == Level.NETHER;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        intensityO = intensity;
        boolean active = current != NetherEventType.NONE && !pendingEnd && inNether();
        intensity = Mth.approach(intensity, active ? 1.0F : 0.0F, 0.01F);
        if (pendingEnd && intensity <= 0.0F) {
            current = NetherEventType.NONE;
            pendingEnd = false;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.isPaused() || intensity <= 0.0F) {
            return;
        }
        ParticleOptions particle = switch (current) {
            case ASHFALL -> ModParticles.ASH_FLAKE.get();
            case SULFUR_STORM -> ModParticles.SULFUR_PUFF.get();
            case ERUPTION -> ModParticles.EMBER_SPARK.get();
            default -> null;
        };
        if (particle == null) {
            return;
        }
        RandomSource random = minecraft.level.random;
        int count = Mth.ceil(intensity * (current == NetherEventType.ASHFALL ? 12 : 5));
        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() - 0.5D) * 24.0D;
            double y = player.getY() + random.nextDouble() * 10.0D - 2.0D;
            double z = player.getZ() + (random.nextDouble() - 0.5D) * 24.0D;
            minecraft.level.addParticle(particle, x, y, z, 0.0D, current == NetherEventType.ASHFALL ? -0.03D : 0.01D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        FogType fluid = event.getCamera().getFluidInCamera();
        if (fluid == FogType.LAVA && player.hasEffect(ModEffects.LAVA_VISION.get())) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(24.0F);
            event.setCanceled(true);
            return;
        }
        if (fluid != FogType.NONE || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN || !inNether()) {
            return;
        }
        float strength = Mth.lerp((float) event.getPartialTick(), intensityO, intensity);
        if (strength <= 0.0F || current == NetherEventType.ERUPTION) {
            return;
        }
        if (current == NetherEventType.ASHFALL && SetBonusArmorItem.hasFullSet(player, ModArmorMaterials.WRAITH)) {
            return;
        }
        float target = current == NetherEventType.ASHFALL ? 20.0F : 48.0F;
        float far = Mth.lerp(strength, event.getFarPlaneDistance(), Math.min(event.getFarPlaneDistance(), target));
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), far * 0.1F));
        event.setFarPlaneDistance(far);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!inNether() || current == NetherEventType.NONE || event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }
        float strength = Mth.lerp((float) event.getPartialTick(), intensityO, intensity) * 0.6F;
        int color = current.fogColor();
        event.setRed(Mth.lerp(strength, event.getRed(), ((color >> 16) & 0xFF) / 255.0F));
        event.setGreen(Mth.lerp(strength, event.getGreen(), ((color >> 8) & 0xFF) / 255.0F));
        event.setBlue(Mth.lerp(strength, event.getBlue(), (color & 0xFF) / 255.0F));
    }
}
