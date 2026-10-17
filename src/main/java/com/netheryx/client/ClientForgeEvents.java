package com.netheryx.client;

import com.netheryx.NetheryX;
import com.netheryx.util.Infusion;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Evenements client du bus Forge (infobulles). */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {
    }

    /** Affiche l'infusion d'onyx d'un objet sous son nom. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Infusion infusion = Infusion.of(event.getItemStack());
        if (infusion != null) {
            event.getToolTip().add(1, Component.translatable("infusion.netheryx." + infusion.id()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
