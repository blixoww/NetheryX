package com.netheryx.item;

import com.netheryx.entity.NetherHeart;
import com.netheryx.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Coeur scelle : fabrique avec les trophees des trois boss de donjon. Un clic droit dans le Nether
 * l'ouvre et reveille le Coeur du Nether au-dessus du joueur.
 */
public class SealedHeartItem extends Item {
    public SealedHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.dimension() != Level.NETHER) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.netheryx.sealed_heart.wrong_dimension").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (level instanceof ServerLevel server) {
            NetherHeart boss = ModEntities.NETHER_HEART.get().create(server);
            if (boss == null) {
                return InteractionResultHolder.fail(stack);
            }
            boss.moveTo(player.getX(), player.getY() + 6.0D, player.getZ(), player.getYRot(), 0.0F);
            boss.startAwakening();
            server.addFreshEntity(boss);
            server.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 2.0F, 0.6F);
            for (Player nearby : server.players()) {
                if (nearby.distanceToSqr(player) < 128 * 128) {
                    nearby.displayClientMessage(Component.translatable("message.netheryx.nether_heart_awakens").withStyle(ChatFormatting.DARK_RED), false);
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.sealed_heart.tooltip").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.netheryx.sealed_heart.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
