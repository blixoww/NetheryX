package com.netheryx.network;

import com.netheryx.world.NetherEventType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Serveur -> client : evenement du Nether en cours (pour le brouillard et les particules). */
public class NetherEventPacket {
    private final NetherEventType type;

    public NetherEventPacket(NetherEventType type) {
        this.type = type;
    }

    public NetherEventPacket(FriendlyByteBuf buf) {
        this.type = NetherEventType.byId(buf.readByte());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(this.type.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.netheryx.client.ClientNetherEvents.setCurrent(this.type));
        context.get().setPacketHandled(true);
    }
}
