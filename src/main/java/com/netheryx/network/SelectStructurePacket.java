package com.netheryx.network;

import com.netheryx.item.SoulCompassItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> serveur : structure choisie dans l'interface de la boussole des ames. */
public class SelectStructurePacket {
    private final ResourceLocation structure;

    public SelectStructurePacket(ResourceLocation structure) {
        this.structure = structure;
    }

    public SelectStructurePacket(FriendlyByteBuf buf) {
        this.structure = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.structure);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            SoulCompassItem.locateFor(player, this.structure);
        }
        context.get().setPacketHandled(true);
    }
}
