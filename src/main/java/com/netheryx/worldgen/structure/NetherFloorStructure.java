package com.netheryx.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.netheryx.NetheryX;
import com.netheryx.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
            Codec.intRange(1, 16).optionalFieldOf("floor_tolerance", 3).forGetter(structure -> structure.floorTolerance),
            Codec.intRange(0, 128).optionalFieldOf("gate_reach", 0).forGetter(structure -> structure.gateReach)
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
    /** Forteresse : distance du centre au bout du pont d'entree (vers le sud du gabarit). Ce point doit toucher le sol. */
    private final int gateReach;

    /** Distance des indices au bord du donjon (en blocs) : entre MIN et MIN + SPREAD. */
    private static final int APPROACH_MIN = 10;
    private static final int APPROACH_SPREAD = 22;
    /**
     * Ordre de priorite de toutes les structures du mod : chacune cede la place a celles qui la precedent. L'ordre ne depend
     * que de la graine, le resultat est donc le meme quel que soit le chunk genere en premier.
     */
    public static final List<String> PRIORITY = List.of("onyx_citadel", "infernal_sanctum", "withered_catacombs",
            "nether_garden", "nomad_caravan", "basalt_forge", "wyvern_nest", "traveler_camp", "scorched_outpost",
            "bone_shrine", "crystal_hollow", "lava_forge", "hidden_cache");
    /** Petites structures vanilla du Nether que nos structures evitent aussi. */
    private static final List<ResourceLocation> VANILLA_SMALL = List.of(new ResourceLocation("minecraft", "ruined_portals"),
            new ResourceLocation("minecraft", "nether_fossils"));
    private static final Set<String> LARGE = Set.of("onyx_citadel", "infernal_sanctum", "withered_catacombs");
    /** Distance maximale (en chunks) entre le chunk d'origine d'une structure et ses pieces. */
    private static final int LARGE_REACH = 9;
    private static final int SMALL_REACH = 3;
    /** Ecart minimal entre deux structures, en blocs. */
    private static final int CLEARANCE = 2;
    private static final TagKey<Block> FOUNDATION = TagKey.create(Registries.BLOCK, NetheryX.id("keep_foundation"));
    private static final int MAX_PIER = 96;

    /** Pieces deja calculees : une structure est interrogee par toutes ses voisines moins prioritaires. */
    private static final Map<String, List<BoundingBox>> PIECES = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<BoundingBox>> eldest) {
            return this.size() > 4096;
        }
    });

    /** Marge d'air exigee au-dessus du haut de la structure (pas de plafond qui la coupe). */
    private static final int HEADROOM = 3;
    /** Part minimale des colonnes de l'emprise qui doivent avoir un sol, et un volume degage. */
    private static final double GROUNDED_SHARE = 0.85D;
    private static final double OPEN_SHARE = 0.85D;
    /** Hauteur de l'enceinte des forteresses au-dessus du sol : c'est elle, pas la fleche, qui doit etre degagee. */
    private static final int FORTRESS_WALLS = 10;
    private static final double FORTRESS_GROUNDED = 0.4D;
    private static final double FORTRESS_OPEN = 0.55D;
    /** Hauteurs atteignables depuis les marches du pont d'entree, par rapport au sol du centre. */
    private static final int GATE_DROP = 8;
    private static final int GATE_RISE = 5;

    public NetherFloorStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
                                Optional<ResourceLocation> startJigsawName, int size, int maxY, int minY,
                                int requiredAir, int maxDistanceFromCenter, int sink, int footprint,
                                Optional<Holder<StructureTemplatePool>> approachPool, int approachCount, int entranceX, int entranceZ, int floorTolerance, int gateReach) {
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
        this.gateReach = gateReach;
    }

    /** Boites des pieces reellement generees par une structure a partir d'un chunk (vide si elle n'y nait pas). */
    public static List<BoundingBox> piecesOf(GenerationContext context, Structure structure, ResourceLocation id, ChunkPos chunk) {
        String key = context.seed() + "/" + id + "/" + chunk.toLong();
        List<BoundingBox> known = PIECES.get(key);
        if (known != null) {
            return known;
        }
        StructureStart start = structure.generate(context.registryAccess(), context.chunkGenerator(), context.biomeSource(), context.randomState(),
                context.structureTemplateManager(), context.seed(), chunk, 0, context.heightAccessor(), structure.biomes()::contains);
        List<BoundingBox> boxes = start.isValid() ? start.getPieces().stream().map(StructurePiece::getBoundingBox).toList() : List.of();
        PIECES.put(key, boxes);
        return boxes;
    }

    /**
     * Pieces de ce qui nait vraiment sur ce chunk : un ensemble a plusieurs structures (bastion ou forteresse) n'en genere qu'une,
     * tiree comme le fait ChunkGenerator.createStructures. Compter les deux ecarterait nos structures de batiments qui n'existent pas.
     */
    public static List<BoundingBox> chosenPieces(GenerationContext context, StructureSet set, ChunkPos chunk) {
        var structures = context.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<StructureSet.StructureSelectionEntry> entries = new ArrayList<>(set.structures());
        if (entries.size() == 1) {
            Structure only = entries.get(0).structure().value();
            return piecesOf(context, only, structures.getKey(only), chunk);
        }
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(context.seed(), chunk.x, chunk.z);
        int total = entries.stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();
        while (!entries.isEmpty()) {
            int roll = random.nextInt(total);
            int index = 0;
            for (StructureSet.StructureSelectionEntry entry : entries) {
                roll -= entry.weight();
                if (roll < 0) {
                    break;
                }
                index++;
            }
            StructureSet.StructureSelectionEntry entry = entries.get(index);
            Structure structure = entry.structure().value();
            List<BoundingBox> boxes = piecesOf(context, structure, structures.getKey(structure), chunk);
            if (!boxes.isEmpty()) {
                return boxes;
            }
            entries.remove(index);
            total -= entry.weight();
        }
        return List.of();
    }

    /** Vrai si deux boites se recouvrent vu du ciel : l'une creuserait ou couperait forcement l'autre. */
    public static boolean overlapsFromAbove(BoundingBox a, BoundingBox b, int margin) {
        return a.minX() - margin <= b.maxX() && a.maxX() + margin >= b.minX() && a.minZ() - margin <= b.maxZ() && a.maxZ() + margin >= b.minZ();
    }

    private boolean blockedBySet(GenerationContext context, ResourceLocation setId, BoundingBox box, int reach) {
        var sets = context.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        StructureSet set = sets.get(setId);
        if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            return false;
        }
        int spacing = placement.spacing();
        ChunkPos here = new ChunkPos(new BlockPos((box.minX() + box.maxX()) / 2, 0, (box.minZ() + box.maxZ()) / 2));
        for (int regionX = Math.floorDiv(here.x - reach, spacing); regionX <= Math.floorDiv(here.x + reach, spacing); regionX++) {
            for (int regionZ = Math.floorDiv(here.z - reach, spacing); regionZ <= Math.floorDiv(here.z + reach, spacing); regionZ++) {
                ChunkPos candidate = placement.getPotentialStructureChunk(context.seed(), regionX * spacing, regionZ * spacing);
                if (Math.abs(candidate.x - here.x) > reach || Math.abs(candidate.z - here.z) > reach) {
                    continue;
                }
                for (BoundingBox piece : chosenPieces(context, set, candidate)) {
                    if (overlapsFromAbove(box, piece, CLEARANCE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Cette boite empiete-t-elle sur une structure prioritaire ? On calcule les vraies pieces des voisines : ni emplacement
     * candidat vide, ni marge arbitraire. Bastions et forteresses sont toujours prioritaires.
     */
    private boolean blocked(GenerationContext context, BoundingBox box) {
        ResourceLocation self = context.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this);
        if (self == null || this.blockedBySet(context, new ResourceLocation("minecraft", "nether_complexes"), box, LARGE_REACH + (box.getXSpan() >> 5))) {
            return self != null;
        }
        // Portails en ruine et fossiles du Nether : poses avant nos structures, ils se melangeraient a elles
        int smallReach = SMALL_REACH + (Math.max(box.getXSpan(), box.getZSpan()) >> 5) + 1;
        for (ResourceLocation vanilla : VANILLA_SMALL) {
            if (this.blockedBySet(context, vanilla, box, smallReach)) {
                return true;
            }
        }
        for (String name : PRIORITY) {
            if (name.equals(self.getPath())) {
                return false;
            }
            int reach = (LARGE.contains(name) ? LARGE_REACH : SMALL_REACH) + (Math.max(box.getXSpan(), box.getZSpan()) >> 5) + 1;
            if (this.blockedBySet(context, NetheryX.id(name), box, reach)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        WorldgenRandom random = context.random();
        Rotation drawn = Rotation.getRandom(random);
        StructurePoolElement element = this.startPool.value().getRandomTemplate(random);
        StructureTemplateManager templates = context.structureTemplateManager();
        // Emprise de la piece, posee a l'origine (rotation tiree ; une forteresse tourne ensuite vers le sol qui recoit son pont)
        BoundingBox local = element.getBoundingBox(templates, BlockPos.ZERO, drawn);
        int above = Math.max(2, local.getYSpan() - this.sink);
        BoundingBox surface = local;
        if (this.entranceX >= 0) {
            // Forteresse : son centre est au milieu du chunk. Ses tours peuvent percer une voute, mais l'enceinte doit etre a l'air libre.
            BlockPos center = StructureTemplate.transform(new BlockPos(this.entranceX, 0, this.entranceZ), Mirror.NONE, drawn, BlockPos.ZERO);
            int half = this.footprint / 2;
            surface = new BoundingBox(center.getX() - half, 0, center.getZ() - half, center.getX() + half, local.maxY(), center.getZ() + half);
            above = Math.min(above, FORTRESS_WALLS);
        }
        BlockPos anchor = this.entranceX >= 0 ? StructureTemplate.transform(new BlockPos(this.entranceX, 0, this.entranceZ), Mirror.NONE, drawn, BlockPos.ZERO)
                : new BlockPos((local.minX() + local.maxX()) / 2, 0, (local.minZ() + local.maxZ()) / 2);
        int offsetX = x - anchor.getX();
        int offsetZ = z - anchor.getZ();

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
                Rotation rotation = this.gateRotation(context, drawn, x, z, y);
                if (rotation == null) {
                    airRun = 0;
                    continue;
                }
                if (rotation != drawn) {
                    BlockPos turned = StructureTemplate.transform(new BlockPos(this.entranceX, 0, this.entranceZ), Mirror.NONE, rotation, BlockPos.ZERO);
                    offsetX = x - turned.getX();
                    offsetZ = z - turned.getZ();
                }
                BlockPos origin = new BlockPos(offsetX, y + 1 - this.sink, offsetZ);
                // Niveau du sol pour l'adaptation du terrain : au-dessus, le volume est creuse ; dessous (partie souterraine
                // d'un donjon), la roche reste en place
                PoolElementStructurePiece piece = new PoolElementStructurePiece(templates, element, origin,
                        Math.max(element.getGroundLevelDelta(), this.sink), rotation, element.getBoundingBox(templates, origin, rotation));
                // La hauteur ne change rien ici : deux structures l'une au-dessus de l'autre se couperaient quand meme
                if (this.blocked(context, piece.getBoundingBox())) {
                    return Optional.empty();
                }
                int floorY = y;
                return Optional.of(new GenerationStub(new BlockPos(x, y + 1, z), builder -> {
                    builder.addPiece(piece);
                    this.addApproach(context, builder, piece.getBoundingBox(), floorY);
                }));
            }
            airRun = 0;
        }
        return Optional.empty();
    }

    /**
     * Point d'arrivee d'une teleportation vers cette structure (boussole du createur) : devant le pont d'entree d'une
     * forteresse, au milieu du sol d'une petite structure. Hauteur des pieds.
     */
    public BlockPos arrival(StructureStart start) {
        if (start.getPieces().isEmpty() || !(start.getPieces().get(0) instanceof PoolElementStructurePiece main)) {
            BoundingBox box = start.getBoundingBox();
            return new BlockPos((box.minX() + box.maxX()) / 2, box.minY() + 1, (box.minZ() + box.maxZ()) / 2);
        }
        BlockPos origin = main.getPosition();
        int feetY = origin.getY() + this.sink;
        if (this.entranceX >= 0) {
            BlockPos center = origin.offset(StructureTemplate.transform(new BlockPos(this.entranceX, 0, this.entranceZ), Mirror.NONE,
                    main.getRotation(), BlockPos.ZERO));
            BlockPos gate = StructureTemplate.transform(new BlockPos(0, 0, this.gateReach + 3), Mirror.NONE, main.getRotation(), BlockPos.ZERO);
            return new BlockPos(center.getX() + gate.getX(), feetY, center.getZ() + gate.getZ());
        }
        BoundingBox box = main.getBoundingBox();
        return new BlockPos((box.minX() + box.maxX()) / 2, feetY, (box.minZ() + box.maxZ()) / 2);
    }

    /** Centre de la piece principale (pour orienter le joueur vers la structure a son arrivee). */
    public static BlockPos center(StructureStart start) {
        BoundingBox box = start.getPieces().isEmpty() ? start.getBoundingBox() : start.getPieces().get(0).getBoundingBox();
        return new BlockPos((box.minX() + box.maxX()) / 2, (box.minY() + box.maxY()) / 2, (box.minZ() + box.maxZ()) / 2);
    }

    /** Rotation qui amene le bout du pont d'entree sur un sol praticable, en commencant par celle tiree au sort ; null s'il n'y en a pas. */
    private Rotation gateRotation(GenerationContext context, Rotation drawn, int x, int z, int floorY) {
        if (this.gateReach == 0) {
            return drawn;
        }
        for (int turn = 0; turn < 4; turn++) {
            Rotation rotation = drawn.getRotated(Rotation.values()[turn]);
            BlockPos tip = StructureTemplate.transform(new BlockPos(0, 0, this.gateReach), Mirror.NONE, rotation, BlockPos.ZERO);
            NoiseColumn column = context.chunkGenerator().getBaseColumn(x + tip.getX(), z + tip.getZ(), context.heightAccessor(), context.randomState());
            for (int y = floorY - GATE_DROP; y <= floorY + GATE_RISE; y++) {
                BlockState state = column.getBlock(y);
                if (!state.isAir() && state.getFluidState().isEmpty() && column.getBlock(y + 1).isAir()) {
                    return rotation;
                }
            }
        }
        return null;
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
        // Une forteresse enjambe les creux sur ses arcades : seul un relief qui la traverserait la gene vraiment
        boolean fortress = this.entranceX >= 0;
        return grounded >= columns * (fortress ? FORTRESS_GROUNDED : GROUNDED_SHARE) && open >= columns * (fortress ? FORTRESS_OPEN : OPEN_SHARE);
    }

    /** Seme les indices d'approche autour de la forteresse, chacun pose sur le sol trouve a sa position. */
    private void addApproach(GenerationContext context, StructurePiecesBuilder builder, BoundingBox dungeon, int floorY) {
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
            // Les indices aussi cedent la place : a la forteresse, entre eux et aux structures prioritaires
            if (placed.stream().anyMatch(other -> overlapsFromAbove(box, other, CLEARANCE)) || this.blocked(context, box)) {
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

    /**
     * Prolonge les piles de fondation d'une forteresse jusqu'au sol, comme les piles des forteresses du Nether. Le gabarit
     * s'arrete a sa couche du bas : seuls les blocs de fondation qu'il y pose continuent vers le bas.
     */
    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random, BoundingBox chunkBox,
                           ChunkPos chunkPos, PiecesContainer pieces) {
        if (this.entranceX < 0 || pieces.pieces().isEmpty()) {
            return;
        }
        BoundingBox whole = pieces.pieces().get(0).getBoundingBox();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = Math.max(whole.minX(), chunkBox.minX()); x <= Math.min(whole.maxX(), chunkBox.maxX()); x++) {
            for (int z = Math.max(whole.minZ(), chunkBox.minZ()); z <= Math.min(whole.maxZ(), chunkBox.maxZ()); z++) {
                BlockState foot = level.getBlockState(pos.set(x, whole.minY(), z));
                if (!foot.is(FOUNDATION)) {
                    continue;
                }
                for (int y = whole.minY() - 1, depth = 0; y > level.getMinBuildHeight() && depth < MAX_PIER; y--, depth++) {
                    BlockState below = level.getBlockState(pos.set(x, y, z));
                    if (!below.isAir() && below.getFluidState().isEmpty() && !below.canBeReplaced()) {
                        break;
                    }
                    level.setBlock(pos, foot, 2);
                }
            }
        }
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.NETHER_FLOOR.get();
    }
}
