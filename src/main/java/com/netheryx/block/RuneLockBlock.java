package com.netheryx.block;

import com.netheryx.registry.ModBlocks;
import com.netheryx.registry.ModItems;
import com.netheryx.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Verrou runique : la rotonde d'un donjon en compte trois. Chaque clef runique, gagnee au fond d'une aile, en allume un ;
 * quand tous brillent, les Sceaux du gardien qui barrent la descente vers l'arene se dissolvent.
 */
public class RuneLockBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** Portee de recherche des autres verrous et de la herse : la rotonde et la descente, pas le coffre-fort (bien plus bas). */
    private static final int RANGE = 24;
    private static final int BELOW = 11;
    private static final int ABOVE = 3;

    public RuneLockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(LIT)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.RUNE_KEY.get())) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.netheryx.rune_lock.need_key").withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level instanceof ServerLevel server) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            server.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
            server.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.2F, 1.0F);
            server.sendParticles(ModParticles.RUNE_GLYPH.get(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 16, 0.5D, 0.4D, 0.5D, 0.0D);
            int remaining = countUnlit(server, pos);
            if (remaining > 0) {
                announce(server, pos, Component.translatable("message.netheryx.rune_lock.remaining", remaining).withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                openGate(server, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static int countUnlit(ServerLevel level, BlockPos center) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE, -2, -RANGE), center.offset(RANGE, 2, RANGE))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.RUNE_LOCK.get()) && !state.getValue(LIT)) {
                count++;
            }
        }
        return count;
    }

    /** Tous les verrous brillent : la herse de sceaux se dissout, l'arene est ouverte. */
    private static void openGate(ServerLevel level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE, -BELOW, -RANGE), center.offset(RANGE, ABOVE, RANGE))) {
            if (level.getBlockState(pos).is(ModBlocks.GUARDIAN_SEAL.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12, 0.3D, 0.3D, 0.3D, 0.08D);
            }
        }
        level.playSound(null, center, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.8F, 1.5F);
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 0.8F);
        announce(level, center, Component.translatable("message.netheryx.rune_lock.open").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
    }

    private static void announce(ServerLevel level, BlockPos center, Component message) {
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(center).inflate(48.0D))) {
            player.displayClientMessage(message, false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) ? random.nextInt(2) == 0 : random.nextInt(8) == 0) {
            level.addParticle(ModParticles.RUNE_GLYPH.get(), pos.getX() + random.nextDouble(), pos.getY() + 1.05D,
                    pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }
}
