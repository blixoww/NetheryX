package com.netheryx.worldgen;

import com.netheryx.registry.ModBlocks;
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

    public static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.ConditionSource aboveLavaSea = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
        SurfaceRules.ConditionSource nearRoof = SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), state(Blocks.BEDROCK)),
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.verticalGradient("bedrock_roof", VerticalAnchor.belowTop(5), VerticalAnchor.top())), state(Blocks.BEDROCK)),
                SurfaceRules.ifTrue(nearRoof, state(Blocks.NETHERRACK)),

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
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.25D), state(Blocks.SMOOTH_QUARTZ)),
                                state(Blocks.BLACKSTONE))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(Blocks.BLACKSTONE)))),

                state(Blocks.NETHERRACK)
        );
    }

    private static SurfaceRules.RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
