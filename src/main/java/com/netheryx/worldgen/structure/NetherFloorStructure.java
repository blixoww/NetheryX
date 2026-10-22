package com.netheryx.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.netheryx.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

/**
 * Structure posee sur le sol du Nether (une seule piece, tiree du {@code start_pool}).
 * <p>
 * Les heightmaps sont inutilisables dans le Nether (plafond de bedrock) : on parcourt la colonne de bruit du centre du
 * chunk, du haut vers le bas, a la recherche d'un sol solide sous une poche d'air. Pour chaque sol candidat, la piece
 * (dans sa rotation tiree au hasard) est centree sur le chunk et on verifie <b>toute son emprise</b> : sur une grille de
 * colonnes, le sol doit etre a une hauteur proche et tout le volume de la structure (plus une marge au-dessus) doit
 * etre degage. Sinon on essaie le sol suivant plus bas. Plus de structure enterree, coupee par un plafond ou un mur de
 * roche, ou suspendue au-dessus d'un ravin.
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
    // start_jigsaw_name, size, max_distance_from_center et footprint restent lus pour la compatibilite des JSON, mais
    // la piece unique est maintenant placee et verifiee directement d'apres son emprise reelle.
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final int maxY;
    private final int minY;
    private final int requiredAir;
    private final int maxDistanceFromCenter;
    /** Nombre de blocs dont la structure est enfoncee sous le sol (donjons souterrains). */
    private final int sink;
    private final int footprint;

    /** Le sol de chaque colonne de l'emprise doit etre a cette distance (en blocs) du sol au centre. */
    private static final int FLOOR_TOLERANCE = 3;
    /** Marge d'air exigee au-dessus du haut de la structure (pas de plafond qui la coupe). */
    private static final int HEADROOM = 3;
    /** Part minimale des colonnes de l'emprise qui doivent avoir un sol, et un volume degage. */
    private static final double GROUNDED_SHARE = 0.85D;
    private static final double OPEN_SHARE = 0.85D;

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
        WorldgenRandom random = context.random();
        Rotation rotation = Rotation.getRandom(random);
        StructurePoolElement element = this.startPool.value().getRandomTemplate(random);
        StructureTemplateManager templates = context.structureTemplateManager();
        // Emprise de la piece tournee, posee a l'origine : on la decale pour la centrer sur le chunk
        BoundingBox local = element.getBoundingBox(templates, BlockPos.ZERO, rotation);
        int offsetX = x - (local.minX() + local.maxX()) / 2;
        int offsetZ = z - (local.minZ() + local.maxZ()) / 2;
        int above = Math.max(2, local.getYSpan() - this.sink);

        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        int airRun = 0;
        for (int y = this.maxY; y >= this.minY; y--) {
            BlockState state = column.getBlock(y);
            if (state.isAir()) {
                airRun++;
                continue;
            }
            if (airRun >= Math.min(this.requiredAir, above) && state.getFluidState().isEmpty()
                    && this.fits(context, local, offsetX, offsetZ, y, above)) {
                BlockPos origin = new BlockPos(offsetX, y + 1 - this.sink, offsetZ);
                PoolElementStructurePiece piece = new PoolElementStructurePiece(templates, element, origin,
                        element.getGroundLevelDelta(), rotation, element.getBoundingBox(templates, origin, rotation));
                return Optional.of(new GenerationStub(new BlockPos(x, y + 1, z), builder -> builder.addPiece(piece)));
            }
            airRun = 0;
        }
        return Optional.empty();
    }

    /**
     * Verifie l'emprise reelle de la piece sur une grille de colonnes (bords compris) : sol proche de celui du centre,
     * et volume de la structure degage jusqu'a {@link #HEADROOM} blocs au-dessus de son sommet.
     */
    private boolean fits(GenerationContext context, BoundingBox local, int offsetX, int offsetZ, int floorY, int above) {
        int minX = local.minX() + offsetX;
        int maxX = local.maxX() + offsetX;
        int minZ = local.minZ() + offsetZ;
        int maxZ = local.maxZ() + offsetZ;
        int step = Math.max(2, Math.min(4, Math.max(maxX - minX, maxZ - minZ) / 6));
        int columns = 0;
        int grounded = 0;
        int open = 0;
        for (int cx = minX; cx <= maxX + step - 1; cx += step) {
            for (int cz = minZ; cz <= maxZ + step - 1; cz += step) {
                NoiseColumn column = context.chunkGenerator().getBaseColumn(Math.min(cx, maxX), Math.min(cz, maxZ),
                        context.heightAccessor(), context.randomState());
                columns++;
                if (hasFloorNear(column, floorY)) {
                    grounded++;
                }
                int blocked = 0;
                for (int y = floorY + 2; y <= floorY + above + HEADROOM; y++) {
                    if (!column.getBlock(y).isAir()) {
                        blocked++;
                    }
                }
                // Un leger surplomb est tolere sur les tres hautes structures (tours de la Citadelle)
                if (blocked <= Math.max(1, above / 8)) {
                    open++;
                }
            }
        }
        return grounded >= columns * GROUNDED_SHARE && open >= columns * OPEN_SHARE;
    }

    private static boolean hasFloorNear(NoiseColumn column, int floorY) {
        for (int y = floorY - FLOOR_TOLERANCE; y <= floorY + 1; y++) {
            BlockState state = column.getBlock(y);
            if (!state.isAir() && state.getFluidState().isEmpty() && column.getBlock(y + 1).isAir()) {
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
