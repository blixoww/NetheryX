package com.netheryx.client.screen;

import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.SelectRecallPacket;
import com.netheryx.util.PortalMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Interface de la Pierre de rappel : liste des portails memorises (dimension, coordonnees, distance),
 * un bouton pour rejoindre le point de reapparition et, pour chaque portail, un bouton pour l'oublier.
 */
public class RecallStoneScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int FORGET_WIDTH = 20;

    private final List<PortalMemory.Portal> portals;

    public RecallStoneScreen(List<PortalMemory.Portal> portals) {
        super(Component.translatable("screen.netheryx.recall_stone.title"));
        this.portals = portals;
    }

    /** Nom de dimension : traduction si elle existe, sinon l'identifiant mis en forme. */
    public static Component dimensionName(ResourceLocation id) {
        String key = "dimension." + id.getNamespace() + "." + id.getPath();
        if (I18n.exists(key)) {
            return Component.translatable(key);
        }
        StringBuilder pretty = new StringBuilder();
        for (String word : id.getPath().split("[_/]")) {
            if (!word.isEmpty()) {
                pretty.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return Component.literal(pretty.toString().trim());
    }

    private Component label(PortalMemory.Portal portal) {
        BlockPos pos = portal.pos();
        Minecraft minecraft = Minecraft.getInstance();
        String coords = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        if (minecraft.player != null && minecraft.player.level().dimension().location().equals(portal.dimension())) {
            int distance = (int) Math.sqrt(minecraft.player.blockPosition().distSqr(pos));
            return Component.translatable("screen.netheryx.recall_stone.portal_near", dimensionName(portal.dimension()), coords, distance);
        }
        return Component.translatable("screen.netheryx.recall_stone.portal", dimensionName(portal.dimension()), coords);
    }

    @Override
    protected void init() {
        int total = this.portals.size() + 1;
        int x = this.width / 2 - (BUTTON_WIDTH + FORGET_WIDTH + 4) / 2;
        int y = Math.max(36, this.height / 2 - total * 11);
        for (int i = 0; i < this.portals.size(); i++) {
            int index = i;
            this.addRenderableWidget(Button.builder(this.label(this.portals.get(i)), button -> this.select(index, false))
                    .bounds(x, y, BUTTON_WIDTH, 20).build());
            Button forget = Button.builder(Component.literal("x"), button -> this.select(index, true))
                    .bounds(x + BUTTON_WIDTH + 4, y, FORGET_WIDTH, 20)
                    .tooltip(Tooltip.create(Component.translatable("screen.netheryx.recall_stone.forget"))).build();
            this.addRenderableWidget(forget);
            y += 22;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("screen.netheryx.recall_stone.respawn"),
                button -> this.select(SelectRecallPacket.RESPAWN, false)).bounds(x, y + 6, BUTTON_WIDTH, 20).build());
    }

    private void select(int index, boolean forget) {
        NetheryXNetwork.CHANNEL.sendToServer(new SelectRecallPacket(index, forget));
        if (!forget) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int total = this.portals.size() + 1;
        int top = Math.max(36, this.height / 2 - total * 11);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top - 22, 0xD07CFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
