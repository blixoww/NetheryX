package com.netheryx.event;

import com.netheryx.NetheryX;
import com.netheryx.config.NetheryXConfig;
import com.netheryx.item.OnyxArmorItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Evenements de jeu (bus Forge). */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class ForgeBusEvents {
    private ForgeBusEvents() {
    }

    /** Bonus d'ensemble de l'armure en Onyx : Resistance I. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide || player.tickCount % 40 != 0
                || !NetheryXConfig.ONYX_SET_BONUS.get()) {
            return;
        }
        if (OnyxArmorItem.hasFullSet(player)) {
            MobEffectInstance current = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (current == null || current.getDuration() < 60) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, true, false, true));
            }
        }
    }
}
