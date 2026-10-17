package com.netheryx.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.netheryx.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

/**
 * Structure "jigsaw" posee sur le sol du Nether.
 * <p>
 * Les heightmaps sont inutilisables dans le Nether (plafond de bedrock) : on parcourt donc la colonne de bruit
 * du centre du chunk, du haut vers le bas, pour trouver une poche d'air assez haute au-dessus d'un sol solide.
 * Tout le reste (pieces, biomes, espacement) est pilote par les JSON de {@code data/netheryx/worldgen}.
 */
public class NetherFloorStructure extends Structure {
    public static final Codec<NetherFloorStructure> CODEC = RecordCodecBuilder.<NetherFloorStructure>mapCodec(instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
            Codec.intRange(0, 7).fieldOf("size").forGetter(structure -> structure.size),
            Codec.intRange(32, 120).optionalFieldOf("max_y", 100).forGetter(structure -> structure.maxY),
            Codec.intRange(31, 120).optionalFieldOf("min_y", 32).forGetter(structure -> structure.minY),
            Codec.intRange(1, 32).optionalFieldOf("required_air", 8).forGetter(structure -> structure.requiredAir),
            Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center", 64).forGetter(structure -> structure.maxDistanceFromCenter),
            Codec.intRange(0, 32).optionalFieldOf("sink", 0).forGetter(structure -> structure.sink),
            Codec.intRange(0, 64).optionalFieldOf("footprint", 0).forGetter(structure -> structure.footprint)
    ).apply(instance, NetherFloorStructure::new)).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final int maxY;
    private final int minY;
    private final int requiredAir;
    private final int maxDistanceFromCenter;
    /** Nombre de blocs dont la structure est enfoncee sous le sol (donjons souterrains). */
    private final int sink;
    /** Taille de la structure : le sol doit exister a une hauteur proche sur toute son emprise (0 = pas de verification). */
    private final int footprint;

    /** Ecart de hauteur tolere entre le sol au centre et le sol sous chaque quart de la structure. */
    private static final int FLOOR_TOLERANCE = 7;

    public NetherFloorStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
                                Optional<ResourceLocation> startJigsawName, int size, int maxY, int minY,
                                int requiredAir, int maxDistanceFromCenter, int sink, int footprint) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.maxY = maxY;
        this.minY = minY;
        this.requiredAir = requiredAir;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.sink = sink;
        this.footprint = footprint;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());

        int airRun = 0;
        for (int y = this.maxY; y >= this.minY; y--) {
            BlockState state = column.getBlock(y);
            if (state.isAir()) {
                airRun++;
                continue;
            }
            if (airRun >= this.requiredAir && state.getFluidState().isEmpty()) {
                if (!this.fitsFootprint(context, x, y, z)) {
                    // Sol trop accidente (ravin, falaise, lac de lave) : on n'installe pas la structure ici.
                    return Optional.empty();
                }
                BlockPos start = new BlockPos(x, y + 1 - this.sink, z);
                return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size, start,
                        false, Optional.empty(), this.maxDistanceFromCenter);
            }
            airRun = 0;
        }
        return Optional.empty();
    }

    /**
     * La piece s'etend dans un des quatre quarts autour du point de depart selon sa rotation (aleatoire).
     * On verifie donc qu'un sol existe, a une hauteur proche, au centre de chacun des quatre quarts.
     */
    private boolean fitsFootprint(GenerationContext context, int x, int floorY, int z) {
        if (this.footprint <= 0) {
            return true;
        }
        int half = this.footprint / 2;
        for (int dx : new int[]{-half, half}) {
            for (int dz : new int[]{-half, half}) {
                NoiseColumn column = context.chunkGenerator().getBaseColumn(x + dx, z + dz, context.heightAccessor(), context.randomState());
                if (!hasFloorNear(column, floorY)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasFloorNear(NoiseColumn column, int floorY) {
        for (int y = floorY - FLOOR_TOLERANCE; y <= floorY + FLOOR_TOLERANCE; y++) {
            BlockState state = column.getBlock(y);
            if (!state.isAir() && state.getFluidState().isEmpty() && column.getBlock(y + 1).isAir()
                    && column.getBlock(y + 2).isAir()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.NETHER_FLOOR.get();
    }
}
