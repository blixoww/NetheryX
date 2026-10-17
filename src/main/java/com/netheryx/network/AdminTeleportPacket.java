package com.netheryx.network;

import com.netheryx.item.AdminCompassItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> serveur : destination choisie dans la Boussole du createur. */
public class AdminTeleportPacket {
    private final ResourceLocation target;
    private final boolean biome;

    public AdminTeleportPacket(ResourceLocation target, boolean biome) {
        this.target = target;
        this.biome = biome;
    }

    public AdminTeleportPacket(FriendlyByteBuf buf) {
        this.target = buf.readResourceLocation();
        this.biome = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.target);
        buf.writeBoolean(this.biome);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            AdminCompassItem.teleport(player, this.target, this.biome);
        }
        context.get().setPacketHandled(true);
    }
}
