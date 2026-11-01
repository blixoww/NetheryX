package com.netheryx.item;

import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.OpenAtlasPacket;
import com.netheryx.world.NetherAtlasData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Atlas du Nether : la carte des biomes et des structures que le joueur a deja parcourus. */
public class NetherAtlasItem extends Item {
    public NetherAtlasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            if (level.dimension() != Level.NETHER) {
                player.displayClientMessage(Component.translatable("item.netheryx.nether_atlas.wrong_dimension").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            NetheryXNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packetFor(server, serverPlayer));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static OpenAtlasPacket packetFor(ServerLevel level, ServerPlayer player) {
        NetherAtlasData data = NetherAtlasData.get(level);
        data.record(level, player);
        List<Integer> colors = new ArrayList<>();
        var biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        for (String name : data.palette()) {
            var biome = biomes.get(new ResourceLocation(name));
            colors.add(biome == null ? 0x552222 : biome.getFogColor());
        }
        return new OpenAtlasPacket(data.cellsOf(player.getUUID()), data.palette(), colors, data.markersOf(player.getUUID()),
                player.getBlockX(), player.getBlockZ());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.nether_atlas.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
