package com.netheryx.client.screen;

import com.netheryx.network.NetheryXNetwork;
import com.netheryx.network.SelectStructurePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface de la boussole des ames : liste paginee et filtrable de toutes les structures (vanilla et moddees)
 * qui peuvent exister dans la dimension du joueur. Cliquer une structure lance la recherche cote serveur.
 */
public class SoulCompassScreen extends Screen {
    private static final int PER_PAGE = 8;
    private static final int BUTTON_WIDTH = 240;

    private final List<ResourceLocation> allStructures;
    private List<ResourceLocation> filtered;
    private EditBox search;
    private int page;

    public SoulCompassScreen(List<ResourceLocation> structures) {
        super(Component.translatable("screen.netheryx.soul_compass.title"));
        this.allStructures = structures;
        this.filtered = structures;
    }

    /** Nom affiche : traduction si elle existe, sinon l'identifiant mis en forme ("Bastion Remnant (minecraft)"). */
    public static Component displayName(ResourceLocation id) {
        String key = "structure." + id.getNamespace() + "." + id.getPath();
        if (I18n.exists(key)) {
            return Component.translatable(key);
        }
        String path = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        StringBuilder pretty = new StringBuilder();
        for (String word : path.split("_")) {
            if (!word.isEmpty()) {
                pretty.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return Component.literal(pretty.toString().trim() + " (" + id.getNamespace() + ")");
    }

    @Override
    protected void init() {
        String previous = this.search == null ? "" : this.search.getValue();
        this.search = new EditBox(this.font, this.width / 2 - BUTTON_WIDTH / 2, 32, BUTTON_WIDTH, 18,
                Component.translatable("screen.netheryx.soul_compass.search"));
        this.search.setValue(previous);
        this.search.setResponder(text -> {
            this.page = 0;
            this.applyFilter(text);
            this.rebuildButtons();
        });
        this.applyFilter(previous);
        this.rebuildButtons();
    }

    private void applyFilter(String text) {
        String query = text.toLowerCase(Locale.ROOT);
        List<ResourceLocation> result = new ArrayList<>();
        for (ResourceLocation id : this.allStructures) {
            if (query.isEmpty() || id.toString().contains(query)
                    || displayName(id).getString().toLowerCase(Locale.ROOT).contains(query)) {
                result.add(id);
            }
        }
        this.filtered = result;
    }

    private void rebuildButtons() {
        this.clearWidgets();
        this.addRenderableWidget(this.search);
        int pages = Math.max(1, (this.filtered.size() + PER_PAGE - 1) / PER_PAGE);
        this.page = Math.min(this.page, pages - 1);
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        int y = 56;
        for (int i = this.page * PER_PAGE; i < Math.min(this.filtered.size(), (this.page + 1) * PER_PAGE); i++) {
            ResourceLocation id = this.filtered.get(i);
            this.addRenderableWidget(Button.builder(displayName(id), button -> this.select(id))
                    .bounds(x, y, BUTTON_WIDTH, 20).build());
            y += 22;
        }
        Button previous = Button.builder(Component.literal("<"), button -> {
            this.page--;
            this.rebuildButtons();
        }).bounds(x, y + 6, 40, 20).build();
        previous.active = this.page > 0;
        Button next = Button.builder(Component.literal(">"), button -> {
            this.page++;
            this.rebuildButtons();
        }).bounds(x + BUTTON_WIDTH - 40, y + 6, 40, 20).build();
        next.active = this.page < pages - 1;
        this.addRenderableWidget(previous);
        this.addRenderableWidget(next);
        this.setInitialFocus(this.search);
    }

    private void select(ResourceLocation id) {
        NetheryXNetwork.CHANNEL.sendToServer(new SelectStructurePacket(id));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0x7FE8FF);
        int pages = Math.max(1, (this.filtered.size() + PER_PAGE - 1) / PER_PAGE);
        graphics.drawCenteredString(this.font, Component.translatable("screen.netheryx.soul_compass.page", this.page + 1, pages,
                this.filtered.size()), this.width / 2, 56 + PER_PAGE * 22 + 12, 0xAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
