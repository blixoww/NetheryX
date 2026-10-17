package com.netheryx.network;

import com.netheryx.util.PortalMemory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Serveur -> client : portails memorises, pour choisir la destination de la Pierre de rappel. */
public class OpenRecallSelectionPacket {
    private final List<PortalMemory.Portal> portals;

    public OpenRecallSelectionPacket(List<PortalMemory.Portal> portals) {
        this.portals = portals;
    }

    public OpenRecallSelectionPacket(FriendlyByteBuf buf) {
        this.portals = buf.readList(PortalMemory.Portal::read);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.portals, (b, portal) -> portal.write(b));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.netheryx.client.ClientPacketHandler.openRecallSelection(this.portals));
        context.get().setPacketHandled(true);
    }
}
