package com.netheryx.registry;

import com.netheryx.NetheryX;
import com.netheryx.block.BlazePepperCropBlock;
import com.netheryx.block.CinderWheatCropBlock;
import com.netheryx.block.CrustedLavaBlock;
import com.netheryx.block.EmberNyliumBlock;
import com.netheryx.block.FireBerryBushBlock;
import com.netheryx.block.GuardianSealBlock;
import com.netheryx.block.NetherPlantBlock;
import com.netheryx.block.OnyxAnvilBlock;
import com.netheryx.block.OnyxFurnaceBlock;
import com.netheryx.block.SoulLilyBlock;
import com.netheryx.block.SoulTuberCropBlock;
import com.netheryx.block.StrippableLogBlock;
import com.netheryx.worldgen.ModFeatureKeys;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.LanternBlock;
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
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_QUARTZ_ORE)
                    .mapColor(MapColor.COLOR_BLACK).strength(4.5F, 9.0F).requiresCorrectToolForDrops(),
                    UniformInt.of(4, 9)));
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
            () -> new AmethystClusterBlock(7, 3, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.5F)
                    .lightLevel(state -> 6).pushReaction(PushReaction.DESTROY)));

    // --- Ash Wastes -------------------------------------------------------------------------
    public static final RegistryObject<Block> ASH_BLOCK = register("ash_block",
            () -> new SandBlock(0x5E5A57, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .instrument(NoteBlockInstrument.SNARE).strength(0.5F).sound(SoundType.SAND)));
    public static final RegistryObject<Block> CHARRED_SHRUB = register("charred_shrub",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_BLACK)));

    // --- Ember Grove ------------------------------------------------------------------------
    public static final RegistryObject<Block> EMBER_NYLIUM = register("ember_nylium",
            () -> new EmberNyliumBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_NYLIUM)
                    .mapColor(MapColor.COLOR_ORANGE).lightLevel(state -> 3)));
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
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK).mapColor(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> EMBER_FUNGUS = register("ember_fungus",
            () -> new FungusBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_FUNGUS).mapColor(MapColor.COLOR_ORANGE)
                    .lightLevel(state -> 7), ModFeatureKeys.EMBER_FUNGUS_PLANTED, ModBlocks.EMBER_NYLIUM.get()));
    public static final RegistryObject<Block> EMBER_ROOTS = register("ember_roots",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.ROOTS)));

    // --- Foret fletrie ------------------------------------------------------------------------
    public static final RegistryObject<Block> WITHERED_SOIL = register("withered_soil",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SOUL_SOIL).mapColor(MapColor.COLOR_LIGHT_GRAY)));
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
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WARPED_WART_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> WITHERED_FUNGUS = register("withered_fungus",
            () -> new FungusBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_FUNGUS).mapColor(MapColor.COLOR_GRAY),
                    ModFeatureKeys.WITHERED_FUNGUS_PLANTED, ModBlocks.WITHERED_SOIL.get()));

    // --- Marais des ames et Faille de magma --------------------------------------------------
    public static final RegistryObject<Block> SOUL_LILY = register("soul_lily",
            () -> new SoulLilyBlock(plant(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> 10)));
    public static final RegistryObject<Block> MAGMA_BRICKS = register("magma_bricks",
            () -> new Block(magmaBricks()));
    public static final RegistryObject<Block> MAGMA_BRICK_STAIRS = register("magma_brick_stairs",
            () -> new StairBlock(() -> ModBlocks.MAGMA_BRICKS.get().defaultBlockState(), magmaBricks()));
    public static final RegistryObject<Block> MAGMA_BRICK_SLAB = register("magma_brick_slab",
            () -> new SlabBlock(magmaBricks()));
    public static final RegistryObject<Block> MAGMA_BRICK_WALL = register("magma_brick_wall",
            () -> new WallBlock(magmaBricks().forceSolidOn()));

    // --- Eclairage -----------------------------------------------------------------------------
    public static final RegistryObject<Block> ONYX_LANTERN = register("onyx_lantern",
            () -> new LanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN).mapColor(MapColor.COLOR_PURPLE).lightLevel(state -> 13)));
    public static final RegistryObject<Block> EMBER_LAMP = register("ember_lamp",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SHROOMLIGHT).mapColor(MapColor.COLOR_ORANGE).lightLevel(state -> 15)));

    // --- Sulfur Springs ---------------------------------------------------------------------
    public static final RegistryObject<Block> SULFUR_ROCK = register("sulfur_rock",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.2F, 4.0F)
                    .sound(SoundType.NETHERRACK)));
    public static final RegistryObject<Block> SULFUR_ORE = register("sulfur_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)
                    .mapColor(MapColor.COLOR_YELLOW), UniformInt.of(1, 3)));
    public static final RegistryObject<Block> SULFUR_BLOCK = register("sulfur_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .requiresCorrectToolForDrops().strength(1.5F, 3.0F).sound(SoundType.CALCITE)));
    public static final RegistryObject<Block> SULFUR_CLUSTER = register("sulfur_cluster",
            () -> new AmethystClusterBlock(5, 3, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .forceSolidOn().noOcclusion().sound(SoundType.AMETHYST_CLUSTER).strength(1.0F)
                    .lightLevel(state -> 3).pushReaction(PushReaction.DESTROY)));

    // --- Cultures (sans objet direct : la graine/le fruit place le bloc) ---------------------
    public static final RegistryObject<Block> BLAZE_PEPPER_CROP = BLOCKS.register("blaze_pepper_crop",
            () -> new BlazePepperCropBlock(crop()));
    public static final RegistryObject<Block> SOUL_TUBER_CROP = BLOCKS.register("soul_tuber_crop",
            () -> new SoulTuberCropBlock(crop()));
    public static final RegistryObject<Block> FIRE_BERRY_BUSH = BLOCKS.register("fire_berry_bush",
            () -> new FireBerryBushBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH)
                    .mapColor(MapColor.COLOR_RED).lightLevel(FireBerryBushBlock::lightLevel)));

    // --- Prairie de braise : herbe, fleurs, cultures --------------------------------------------
    public static final RegistryObject<Block> NETHER_GRASS_BLOCK = register("nether_grass_block",
            () -> new EmberNyliumBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_NYLIUM).mapColor(MapColor.COLOR_ORANGE)
                    .sound(SoundType.GRASS), ModFeatureKeys.MEADOW_VEGETATION_BONEMEAL));
    public static final RegistryObject<Block> NETHER_GRASS = register("nether_grass",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> EMBER_BLOOM = register("ember_bloom",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_ORANGE).sound(SoundType.GRASS).lightLevel(state -> 5)));
    public static final RegistryObject<Block> ASH_DAISY = register("ash_daisy",
            () -> new NetherPlantBlock(plant(MapColor.SNOW).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> CRIMSON_BELLFLOWER = register("crimson_bellflower",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_RED).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> SULFUR_TULIP = register("sulfur_tulip",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).lightLevel(state -> 3)));
    public static final RegistryObject<Block> GLOWCAP = BLOCKS.register("glowcap",
            () -> new NetherPlantBlock(plant(MapColor.COLOR_CYAN).sound(SoundType.FUNGUS).lightLevel(state -> 9)));
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

    // --- Technique -------------------------------------------------------------------------
    public static final RegistryObject<Block> CRUSTED_LAVA = BLOCKS.register("crusted_lava",
            () -> new CrustedLavaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER)
                    .strength(0.5F).randomTicks().lightLevel(state -> 7).sound(SoundType.BASALT)
                    .isValidSpawn((state, level, pos, type) -> false).noLootTable()));

    private ModBlocks() {
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
}
