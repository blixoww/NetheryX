package com.netheryx.client;

import com.netheryx.NetheryX;
import com.netheryx.registry.ModItems;
import com.netheryx.util.CuriosCompat;
import com.netheryx.util.Infusion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Evenements client du bus Forge (infobulles, effets d'ecran). */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {
    }

    /** Voile ignifuge : masque les flammes plein ecran quand le joueur brule. */
    @SubscribeEvent
    public static void onScreenEffect(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && hasFireVeil(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    private static boolean hasFireVeil(Player player) {
        Item veil = ModItems.FIRE_VEIL.get();
        return player.getInventory().contains(new ItemStack(veil)) || CuriosCompat.isEquipped(player, veil);
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
