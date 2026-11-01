package com.netheryx.world;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ce que chaque joueur a explore du Nether (sauvegarde avec la dimension) : une grille de biomes par cases de {@link #CELL}
 * blocs, et les structures visitees. L'Atlas du Nether en fait une carte.
 */
public class NetherAtlasData extends SavedData {
    public static final int CELL = 32;
    /** Rayon decouvert autour du joueur, en cases. */
    private static final int REVEAL = 3;
    private static final String NAME = "netheryx_nether_atlas";

    public record Marker(int x, int z, String structure) {
    }

    /** Biomes connus du monde : les cases gardent un simple numero. */
    private final List<String> palette = new ArrayList<>();
    private final Map<String, Integer> paletteIndex = new HashMap<>();
    private final Map<UUID, Long2IntOpenHashMap> cells = new HashMap<>();
    private final Map<UUID, Map<Long, Marker>> markers = new HashMap<>();

    public static NetherAtlasData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(NetherAtlasData::load, NetherAtlasData::new, NAME);
    }

    private int biomeIndex(String biome) {
        return this.paletteIndex.computeIfAbsent(biome, key -> {
            this.palette.add(key);
            return this.palette.size() - 1;
        });
    }

    /** Note ce que le joueur voit autour de lui, et la structure qu'il traverse. */
    public void record(ServerLevel level, ServerPlayer player) {
        Long2IntOpenHashMap map = this.cells.computeIfAbsent(player.getUUID(), id -> new Long2IntOpenHashMap());
        int cx = Math.floorDiv(player.getBlockX(), CELL);
        int cz = Math.floorDiv(player.getBlockZ(), CELL);
        for (int dx = -REVEAL; dx <= REVEAL; dx++) {
            for (int dz = -REVEAL; dz <= REVEAL; dz++) {
                long key = ChunkPos.asLong(cx + dx, cz + dz);
                if (dx * dx + dz * dz <= REVEAL * REVEAL + 1 && !map.containsKey(key)) {
                    BlockPos center = new BlockPos((cx + dx) * CELL + CELL / 2, player.getBlockY(), (cz + dz) * CELL + CELL / 2);
                    String biome = level.getBiome(center).unwrapKey().map(k -> k.location().toString()).orElse("minecraft:nether_wastes");
                    map.put(key, this.biomeIndex(biome));
                    this.setDirty();
                }
            }
        }
        this.recordStructure(level, player);
    }

    private void recordStructure(ServerLevel level, ServerPlayer player) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        BlockPos pos = player.blockPosition();
        for (Structure structure : level.structureManager().getAllStructuresAt(pos).keySet()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structure);
            ResourceLocation id = registry.getKey(structure);
            if (start.isValid() && id != null) {
                Map<Long, Marker> known = this.markers.computeIfAbsent(player.getUUID(), key -> new LinkedHashMap<>());
                long key = start.getChunkPos().toLong();
                if (!known.containsKey(key)) {
                    known.put(key, new Marker(player.getBlockX(), player.getBlockZ(), id.toString()));
                    this.setDirty();
                }
            }
        }
    }

    public Long2IntMap cellsOf(UUID player) {
        return this.cells.getOrDefault(player, new Long2IntOpenHashMap());
    }

    public List<Marker> markersOf(UUID player) {
        return List.copyOf(this.markers.getOrDefault(player, Map.of()).values());
    }

    public List<String> palette() {
        return this.palette;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag names = new ListTag();
        this.palette.forEach(name -> names.add(StringTag.valueOf(name)));
        tag.put("Biomes", names);
        ListTag players = new ListTag();
        Map<UUID, Boolean> everyone = new HashMap<>();
        this.cells.keySet().forEach(id -> everyone.put(id, true));
        this.markers.keySet().forEach(id -> everyone.put(id, true));
        for (UUID id : everyone.keySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", id);
            Long2IntOpenHashMap map = this.cells.getOrDefault(id, new Long2IntOpenHashMap());
            long[] keys = new long[map.size()];
            int[] values = new int[map.size()];
            int i = 0;
            for (Long2IntMap.Entry e : map.long2IntEntrySet()) {
                keys[i] = e.getLongKey();
                values[i++] = e.getIntValue();
            }
            entry.putLongArray("Cells", keys);
            entry.putIntArray("Values", values);
            ListTag marks = new ListTag();
            for (Map.Entry<Long, Marker> e : this.markers.getOrDefault(id, Map.of()).entrySet()) {
                CompoundTag mark = new CompoundTag();
                mark.putLong("Key", e.getKey());
                mark.putInt("X", e.getValue().x());
                mark.putInt("Z", e.getValue().z());
                mark.putString("Structure", e.getValue().structure());
                marks.add(mark);
            }
            entry.put("Markers", marks);
            players.add(entry);
        }
        tag.put("Players", players);
        return tag;
    }

    private static NetherAtlasData load(CompoundTag tag) {
        NetherAtlasData data = new NetherAtlasData();
        for (Tag name : tag.getList("Biomes", Tag.TAG_STRING)) {
            data.biomeIndex(name.getAsString());
        }
        for (Tag t : tag.getList("Players", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            UUID id = entry.getUUID("Player");
            long[] keys = entry.getLongArray("Cells");
            int[] values = entry.getIntArray("Values");
            Long2IntOpenHashMap map = new Long2IntOpenHashMap();
            for (int i = 0; i < keys.length && i < values.length; i++) {
                map.put(keys[i], values[i]);
            }
            data.cells.put(id, map);
            Map<Long, Marker> known = new LinkedHashMap<>();
            for (Tag m : entry.getList("Markers", Tag.TAG_COMPOUND)) {
                CompoundTag mark = (CompoundTag) m;
                known.put(mark.getLong("Key"), new Marker(mark.getInt("X"), mark.getInt("Z"), mark.getString("Structure")));
            }
            data.markers.put(id, known);
        }
        return data;
    }
}
