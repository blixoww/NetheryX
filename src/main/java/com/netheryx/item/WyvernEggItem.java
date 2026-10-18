package com.netheryx.item;

import com.netheryx.entity.AshWyvern;
import com.netheryx.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Oeuf de wyverne de cendre : pose sur un bloc, il eclot aussitot en une wyverne apprivoisee par le joueur. */
public class WyvernEggItem extends Item {
    public WyvernEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (context.getLevel() instanceof ServerLevel level) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            AshWyvern wyvern = ModEntities.ASH_WYVERN.get().create(level);
            if (wyvern == null) {
                return InteractionResult.FAIL;
            }
            wyvern.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
            wyvern.tame(player);
            if (context.getItemInHand().hasCustomHoverName()) {
                wyvern.setCustomName(context.getItemInHand().getHoverName());
            }
            level.addFreshEntity(wyvern);
            level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.NEUTRAL, 1.5F, 0.7F);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 30, 0.8D, 0.6D, 0.8D, 0.02D);
            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.ash_wyvern_egg.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.netheryx.ash_wyvern_egg.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
