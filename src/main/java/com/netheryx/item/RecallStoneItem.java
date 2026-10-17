package com.netheryx.item;

import com.netheryx.config.NetheryXConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Pierre de rappel : apres 3 secondes de concentration, renvoie le joueur a son point de reapparition
 * (lit ou ancre), ou au point d'apparition du monde. Reutilisable, avec un temps de recharge configurable.
 */
public class RecallStoneItem extends Item {
    private static final int USE_DURATION = 60;

    public RecallStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (level.isClientSide) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, entity.getRandomX(0.8D), entity.getRandomY(), entity.getRandomZ(0.8D), 0.0D, 0.05D, 0.0D);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            recall(player);
            player.getCooldowns().addCooldown(this, NetheryXConfig.RECALL_STONE_COOLDOWN.get() * 20);
        }
        return stack;
    }

    private static void recall(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel target = server.getLevel(player.getRespawnDimension());
        BlockPos respawn = player.getRespawnPosition();
        Vec3 destination = null;
        if (target != null && respawn != null) {
            destination = findSafeSpotNear(target, respawn);
        }
        if (destination == null) {
            target = server.overworld();
            BlockPos spawn = target.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getSharedSpawnPos());
            destination = Vec3.atBottomCenterOf(spawn);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.teleportTo(target, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        target.playSound(null, BlockPos.containing(destination), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    /**
     * Cherche, autour du lit ou de l'ancre, une position ou le joueur tient debout (sol solide, deux blocs libres).
     * N'utilise que des API stables pour rester compatible avec les futures versions.
     */
    private static Vec3 findSafeSpotNear(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= 2; dy++) {
            for (int radius = 0; radius <= 2; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        if (canStandAt(level, pos)) {
                            return Vec3.atBottomCenterOf(pos);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet).isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.recall_stone.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
