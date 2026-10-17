package com.netheryx.item;

import com.netheryx.config.NetheryXConfig;
import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.OpenRecallSelectionPacket;
import com.netheryx.network.SelectRecallPacket;
import com.netheryx.registry.ModItems;
import com.netheryx.util.PortalMemory;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pierre de rappel : apres 3 secondes de concentration, ramene le joueur a un portail du Nether qu'il a emprunte.
 * <ul>
 *     <li>aucun portail memorise : retour au point de reapparition (lit, ancre ou point d'apparition du monde) ;</li>
 *     <li>un seul portail : teleportation directe ;</li>
 *     <li>plusieurs portails : une interface demande lequel rejoindre (ou le point de reapparition).</li>
 * </ul>
 * Reutilisable, avec un temps de recharge configurable.
 */
public class RecallStoneItem extends Item {
    private static final int USE_DURATION = 60;
    /** Delai (en ticks) pendant lequel un choix fait dans l'interface est accepte apres la concentration. */
    private static final long SELECTION_WINDOW = 20L * 60L;
    private static final Map<UUID, Long> PENDING = new ConcurrentHashMap<>();

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
            // Spirale de particules qui se resserre a mesure que la concentration avance
            float progress = 1.0F - (float) remainingTicks / USE_DURATION;
            double radius = 1.2D - progress * 0.9D;
            double angle = remainingTicks * 0.45D;
            for (int i = 0; i < 2; i++) {
                double a = angle + i * Math.PI;
                level.addParticle(ParticleTypes.REVERSE_PORTAL, entity.getX() + Math.cos(a) * radius, entity.getY() + progress * 2.0D,
                        entity.getZ() + Math.sin(a) * radius, 0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            List<PortalMemory.Portal> portals = PortalMemory.get(player);
            if (portals.size() > 1) {
                PENDING.put(player.getUUID(), level.getGameTime() + SELECTION_WINDOW);
                NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenRecallSelectionPacket(portals));
            } else if (portals.size() == 1) {
                travel(player, 0);
            } else {
                travel(player, SelectRecallPacket.RESPAWN);
            }
        }
        return stack;
    }

    /** Traite le choix fait dans l'interface (verifie que la pierre a bien ete chargee juste avant). */
    public static void handleSelection(ServerPlayer player, int index, boolean forget) {
        Long deadline = PENDING.get(player.getUUID());
        if (deadline == null || player.level().getGameTime() > deadline || !holdsReadyStone(player)) {
            PENDING.remove(player.getUUID());
            return;
        }
        List<PortalMemory.Portal> portals = PortalMemory.get(player);
        if (forget) {
            if (index >= 0 && index < portals.size()) {
                portals.remove(index);
                PortalMemory.set(player, portals);
            }
            NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenRecallSelectionPacket(portals));
            return;
        }
        PENDING.remove(player.getUUID());
        if (index == SelectRecallPacket.RESPAWN || (index >= 0 && index < portals.size())) {
            travel(player, index);
        }
    }

    private static boolean holdsReadyStone(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof RecallStoneItem && !player.getCooldowns().isOnCooldown(held.getItem())) {
                return true;
            }
        }
        return false;
    }

    private static void travel(ServerPlayer player, int index) {
        boolean arrived = index == SelectRecallPacket.RESPAWN ? recallToRespawn(player) : recallToPortal(player, index);
        if (arrived) {
            player.getCooldowns().addCooldown(ModItems.RECALL_STONE.get(), NetheryXConfig.RECALL_STONE_COOLDOWN.get() * 20);
        }
    }

    private static boolean recallToPortal(ServerPlayer player, int index) {
        List<PortalMemory.Portal> portals = PortalMemory.get(player);
        PortalMemory.Portal portal = portals.get(index);
        ServerLevel target = player.server.getLevel(portal.levelKey());
        BlockPos portalPos = target == null ? null : PortalMemory.findPortalNear(target, portal.pos(), 2).orElse(null);
        Vec3 destination = portalPos == null ? null : findSafeSpotNear(target, portalPos, 4);
        if (destination == null) {
            // Portail detruit ou inaccessible : on l'oublie et on previent le joueur
            portals.remove(index);
            PortalMemory.set(player, portals);
            player.displayClientMessage(Component.translatable("item.netheryx.recall_stone.portal_lost").withStyle(ChatFormatting.RED), true);
            return false;
        }
        teleport(player, target, destination);
        return true;
    }

    private static boolean recallToRespawn(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel target = server.getLevel(player.getRespawnDimension());
        BlockPos respawn = player.getRespawnPosition();
        Vec3 destination = null;
        if (target != null && respawn != null) {
            destination = findSafeSpotNear(target, respawn, 2);
        }
        if (destination == null) {
            target = server.overworld();
            BlockPos spawn = target.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getSharedSpawnPos());
            destination = Vec3.atBottomCenterOf(spawn);
        }
        teleport(player, target, destination);
        return true;
    }

    private static void teleport(ServerPlayer player, ServerLevel target, Vec3 destination) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY(1.0D), player.getZ(), 40, 0.4D, 0.8D, 0.4D, 0.05D);
        player.teleportTo(target, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        player.setPortalCooldown();
        target.playSound(null, BlockPos.containing(destination), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
        target.sendParticles(ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 1.0D, destination.z, 40, 0.4D, 0.8D, 0.4D, 0.05D);
    }

    /**
     * Cherche, autour d'un point, une position ou le joueur tient debout (sol solide, deux blocs libres, hors portail).
     * N'utilise que des API stables pour rester compatible avec les futures versions.
     */
    private static Vec3 findSafeSpotNear(ServerLevel level, BlockPos origin, int maxRadius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dy : new int[]{0, -1, 1, -2, 2, 3}) {
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
        BlockPos head = feet.above();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                && !level.getBlockState(feet).is(Blocks.NETHER_PORTAL)
                && !level.getBlockState(head).is(Blocks.NETHER_PORTAL)
                && level.getFluidState(feet).isEmpty()
                && level.getFluidState(head).isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.recall_stone.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
