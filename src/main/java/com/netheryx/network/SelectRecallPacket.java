package com.netheryx.network;

import com.netheryx.item.RecallStoneItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> serveur : choix fait dans l'interface de la Pierre de rappel.
 * {@code index} = position du portail dans la liste, ou {@link #RESPAWN} pour le point de reapparition ;
 * {@code forget} = oublier ce portail au lieu de s'y rendre.
 */
public class SelectRecallPacket {
    public static final int RESPAWN = -1;

    private final int index;
    private final boolean forget;

    public SelectRecallPacket(int index, boolean forget) {
        this.index = index;
        this.forget = forget;
    }

    public SelectRecallPacket(FriendlyByteBuf buf) {
        this.index = buf.readVarInt();
        this.forget = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.index);
        buf.writeBoolean(this.forget);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            RecallStoneItem.handleSelection(player, this.index, this.forget);
        }
        context.get().setPacketHandled(true);
    }
}
