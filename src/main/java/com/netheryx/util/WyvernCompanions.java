package com.netheryx.util;

import com.netheryx.NetheryX;
import com.netheryx.entity.AshWyvern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Compagnons wyvernes : elles suivent leur maitre d'une dimension a l'autre, et le Sifflet les rappelle de n'importe
 * ou (la zone ou elles se trouvent est chargee le temps de les retrouver).
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class WyvernCompanions {
    private static final TicketType<ChunkPos> SUMMON_TICKET = TicketType.create("netheryx_wyvern", Comparator.comparingLong(ChunkPos::toLong), 200);
    private static final Map<UUID, List<UUID>> FOLLOWERS = new HashMap<>();
    private static final List<Pending> PENDING = new ArrayList<>();

    private record Pending(WyvernRegistry.Entry entry, UUID player, int[] ticksLeft) {
    }

    private WyvernCompanions() {
    }

    // --- Suivre a travers les dimensions ------------------------------------------------------------

    @SubscribeEvent
    public static void onTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<UUID> followers = new ArrayList<>();
        for (AshWyvern wyvern : player.serverLevel().getEntitiesOfClass(AshWyvern.class, player.getBoundingBox().inflate(48.0D),
                w -> w.isTame() && w.isOwnedBy(player) && !w.isOrderedToSit() && !w.isVehicle())) {
            followers.add(wyvern.getUUID());
        }
        if (!followers.isEmpty()) {
            FOLLOWERS.put(player.getUUID(), followers);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<UUID> followers = FOLLOWERS.remove(player.getUUID());
        ServerLevel from = player.server.getLevel(event.getFrom());
        if (from == null) {
            return;
        }
        if (followers == null) {
            // Filet de securite : toute wyverne du joueur restee dans l'ancienne dimension et qui ne devait pas attendre
            followers = new ArrayList<>();
            for (WyvernRegistry.Entry entry : WyvernRegistry.get(player.server).ownedBy(player.getUUID())) {
                if (!entry.stored() && entry.dimension().equals(event.getFrom())) {
                    followers.add(entry.wyvern());
                }
            }
        }
        for (UUID id : followers) {
            if (from.getEntity(id) instanceof AshWyvern wyvern && !wyvern.isOrderedToSit()) {
                bring(wyvern, player);
            }
        }
    }

    // --- Sifflet : rappel depuis n'importe ou ------------------------------------------------------

    /**
     * Rappelle toutes les wyvernes du joueur. Renvoie le nombre de wyvernes rappelees, ou -1 si sa seule wyverne est
     * rangee dans un cristal (rien a rappeler).
     */
    public static int summon(ServerPlayer player) {
        MinecraftServer server = player.server;
        List<WyvernRegistry.Entry> entries = WyvernRegistry.get(server).ownedBy(player.getUUID());
        entries.removeIf(WyvernRegistry.Entry::stored);
        if (entries.isEmpty() && !WyvernRegistry.get(server).ownedBy(player.getUUID()).isEmpty()) {
            return -1;
        }
        for (WyvernRegistry.Entry entry : entries) {
            ServerLevel level = server.getLevel(entry.dimension());
            if (level == null) {
                continue;
            }
            if (level.getEntity(entry.wyvern()) instanceof AshWyvern wyvern) {
                bring(wyvern, player);
            } else {
                ChunkPos chunk = new ChunkPos(entry.pos());
                level.getChunkSource().addRegionTicket(SUMMON_TICKET, chunk, 2, chunk);
                PENDING.add(new Pending(entry, player.getUUID(), new int[]{100}));
            }
        }
        return entries.size();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        Iterator<Pending> it = PENDING.iterator();
        while (it.hasNext()) {
            Pending pending = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(pending.player());
            ServerLevel level = server.getLevel(pending.entry().dimension());
            if (player == null || level == null) {
                it.remove();
                continue;
            }
            if (level.getEntity(pending.entry().wyvern()) instanceof AshWyvern wyvern) {
                bring(wyvern, player);
                it.remove();
            } else if (--pending.ticksLeft()[0] <= 0) {
                // Introuvable meme zone chargee : l'entree est perimee, on l'oublie (le joueur pourra en avoir une autre)
                WyvernRegistry.get(server).remove(pending.entry().wyvern());
                player.displayClientMessage(Component.translatable("message.netheryx.wyvern_not_found").withStyle(ChatFormatting.GRAY), false);
                it.remove();
            }
        }
    }

    /** Amene la wyverne aupres du joueur, au besoin dans une autre dimension. */
    public static void bring(AshWyvern wyvern, ServerPlayer player) {
        Vec3 target = safeSpot(player);
        wyvern.setOrderedToSit(false);
        wyvern.setTarget(null);
        if (wyvern.level() == player.level()) {
            ((ServerLevel) wyvern.level()).sendParticles(ParticleTypes.LARGE_SMOKE, wyvern.getX(), wyvern.getY(1.0D), wyvern.getZ(), 20, 1.0D, 0.6D, 1.0D, 0.02D);
            wyvern.teleportTo(target.x, target.y, target.z);
        } else {
            Entity moved = wyvern.changeDimension(player.serverLevel(), new ITeleporter() {
                @Override
                public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                    return new PortalInfo(target, Vec3.ZERO, player.getYRot(), 0.0F);
                }

                @Override
                public boolean playTeleportSound(ServerPlayer p, ServerLevel sourceWorld, ServerLevel destWorld) {
                    return false;
                }
            });
            if (moved == null) {
                return;
            }
        }
        player.serverLevel().playSound(null, BlockPos.containing(target), SoundEvents.ENDER_DRAGON_FLAP, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.2F);
        player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, target.x, target.y + 1.0D, target.z, 20, 1.0D, 0.6D, 1.0D, 0.02D);
    }

    /** Un peu derriere le joueur, sur une case libre si possible. */
    private static Vec3 safeSpot(ServerPlayer player) {
        Vec3 back = player.position().subtract(player.getLookAngle().multiply(3.0D, 0.0D, 3.0D));
        BlockPos pos = BlockPos.containing(back);
        ServerLevel level = player.serverLevel();
        for (int dy = 0; dy < 4; dy++) {
            BlockPos p = pos.above(dy);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                return new Vec3(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D);
            }
        }
        return player.position();
    }
}
