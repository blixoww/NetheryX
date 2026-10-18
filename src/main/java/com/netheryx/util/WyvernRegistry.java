package com.netheryx.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registre des Wyvernes de cendre apprivoisees (sauvegarde avec le monde) : derniere dimension et position connues de
 * chacune. Le Sifflet de wyverne s'en sert pour retrouver une wyverne meme dans une zone non chargee ou une autre
 * dimension : on ne peut pas la perdre.
 */
public class WyvernRegistry extends SavedData {
    private static final String NAME = "netheryx_wyverns";

    public record Entry(UUID wyvern, UUID owner, ResourceKey<Level> dimension, BlockPos pos) {
    }

    private final Map<UUID, Entry> entries = new HashMap<>();

    public static WyvernRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(WyvernRegistry::load, WyvernRegistry::new, NAME);
    }

    public void update(UUID wyvern, UUID owner, ResourceKey<Level> dimension, BlockPos pos) {
        Entry previous = this.entries.get(wyvern);
        if (previous == null || !previous.owner().equals(owner) || !previous.dimension().equals(dimension) || !previous.pos().equals(pos)) {
            this.entries.put(wyvern, new Entry(wyvern, owner, dimension, pos.immutable()));
            this.setDirty();
        }
    }

    public void remove(UUID wyvern) {
        if (this.entries.remove(wyvern) != null) {
            this.setDirty();
        }
    }

    public List<Entry> ownedBy(UUID owner) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : this.entries.values()) {
            if (entry.owner().equals(owner)) {
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Entry entry : this.entries.values()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Wyvern", entry.wyvern());
            e.putUUID("Owner", entry.owner());
            e.putString("Dimension", entry.dimension().location().toString());
            e.put("Pos", NbtUtils.writeBlockPos(entry.pos()));
            list.add(e);
        }
        tag.put("Wyverns", list);
        return tag;
    }

    private static WyvernRegistry load(CompoundTag tag) {
        WyvernRegistry registry = new WyvernRegistry();
        for (Tag t : tag.getList("Wyverns", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) t;
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(e.getString("Dimension")));
            Entry entry = new Entry(e.getUUID("Wyvern"), e.getUUID("Owner"), dimension, NbtUtils.readBlockPos(e.getCompound("Pos")));
            registry.entries.put(entry.wyvern(), entry);
        }
        return registry;
    }
}
