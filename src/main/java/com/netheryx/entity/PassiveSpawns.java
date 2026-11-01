package com.netheryx.entity;

import com.netheryx.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Regle d'apparition des animaux du Nether : sur un vrai sol naturel (jamais sur les briques d'une structure) et par
 * troupeaux espaces. Les animaux ne disparaissent pas d'eux-memes : sans plafond, ils finissent par tout envahir et
 * saturer le quota d'apparition du jeu, jusqu'a ne plus rien laisser apparaitre ailleurs.
 */
public final class PassiveSpawns {
    private static final double HERD_RANGE = 32.0D;
    private static final int MAX_SAME_NEARBY = 5;
    private static final double AREA_RANGE = 56.0D;
    private static final int MAX_ANIMALS_NEARBY = 12;

    private PassiveSpawns() {
    }

    public static boolean check(EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason == MobSpawnType.SPAWNER || reason == MobSpawnType.SPAWN_EGG) {
            return true;
        }
        if (!level.getBlockState(pos.below()).is(ModTags.Blocks.NETHER_ANIMALS_SPAWNABLE_ON) || !level.getFluidState(pos).isEmpty()) {
            return false;
        }
        AABB herd = new AABB(pos).inflate(HERD_RANGE, 12.0D, HERD_RANGE);
        if (level.getEntitiesOfClass(Mob.class, herd, mob -> mob.getType() == type).size() >= MAX_SAME_NEARBY) {
            return false;
        }
        return level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(AREA_RANGE, 16.0D, AREA_RANGE)).size() < MAX_ANIMALS_NEARBY;
    }
}
