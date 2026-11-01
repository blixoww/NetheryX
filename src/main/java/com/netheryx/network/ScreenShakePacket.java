package com.netheryx.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Serveur -> client : fait trembler la camera (impacts et phases de boss). */
public class ScreenShakePacket {
    private final float intensity;
    private final int ticks;

    public ScreenShakePacket(float intensity, int ticks) {
        this.intensity = intensity;
        this.ticks = ticks;
    }

    public ScreenShakePacket(FriendlyByteBuf buf) {
        this.intensity = buf.readFloat();
        this.ticks = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(this.intensity);
        buf.writeVarInt(this.ticks);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.netheryx.client.ClientShake.start(this.intensity, this.ticks));
        context.get().setPacketHandled(true);
    }
}
