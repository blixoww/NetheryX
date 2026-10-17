package com.netheryx.event;

import com.netheryx.NetheryX;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.item.OnyxHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Minage 3x3 du marteau en Onyx. Chaque bloc supplementaire est casse via
 * {@code ServerPlayerGameMode#destroyBlock}, qui declenche a nouveau {@link BlockEvent.BreakEvent} :
 * les mods de protection de terrain peuvent donc l'annuler normalement.
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class HammerMiningHandler {
    private static final double REACH = 6.0D;
    /** Joueurs en cours de minage de zone, pour eviter la recursion. */
    private static final Set<UUID> MINING = new HashSet<>();

    private HammerMiningHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || event.isCanceled()) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof OnyxHammerItem) || player.isShiftKeyDown()
                || !NetheryXConfig.HAMMER_AREA_MINING.get() || MINING.contains(player.getUUID())) {
            return;
        }
        BlockState origin = event.getState();
        if (!tool.isCorrectToolForDrops(origin)) {
            return;
        }

        Direction face = findHitFace(player);
        if (face == null) {
            return;
        }

        Level level = player.level();
        BlockPos center = event.getPos();
        MINING.add(player.getUUID());
        try {
            for (BlockPos pos : areaAround(center, face)) {
                if (pos.equals(center) || tool.isEmpty()) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F || !tool.isCorrectToolForDrops(state)
                        || state.getDestroySpeed(level, pos) > origin.getDestroySpeed(level, center) + 3.0F) {
                    continue;
                }
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            MINING.remove(player.getUUID());
        }
    }

    private static Direction findHitFace(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(REACH));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getDirection() : null;
    }

    private static Iterable<BlockPos> areaAround(BlockPos center, Direction face) {
        return switch (face.getAxis()) {
            case Y -> BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1));
            case X -> BlockPos.betweenClosed(center.offset(0, -1, -1), center.offset(0, 1, 1));
            case Z -> BlockPos.betweenClosed(center.offset(-1, -1, 0), center.offset(1, 1, 0));
        };
    }
}
