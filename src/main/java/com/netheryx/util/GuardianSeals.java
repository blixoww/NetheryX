package com.netheryx.util;

import com.netheryx.block.WardedBlock;
import com.netheryx.registry.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

/** Ouverture des salles au tresor : appele a la mort d'un boss de donjon. */
public final class GuardianSeals {
    /** Rayon de recherche des sceaux autour du boss (couvre une salle de donjon entiere). */
    public static final int RADIUS = 34;

    private GuardianSeals() {
    }

    public static void breakSealsAround(ServerLevel level, BlockPos center) {
        int broken = 0;
        // Sceaux et maconnerie scellee : la salle au tresor s'ouvre, sa tour comprise (jusqu'a 32 blocs au-dessus)
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -8, -RADIUS), center.offset(RADIUS, 32, RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.GUARDIAN_SEAL.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        8, 0.3D, 0.3D, 0.3D, 0.05D);
                broken++;
            } else if (state.is(ModBlocks.WARDED_BRICKS.get())) {
                level.setBlock(pos, state.getValue(WardedBlock.VARIANT).plain(), Block.UPDATE_CLIENTS);
                if (level.random.nextInt(6) == 0) {
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            2, 0.3D, 0.3D, 0.3D, 0.02D);
                }
            }
        }
        if (broken > 0) {
            level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.2F);
            for (Player player : level.getEntitiesOfClass(Player.class, new AABB(center).inflate(RADIUS + 16))) {
                player.displayClientMessage(Component.translatable("message.netheryx.seal_broken").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
        }
    }
}
