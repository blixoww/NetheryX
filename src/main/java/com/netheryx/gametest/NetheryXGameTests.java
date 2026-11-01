package com.netheryx.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.netheryx.NetheryX;
import com.netheryx.block.RuneLockBlock;
import com.netheryx.entity.boss.BossEncounter;
import com.netheryx.entity.boss.BossEntity;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModEntities;
import com.netheryx.registry.ModFeatures;
import com.netheryx.registry.ModItems;
import com.netheryx.worldgen.structure.StructureRestorer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
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

    /** Les trois clefs allument les trois verrous : les sceaux tombent, et seulement a ce moment-la. */
    @GameTest(template = AREA)
    public static void runeLocksOpenGate(GameTestHelper helper) {
        BlockPos[] locks = {new BlockPos(1, 12, 1), new BlockPos(5, 12, 1), new BlockPos(3, 12, 5)};
        BlockPos seal = new BlockPos(3, 6, 3);
        BlockPos vaultSeal = new BlockPos(3, 0, 3);
        for (BlockPos lock : locks) {
            helper.setBlock(lock, ModBlocks.RUNE_LOCK.get());
        }
        helper.setBlock(seal, ModBlocks.GUARDIAN_SEAL.get());
        helper.setBlock(vaultSeal, ModBlocks.GUARDIAN_SEAL.get());
        Player player = helper.makeMockSurvivalPlayer();
        for (int i = 0; i < locks.length; i++) {
            if (!helper.getBlockState(seal).is(ModBlocks.GUARDIAN_SEAL.get())) {
                helper.fail("La herse s'est ouverte trop tot, apres " + i + " clef(s)");
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.RUNE_KEY.get()));
            BlockPos lock = helper.absolutePos(locks[i]);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(lock), Direction.UP, lock, false);
            helper.getBlockState(locks[i]).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            if (!helper.getBlockState(locks[i]).getValue(RuneLockBlock.LIT)) {
                helper.fail("Le verrou ne s'est pas allume");
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                helper.fail("La clef n'a pas ete consommee");
            }
        }
        if (helper.getBlockState(seal).is(ModBlocks.GUARDIAN_SEAL.get())) {
            helper.fail("La herse est restee fermee apres les trois clefs");
        }
        if (!helper.getBlockState(vaultSeal).is(ModBlocks.GUARDIAN_SEAL.get())) {
            helper.fail("Les verrous ont ouvert le sceau du coffre-fort");
        }
        helper.succeed();
    }

    /**
     * De bout en bout, dans un vrai Nether : un donjon genere contient ses verrous, sa herse et ses coffres, et le
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
            int locks = 0;
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
                                if (state.is(ModBlocks.RUNE_LOCK.get())) {
                                    locks++;
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
            NetheryX.LOGGER.info("DONJON genere en {} : {} verrous, {} sceaux, {} coffres, {} objet(s) au sol", found.getFirst(), locks, seals, chests, dropped);
            if (locks != 3) {
                helper.fail("Verrous runiques : " + locks + " au lieu de 3");
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

    /** Releve de generation dans un vrai Nether (voir le journal, lignes SONDE). */
    @GameTest(template = AREA, timeoutTicks = 200)
    public static void worldgenReport(GameTestHelper helper) {
        WorldgenProbe.report(helper.getLevel(), NetheryX.LOGGER::info, "onyx_citadel", "infernal_sanctum", "withered_catacombs",
                "basalt_forge", "wyvern_nest", "nomad_caravan", "nether_garden", "traveler_camp", "hidden_cache");
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
