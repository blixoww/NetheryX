package com.netheryx.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Lame abyssale : l'arme ultime de NetheryX (13 de degats). Chaque coup embrase et fletrit la cible et rend un coeur
 * au porteur ; un clic droit libere une vague abyssale qui fauche tout ce qui se trouve devant lui (recharge 8 s).
 */
public class AbyssalBladeItem extends SwordItem {
    private static final double WAVE_RANGE = 8.0D;

    public AbyssalBladeItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(8);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1), attacker);
        attacker.heal(2.0F);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
            Vec3 origin = player.position().add(0.0D, 1.0D, 0.0D);
            for (int step = 1; step <= WAVE_RANGE; step++) {
                Vec3 point = origin.add(look.scale(step));
                double spread = 0.3D + step * 0.25D;
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 6 + step, spread, 0.3D, spread, 0.02D);
                server.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 3, spread, 0.3D, spread, 0.02D);
            }
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(WAVE_RANGE, 2.0D, WAVE_RANGE),
                    entity -> entity != player && entity.isAlive() && !entity.isAlliedTo(player))) {
                Vec3 offset = victim.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
                double distance = offset.length();
                // Cone d'environ 70 degres devant le joueur
                if (distance > WAVE_RANGE || distance < 0.01D || offset.normalize().dot(look) < 0.55D) {
                    continue;
                }
                victim.hurt(player.damageSources().playerAttack(player), 12.0F);
                victim.setSecondsOnFire(5);
                victim.knockback(1.2D, -look.x, -look.z);
            }
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 0.7F);
            stack.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(hand));
        }
        player.getCooldowns().addCooldown(this, 160);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.netheryx.abyssal_blade.tooltip").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.netheryx.abyssal_blade.tooltip2").withStyle(ChatFormatting.GOLD));
    }
}
