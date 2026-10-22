package com.netheryx.worldgen.structure;

import com.netheryx.NetheryX;
import com.netheryx.registry.ModStructureTypes;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Remet les structures NetheryX en etat une fois leur terrain termine.
 * <p>
 * Une structure est posee a la derniere etape de decoration de son chunk, mais les chunks voisins decores ensuite
 * peuvent encore deborder dedans (feuillage des arbres, colonnes et deltas de basalte...). Quand un chunk est charge
 * pour de bon, tous ses voisins ont fini leur decoration : on y repose alors le gabarit de chaque structure (blocs et
 * air, sans les creatures), une seule fois. Seuls les chunks jamais frequentes par un joueur sont concernes, pour ne
 * jamais toucher a une structure deja exploree (coffres pilles, constructions).
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class StructureRestorer {
    /** Un chunk ou un joueur est passe plus longtemps que cela (en ticks) n'est plus restaure. */
    private static final long MAX_INHABITED = 100L;
    private static final int CHUNKS_PER_TICK = 4;
    private static final Queue<Pending> PENDING = new ConcurrentLinkedQueue<>();

    private record Pending(ResourceKey<Level> dimension, ChunkPos pos) {
    }

    private StructureRestorer() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)
                || chunk.getInhabitedTime() > MAX_INHABITED || !hasOurStructure(chunk)) {
            return;
        }
        PENDING.add(new Pending(level.dimension(), chunk.getPos()));
    }

    private static boolean hasOurStructure(LevelChunk chunk) {
        for (Map.Entry<Structure, LongSet> entry : chunk.getAllReferences().entrySet()) {
            if (entry.getKey().type() == ModStructureTypes.NETHER_FLOOR.get() && !entry.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (int i = 0; i < CHUNKS_PER_TICK && !PENDING.isEmpty(); i++) {
            Pending pending = PENDING.poll();
            ServerLevel level = server.getLevel(pending.dimension());
            ChunkPos pos = pending.pos();
            if (level == null || !level.hasChunk(pos.x, pos.z)) {
                continue;
            }
            Restored restored = Restored.get(level);
            if (restored.chunks.contains(pos.toLong())) {
                continue;
            }
            LevelChunk chunk = level.getChunk(pos.x, pos.z);
            if (chunk.getInhabitedTime() <= MAX_INHABITED) {
                restore(level, chunk);
            }
            restored.chunks.add(pos.toLong());
            restored.setDirty();
        }
    }

    /** Repose, dans ce chunk seulement, le gabarit de chaque structure NetheryX qui le traverse. */
    private static void restore(ServerLevel level, LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        BoundingBox chunkBox = new BoundingBox(pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(),
                pos.getMaxBlockX(), level.getMaxBuildHeight() - 1, pos.getMaxBlockZ());
        var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (StructureStart start : level.structureManager().startsForStructure(pos,
                structure -> structure.type() == ModStructureTypes.NETHER_FLOOR.get())) {
            ResourceLocation key = structures.getKey(start.getStructure());
            if (key == null || !start.isValid()) {
                continue;
            }
            // Chaque structure NetheryX n'a qu'un gabarit : netheryx:<nom>/main
            Optional<StructureTemplate> template = level.getServer().getStructureManager()
                    .get(new ResourceLocation(key.getNamespace(), key.getPath() + "/main"));
            if (template.isEmpty()) {
                continue;
            }
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof PoolElementStructurePiece pool && piece.getBoundingBox().intersects(chunkBox)) {
                    StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(pool.getRotation())
                            .setBoundingBox(chunkBox).setKnownShape(true).setIgnoreEntities(true);
                    template.get().placeInWorld(level, pool.getPosition(), pool.getPosition(), settings, level.getRandom(),
                            Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** Chunks deja restaures (sauvegarde par dimension) : la restauration n'a lieu qu'une fois. */
    private static final class Restored extends SavedData {
        private static final String NAME = "netheryx_restored_structures";
        private final LongSet chunks = new LongOpenHashSet();

        static Restored get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(Restored::load, Restored::new, NAME);
        }

        private static Restored load(CompoundTag tag) {
            Restored restored = new Restored();
            for (long value : tag.getLongArray("Chunks")) {
                restored.chunks.add(value);
            }
            return restored;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putLongArray("Chunks", this.chunks.toLongArray());
            return tag;
        }
    }
}
