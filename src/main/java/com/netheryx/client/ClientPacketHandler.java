package com.netheryx.client;

import com.netheryx.client.screen.SoulCompassScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Code client appele par les paquets reseau (isole pour ne jamais etre charge cote serveur dedie). */
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void openSoulCompass(List<ResourceLocation> structures) {
        Minecraft.getInstance().setScreen(new SoulCompassScreen(structures));
    }
}
