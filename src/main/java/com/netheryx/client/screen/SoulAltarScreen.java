package com.netheryx.client.screen;

import com.netheryx.NetheryX;
import com.netheryx.blockentity.SoulAltarBlockEntity;
import com.netheryx.menu.SoulAltarMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * Interface de l'Autel des ames. A gauche, le guide des offrandes (l'offrande reconnue est surlignee) ; a droite,
 * l'objet et l'offrande, le canal du rituel qui se remplit, et un cadre qui dit clairement ce qui manque, ce que
 * l'infusion donnera, ce qu'elle coute et combien de flammes des ames entourent l'autel.
 */
public class SoulAltarScreen extends AbstractContainerScreen<SoulAltarMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetheryX.MOD_ID, "textures/gui/soul_altar.png");
    private static final int TEX_W = 512;
    private static final int TEX_H = 256;
    private static final int MAX_FLAME_ICONS = 8;

    private Button infuse;

    public SoulAltarScreen(SoulAltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = SoulAltarMenu.MAIN_X + 176;
        this.imageHeight = 186;
        this.titleLabelX = SoulAltarMenu.MAIN_X + 8;
        this.inventoryLabelX = SoulAltarMenu.MAIN_X + 8;
        this.inventoryLabelY = 92;
    }

    @Override
    protected void init() {
        super.init();
        this.infuse = this.addRenderableWidget(Button.builder(Component.translatable("screen.netheryx.soul_altar.infuse"), button -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        }).bounds(this.leftPos + SoulAltarMenu.MAIN_X + 42, this.topPos + 66, 127, 20).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.infuse.active = this.menu.getStatus() == SoulAltarBlockEntity.Status.READY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        // Infobulles du guide : survol d'une offrande
        for (int k = 0; k < SoulAltarBlockEntity.OFFERINGS.length; k++) {
            int x = this.leftPos + 10;
            int y = this.topPos + 22 + k * 34;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.renderTooltip(this.font, new ItemStack(SoulAltarBlockEntity.OFFERINGS[k].item().get()), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int main = this.leftPos + SoulAltarMenu.MAIN_X;
        graphics.blit(TEXTURE, main, this.topPos, 0, 0, 176, 186, TEX_W, TEX_H);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 176, 0, 104, 124, TEX_W, TEX_H);
        // Canal du rituel : se remplit de bas en haut pendant l'infusion
        int filled = Math.round(18 * this.menu.getProgress());
        if (filled > 0) {
            graphics.blit(TEXTURE, main + 21, this.topPos + 41 + 18 - filled, 304, 18 - filled, 8, filled, TEX_W, TEX_H);
        }
        // Surlignage de l'offrande reconnue dans le guide
        int index = this.menu.getOfferingIndex();
        if (index >= 0 && index < SoulAltarBlockEntity.OFFERINGS.length) {
            int y = this.topPos + 17 + index * 34;
            graphics.renderOutline(this.leftPos + 6, y, 92, 32, 0xFF7FE8FF);
        }
        for (int k = 0; k < SoulAltarBlockEntity.OFFERINGS.length; k++) {
            SoulAltarBlockEntity.Offering offering = SoulAltarBlockEntity.OFFERINGS[k];
            ItemStack icon = new ItemStack(offering.item().get(), offering.count());
            int x = this.leftPos + 10;
            int y = this.topPos + 22 + k * 34;
            graphics.renderItem(icon, x, y);
            graphics.renderItemDecorations(this.font, icon, x, y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        // Guide des offrandes
        graphics.drawString(this.font, Component.translatable("screen.netheryx.soul_altar.guide"), 8, 6, 0x404040, false);
        for (int k = 0; k < SoulAltarBlockEntity.OFFERINGS.length; k++) {
            SoulAltarBlockEntity.Offering offering = SoulAltarBlockEntity.OFFERINGS[k];
            int y = 20 + k * 34;
            graphics.drawString(this.font, Component.translatable(offering.enchantment().get().getDescriptionId()), 30, y, 0x5A2A8A, false);
            List<FormattedCharSequence> lines = this.font.split(
                    Component.translatable("screen.netheryx.soul_altar.targets." + offering.targets()), 66);
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                graphics.drawString(this.font, lines.get(i), 30, y + 10 + i * 9, 0x505050, false);
            }
        }
        // Cadre d'information
        int x = SoulAltarMenu.MAIN_X + 46;
        SoulAltarBlockEntity.Status status = this.menu.getStatus();
        graphics.drawString(this.font, this.headline(status), x, 21, 0xFFFFFF, false);
        int cost = this.menu.getCost();
        if (this.menu.getResultLevel() > 0 && status != SoulAltarBlockEntity.Status.MAX_LEVEL
                && status != SoulAltarBlockEntity.Status.INCOMPATIBLE) {
            boolean enough = status != SoulAltarBlockEntity.Status.NO_EXPERIENCE;
            graphics.drawString(this.font, Component.translatable("screen.netheryx.soul_altar.cost", cost)
                    .withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.RED), x, 33, 0xFFFFFF, false);
        }
        int flames = this.menu.getFlames();
        boolean enoughFlames = flames >= SoulAltarBlockEntity.REQUIRED_FLAMES;
        Component label = Component.translatable("screen.netheryx.soul_altar.flames", flames, SoulAltarBlockEntity.REQUIRED_FLAMES)
                .withStyle(enoughFlames ? ChatFormatting.AQUA : ChatFormatting.RED);
        graphics.drawString(this.font, label, x, 47, 0xFFFFFF, false);
        int iconX = x + this.font.width(label) + 3;
        int shown = Math.max(SoulAltarBlockEntity.REQUIRED_FLAMES, Math.min(MAX_FLAME_ICONS, flames));
        for (int i = 0; i < shown && iconX + i * 10 + 9 <= SoulAltarMenu.MAIN_X + 168; i++) {
            graphics.blit(TEXTURE, iconX + i * 10, 45, i < flames ? 284 : 294, 0, 9, 12, TEX_W, TEX_H);
        }
    }

    /** Premiere ligne : l'enchantement obtenu si tout est pret, sinon ce qui manque. */
    private Component headline(SoulAltarBlockEntity.Status status) {
        int index = this.menu.getOfferingIndex();
        if ((status == SoulAltarBlockEntity.Status.READY || status == SoulAltarBlockEntity.Status.IN_PROGRESS
                || status == SoulAltarBlockEntity.Status.NO_EXPERIENCE || status == SoulAltarBlockEntity.Status.NO_FLAMES
                || status == SoulAltarBlockEntity.Status.NOT_ENOUGH_OFFERING) && index >= 0) {
            Enchantment enchantment = SoulAltarBlockEntity.OFFERINGS[index].enchantment().get();
            MutableComponent name = Component.translatable(enchantment.getDescriptionId());
            if (enchantment.getMaxLevel() > 1) {
                name.append(" ").append(Component.translatable("enchantment.level." + this.menu.getResultLevel()));
            }
            ChatFormatting color = status == SoulAltarBlockEntity.Status.READY || status == SoulAltarBlockEntity.Status.IN_PROGRESS
                    ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY;
            if (status == SoulAltarBlockEntity.Status.NOT_ENOUGH_OFFERING) {
                return Component.translatable("screen.netheryx.soul_altar.status.not_enough_offering",
                        SoulAltarBlockEntity.OFFERINGS[index].count()).withStyle(ChatFormatting.RED);
            }
            return status == SoulAltarBlockEntity.Status.IN_PROGRESS
                    ? Component.translatable("screen.netheryx.soul_altar.status.in_progress").withStyle(ChatFormatting.AQUA)
                    : Component.literal("» ").append(name).withStyle(color);
        }
        ChatFormatting color = status == SoulAltarBlockEntity.Status.NO_ITEM || status == SoulAltarBlockEntity.Status.NO_OFFERING
                ? ChatFormatting.GRAY : ChatFormatting.RED;
        return Component.translatable("screen.netheryx.soul_altar.status." + status.name().toLowerCase(java.util.Locale.ROOT))
                .withStyle(color);
    }
}
