package com.netheryx.client;

import com.netheryx.NetheryX;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Tremblement de camera des combats de boss : secousse qui s'amortit, respecte l'option d'accessibilite du jeu. */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientShake {
    private static float intensity;
    private static int remaining;
    private static int total = 1;

    private ClientShake() {
    }

    public static void start(float newIntensity, int ticks) {
        if (newIntensity >= intensity * ((float) remaining / total)) {
            intensity = newIntensity;
            remaining = ticks;
            total = Math.max(1, ticks);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remaining > 0 && !Minecraft.getInstance().isPaused()) {
            remaining--;
        }
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        if (remaining <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        // « Echelle des tremblements d'ecran » des options d'accessibilite : 0 desactive tout
        float scale = minecraft.options.screenEffectScale().get().floatValue();
        float time = (float) event.getPartialTick() + (minecraft.level != null ? minecraft.level.getGameTime() : 0L);
        float strength = intensity * scale * ((remaining - (float) event.getPartialTick()) / total);
        event.setPitch(event.getPitch() + Mth.sin(time * 1.9F) * strength);
        event.setYaw(event.getYaw() + Mth.cos(time * 2.3F) * strength * 0.7F);
        event.setRoll(event.getRoll() + Mth.sin(time * 1.3F) * strength * 0.6F);
    }
}
