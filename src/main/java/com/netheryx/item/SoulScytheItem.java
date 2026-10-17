package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Faux des ames : chaque coup porte rend de la vie a son porteur. Objet de tresor uniquement. */
public class SoulScytheItem extends SwordItem {
    private static final float LIFE_STEAL = 2.0F;

    public SoulScytheItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.heal(LIFE_STEAL);
        if (attacker.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.6D), target.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.02D);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.soul_scythe.tooltip").withStyle(ChatFormatting.AQUA));
    }
}
