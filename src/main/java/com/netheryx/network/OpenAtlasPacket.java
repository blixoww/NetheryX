package com.netheryx.network;

import com.netheryx.world.NetherAtlasData;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Serveur -> client : ce que le joueur a explore du Nether, pour afficher l'Atlas. */
public class OpenAtlasPacket {
    /** Plafond de securite (la limite d'un paquet est d'un mega-octet) : environ 2600 x 2600 blocs explores. */
    private static final int MAX_CELLS = 80000;

    private final Long2IntMap cells;
    private final List<String> biomes;
    private final List<Integer> colors;
    private final List<NetherAtlasData.Marker> markers;
    private final int playerX;
    private final int playerZ;

    public OpenAtlasPacket(Long2IntMap cells, List<String> biomes, List<Integer> colors, List<NetherAtlasData.Marker> markers, int playerX, int playerZ) {
        this.cells = cells;
        this.biomes = biomes;
        this.colors = colors;
        this.markers = markers;
        this.playerX = playerX;
        this.playerZ = playerZ;
    }

    public OpenAtlasPacket(FriendlyByteBuf buf) {
        this.playerX = buf.readInt();
        this.playerZ = buf.readInt();
        this.biomes = buf.readList(FriendlyByteBuf::readUtf);
        this.colors = buf.readList(FriendlyByteBuf::readInt);
        this.markers = buf.readList(b -> new NetherAtlasData.Marker(b.readInt(), b.readInt(), b.readUtf()));
        int count = buf.readVarInt();
        this.cells = new Long2IntOpenHashMap(count);
        for (int i = 0; i < count; i++) {
            long key = buf.readLong();
            this.cells.put(key, buf.readVarInt());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.playerX);
        buf.writeInt(this.playerZ);
        buf.writeCollection(this.biomes, FriendlyByteBuf::writeUtf);
        buf.writeCollection(this.colors, FriendlyByteBuf::writeInt);
        buf.writeCollection(this.markers, (b, marker) -> {
            b.writeInt(marker.x());
            b.writeInt(marker.z());
            b.writeUtf(marker.structure());
        });
        int count = Math.min(MAX_CELLS, this.cells.size());
        buf.writeVarInt(count);
        int written = 0;
        for (Long2IntMap.Entry entry : this.cells.long2IntEntrySet()) {
            if (written++ >= count) {
                break;
            }
            buf.writeLong(entry.getLongKey());
            buf.writeVarInt(entry.getIntValue());
        }
    }

    public int cellCount() {
        return this.cells.size();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.netheryx.client.ClientPacketHandler.openAtlas(this.cells, this.biomes,
                this.colors, this.markers, this.playerX, this.playerZ));
        context.get().setPacketHandled(true);
    }
}
