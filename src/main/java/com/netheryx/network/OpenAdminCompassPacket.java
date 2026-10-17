package com.netheryx.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Serveur -> client : structures et biomes de la dimension, pour la Boussole du createur. */
public class OpenAdminCompassPacket {
    private final List<ResourceLocation> structures;
    private final List<ResourceLocation> biomes;

    public OpenAdminCompassPacket(List<ResourceLocation> structures, List<ResourceLocation> biomes) {
        this.structures = structures;
        this.biomes = biomes;
    }

    public OpenAdminCompassPacket(FriendlyByteBuf buf) {
        this.structures = buf.readList(FriendlyByteBuf::readResourceLocation);
        this.biomes = buf.readList(FriendlyByteBuf::readResourceLocation);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.structures, FriendlyByteBuf::writeResourceLocation);
        buf.writeCollection(this.biomes, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.netheryx.client.ClientPacketHandler.openAdminCompass(this.structures, this.biomes));
        context.get().setPacketHandled(true);
    }
}
