package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.block.HangingFruitBlock;
import com.netheryx.block.HangingVinesBlock;
import com.netheryx.block.HangingVinesPlantBlock;
import com.netheryx.block.InfernalCauldronBlock;
import com.netheryx.block.LavaTrapBlock;
import com.netheryx.block.RimeIceBlock;
import com.netheryx.block.SoulAltarBlock;
import com.netheryx.block.WardLanternBlock;
import com.netheryx.block.WardedBlock;
import com.netheryx.block.WartvineBlock;
import com.netheryx.block.WartvinePlantBlock;
import net.minecraft.core.particles.ParticleTypes;
import com.netheryx.block.QuartzGlassBlock;
import com.netheryx.block.QuartzGlassPaneBlock;
import com.netheryx.block.AmbientBlock;
import com.netheryx.block.AmbientClusterBlock;
import com.netheryx.block.AmbientFungusBlock;
import com.netheryx.block.AmbientLanternBlock;
import com.netheryx.block.AmbientOreBlock;
import com.netheryx.block.AmbientParticles;
import com.netheryx.block.BlazePepperCropBlock;
import com.netheryx.block.CinderWheatCropBlock;
import com.netheryx.block.CrustedLavaBlock;
import com.netheryx.block.EmberNyliumBlock;
import com.netheryx.block.FireBerryBushBlock;
import com.netheryx.block.GuardianSealBlock;
import com.netheryx.block.NetherDoublePlantBlock;
import com.netheryx.block.NetherPlantBlock;
import com.netheryx.block.OnyxAnvilBlock;
import com.netheryx.block.OnyxFurnaceBlock;
import com.netheryx.block.SoulLilyBlock;
import com.netheryx.block.SoulTuberCropBlock;
import com.netheryx.block.StrippableLogBlock;
import com.netheryx.util.ModTags;
import com.netheryx.worldgen.ModFeatureKeys;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NetheryX.MOD_ID);

    // --- Onyx -------------------------------------------------------------------------------
    public static final RegistryObject<Block> ONYX_ORE = register("onyx_ore",
            () -> new AmbientOreBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_QUARTZ_ORE)
                    .mapColor(MapColor.COLOR_BLACK).strength(4.5F, 9.0F).requiresCorrectToolForDrops(),
                    UniformInt.of(4, 9), particles(ModParticles.ONYX_GLINT, 6, AmbientParticles.Mode.FACES)));
    public static final RegistryObject<Block> ONYX_BLOCK = register("onyx_block",
            () -> new Block(onyxStone().strength(6.0F, 12.0F).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> POLISHED_ONYX = register("polished_onyx",
            () -> new Block(onyxStone()));
    public static final RegistryObject<Block> ONYX_BRICKS = register("onyx_bricks",
            () -> new Block(onyxStone()));
    public static final RegistryObject<Block> ONYX_BRICK_STAIRS = register("onyx_brick_stairs",
            () -> new StairBlock(() -> ModBlocks.ONYX_BRICKS.get().defaultBlockState(), onyxStone()));
    public static final RegistryObject<Block> ONYX_BRICK_SLAB = register("onyx_brick_slab",
            () -> new SlabBlock(onyxStone()));
    public static final RegistryObject<Block> ONYX_BRICK_WALL = register("onyx_brick_wall",
            () -> new WallBlock(onyxStone().forceSolidOn()));
    public static final RegistryObject<Block> ONYX_CLUSTER = register("onyx_cluster",
            () -> new AmbientClusterBlock(7, 3, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.5F)
                    .lightLevel(state -> 6).pushReaction(PushReaction.DESTROY),
                    particles(ModParticles.ONYX_GLINT, 3, AmbientParticles.Mode.INSIDE)));

    // --- Ash Wastes -------------------------------------------------------------------------
    public static final RegistryObject<Block> ASH_BLOCK = register("ash_block",
            () -> new SandBlock(0x5E5A57, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)));
    public static final RegistryObject<Block> CHARRED_SHRUB = register("charred_shrub",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_BLACK), particles(ModParticles.ASH_FLAKE, 25, AmbientParticles.Mode.INSIDE)));

    // --- Ember Grove ------------------------------------------------------------------------
    public static final RegistryObject<Block> EMBER_NYLIUM = register("ember_nylium",
            () -> new EmberNyliumBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_NYLIUM)
                    .mapColor(MapColor.COLOR_ORANGE).lightLevel(state -> 3),
                    particles(ModParticles.EMBER_SPARK, 40, AmbientParticles.Mode.ABOVE)));
    public static final RegistryObject<Block> STRIPPED_EMBER_STEM = register("stripped_ember_stem",
            () -> new RotatedPillarBlock(emberWood()));
    public static final RegistryObject<Block> EMBER_STEM = register("ember_stem",
            () -> new StrippableLogBlock(emberWood(), STRIPPED_EMBER_STEM));
    public static final RegistryObject<Block> STRIPPED_EMBER_HYPHAE = register("stripped_ember_hyphae",
            () -> new RotatedPillarBlock(emberWood()));
    public static final RegistryObject<Block> EMBER_HYPHAE = register("ember_hyphae",
            () -> new StrippableLogBlock(emberWood(), STRIPPED_EMBER_HYPHAE));
    public static final RegistryObject<Block> EMBER_PLANKS = register("ember_planks",
            () -> new Block(emberWood()));
    public static final RegistryObject<Block> EMBER_STAIRS = register("ember_stairs",
            () -> new StairBlock(() -> ModBlocks.EMBER_PLANKS.get().defaultBlockState(), emberWood()));
    public static final RegistryObject<Block> EMBER_SLAB = register("ember_slab",
            () -> new SlabBlock(emberWood()));
    public static final RegistryObject<Block> EMBER_FENCE = register("ember_fence",
            () -> new FenceBlock(emberWood()));
    public static final RegistryObject<Block> EMBER_FENCE_GATE = register("ember_fence_gate",
            () -> new FenceGateBlock(emberWood().forceSolidOn(), WoodType.CRIMSON));
    public static final RegistryObject<Block> EMBER_DOOR = register("ember_door",
            () -> new DoorBlock(emberWood().noOcclusion().pushReaction(PushReaction.DESTROY), BlockSetType.CRIMSON),
            block -> new DoubleHighBlockItem(block, new Item.Properties()));
    public static final RegistryObject<Block> EMBER_TRAPDOOR = register("ember_trapdoor",
            () -> new TrapDoorBlock(emberWood().noOcclusion().isValidSpawn((state, level, pos, type) -> false), BlockSetType.CRIMSON));
    public static final RegistryObject<Block> EMBER_BUTTON = register("ember_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                    .pushReaction(PushReaction.DESTROY), BlockSetType.CRIMSON, 30, true));
    public static final RegistryObject<Block> EMBER_PRESSURE_PLATE = register("ember_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    emberWood().forceSolidOn().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY),
                    BlockSetType.CRIMSON));
    public static final RegistryObject<Block> EMBER_WART_BLOCK = register("ember_wart_block",
            () -> new AmbientBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK).mapColor(MapColor.COLOR_ORANGE),
                    particles(ModParticles.EMBER_SPARK, 20, AmbientParticles.Mode.BELOW)));
    public static final RegistryObject<Block> EMBER_FUNGUS = register("ember_fungus",
            () -> new AmbientFungusBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_FUNGUS).mapColor(MapColor.COLOR_ORANGE)
                    .lightLevel(state -> 7), ModFeatureKeys.EMBER_FUNGUS_PLANTED, ModBlocks.EMBER_NYLIUM.get(),
                    particles(ModParticles.EMBER_SPARK, 8, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> EMBER_ROOTS = register("ember_roots",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.ROOTS),
                    particles(ModParticles.EMBER_SPARK, 30, AmbientParticles.Mode.INSIDE)));

    // --- Foret fletrie ------------------------------------------------------------------------
    public static final RegistryObject<Block> WITHERED_SOIL = register("withered_soil",
            () -> new AmbientBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_SOIL).mapColor(MapColor.COLOR_LIGHT_GRAY),
                    particles(ModParticles.WITHER_SPORE, 60, AmbientParticles.Mode.ABOVE)));
    public static final RegistryObject<Block> STRIPPED_WITHERED_STEM = register("stripped_withered_stem",
            () -> new RotatedPillarBlock(witheredWood()));
    public static final RegistryObject<Block> WITHERED_STEM = register("withered_stem",
            () -> new StrippableLogBlock(witheredWood(), STRIPPED_WITHERED_STEM));
    public static final RegistryObject<Block> STRIPPED_WITHERED_HYPHAE = register("stripped_withered_hyphae",
            () -> new RotatedPillarBlock(witheredWood()));
    public static final RegistryObject<Block> WITHERED_HYPHAE = register("withered_hyphae",
            () -> new StrippableLogBlock(witheredWood(), STRIPPED_WITHERED_HYPHAE));
    public static final RegistryObject<Block> WITHERED_PLANKS = register("withered_planks",
            () -> new Block(witheredWood()));
    public static final RegistryObject<Block> WITHERED_STAIRS = register("withered_stairs",
            () -> new StairBlock(() -> ModBlocks.WITHERED_PLANKS.get().defaultBlockState(), witheredWood()));
    public static final RegistryObject<Block> WITHERED_SLAB = register("withered_slab",
            () -> new SlabBlock(witheredWood()));
    public static final RegistryObject<Block> WITHERED_FENCE = register("withered_fence",
            () -> new FenceBlock(witheredWood()));
    public static final RegistryObject<Block> WITHERED_FENCE_GATE = register("withered_fence_gate",
            () -> new FenceGateBlock(witheredWood().forceSolidOn(), WoodType.WARPED));
    public static final RegistryObject<Block> WITHERED_DOOR = register("withered_door",
            () -> new DoorBlock(witheredWood().noOcclusion().pushReaction(PushReaction.DESTROY), BlockSetType.WARPED),
            block -> new DoubleHighBlockItem(block, new Item.Properties()));
    public static final RegistryObject<Block> WITHERED_TRAPDOOR = register("withered_trapdoor",
            () -> new TrapDoorBlock(witheredWood().noOcclusion().isValidSpawn((state, level, pos, type) -> false), BlockSetType.WARPED));
    public static final RegistryObject<Block> WITHERED_BUTTON = register("withered_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                    .pushReaction(PushReaction.DESTROY), BlockSetType.WARPED, 30, true));
    public static final RegistryObject<Block> WITHERED_PRESSURE_PLATE = register("withered_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    witheredWood().forceSolidOn().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY),
                    BlockSetType.WARPED));
    public static final RegistryObject<Block> WITHERED_WART_BLOCK = register("withered_wart_block",
            () -> new AmbientBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_WART_BLOCK).mapColor(MapColor.COLOR_GRAY),
                    particles(ModParticles.WITHER_SPORE, 10, AmbientParticles.Mode.BELOW)));
    public static final RegistryObject<Block> WITHERED_FUNGUS = register("withered_fungus",
            () -> new AmbientFungusBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_FUNGUS).mapColor(MapColor.COLOR_GRAY),
                    ModFeatureKeys.WITHERED_FUNGUS_PLANTED, ModBlocks.WITHERED_SOIL.get(),
                    particles(ModParticles.WITHER_SPORE, 8, AmbientParticles.Mode.INSIDE)));

    // --- Marais des ames et Faille de magma --------------------------------------------------
    public static final RegistryObject<Block> SOUL_LILY = register("soul_lily",
            () -> new SoulLilyBlock(plant(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> 10)));
    public static final RegistryObject<Block> MAGMA_BRICKS = register("magma_bricks",
            () -> new AmbientBlock(magmaBricks(), particles(ModParticles.EMBER_SPARK, 30, AmbientParticles.Mode.FACES)));
    public static final RegistryObject<Block> MAGMA_BRICK_STAIRS = register("magma_brick_stairs",
            () -> new StairBlock(() -> ModBlocks.MAGMA_BRICKS.get().defaultBlockState(), magmaBricks()));
    public static final RegistryObject<Block> MAGMA_BRICK_SLAB = register("magma_brick_slab",
            () -> new SlabBlock(magmaBricks()));
    public static final RegistryObject<Block> MAGMA_BRICK_WALL = register("magma_brick_wall",
            () -> new WallBlock(magmaBricks().forceSolidOn()));

    // --- Eclairage -----------------------------------------------------------------------------
    public static final RegistryObject<Block> ONYX_LANTERN = register("onyx_lantern",
            () -> new AmbientLanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN).mapColor(MapColor.COLOR_PURPLE).lightLevel(state -> 13),
                    particles(ModParticles.ONYX_GLINT, 4, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> EMBER_LAMP = register("ember_lamp",
            () -> new AmbientBlock(BlockBehaviour.Properties.copy(Blocks.SHROOMLIGHT).mapColor(MapColor.COLOR_ORANGE).lightLevel(state -> 15),
                    particles(ModParticles.EMBER_SPARK, 10, AmbientParticles.Mode.FACES)));

    // --- Sulfur Springs ---------------------------------------------------------------------
    public static final RegistryObject<Block> SULFUR_ROCK = register("sulfur_rock",
            () -> new AmbientBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.2F, 4.0F)
                    .sound(SoundType.NETHERRACK), particles(ModParticles.SULFUR_PUFF, 50, AmbientParticles.Mode.ABOVE)));
    public static final RegistryObject<Block> SULFUR_ORE = register("sulfur_ore",
            () -> new AmbientOreBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)
                    .mapColor(MapColor.COLOR_YELLOW), UniformInt.of(1, 3), particles(ModParticles.SULFUR_PUFF, 25, AmbientParticles.Mode.FACES)));
    public static final RegistryObject<Block> SULFUR_BLOCK = register("sulfur_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .requiresCorrectToolForDrops().strength(1.5F, 3.0F).sound(SoundType.CALCITE)));
    public static final RegistryObject<Block> SULFUR_CLUSTER = register("sulfur_cluster",
            () -> new AmbientClusterBlock(5, 3, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.0F)
                    .lightLevel(state -> 3).pushReaction(PushReaction.DESTROY),
                    particles(ModParticles.SULFUR_PUFF, 12, AmbientParticles.Mode.INSIDE)));

    // --- Cultures (sans objet direct : la graine/le fruit place le bloc) ---------------------
    public static final RegistryObject<Block> BLAZE_PEPPER_CROP = BLOCKS.register("blaze_pepper_crop",
            () -> new BlazePepperCropBlock(crop()));
    public static final RegistryObject<Block> SOUL_TUBER_CROP = BLOCKS.register("soul_tuber_crop",
            () -> new SoulTuberCropBlock(crop()));
    public static final RegistryObject<Block> FIRE_BERRY_BUSH = BLOCKS.register("fire_berry_bush",
            () -> new FireBerryBushBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH)
                    .mapColor(MapColor.COLOR_RED).lightLevel(FireBerryBushBlock::lightLevel)));
    // Fruits des arbres du Nether : grappes suspendues sous les houppiers (le fruit les replante)
    public static final RegistryObject<Block> EMBER_FIG_CLUSTER = BLOCKS.register("ember_fig_cluster",
            () -> new HangingFruitBlock(fruitCluster(MapColor.COLOR_ORANGE), ModTags.Blocks.EMBER_FIG_CANOPY, () -> ModItems.EMBER_FIG.get()));
    public static final RegistryObject<Block> WITHERED_PLUM_CLUSTER = BLOCKS.register("withered_plum_cluster",
            () -> new HangingFruitBlock(fruitCluster(MapColor.COLOR_PURPLE), ModTags.Blocks.WITHERED_PLUM_CANOPY,
                    () -> ModItems.WITHERED_PLUM.get()));

    // --- Prairie de braise : herbe, fleurs, cultures --------------------------------------------
    public static final RegistryObject<Block> NETHER_GRASS_BLOCK = register("nether_grass_block",
            () -> new EmberNyliumBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_NYLIUM).mapColor(MapColor.COLOR_ORANGE)
                    .sound(SoundType.GRASS), ModFeatureKeys.MEADOW_VEGETATION_BONEMEAL,
                    particles(ModParticles.EMBER_POLLEN, 80, AmbientParticles.Mode.ABOVE)));
    public static final RegistryObject<Block> NETHER_GRASS = register("nether_grass",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> EMBER_BLOOM = register("ember_bloom",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.GRASS).lightLevel(state -> 5),
                    particles(ModParticles.EMBER_SPARK, 10, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> ASH_DAISY = register("ash_daisy",
            () -> new NetherPlantBlock(plant(MapColor.SNOW).sound(SoundType.GRASS),
                    particles(ModParticles.ASH_FLAKE, 18, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> CRIMSON_BELLFLOWER = register("crimson_bellflower",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_RED).sound(SoundType.GRASS),
                    particles(ModParticles.EMBER_POLLEN, 8, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> SULFUR_TULIP = register("sulfur_tulip",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).lightLevel(state -> 3),
                    particles(ModParticles.SULFUR_PUFF, 20, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> GLOWCAP = BLOCKS.register("glowcap",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_CYAN).sound(SoundType.FUNGUS).lightLevel(state -> 9),
                    particles(ModParticles.SOUL_WISP, 10, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> CINDER_WHEAT_CROP = BLOCKS.register("cinder_wheat_crop",
            () -> new CinderWheatCropBlock(crop()));

    public static final RegistryObject<Block> POTTED_EMBER_BLOOM = pot("ember_bloom", EMBER_BLOOM);
    public static final RegistryObject<Block> POTTED_ASH_DAISY = pot("ash_daisy", ASH_DAISY);
    public static final RegistryObject<Block> POTTED_CRIMSON_BELLFLOWER = pot("crimson_bellflower", CRIMSON_BELLFLOWER);
    public static final RegistryObject<Block> POTTED_SULFUR_TULIP = pot("sulfur_tulip", SULFUR_TULIP);
    public static final RegistryObject<Block> POTTED_SOUL_LILY = pot("soul_lily", SOUL_LILY);
    public static final RegistryObject<Block> POTTED_GLOWCAP = pot("glowcap", GLOWCAP);
    public static final RegistryObject<Block> POTTED_EMBER_FUNGUS = pot("ember_fungus", EMBER_FUNGUS);
    public static final RegistryObject<Block> POTTED_WITHERED_FUNGUS = pot("withered_fungus", WITHERED_FUNGUS);

    // --- Ateliers en onyx ------------------------------------------------------------------------
    public static final RegistryObject<Block> ONYX_ANVIL = register("onyx_anvil",
            () -> new OnyxAnvilBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL).mapColor(MapColor.COLOR_BLACK)));
    public static final RegistryObject<Block> ONYX_FURNACE = register("onyx_furnace",
            () -> new OnyxFurnaceBlock(BlockBehaviour.Properties.copy(Blocks.FURNACE).mapColor(MapColor.COLOR_BLACK)
                    .lightLevel(state -> state.getValue(OnyxFurnaceBlock.LIT) ? 13 : 0)));

    // --- Donjons --------------------------------------------------------------------------------
    /** Indestructible : ferme les salles au tresor et se brise a la mort du boss du donjon. */
    public static final RegistryObject<Block> GUARDIAN_SEAL = register("guardian_seal",
            () -> new GuardianSealBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F).noLootTable().lightLevel(state -> 9).sound(SoundType.DEEPSLATE_TILES)
                    .isValidSpawn((state, level, pos, type) -> false).pushReaction(PushReaction.BLOCK)));

    /** Maconnerie scellee des salles au tresor : indestructible jusqu'a la mort du boss. */
    public static final RegistryObject<Block> WARDED_BRICKS = BLOCKS.register("warded_bricks",
            () -> new WardedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F).noLootTable().sound(SoundType.DEEPSLATE_BRICKS)
                    .isValidSpawn((state, level, pos, type) -> false).pushReaction(PushReaction.BLOCK)));

    // --- Technique -------------------------------------------------------------------------
    public static final RegistryObject<Block> CRUSTED_LAVA = BLOCKS.register("crusted_lava",
            () -> new CrustedLavaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER)
                    .strength(0.5F).randomTicks().lightLevel(state -> 7).sound(SoundType.BASALT)
                    .isValidSpawn((state, level, pos, type) -> false).noLootTable()));

    // =============================================================================================
    // 1.2.0 : Fourre de moelle, Gouffre de givre, Enchevetrement de verrues, Recifs de quartz
    // =============================================================================================

    // --- Fourre de moelle -------------------------------------------------------------------------
    public static final RegistryObject<Block> OSSIFIED_NYLIUM = register("ossified_nylium",
            () -> new EmberNyliumBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_NYLIUM).mapColor(MapColor.TERRACOTTA_WHITE),
                    ModFeatureKeys.MARROW_VEGETATION_BONEMEAL, particles(() -> ParticleTypes.WHITE_ASH, 30, AmbientParticles.Mode.ABOVE)));
    public static final RegistryObject<Block> MARROW_CAP = register("marrow_cap",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WARPED_WART_BLOCK).mapColor(MapColor.TERRACOTTA_WHITE)
                    .sound(SoundType.BONE_BLOCK)));
    public static final RegistryObject<Block> MARROW_VINES_PLANT = BLOCKS.register("marrow_vines_plant",
            () -> new HangingVinesPlantBlock(vines(MapColor.TERRACOTTA_WHITE, 6), ModBlocks.MARROW_VINES));
    public static final RegistryObject<Block> MARROW_VINES = register("marrow_vines",
            () -> new HangingVinesBlock(vines(MapColor.TERRACOTTA_WHITE, 6).randomTicks(), MARROW_VINES_PLANT));
    public static final RegistryObject<Block> MARROW_SPROUTS = register("marrow_sprouts",
            () -> new NetherPlantBlock(plant(MapColor.TERRACOTTA_WHITE).sound(SoundType.BONE_BLOCK),
                    particles(() -> ParticleTypes.WHITE_ASH, 40, AmbientParticles.Mode.INSIDE)));

    // --- Bois : marrow -----------------------------------------------------------------------
    public static final RegistryObject<Block> STRIPPED_MARROW_STEM = register("stripped_marrow_stem",
            () -> new RotatedPillarBlock(marrowWood()));
    public static final RegistryObject<Block> MARROW_STEM = register("marrow_stem",
            () -> new StrippableLogBlock(marrowWood(), STRIPPED_MARROW_STEM));
    public static final RegistryObject<Block> STRIPPED_MARROW_HYPHAE = register("stripped_marrow_hyphae",
            () -> new RotatedPillarBlock(marrowWood()));
    public static final RegistryObject<Block> MARROW_HYPHAE = register("marrow_hyphae",
            () -> new StrippableLogBlock(marrowWood(), STRIPPED_MARROW_HYPHAE));
    public static final RegistryObject<Block> MARROW_PLANKS = register("marrow_planks",
            () -> new Block(marrowWood()));
    public static final RegistryObject<Block> MARROW_STAIRS = register("marrow_stairs",
            () -> new StairBlock(() -> ModBlocks.MARROW_PLANKS.get().defaultBlockState(), marrowWood()));
    public static final RegistryObject<Block> MARROW_SLAB = register("marrow_slab",
            () -> new SlabBlock(marrowWood()));
    public static final RegistryObject<Block> MARROW_FENCE = register("marrow_fence",
            () -> new FenceBlock(marrowWood()));
    public static final RegistryObject<Block> MARROW_FENCE_GATE = register("marrow_fence_gate",
            () -> new FenceGateBlock(marrowWood().forceSolidOn(), WoodType.WARPED));
    public static final RegistryObject<Block> MARROW_DOOR = register("marrow_door",
            () -> new DoorBlock(marrowWood().noOcclusion().pushReaction(PushReaction.DESTROY), BlockSetType.WARPED),
            block -> new DoubleHighBlockItem(block, new Item.Properties()));
    public static final RegistryObject<Block> MARROW_TRAPDOOR = register("marrow_trapdoor",
            () -> new TrapDoorBlock(marrowWood().noOcclusion().isValidSpawn((state, level, pos, type) -> false), BlockSetType.WARPED));
    public static final RegistryObject<Block> MARROW_BUTTON = register("marrow_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                    .pushReaction(PushReaction.DESTROY), BlockSetType.WARPED, 30, true));
    public static final RegistryObject<Block> MARROW_PRESSURE_PLATE = register("marrow_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    marrowWood().forceSolidOn().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY),
                    BlockSetType.WARPED));

    // --- Gouffre de givre ---------------------------------------------------------------------
    public static final RegistryObject<Block> RIMESTONE = register("rimestone", () -> new Block(rimestone()));
    public static final RegistryObject<Block> RIMESTONE_BRICKS = register("rimestone_bricks", () -> new Block(rimestone()));
    public static final RegistryObject<Block> RIMESTONE_BRICK_STAIRS = register("rimestone_brick_stairs",
            () -> new StairBlock(() -> ModBlocks.RIMESTONE_BRICKS.get().defaultBlockState(), rimestone()));
    public static final RegistryObject<Block> RIMESTONE_BRICK_SLAB = register("rimestone_brick_slab", () -> new SlabBlock(rimestone()));
    public static final RegistryObject<Block> RIMESTONE_BRICK_WALL = register("rimestone_brick_wall",
            () -> new WallBlock(rimestone().forceSolidOn()));
    public static final RegistryObject<Block> RIME_ICE = register("rime_ice",
            () -> new RimeIceBlock(BlockBehaviour.Properties.copy(Blocks.PACKED_ICE).mapColor(MapColor.ICE).randomTicks()
                    .lightLevel(state -> 4)));
    public static final RegistryObject<Block> RIME_CRYSTAL = register("rime_crystal",
            () -> new AmbientClusterBlock(7, 3, BlockBehaviour.Properties.of().mapColor(MapColor.ICE)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.2F)
                    .lightLevel(state -> 7).pushReaction(PushReaction.DESTROY),
                    particles(ModParticles.FROST_MOTE, 4, AmbientParticles.Mode.INSIDE)));

    // --- Enchevetrement de verrues ---------------------------------------------------------------
    public static final RegistryObject<Block> WART_MOSS = register("wart_moss",
            () -> new EmberNyliumBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_NYLIUM).mapColor(MapColor.CRIMSON_NYLIUM)
                    .sound(SoundType.MOSS), ModFeatureKeys.WART_VEGETATION_BONEMEAL,
                    particles(() -> ParticleTypes.CRIMSON_SPORE, 30, AmbientParticles.Mode.ABOVE)));
    public static final RegistryObject<Block> WART_BULB = register("wart_bulb",
            () -> new AmbientBlock(BlockBehaviour.Properties.copy(Blocks.SHROOMLIGHT).mapColor(MapColor.COLOR_RED).lightLevel(state -> 13),
                    particles(ModParticles.EMBER_POLLEN, 8, AmbientParticles.Mode.BELOW)));
    public static final RegistryObject<Block> WARTVINE_PLANT = BLOCKS.register("wartvine_plant",
            () -> new WartvinePlantBlock(vines(MapColor.COLOR_RED, 0).lightLevel(WartvineBlock::lightLevel), ModBlocks.WARTVINE));
    public static final RegistryObject<Block> WARTVINE = BLOCKS.register("wartvine",
            () -> new WartvineBlock(vines(MapColor.COLOR_RED, 0).lightLevel(WartvineBlock::lightLevel), WARTVINE_PLANT));

    // --- Bois : wartstalk -----------------------------------------------------------------------
    public static final RegistryObject<Block> STRIPPED_WARTSTALK_STEM = register("stripped_wartstalk_stem",
            () -> new RotatedPillarBlock(wartstalkWood()));
    public static final RegistryObject<Block> WARTSTALK_STEM = register("wartstalk_stem",
            () -> new StrippableLogBlock(wartstalkWood(), STRIPPED_WARTSTALK_STEM));
    public static final RegistryObject<Block> STRIPPED_WARTSTALK_HYPHAE = register("stripped_wartstalk_hyphae",
            () -> new RotatedPillarBlock(wartstalkWood()));
    public static final RegistryObject<Block> WARTSTALK_HYPHAE = register("wartstalk_hyphae",
            () -> new StrippableLogBlock(wartstalkWood(), STRIPPED_WARTSTALK_HYPHAE));
    public static final RegistryObject<Block> WARTSTALK_PLANKS = register("wartstalk_planks",
            () -> new Block(wartstalkWood()));
    public static final RegistryObject<Block> WARTSTALK_STAIRS = register("wartstalk_stairs",
            () -> new StairBlock(() -> ModBlocks.WARTSTALK_PLANKS.get().defaultBlockState(), wartstalkWood()));
    public static final RegistryObject<Block> WARTSTALK_SLAB = register("wartstalk_slab",
            () -> new SlabBlock(wartstalkWood()));
    public static final RegistryObject<Block> WARTSTALK_FENCE = register("wartstalk_fence",
            () -> new FenceBlock(wartstalkWood()));
    public static final RegistryObject<Block> WARTSTALK_FENCE_GATE = register("wartstalk_fence_gate",
            () -> new FenceGateBlock(wartstalkWood().forceSolidOn(), WoodType.CRIMSON));
    public static final RegistryObject<Block> WARTSTALK_DOOR = register("wartstalk_door",
            () -> new DoorBlock(wartstalkWood().noOcclusion().pushReaction(PushReaction.DESTROY), BlockSetType.CRIMSON),
            block -> new DoubleHighBlockItem(block, new Item.Properties()));
    public static final RegistryObject<Block> WARTSTALK_TRAPDOOR = register("wartstalk_trapdoor",
            () -> new TrapDoorBlock(wartstalkWood().noOcclusion().isValidSpawn((state, level, pos, type) -> false), BlockSetType.CRIMSON));
    public static final RegistryObject<Block> WARTSTALK_BUTTON = register("wartstalk_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                    .pushReaction(PushReaction.DESTROY), BlockSetType.CRIMSON, 30, true));
    public static final RegistryObject<Block> WARTSTALK_PRESSURE_PLATE = register("wartstalk_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    wartstalkWood().forceSolidOn().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY),
                    BlockSetType.CRIMSON));

    // --- Jungle infernale -------------------------------------------------------------------------
    public static final RegistryObject<Block> HELLFERN = register("hellfern",
            () -> new NetherDoublePlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)),
            block -> new DoubleHighBlockItem(block, new Item.Properties()));
    public static final RegistryObject<Block> BLAZING_GRASS = register("blazing_grass",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.GRASS).lightLevel(state -> 2),
                    particles(ModParticles.EMBER_POLLEN, 30, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> EMBER_VINES_PLANT = BLOCKS.register("ember_vines_plant",
            () -> new HangingVinesPlantBlock(vines(MapColor.COLOR_ORANGE, 8), ModBlocks.EMBER_VINES));
    public static final RegistryObject<Block> EMBER_VINES = register("ember_vines",
            () -> new HangingVinesBlock(vines(MapColor.COLOR_ORANGE, 8).randomTicks(), EMBER_VINES_PLANT));
    public static final RegistryObject<Block> EMBER_FIREFLY_JAR = register("ember_firefly_jar",
            () -> new AmbientLanternBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN).sound(SoundType.GLASS).lightLevel(state -> 15),
                    particles(ModParticles.EMBER_POLLEN, 3, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> SOUL_FIREFLY_JAR = register("soul_firefly_jar",
            () -> new AmbientLanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN).sound(SoundType.GLASS).lightLevel(state -> 12),
                    particles(ModParticles.SOUL_WISP, 3, AmbientParticles.Mode.INSIDE)));

    // --- Lac de verre en fusion ------------------------------------------------------------------
    public static final RegistryObject<Block> MOLTEN_GLASS = register("molten_glass",
            () -> new MagmaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.8F)
                    .sound(SoundType.GLASS).lightLevel(state -> 12).noOcclusion().requiresCorrectToolForDrops()
                    .isValidSpawn((state, level, pos, type) -> type.fireImmune()).hasPostProcess((state, level, pos) -> true)
                    .emissiveRendering((state, level, pos) -> true)));
    public static final RegistryObject<Block> MOLTEN_CRYSTAL = register("molten_crystal",
            () -> new AmbientClusterBlock(7, 3, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.5F)
                    .lightLevel(state -> 9).pushReaction(PushReaction.DESTROY),
                    particles(ModParticles.EMBER_SPARK, 4, AmbientParticles.Mode.INSIDE)));

    // --- Foret petrifiee d'onyx ------------------------------------------------------------------
    public static final RegistryObject<Block> PETRIFIED_LOG = register("petrified_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BASALT).mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5F, 6.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ONYX_CRYSTAL_LEAVES = register("onyx_crystal_leaves",
            () -> new HalfTransparentBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.6F)
                    .sound(SoundType.AMETHYST).noOcclusion().lightLevel(state -> 5)
                    .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    // --- Recifs de quartz ------------------------------------------------------------------------
    public static final RegistryObject<Block> RICH_QUARTZ_ORE = register("rich_quartz_ore",
            () -> new AmbientOreBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_QUARTZ_ORE).strength(3.5F, 3.0F),
                    UniformInt.of(3, 7), particles(ModParticles.QUARTZ_GLINT, 6, AmbientParticles.Mode.FACES)));
    public static final RegistryObject<Block> QUARTZ_CLUSTER = register("quartz_cluster",
            () -> new AmbientClusterBlock(7, 3, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.2F)
                    .lightLevel(state -> 4).pushReaction(PushReaction.DESTROY),
                    particles(ModParticles.QUARTZ_GLINT, 4, AmbientParticles.Mode.INSIDE)));
    public static final RegistryObject<Block> QUARTZ_GLASS = register("quartz_glass",
            () -> new QuartzGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).mapColor(MapColor.QUARTZ).strength(0.6F)));
    public static final RegistryObject<Block> QUARTZ_GLASS_PANE = register("quartz_glass_pane",
            () -> new QuartzGlassPaneBlock(BlockBehaviour.Properties.copy(Blocks.GLASS_PANE).mapColor(MapColor.QUARTZ).strength(0.6F)));
    public static final RegistryObject<Block> QUARTZ_LAMP = register("quartz_lamp",
            () -> new AmbientBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).mapColor(MapColor.QUARTZ).strength(0.6F)
                    .lightLevel(state -> 15), particles(ModParticles.QUARTZ_GLINT, 12, AmbientParticles.Mode.FACES)));

    // --- Ateliers et rituels -----------------------------------------------------------------------
    public static final RegistryObject<Block> LAVA_TRAP = register("lava_trap",
            () -> new LavaTrapBlock(BlockBehaviour.Properties.copy(Blocks.CHAIN).mapColor(MapColor.COLOR_BLACK).strength(3.0F, 1200.0F)
                    .noOcclusion()));
    public static final RegistryObject<Block> INFERNAL_CAULDRON = register("infernal_cauldron",
            () -> new InfernalCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON).mapColor(MapColor.COLOR_BLACK)
                    .lightLevel(state -> state.getValue(InfernalCauldronBlock.LEVEL) > 0 ? 7 : 0)));
    public static final RegistryObject<Block> SOUL_ALTAR = register("soul_altar",
            () -> new SoulAltarBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BLACKSTONE).mapColor(MapColor.COLOR_CYAN)
                    .strength(5.0F, 1200.0F).lightLevel(state -> 7).noOcclusion()));
    public static final RegistryObject<Block> WARD_LANTERN = register("ward_lantern",
            () -> new WardLanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN).mapColor(MapColor.COLOR_CYAN)
                    .lightLevel(state -> 15)));

    private ModBlocks() {
    }

    private static AmbientParticles particles(Supplier<? extends ParticleOptions> particle, int chance, AmbientParticles.Mode mode) {
        return AmbientParticles.of(particle, chance, mode);
    }

    private static BlockBehaviour.Properties onyxStone() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(3.0F, 9.0F).sound(SoundType.DEEPSLATE_BRICKS);
    }

    private static BlockBehaviour.Properties emberWood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD);
    }

    private static BlockBehaviour.Properties witheredWood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD);
    }

    private static BlockBehaviour.Properties marrowWood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD);
    }

    private static BlockBehaviour.Properties wartstalkWood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_STEM).instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD);
    }

    private static BlockBehaviour.Properties rimestone() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.ICE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE_TILES);
    }

    private static BlockBehaviour.Properties vines(MapColor color, int light) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().instabreak().sound(SoundType.WEEPING_VINES)
                .lightLevel(state -> light).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties magmaBricks() {
        return BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS).mapColor(MapColor.NETHER).lightLevel(state -> 4);
    }

    private static BlockBehaviour.Properties plant(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).replaceable().noCollission().instabreak()
                .sound(SoundType.NETHER_SPROUTS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties crop() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noCollission().randomTicks().instabreak()
                .sound(SoundType.NETHER_WART).pushReaction(PushReaction.DESTROY);
    }

    /** Pot de fleur ; la plante est rattachee au pot vanilla pendant le common setup ({@code CompatSetup}). */
    private static RegistryObject<Block> pot(String plant, RegistryObject<Block> content) {
        return BLOCKS.register("potted_" + plant, () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, content,
                BlockBehaviour.Properties.copy(Blocks.POTTED_CRIMSON_FUNGUS)));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        return register(name, block, b -> new BlockItem(b, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block,
                                                                Function<T, ? extends Item> itemFactory) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> itemFactory.apply(registered.get()));
        return registered;
    }

    private static BlockBehaviour.Properties fruitCluster(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).noCollission().randomTicks().instabreak()
                .sound(SoundType.CAVE_VINES).lightLevel(HangingFruitBlock::lightLevel).pushReaction(PushReaction.DESTROY);
    }
}
