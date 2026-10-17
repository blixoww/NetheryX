package com.netheryx.network;

import com.netheryx.NetheryX;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal reseau du mod (boussole des ames : liste des structures et choix du joueur). */
public final class NetheryXNetwork {
    private static final String PROTOCOL = "1";
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
    }
}
