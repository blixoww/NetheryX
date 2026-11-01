package com.netheryx.worldgen;

import com.netheryx.registry.ModBlocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/**
 * Regles de surface appliquees par TerraBlender aux biomes de l'espace de noms {@code netheryx}.
 * Elles reprennent la bedrock et la netherrack du Nether vanilla puis ajoutent le sol propre a chaque biome.
 */
public final class NetheryXSurfaceRules {
    private NetheryXSurfaceRules() {
    }

    /** Sol (au-dessus de la mer de lave), sous-sol et voute du biome, puis sa roche dans toute la masse. */
    private static SurfaceRules.RuleSource skin(ResourceKey<Biome> biome, SurfaceRules.RuleSource floor, Block under, Block ceiling, SurfaceRules.RuleSource stone) {
        SurfaceRules.ConditionSource aboveLavaSea = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
        return SurfaceRules.ifTrue(SurfaceRules.isBiome(biome), SurfaceRules.sequence(
                SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, floor)),
                SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(under))),
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, state(ceiling)),
                stone));
    }

    /** Tache de bloc au sol, sinon la regle de repli. */
    private static SurfaceRules.RuleSource patch(Block block, double threshold, SurfaceRules.RuleSource otherwise) {
        return SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PATCH, threshold), state(block)), otherwise);
    }

    /** Deux roches en grandes veines dans la masse. */
    private static SurfaceRules.RuleSource strata(Block a, Block b) {
        return SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.05D), state(a)), state(b));
    }

    public static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.ConditionSource aboveLavaSea = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
        SurfaceRules.ConditionSource nearRoof = SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), state(Blocks.BEDROCK)),
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), state(Blocks.BEDROCK)),
                SurfaceRules.ifTrue(nearRoof, state(Blocks.NETHERRACK)),

                // 1.5.0 : le biome habille tout, sol, murs et voute (sol, sous-sol, voute, roche)
                skin(ModBiomes.EMBER_MEADOW, patch(ModBlocks.EMBER_NYLIUM.get(), 0.6D, state(ModBlocks.NETHER_GRASS_BLOCK.get())), Blocks.BROWN_TERRACOTTA, ModBlocks.EMBER_WART_BLOCK.get(),
                        SurfaceRules.bandlands()),
                skin(ModBiomes.EMBER_GROVE, state(ModBlocks.EMBER_NYLIUM.get()), Blocks.RED_TERRACOTTA, ModBlocks.EMBER_WART_BLOCK.get(),
                        strata(Blocks.ORANGE_TERRACOTTA, Blocks.RED_TERRACOTTA)),
                skin(ModBiomes.HELLWOOD_JUNGLE, patch(ModBlocks.EMBER_NYLIUM.get(), 0.1D, state(ModBlocks.NETHER_GRASS_BLOCK.get())), Blocks.BROWN_TERRACOTTA, ModBlocks.EMBER_WART_BLOCK.get(),
                        strata(Blocks.BROWN_TERRACOTTA, Blocks.TERRACOTTA)),
                skin(ModBiomes.WARTVINE_TANGLE, patch(ModBlocks.WART_MOSS.get(), 0.0D, state(Blocks.CRIMSON_NYLIUM)), Blocks.NETHER_WART_BLOCK, Blocks.NETHER_WART_BLOCK,
                        strata(Blocks.RED_TERRACOTTA, Blocks.NETHER_WART_BLOCK)),
                skin(ModBiomes.SULFUR_SPRINGS, patch(ModBlocks.SULFUR_BLOCK.get(), 0.5D, state(ModBlocks.SULFUR_ROCK.get())), ModBlocks.SULFUR_ROCK.get(), ModBlocks.SULFUR_ROCK.get(),
                        strata(ModBlocks.SULFUR_ROCK.get(), Blocks.YELLOW_TERRACOTTA)),
                skin(ModBiomes.QUARTZ_REEF, patch(Blocks.QUARTZ_BLOCK, 0.3D, state(Blocks.CALCITE)), Blocks.CALCITE, Blocks.CALCITE,
                        strata(Blocks.CALCITE, Blocks.DIORITE)),
                skin(ModBiomes.MARROW_THICKET, state(ModBlocks.OSSIFIED_NYLIUM.get()), Blocks.DRIPSTONE_BLOCK, Blocks.BONE_BLOCK,
                        strata(Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE)),
                skin(ModBiomes.RIMEFROST_CHASM, patch(Blocks.SNOW_BLOCK, 0.0D, state(ModBlocks.RIMESTONE.get())), ModBlocks.RIMESTONE.get(), Blocks.PACKED_ICE,
                        strata(ModBlocks.RIMESTONE.get(), Blocks.PACKED_ICE)),
                skin(ModBiomes.WITHERED_FOREST, state(ModBlocks.WITHERED_SOIL.get()), Blocks.SMOOTH_BASALT, Blocks.TUFF,
                        strata(Blocks.DEEPSLATE, Blocks.TUFF)),
                skin(ModBiomes.PETRIFIED_ONYX_FOREST, patch(Blocks.CALCITE, 0.45D, state(Blocks.SMOOTH_BASALT)), Blocks.BLACKSTONE, Blocks.BLACKSTONE,
                        SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.55D), state(Blocks.AMETHYST_BLOCK)), strata(Blocks.BLACKSTONE, Blocks.DEEPSLATE))),
                skin(ModBiomes.ASHEN_WASTES, state(ModBlocks.ASH_BLOCK.get()), Blocks.BASALT, Blocks.TUFF,
                        strata(Blocks.BASALT, Blocks.TUFF)),
                skin(ModBiomes.MOLTEN_GLASS_LAKE, patch(ModBlocks.MOLTEN_GLASS.get(), 0.3D, patch(Blocks.OBSIDIAN, -0.15D, state(Blocks.BLACKSTONE))), Blocks.BLACKSTONE, Blocks.BLACKSTONE,
                        SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.5D), state(Blocks.MAGMA_BLOCK)), state(Blocks.BLACKSTONE))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.EMBER_MEADOW),
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.NETHER_GRASS_BLOCK.get())))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.EMBER_GROVE),
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.EMBER_NYLIUM.get())))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.ASHEN_WASTES), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.ASH_BLOCK.get()))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.BASALT)))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.ONYX_DEPTHS), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(Blocks.SMOOTH_BASALT)),
                        state(Blocks.BLACKSTONE))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.SULFUR_SPRINGS), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.SULFUR_ROCK.get())),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(ModBlocks.SULFUR_ROCK.get())))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.SOULFIRE_MARSH), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(Blocks.SOUL_SOIL)),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.SOUL_SAND)))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.WITHERED_FOREST), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.WITHERED_SOIL.get()))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.SMOOTH_BASALT)))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.MAGMA_RIFT), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.2D), state(Blocks.MAGMA_BLOCK)),
                                state(Blocks.BLACKSTONE))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.BLACKSTONE)),
                        state(Blocks.BASALT))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.HELLWOOD_JUNGLE), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.35D), state(ModBlocks.EMBER_NYLIUM.get())),
                                state(ModBlocks.NETHER_GRASS_BLOCK.get())))),
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, state(ModBlocks.EMBER_WART_BLOCK.get())))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.MOLTEN_GLASS_LAKE), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.3D), state(ModBlocks.MOLTEN_GLASS.get())),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, -0.15D), state(Blocks.OBSIDIAN)),
                                state(Blocks.BLACKSTONE))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.BLACKSTONE)))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.PETRIFIED_ONYX_FOREST), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.25D), state(Blocks.CALCITE)),
                                state(Blocks.SMOOTH_BASALT))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.BLACKSTONE)),
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, state(Blocks.BLACKSTONE)))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.MARROW_THICKET), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.OSSIFIED_NYLIUM.get()))),
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, state(Blocks.BONE_BLOCK)))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.RIMEFROST_CHASM), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.15D), state(ModBlocks.RIME_ICE.get())),
                                state(ModBlocks.RIMESTONE.get()))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(ModBlocks.RIMESTONE.get())),
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, state(ModBlocks.RIMESTONE.get())))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.WARTVINE_TANGLE),
                        SurfaceRules.ifTrue(aboveLavaSea, SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, state(ModBlocks.WART_MOSS.get())))),

                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.QUARTZ_REEF), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.35D), state(Blocks.SMOOTH_QUARTZ)),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.15D), state(Blocks.QUARTZ_BRICKS)),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, -0.1D), state(Blocks.POLISHED_BLACKSTONE)),
                                state(Blocks.BLACKSTONE))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.BLACKSTONE)),
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.3D), state(Blocks.SMOOTH_QUARTZ)),
                                state(Blocks.BLACKSTONE))))),

                state(Blocks.NETHERRACK)
        );
    }

    private static SurfaceRules.RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
