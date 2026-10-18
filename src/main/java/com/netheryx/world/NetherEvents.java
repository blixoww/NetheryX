package com.netheryx.world;

import com.netheryx.NetheryX;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.entity.AshWraith;
import com.netheryx.entity.WitheredKnight;
import com.netheryx.network.NetherEventPacket;
import com.netheryx.network.NetheryXNetwork;
import com.netheryx.registry.ModItems;
import com.netheryx.worldgen.ModBiomes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Moteur des evenements du Nether : declenche periodiquement un evenement, applique ses effets autour
 * des joueurs et synchronise l'etat avec les clients (brouillard, particules).
 */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class NetherEvents {
    private static final NetherEventType[] RANDOM_EVENTS = {NetherEventType.ASHFALL, NetherEventType.SULFUR_STORM, NetherEventType.ERUPTION};

    private NetherEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.dimension() != Level.NETHER) {
            return;
        }
        NetherEventData data = NetherEventData.get(level);
        if (data.current == NetherEventType.NONE) {
            if (!NetheryXConfig.NETHER_EVENTS.get() || level.players().isEmpty()) {
                return;
            }
            if (--data.cooldown <= 0) {
                start(level, RANDOM_EVENTS[level.random.nextInt(RANDOM_EVENTS.length)]);
            }
            data.setDirty();
            return;
        }
        if (--data.remaining <= 0) {
            stop(level);
            return;
        }
        if (level.getGameTime() % 20L == 0L) {
            data.setDirty();
            for (ServerPlayer player : level.players()) {
                applyEffects(level, player, data.current, level.random);
            }
        }
    }

    public static void start(ServerLevel level, NetherEventType type) {
        NetherEventData data = NetherEventData.get(level);
        data.current = type;
        data.remaining = type.duration();
        data.setDirty();
        broadcast(level, type);
        for (ServerPlayer player : level.players()) {
            player.displayClientMessage(Component.translatable("event.netheryx." + type.id() + ".start").withStyle(ChatFormatting.GOLD), false);
        }
    }

    public static void stop(ServerLevel level) {
        NetherEventData data = NetherEventData.get(level);
        NetherEventType ended = data.current;
        data.current = NetherEventType.NONE;
        data.remaining = 0;
        int min = NetheryXConfig.NETHER_EVENT_MIN_DELAY.get();
        int max = Math.max(min, NetheryXConfig.NETHER_EVENT_MAX_DELAY.get());
        data.cooldown = 20 * 60 * (min + level.random.nextInt(max - min + 1));
        data.setDirty();
        broadcast(level, NetherEventType.NONE);
        if (ended != NetherEventType.NONE) {
            for (ServerPlayer player : level.players()) {
                player.displayClientMessage(Component.translatable("event.netheryx." + ended.id() + ".end").withStyle(ChatFormatting.GRAY), false);
            }
        }
    }

    private static void broadcast(ServerLevel level, NetherEventType type) {
        NetheryXNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), new NetherEventPacket(type));
    }

    /** Envoie l'etat courant a un joueur qui arrive (connexion ou changement de dimension). */
    private static void sync(ServerPlayer player) {
        NetherEventType type = player.level().dimension() == Level.NETHER
                ? NetherEventData.get(player.serverLevel()).current() : NetherEventType.NONE;
        NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new NetherEventPacket(type));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    private static void applyEffects(ServerLevel level, ServerPlayer player, NetherEventType type, RandomSource random) {
        switch (type) {
            case ASHFALL -> {
                for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32.0D),
                        mob -> mob instanceof AshWraith || mob instanceof WitheredKnight)) {
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
                }
            }
            case SULFUR_STORM -> {
                if (random.nextInt(3) == 0) {
                    BlockPos drop = belowCeiling(level, player.blockPosition().offset(random.nextInt(25) - 12, 0, random.nextInt(25) - 12));
                    if (drop != null) {
                        ItemEntity item = new ItemEntity(level, drop.getX() + 0.5D, drop.getY(), drop.getZ() + 0.5D,
                                new ItemStack(ModItems.SULFUR.get(), 1 + random.nextInt(2)));
                        item.setDeltaMovement(0.0D, -0.2D, 0.0D);
                        level.addFreshEntity(item);
                    }
                }
            }
            case ERUPTION -> {
                var biome = level.getBiome(player.blockPosition());
                if (!biome.is(ModBiomes.MAGMA_RIFT) && !biome.is(ModBiomes.MOLTEN_GLASS_LAKE) && !biome.is(Biomes.BASALT_DELTAS)) {
                    return;
                }
                BlockPos drop = belowCeiling(level, player.blockPosition().offset(random.nextInt(33) - 16, 0, random.nextInt(33) - 16));
                if (drop == null) {
                    return;
                }
                int roll = random.nextInt(10);
                if (roll < 6) {
                    SmallFireball bomb = new SmallFireball(level, drop.getX() + 0.5D, drop.getY(), drop.getZ() + 0.5D,
                            (random.nextDouble() - 0.5D) * 0.1D, -0.6D, (random.nextDouble() - 0.5D) * 0.1D);
                    level.addFreshEntity(bomb);
                } else if (roll < 8) {
                    FallingBlockEntity.fall(level, drop, Blocks.MAGMA_BLOCK.defaultBlockState());
                } else {
                    ItemStack ejecta = switch (random.nextInt(6)) {
                        case 0 -> new ItemStack(ModItems.ONYX_SHARD.get());
                        case 1 -> new ItemStack(Items.MAGMA_CREAM, 1 + random.nextInt(2));
                        case 2 -> new ItemStack(Items.QUARTZ, 2 + random.nextInt(3));
                        default -> new ItemStack(Items.GOLD_NUGGET, 2 + random.nextInt(5));
                    };
                    level.addFreshEntity(new ItemEntity(level, drop.getX() + 0.5D, drop.getY(), drop.getZ() + 0.5D, ejecta));
                }
                if (random.nextInt(4) == 0) {
                    level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 0.4F, 0.5F);
                }
            }
            default -> {
            }
        }
    }

    /** Premiere case d'air sous la voute au-dessus de {@code start}, ou {@code null}. */
    private static BlockPos belowCeiling(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int dy = 0; dy < 32; dy++) {
            pos.move(0, 1, 0);
            if (!level.getBlockState(pos).isAir()) {
                return pos.below().immutable();
            }
        }
        return null;
    }
}
