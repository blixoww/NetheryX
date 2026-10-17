package com.netheryx.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Etat persistant de l'evenement du Nether (sauvegarde avec la dimension). */
public class NetherEventData extends SavedData {
    private static final String NAME = "netheryx_nether_event";

    NetherEventType current = NetherEventType.NONE;
    int remaining;
    int cooldown = 20 * 60 * 10;

    public static NetherEventData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(NetherEventData::load, NetherEventData::new, NAME);
    }

    private static NetherEventData load(CompoundTag tag) {
        NetherEventData data = new NetherEventData();
        data.current = NetherEventType.byId(tag.getInt("Event"));
        data.remaining = tag.getInt("Remaining");
        data.cooldown = tag.getInt("Cooldown");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Event", this.current.ordinal());
        tag.putInt("Remaining", this.remaining);
        tag.putInt("Cooldown", this.cooldown);
        return tag;
    }

    public NetherEventType current() {
        return this.current;
    }
}
