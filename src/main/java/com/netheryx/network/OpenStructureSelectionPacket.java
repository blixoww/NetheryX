package com.netheryx.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Serveur -> client : liste des structures localisables dans la dimension du joueur. */
public class OpenStructureSelectionPacket {
    private final List<ResourceLocation> structures;

    public OpenStructureSelectionPacket(List<ResourceLocation> structures) {
        this.structures = structures;
    }

    public OpenStructureSelectionPacket(FriendlyByteBuf buf) {
        this.structures = buf.readList(FriendlyByteBuf::readResourceLocation);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.structures, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.netheryx.client.ClientPacketHandler.openSoulCompass(this.structures));
        context.get().setPacketHandled(true);
    }
}
