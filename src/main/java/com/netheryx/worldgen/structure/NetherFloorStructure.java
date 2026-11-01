package com.netheryx.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.netheryx.NetheryX;
import com.netheryx.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
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
 * <p>
 * Avec un {@code approach_pool}, quelques indices (ruines, restes d'explorateurs, panneaux) sont semes autour, chacun
 * sur son propre sol : on repere le donjon de loin, sans boussole.
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
            Codec.intRange(0, 64).optionalFieldOf("footprint", 0).forGetter(structure -> structure.footprint),
            StructureTemplatePool.CODEC.optionalFieldOf("approach_pool").forGetter(structure -> structure.approachPool),
            Codec.intRange(0, 12).optionalFieldOf("approach_count", 0).forGetter(structure -> structure.approachCount),
            Codec.intRange(-1, 255).optionalFieldOf("entrance_x", -1).forGetter(structure -> structure.entranceX),
            Codec.intRange(-1, 255).optionalFieldOf("entrance_z", -1).forGetter(structure -> structure.entranceZ),
            Codec.intRange(1, 16).optionalFieldOf("floor_tolerance", 3).forGetter(structure -> structure.floorTolerance)
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
    private final Optional<Holder<StructureTemplatePool>> approachPool;
    private final int approachCount;
    /** Donjon souterrain : position de l'entree dans le gabarit (-1 : la structure entiere est posee en surface). */
    private final int entranceX;
    private final int entranceZ;
    /** Ecart de hauteur tolere entre le sol du centre et celui de l'emprise (plus grand si le terrain s'adapte). */
    private final int floorTolerance;

    /** Distance des indices au bord du donjon (en blocs) : entre MIN et MIN + SPREAD. */
    private static final int APPROACH_MIN = 10;
    private static final int APPROACH_SPREAD = 22;
    private static final String[] DUNGEON_SETS = {"onyx_citadel", "infernal_sanctum", "withered_catacombs"};
    /** Un donjon s'etend a 75 blocs au plus de son entree, dans n'importe quelle rotation. */
    private static final int DUNGEON_REACH_CHUNKS = 6;

    /** Marge d'air exigee au-dessus du haut de la structure (pas de plafond qui la coupe). */
    private static final int HEADROOM = 3;
    /** Part minimale des colonnes de l'emprise qui doivent avoir un sol, et un volume degage. */
    private static final double GROUNDED_SHARE = 0.85D;
    private static final double OPEN_SHARE = 0.85D;

    public NetherFloorStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
                                Optional<ResourceLocation> startJigsawName, int size, int maxY, int minY,
                                int requiredAir, int maxDistanceFromCenter, int sink, int footprint,
                                Optional<Holder<StructureTemplatePool>> approachPool, int approachCount, int entranceX, int entranceZ, int floorTolerance) {
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
        this.approachPool = approachPool;
        this.approachCount = approachCount;
        this.entranceX = entranceX;
        this.entranceZ = entranceZ;
        this.floorTolerance = floorTolerance;
    }

    /**
     * Vrai si un grand donjon peut se trouver ici : il s'etend sur 75 blocs autour de son entree, sous la surface, et une
     * petite structure enfoncee dans le sol creuserait dans ses salles. On lit les emplacements candidats de chaque donjon, sans
     * les generer (un candidat sans donjon coute au pire une petite structure).
     */
    private static boolean nearDungeon(GenerationContext context) {
        var sets = context.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        ChunkPos here = context.chunkPos();
        for (String name : DUNGEON_SETS) {
            var set = sets.get(NetheryX.id(name));
            if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
                continue;
            }
            int spacing = placement.spacing();
            int regionX = Math.floorDiv(here.x, spacing);
            int regionZ = Math.floorDiv(here.z, spacing);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    ChunkPos candidate = placement.getPotentialStructureChunk(context.seed(), (regionX + dx) * spacing, (regionZ + dz) * spacing);
                    if (Math.abs(candidate.x - here.x) <= DUNGEON_REACH_CHUNKS && Math.abs(candidate.z - here.z) <= DUNGEON_REACH_CHUNKS) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // Seule une structure qui s'enfonce dans le sol peut percer le plafond d'une salle de donjon
        if (this.entranceX < 0 && this.sink > 0 && nearDungeon(context)) {
            return Optional.empty();
        }
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
        BoundingBox surface = local;
        if (this.entranceX >= 0) {
            // Donjon souterrain : seule l'entree doit tenir en surface, le reste est creuse dans la roche. L'entree est
            // centree sur le chunk, et c'est son emprise qu'on verifie.
            BlockPos entrance = StructureTemplate.transform(new BlockPos(this.entranceX, 0, this.entranceZ), Mirror.NONE, rotation, BlockPos.ZERO);
            offsetX = x - entrance.getX();
            offsetZ = z - entrance.getZ();
            int half = this.footprint / 2;
            surface = new BoundingBox(entrance.getX() - half, 0, entrance.getZ() - half, entrance.getX() + half, local.maxY(), entrance.getZ() + half);
        }

        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        int airRun = 0;
        for (int y = this.maxY; y >= this.minY; y--) {
            BlockState state = column.getBlock(y);
            if (state.isAir()) {
                airRun++;
                continue;
            }
            if (airRun >= Math.min(this.requiredAir, above) && state.getFluidState().isEmpty()
                    && this.fits(context, surface, offsetX, offsetZ, y, above)) {
                BlockPos origin = new BlockPos(offsetX, y + 1 - this.sink, offsetZ);
                PoolElementStructurePiece piece = new PoolElementStructurePiece(templates, element, origin,
                        element.getGroundLevelDelta(), rotation, element.getBoundingBox(templates, origin, rotation));
                int floorY = y;
                BoundingBox surfaceBox = surface;
                return Optional.of(new GenerationStub(new BlockPos(x, y + 1, z), builder -> {
                    builder.addPiece(piece);
                    BoundingBox entranceBox = new BoundingBox(surfaceBox.minX() + origin.getX(), origin.getY(), surfaceBox.minZ() + origin.getZ(),
                            surfaceBox.maxX() + origin.getX(), origin.getY() + surfaceBox.maxY(), surfaceBox.maxZ() + origin.getZ());
                    // Partie enterree du donjon : un indice pose dessus perdrait son sol quand le donjon creuse ses salles
                    BoundingBox whole = piece.getBoundingBox();
                    BoundingBox buried = new BoundingBox(whole.minX(), whole.minY(), whole.minZ(), whole.maxX(), floorY + 1, whole.maxZ());
                    this.addApproach(context, builder, entranceBox, buried, floorY);
                }));
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
                if (hasFloorNear(column, floorY, this.floorTolerance)) {
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

    /** Seme les indices d'approche autour du donjon, chacun pose sur le sol trouve a sa position. */
    private void addApproach(GenerationContext context, StructurePiecesBuilder builder, BoundingBox dungeon, BoundingBox buried, int floorY) {
        if (this.approachPool.isEmpty() || this.approachCount == 0) {
            return;
        }
        WorldgenRandom random = context.random();
        StructureTemplateManager templates = context.structureTemplateManager();
        int centerX = (dungeon.minX() + dungeon.maxX()) / 2;
        int centerZ = (dungeon.minZ() + dungeon.maxZ()) / 2;
        int halfSize = Math.max(dungeon.getXSpan(), dungeon.getZSpan()) / 2;
        List<BoundingBox> placed = new ArrayList<>();
        placed.add(dungeon.inflatedBy(3));
        placed.add(buried);
        for (int i = 0; i < this.approachCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = halfSize + APPROACH_MIN + random.nextInt(APPROACH_SPREAD);
            int px = centerX + (int) Math.round(Math.cos(angle) * distance);
            int pz = centerZ + (int) Math.round(Math.sin(angle) * distance);
            StructurePoolElement element = this.approachPool.get().value().getRandomTemplate(random);
            Rotation rotation = Rotation.getRandom(random);
            int ground = groundNear(context, px, pz, floorY);
            if (ground == Integer.MIN_VALUE) {
                continue;
            }
            BoundingBox local = element.getBoundingBox(templates, BlockPos.ZERO, rotation);
            BlockPos origin = new BlockPos(px - (local.minX() + local.maxX()) / 2, ground + 1, pz - (local.minZ() + local.maxZ()) / 2);
            BoundingBox box = element.getBoundingBox(templates, origin, rotation);
            if (placed.stream().anyMatch(box::intersects)) {
                continue;
            }
            placed.add(box);
            builder.addPiece(new PoolElementStructurePiece(templates, element, origin, element.getGroundLevelDelta(), rotation, box));
        }
    }

    /** Sol (bloc plein sous au moins 4 blocs d'air) le plus proche de la hauteur du donjon, ou MIN_VALUE. */
    private static int groundNear(GenerationContext context, int x, int z, int floorY) {
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        for (int offset = 0; offset <= 12; offset++) {
            for (int y : new int[]{floorY - offset, floorY + offset}) {
                BlockState state = column.getBlock(y);
                if (!state.isAir() && state.getFluidState().isEmpty() && column.getBlock(y + 1).isAir()
                        && column.getBlock(y + 2).isAir() && column.getBlock(y + 3).isAir() && column.getBlock(y + 4).isAir()) {
                    return y;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean hasFloorNear(NoiseColumn column, int floorY, int tolerance) {
        for (int y = floorY - tolerance; y <= floorY + 1; y++) {
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
