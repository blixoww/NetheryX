package com.netheryx.worldgen.feature;

import com.mojang.serialization.Codec;
import com.netheryx.NetheryX;
import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Structures suspendues sous la voute du Nether, au-dessus d'un grand vide :
 * <ul>
 *     <li><b>nid de spectres</b> : boule d'os et de cendre accrochee au plafond, creusee d'une alcove ou veille parfois
 *     un generateur de spectres de cendre ;</li>
 *     <li><b>cage d'explorateur</b> : cage de barreaux pendue a une chaine, avec un crane et un tonneau de butin.</li>
 * </ul>
 * L'origine est la case d'air juste sous la voute (placement {@code environment_scan} vers le haut).
 */
public class CeilingHangFeature extends Feature<NoneFeatureConfiguration> {
    /** Vide minimal sous l'accroche : la structure doit vraiment pendre au-dessus du gouffre. */
    private static final int MIN_DROP = 12;

    public CeilingHangFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(origin).isAir() || !level.getBlockState(origin.above()).isFaceSturdy(level, origin.above(), Direction.DOWN)) {
            return false;
        }
        for (int dy = 1; dy <= MIN_DROP; dy++) {
            if (!level.getBlockState(origin.below(dy)).isAir()) {
                return false;
            }
        }
        return random.nextFloat() < 0.55F ? this.nest(level, origin, random) : this.cage(level, origin, random);
    }

    private boolean nest(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
        BlockState ash = ModBlocks.ASH_BLOCK.get().defaultBlockState();
        BlockState soul = Blocks.SOUL_SOIL.defaultBlockState();
        BlockPos center = origin.below(2);
        int radius = 2 + random.nextInt(2);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy * 1.4D + dz * dz);
                    if (d > radius + 0.3D) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (d < radius - 0.8D) {
                        // Alcove creuse au coeur du nid
                        FeatureHelper.set(level, pos, Blocks.CAVE_AIR.defaultBlockState());
                    } else {
                        float roll = random.nextFloat();
                        FeatureHelper.set(level, pos, roll < 0.45F ? bone : roll < 0.8F ? ash : soul);
                    }
                }
            }
        }
        // Une ouverture vers le bas, et des fils d'os qui pendent
        for (int dy = 0; dy <= radius; dy++) {
            level.setBlock(center.below(dy), Blocks.CAVE_AIR.defaultBlockState(), 2);
        }
        for (int i = 0; i < 4; i++) {
            BlockPos drip = center.offset(random.nextInt(radius * 2 + 1) - radius, -radius - 1, random.nextInt(radius * 2 + 1) - radius);
            for (int k = 0; k < 1 + random.nextInt(3) && level.getBlockState(drip.below(k)).isAir(); k++) {
                level.setBlock(drip.below(k), Blocks.BONE_BLOCK.defaultBlockState(), 2);
            }
        }
        if (random.nextFloat() < 0.35F) {
            level.setBlock(center, Blocks.SPAWNER.defaultBlockState(), 2);
            if (level.getBlockEntity(center) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(ModEntities.ASH_WRAITH.get(), random);
            }
        } else {
            level.setBlock(center, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }
        return true;
    }

    private boolean cage(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int chain = 2 + random.nextInt(5);
        for (int i = 0; i < chain; i++) {
            level.setBlock(origin.below(i), Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y), 2);
        }
        BlockPos top = origin.below(chain);
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState floor = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        // Cage 3 x 3, trois blocs de haut : toit et plancher pleins, barreaux sur les cotes
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(top.offset(dx, 0, dz), floor, 2);
                level.setBlock(top.offset(dx, -4, dz), floor, 2);
                for (int dy = -3; dy <= -1; dy++) {
                    BlockPos pos = top.offset(dx, dy, dz);
                    boolean side = dx != 0 || dz != 0;
                    level.setBlock(pos, side ? bars : Blocks.CAVE_AIR.defaultBlockState(), 2);
                }
            }
        }
        // Les barreaux se raccordent entre eux
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -3; dy <= -1; dy++) {
                    BlockPos pos = top.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.IRON_BARS)) {
                        level.setBlock(pos, state.updateShape(Direction.NORTH, level.getBlockState(pos.north()), level, pos, pos.north())
                                .updateShape(Direction.SOUTH, level.getBlockState(pos.south()), level, pos, pos.south())
                                .updateShape(Direction.EAST, level.getBlockState(pos.east()), level, pos, pos.east())
                                .updateShape(Direction.WEST, level.getBlockState(pos.west()), level, pos, pos.west()), 2);
                    }
                }
            }
        }
        BlockPos inside = top.below(3);
        level.setBlock(inside, Blocks.BARREL.defaultBlockState(), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, inside, NetheryX.id("chests/small_treasure"));
        level.setBlock(inside.above(), Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, random.nextInt(16)), 2);
        level.setBlock(top.below(1), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), 2);
        return true;
    }
}
