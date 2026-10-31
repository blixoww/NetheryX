package com.netheryx.altar;

import com.netheryx.NetheryX;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.List;

/** Interface de l'autel des ames (texture de la version 1.20.1 : panneau principal et guide des offrandes). */
public class GuiSoulAltar extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetheryX.MOD_ID, "textures/gui/soul_altar.png");
    private static final float TEX_W = 512.0F;
    private static final float TEX_H = 256.0F;
    private static final int MAIN = ContainerSoulAltar.MAIN_X;
    private final TileSoulAltar altar;
    private final InventoryPlayer inventory;
    private GuiButton infuse;

    public GuiSoulAltar(InventoryPlayer inventory, TileSoulAltar altar) {
        super(new ContainerSoulAltar(inventory, altar));
        this.altar = altar;
        this.inventory = inventory;
        this.xSize = MAIN + 176;
        this.ySize = 186;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        this.infuse = new GuiButton(0, this.guiLeft + MAIN + 42, this.guiTop + 66, 127, 20,
                StatCollector.translateToLocal("screen.netheryx.soul_altar.infuse"));
        this.buttonList.add(this.infuse);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.infuse.enabled = this.altar.check(this.mc.thePlayer) == TileSoulAltar.Status.READY;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 0);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int main = this.guiLeft + MAIN;
        Gui.func_146110_a(main, this.guiTop, 0, 0, 176, 186, TEX_W, TEX_H);
        Gui.func_146110_a(this.guiLeft, this.guiTop, 176, 0, 104, 124, TEX_W, TEX_H);
        int filled = Math.round(18.0F * this.altar.getProgress() / TileSoulAltar.RITUAL_TICKS);
        if (filled > 0) {
            Gui.func_146110_a(main + 21, this.guiTop + 41 + 18 - filled, 304, 18 - filled, 8, filled, TEX_W, TEX_H);
        }
        int flames = this.altar.flames();
        int shown = Math.max(TileSoulAltar.REQUIRED_FLAMES, Math.min(6, flames));
        for (int i = 0; i < shown; i++) {
            Gui.func_146110_a(main + 46 + i * 10, this.guiTop + 56, i < flames ? 284 : 294, 0, 9, 12, TEX_W, TEX_H);
        }
        int index = TileSoulAltar.offeringIndex(this.altar.getStackInSlot(TileSoulAltar.SLOT_OFFERING));
        if (index >= 0) {
            int y = this.guiTop + 17 + index * 34;
            drawRect(this.guiLeft + 6, y, this.guiLeft + 98, y + 1, 0xFF7FE8FF);
            drawRect(this.guiLeft + 6, y + 31, this.guiLeft + 98, y + 32, 0xFF7FE8FF);
            drawRect(this.guiLeft + 6, y, this.guiLeft + 7, y + 32, 0xFF7FE8FF);
            drawRect(this.guiLeft + 97, y, this.guiLeft + 98, y + 32, 0xFF7FE8FF);
        }
        TileSoulAltar.Offering[] offerings = TileSoulAltar.offerings();
        for (int k = 0; k < offerings.length; k++) {
            ItemStack icon = new ItemStack(offerings[k].item, offerings[k].count);
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), icon, this.guiLeft + 10, this.guiTop + 22 + k * 34);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), icon, this.guiLeft + 10, this.guiTop + 22 + k * 34);
        }
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(StatCollector.translateToLocal("tile.netheryx.soul_altar.name"), MAIN + 8, 6, 0x404040);
        this.fontRendererObj.drawString(StatCollector.translateToLocal(this.inventory.getInventoryName()), MAIN + 8, 93, 0x404040);
        this.fontRendererObj.drawString(StatCollector.translateToLocal("screen.netheryx.soul_altar.guide"), 8, 6, 0x404040);
        TileSoulAltar.Offering[] offerings = TileSoulAltar.offerings();
        for (int k = 0; k < offerings.length; k++) {
            int y = 20 + k * 34;
            if (offerings[k].enchantment != null) {
                this.fontRendererObj.drawString(StatCollector.translateToLocal(offerings[k].enchantment.getName()), 30, y, 0x5A2A8A);
            }
            List<String> lines = this.fontRendererObj.listFormattedStringToWidth(
                    StatCollector.translateToLocal("screen.netheryx.soul_altar.targets." + offerings[k].targets), 66);
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                this.fontRendererObj.drawString(lines.get(i), 30, y + 10 + i * 9, 0x505050);
            }
        }
        int x = MAIN + 46;
        TileSoulAltar.Status status = this.altar.check(this.mc.thePlayer);
        this.fontRendererObj.drawString(headline(status), x, 21, 0xFFFFFF);
        int level = this.altar.resultLevel();
        if (level > 0 && status != TileSoulAltar.Status.MAX_LEVEL && status != TileSoulAltar.Status.INCOMPATIBLE) {
            boolean enough = status != TileSoulAltar.Status.NO_EXPERIENCE;
            this.fontRendererObj.drawString((enough ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + StatCollector.translateToLocalFormatted(
                    "screen.netheryx.soul_altar.cost", level * TileSoulAltar.LEVELS_PER_ENCHANT_LEVEL), x, 33, 0xFFFFFF);
        }
        int flames = this.altar.flames();
        this.fontRendererObj.drawString((flames >= TileSoulAltar.REQUIRED_FLAMES ? EnumChatFormatting.AQUA : EnumChatFormatting.RED)
                + StatCollector.translateToLocalFormatted("screen.netheryx.soul_altar.flames", flames, TileSoulAltar.REQUIRED_FLAMES), x, 45, 0xFFFFFF);
    }

    private String headline(TileSoulAltar.Status status) {
        int index = TileSoulAltar.offeringIndex(this.altar.getStackInSlot(TileSoulAltar.SLOT_OFFERING));
        boolean named = status == TileSoulAltar.Status.READY || status == TileSoulAltar.Status.IN_PROGRESS
                || status == TileSoulAltar.Status.NO_EXPERIENCE || status == TileSoulAltar.Status.NO_FLAMES
                || status == TileSoulAltar.Status.NOT_ENOUGH_OFFERING;
        if (named && index >= 0) {
            TileSoulAltar.Offering offering = TileSoulAltar.offerings()[index];
            String name = offering.enchantment.getTranslatedName(this.altar.resultLevel());
            boolean ready = status == TileSoulAltar.Status.READY || status == TileSoulAltar.Status.IN_PROGRESS;
            if (status == TileSoulAltar.Status.NOT_ENOUGH_OFFERING) {
                return EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("screen.netheryx.soul_altar.status.not_enough_offering", offering.count);
            }
            return (ready ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.GRAY) + name;
        }
        return EnumChatFormatting.GRAY + StatCollector.translateToLocal("screen.netheryx.soul_altar.status." + status.name().toLowerCase(java.util.Locale.ROOT));
    }
}
