package com.netheryx.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.netheryx.NetheryX;
import com.netheryx.entity.boss.BossEncounter;
import com.netheryx.entity.boss.BossEntity;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModFeatures;
import com.netheryx.worldgen.structure.StructureRestorer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tests en jeu ({@code ./gradlew runGameTestServer}) : gabarits des donjons et de leurs indices, structures suspendues
 * sous la voute.
 */
@GameTestHolder(NetheryX.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NetheryXGameTests {
    private static final String AREA = "gametest/column";
    private static final String[] DUNGEONS = {"onyx_citadel", "infernal_sanctum", "withered_catacombs"};
    private static final String[] VARIANTS = {"main", "variant_1", "variant_2", "variant_3", "keep_0", "keep_1", "keep_2"};
    private static final String[] APPROACH = {"ruin", "remains", "signpost"};

    private NetheryXGameTests() {
    }

    /** Chaque variante de donjon et chaque indice d'approche existe et n'est pas vide. */
    @GameTest(template = AREA)
    public static void dungeonTemplatesLoad(GameTestHelper helper) {
        StructureTemplateManager manager = helper.getLevel().getStructureManager();
        for (String dungeon : DUNGEONS) {
            for (String variant : VARIANTS) {
                requireTemplate(helper, manager, dungeon + "/" + variant);
            }
            for (String kind : APPROACH) {
                requireTemplate(helper, manager, dungeon + "_approach/" + kind);
            }
        }
        helper.succeed();
    }

    private static void requireTemplate(GameTestHelper helper, StructureTemplateManager manager, String path) {
        Optional<StructureTemplate> template = manager.get(NetheryX.id(path));
        if (template.isEmpty() || template.get().getSize().getX() == 0) {
            helper.fail("Gabarit manquant ou vide : " + NetheryX.id(path));
        }
    }

    /**
     * Regression 1.4.0 : reposer un gabarit sur une structure deja posee ne doit pas repandre le butin au sol (Minecraft
     * retire le coffre sans effacer sa table de butin : elle etait tiree par terre, puis une seconde fois dans le coffre).
     */
    @GameTest(template = AREA)
    public static void restoringTemplateDropsNothing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StructureTemplate template = level.getStructureManager().get(NetheryX.id("onyx_citadel_approach/remains")).orElseThrow();
        BlockPos origin = helper.absolutePos(new BlockPos(1, 1, 1));
        BoundingBox box = new BoundingBox(origin.getX() - 8, origin.getY() - 8, origin.getZ() - 8,
                origin.getX() + 16, origin.getY() + 16, origin.getZ() + 16);
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings().setBoundingBox(box).setIgnoreEntities(true),
                level.getRandom(), Block.UPDATE_CLIENTS);
        template.placeInWorld(level, origin, origin, StructureRestorer.restoreSettings(Rotation.NONE, box), level.getRandom(),
                Block.UPDATE_CLIENTS);
        int dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(12.0D)).size();
        if (dropped > 0) {
            helper.fail(dropped + " objet(s) repandu(s) au sol par la repose du gabarit");
        }
        helper.succeed();
    }

    // --- Combats : chaque boss affronte un joueur simule, traverse toutes ses phases et lance toutes ses attaques ------

    private static final String ARENA = "gametest/arena";

    private static <T extends Mob & BossEntity> void fight(GameTestHelper helper, EntityType<T> type, int phases, String... expected) {
        ServerLevel level = helper.getLevel();
        // Le joueur simule de Minecraft est en creatif (les attaques l'ignorent) : on utilise un joueur factice vulnerable
        float[] damage = new float[1];
        java.util.Map<String, Float> bySource = new java.util.TreeMap<>();
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "netheryx-test")) {
            @Override
            public boolean isInvulnerableTo(DamageSource source) {
                return false;
            }

            @Override
            public boolean hurt(DamageSource source, float amount) {
                damage[0] += amount;
                bySource.merge(source.getMsgId(), amount, Float::sum);
                return true;
            }
        };
        BlockPos center = helper.absolutePos(new BlockPos(16, 2, 16));
        player.moveTo(center.getX() + 7.5D, center.getY(), center.getZ() + 0.5D);
        level.addFreshEntity(player);
        T boss = type.create(level);
        boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D);
        level.addFreshEntity(boss);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.STRUCTURE, null, null);
        int[] tick = new int[1];
        helper.onEachTick(() -> {
            tick[0]++;
            player.removeAllEffects();
            // Alternance : il court (les attaques annoncees le manquent), puis s'arrete (elles le touchent)
            if ((tick[0] / 200) % 2 == 0) {
                player.moveTo(center.getX() + 0.5D + Math.cos(tick[0] * 0.03D) * 8.0D, center.getY(), center.getZ() + 0.5D + Math.sin(tick[0] * 0.03D) * 8.0D);
            }
            boss.setTarget(player);
            // Le boss descend de phase en phase, sans jamais mourir pendant le test
            float wanted = tick[0] < 500 ? 1.0F : tick[0] < 1100 ? 0.6F : 0.3F;
            if (boss.getHealth() / boss.getMaxHealth() > wanted || boss.getHealth() < boss.getMaxHealth() * 0.1F) {
                boss.setHealth(boss.getMaxHealth() * wanted);
            }
        });
        helper.runAtTickTime(1990, () -> {
            BossEncounter encounter = boss.encounter();
            player.discard();
            if (!boss.isAlive()) {
                helper.fail("Le boss est mort pendant le test");
            }
            if (encounter.phase() != phases) {
                helper.fail("Phase atteinte : " + encounter.phase() + " au lieu de " + phases);
            }
            for (String kind : expected) {
                if (encounter.hazards().started().getOrDefault(kind, 0) == 0) {
                    helper.fail("Attaque jamais lancee : " + kind + " (lancees : " + encounter.hazards().started() + ")");
                }
            }
            if (damage[0] <= 0.0F) {
                helper.fail("Le joueur n'a subi aucun degat");
            }
            NetheryX.LOGGER.info("COMBAT {} : degats subis {} en 100 s, sources {}, attaques {}", EntityType.getKey(type), damage[0], bySource,
                    encounter.hazards().started());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 2000, batch = "sentinel")
    public static void sentinelFight(GameTestHelper helper) {
        fight(helper, ModEntities.ONYX_SENTINEL.get(), 2, "circle", "wave", "beam");
    }

    @GameTest(template = ARENA, timeoutTicks = 2000, batch = "warlock")
    public static void warlockFight(GameTestHelper helper) {
        fight(helper, ModEntities.INFERNAL_WARLOCK.get(), 2, "circle", "wave", "line");
    }

    @GameTest(template = ARENA, timeoutTicks = 2000, batch = "champion")
    public static void championFight(GameTestHelper helper) {
        fight(helper, ModEntities.WITHERED_CHAMPION.get(), 2, "circle", "wave", "line");
    }

    @GameTest(template = ARENA, timeoutTicks = 2000, batch = "golem")
    public static void golemFight(GameTestHelper helper) {
        fight(helper, ModEntities.BASALT_GOLEM.get(), 0);
    }

    @GameTest(template = ARENA, timeoutTicks = 2000, batch = "heart")
    public static void heartFight(GameTestHelper helper) {
        fight(helper, ModEntities.NETHER_HEART.get(), 2, "circle", "wave", "beam");
    }

    /**
     * De bout en bout, dans un vrai Nether : un donjon genere contient son coffre-fort scelle et ses coffres, et le
     * restaurateur de structures ne repand aucun objet au sol (bug corrige en 1.4.0).
     */
    @GameTest(template = AREA, timeoutTicks = 400, batch = "worldgen")
    public static void generatedDungeonIsComplete(GameTestHelper helper) {
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        Registry<Structure> registry = nether.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder<Structure> structure = registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, NetheryX.id("onyx_citadel")));
        Pair<BlockPos, Holder<Structure>> found = nether.getChunkSource().getGenerator().findNearestMapStructure(nether,
                HolderSet.direct(structure), BlockPos.ZERO, 64, false);
        if (found == null) {
            helper.fail("Aucun donjon trouve");
            return;
        }
        ChunkPos center = new ChunkPos(found.getFirst());
        // Le donjon s'etend a moins de 75 blocs de son entree
        int radius = 6;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                nether.setChunkForced(center.x + dx, center.z + dz, true);
                nether.getChunk(center.x + dx, center.z + dz);
            }
        }
        helper.runAtTickTime(200, () -> {
            int boss = 0;
            int seals = 0;
            int chests = 0;
            AABB area = new AABB(center.getMinBlockX() - radius * 16, nether.getMinBuildHeight(), center.getMinBlockZ() - radius * 16,
                    center.getMaxBlockX() + radius * 16, nether.getMaxBuildHeight(), center.getMaxBlockZ() + radius * 16);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    LevelChunk chunk = nether.getChunk(center.x + dx, center.z + dz);
                    for (BlockEntity entity : chunk.getBlockEntities().values()) {
                        if (entity instanceof ChestBlockEntity) {
                            chests++;
                        }
                    }
                    for (int y = 0; y < 128; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = chunk.getBlockState(new BlockPos(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z));
                                if (state.is(ModBlocks.WARDED_BRICKS.get())) {
                                    boss++;
                                } else if (state.is(ModBlocks.GUARDIAN_SEAL.get())) {
                                    seals++;
                                }
                            }
                        }
                    }
                    nether.setChunkForced(center.x + dx, center.z + dz, false);
                }
            }
            // La vegetation naturelle peut se detacher quand le terrain est retouche : seul le reste trahit un bug
            List<ItemEntity> items = nether.getEntitiesOfClass(ItemEntity.class, area,
                    item -> !(item.getItem().getItem() instanceof BlockItem block && block.getBlock() instanceof BushBlock));
            int dropped = items.size();
            items.forEach(item -> NetheryX.LOGGER.info("DONJON objet au sol : {} x{} en {}",
                    item.getItem().getItem(), item.getItem().getCount(), item.blockPosition()));
            NetheryX.LOGGER.info("DONJON genere en {} : {} blocs scelles, {} sceaux, {} coffres, {} objet(s) au sol", found.getFirst(), boss, seals, chests, dropped);
            if (boss < 200) {
                helper.fail("Coffre-fort incomplet : " + boss + " blocs scelles");
            }
            if (seals < 12) {
                helper.fail("Sceaux : " + seals);
            }
            if (chests < 5) {
                helper.fail("Coffres : " + chests);
            }
            if (dropped > 0) {
                helper.fail(dropped + " objet(s) au sol dans le donjon genere");
            }
            helper.succeed();
        });
    }

    /**
     * Crash 1.4.0 (ArrayIndexOutOfBounds dans LongOpenHashSet, en decharge d'une zone apres un teleport) : decharger toutes
     * les creatures du mod, avec une structure generee autour, ne doit jamais modifier la liste des chunks en cours de parcours.
     */
    @GameTest(template = AREA, timeoutTicks = 1500, batch = "unload")
    public static void unloadingEntitiesIsSafe(GameTestHelper helper) throws ReflectiveOperationException {
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        UnloadWatch watch = UnloadWatch.install(nether);
        ChunkPos center = new ChunkPos(new BlockPos(30000, 0, 30000));
        int radius = 20;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                nether.setChunkForced(center.x + dx, center.z + dz, true);
            }
        }
        helper.runAtTickTime(60, () -> {
            int spawned = 0;
            int attempt = 0;
            for (EntityType<?> type : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValues()) {
                if (!EntityType.getKey(type).getNamespace().equals(NetheryX.MOD_ID)) {
                    continue;
                }
                for (int copies = 0; copies < 3; copies++) {
                    attempt++;
                    BlockPos pos = new BlockPos(center.getMinBlockX() + (attempt % 40) * 8 - 160, 70, center.getMinBlockZ() + (attempt / 40) * 8 - 160);
                    var entity = type.create(nether);
                    if (entity != null) {
                        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                        nether.addFreshEntity(entity);
                        spawned++;
                    }
                }
            }
            NetheryX.LOGGER.info("DECHARGE : {} creatures du mod dans {} chunks", spawned, (radius * 2 + 1) * (radius * 2 + 1));
        });
        helper.runAtTickTime(160, () -> {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    nether.setChunkForced(center.x + dx, center.z + dz, false);
                }
            }
        });
        helper.runAtTickTime(1200, () -> {
            NetheryX.LOGGER.info("DECHARGE : {} chunks decharges, {} modification(s) pendant le parcours", watch.unloaded, watch.violations.size());
            watch.violations.forEach(trace -> NetheryX.LOGGER.error("DECHARGE pile :{}", trace));
            if (watch.unloaded < 100) {
                helper.fail("Trop peu de chunks decharges pour conclure : " + watch.unloaded);
            }
            if (!watch.violations.isEmpty()) {
                helper.fail("La liste des chunks a decharger a change pendant son parcours : voir DECHARGE pile");
            }
            helper.succeed();
        });
    }

    /** Galerie (seulement avec NETHERYX_DUMP=1) : copie un morceau de chaque biome genere, pour tools/render_preview.py. */
    @GameTest(template = AREA, timeoutTicks = 900, batch = "gallery")
    public static void biomeGallery(GameTestHelper helper) {
        if (System.getenv("NETHERYX_DUMP") == null) {
            helper.succeed();
            return;
        }
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        Map<String, BlockPos> centers = WorldgenProbe.biomeCenters(nether);
        int size = 64;
        int radius = size / 32 + 1;
        centers.forEach((name, pos) -> {
            ChunkPos chunk = new ChunkPos(pos);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    nether.setChunkForced(chunk.x + dx, chunk.z + dz, true);
                    nether.getChunk(chunk.x + dx, chunk.z + dz);
                }
            }
        });
        helper.runAtTickTime(700, () -> {
            centers.forEach((name, pos) -> {
                try {
                    WorldgenProbe.dump(nether, pos, size, java.nio.file.Path.of("biome_dump", name.replace(':', '_') + ".bin"));
                } catch (java.io.IOException e) {
                    helper.fail("Releve impossible : " + e);
                }
            });
            NetheryX.LOGGER.info("GALERIE : {} biomes copies", centers.size());
            centers.forEach((name, pos) -> {
                java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
                for (var mob : nether.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new AABB(pos).inflate(56.0D, 128.0D, 56.0D))) {
                    if (mob instanceof net.minecraft.world.entity.animal.Animal) {
                        counts.merge(EntityType.getKey(mob.getType()).getPath(), 1, Integer::sum);
                    }
                }
                NetheryX.LOGGER.info("ANIMAUX {} : {}", name, counts);
            });
            helper.succeed();
        });
    }

    /** Releve de generation dans un vrai Nether (voir le journal, lignes SONDE). */
    @GameTest(template = AREA, timeoutTicks = 200)
    public static void worldgenReport(GameTestHelper helper) {
        WorldgenProbe.report(helper.getLevel(), NetheryX.LOGGER::info, "onyx_citadel", "infernal_sanctum", "withered_catacombs",
                "basalt_forge", "wyvern_nest", "nomad_caravan", "nether_garden", "traveler_camp", "hidden_cache", "scorched_outpost", "bone_shrine",
                "crystal_hollow", "lava_forge", "minecraft:fortress", "minecraft:bastion_remnant");
        helper.succeed();
    }

    /** Sous un plafond, au-dessus d'un grand vide, la feature pose un nid ou une cage accroche a la voute. */
    @GameTest(template = AREA)
    public static void ceilingHangPlaces(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                helper.setBlock(x, 25, z, Blocks.BLACKSTONE);
            }
        }
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(3, 24, 3));
        boolean placed = ModFeatures.CEILING_HANG.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                level.getChunkSource().getGenerator(), RandomSource.create(1234L), origin, NoneFeatureConfiguration.INSTANCE));
        if (!placed || level.getBlockState(origin).isAir() && level.getBlockState(origin.below()).isAir()) {
            helper.fail("Rien n'a ete accroche sous la voute");
        }
        helper.succeed();
    }
}
