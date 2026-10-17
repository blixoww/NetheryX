package com.netheryx.network;

import com.netheryx.NetheryX;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal reseau du mod (boussole des ames et pierre de rappel : listes envoyees au client et choix du joueur). */
public final class NetheryXNetwork {
    private static final String PROTOCOL = "4";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(NetheryX.id("main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private NetheryXNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenStructureSelectionPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenStructureSelectionPacket::encode)
                .decoder(OpenStructureSelectionPacket::new)
                .consumerMainThread(OpenStructureSelectionPacket::handle)
                .add();
        CHANNEL.messageBuilder(SelectStructurePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectStructurePacket::encode)
                .decoder(SelectStructurePacket::new)
                .consumerMainThread(SelectStructurePacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenRecallSelectionPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenRecallSelectionPacket::encode)
                .decoder(OpenRecallSelectionPacket::new)
                .consumerMainThread(OpenRecallSelectionPacket::handle)
                .add();
        CHANNEL.messageBuilder(SelectRecallPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectRecallPacket::encode)
                .decoder(SelectRecallPacket::new)
                .consumerMainThread(SelectRecallPacket::handle)
                .add();
        CHANNEL.messageBuilder(NetherEventPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NetherEventPacket::encode)
                .decoder(NetherEventPacket::new)
                .consumerMainThread(NetherEventPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenAdminCompassPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenAdminCompassPacket::encode)
                .decoder(OpenAdminCompassPacket::new)
                .consumerMainThread(OpenAdminCompassPacket::handle)
                .add();
        CHANNEL.messageBuilder(AdminTeleportPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AdminTeleportPacket::encode)
                .decoder(AdminTeleportPacket::new)
                .consumerMainThread(AdminTeleportPacket::handle)
                .add();
    }
}
