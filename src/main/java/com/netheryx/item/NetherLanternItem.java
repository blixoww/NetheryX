package com.netheryx.item;

import com.netheryx.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Lanterne du Nether : un clic droit revele pendant 10 secondes, a travers la roche, les minerais situes dans un rayon
 * de 16 blocs (contours colores visibles du seul porteur). Recharge de 30 secondes, 64 utilisations, se repare avec des
 * eclats de verre en fusion.
 */
public class NetherLanternItem extends Item {
    public static final int RADIUS = 16;
    public static final int DURATION = 200;

    public NetherLanternItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.netheryx.client.OreRevealRenderer.reveal(level, player.blockPosition(), RADIUS, DURATION));
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.6F);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        player.getCooldowns().addCooldown(this, 600);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.MOLTEN_SHARD.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.nether_lantern.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.netheryx.nether_lantern.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
