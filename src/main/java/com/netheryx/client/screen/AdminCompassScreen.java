package com.netheryx.client.screen;

import com.netheryx.network.AdminTeleportPacket;
import com.netheryx.network.NetheryXNetwork;
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
 * Interface de la Boussole du createur : deux onglets (structures / biomes), recherche, pagination.
 * Un clic teleporte directement le joueur.
 */
public class AdminCompassScreen extends Screen {
    private static final int BUTTON_WIDTH = 240;

    private final List<ResourceLocation> structures;
    private final List<ResourceLocation> biomes;
    private boolean biomeMode;
    private List<ResourceLocation> filtered = List.of();
    private EditBox search;
    private int page;

    public AdminCompassScreen(List<ResourceLocation> structures, List<ResourceLocation> biomes) {
        super(Component.translatable("screen.netheryx.admin_compass.title"));
        this.structures = structures;
        this.biomes = biomes;
    }

    private Component name(ResourceLocation id) {
        if (!this.biomeMode) {
            return SoulCompassScreen.displayName(id);
        }
        String key = "biome." + id.getNamespace() + "." + id.getPath();
        return I18n.exists(key) ? Component.literal(I18n.get(key) + " (" + id.getNamespace() + ")") : Component.literal(id.toString());
    }

    @Override
    protected void init() {
        String previous = this.search == null ? "" : this.search.getValue();
        this.search = new EditBox(this.font, this.width / 2 - BUTTON_WIDTH / 2, 56, BUTTON_WIDTH, 18,
                Component.translatable("screen.netheryx.soul_compass.search"));
        this.search.setValue(previous);
        this.search.setResponder(text -> {
            this.page = 0;
            this.rebuild();
        });
        this.rebuild();
    }

    private void rebuild() {
        String query = this.search.getValue().toLowerCase(Locale.ROOT);
        List<ResourceLocation> result = new ArrayList<>();
        for (ResourceLocation id : this.biomeMode ? this.biomes : this.structures) {
            if (query.isEmpty() || id.toString().contains(query) || this.name(id).getString().toLowerCase(Locale.ROOT).contains(query)) {
                result.add(id);
            }
        }
        this.filtered = result;
        this.clearWidgets();
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        Button structuresTab = Button.builder(Component.translatable("screen.netheryx.admin_compass.structures"), b -> this.switchMode(false))
                .bounds(x, 30, BUTTON_WIDTH / 2 - 2, 20).build();
        Button biomesTab = Button.builder(Component.translatable("screen.netheryx.admin_compass.biomes"), b -> this.switchMode(true))
                .bounds(x + BUTTON_WIDTH / 2 + 2, 30, BUTTON_WIDTH / 2 - 2, 20).build();
        structuresTab.active = this.biomeMode;
        biomesTab.active = !this.biomeMode;
        this.addRenderableWidget(structuresTab);
        this.addRenderableWidget(biomesTab);
        this.addRenderableWidget(this.search);
        int pages = Math.max(1, (this.filtered.size() + this.perPage() - 1) / this.perPage());
        this.page = Math.min(this.page, pages - 1);
        int y = 80;
        for (int i = this.page * this.perPage(); i < Math.min(this.filtered.size(), (this.page + 1) * this.perPage()); i++) {
            ResourceLocation id = this.filtered.get(i);
            this.addRenderableWidget(Button.builder(this.name(id), b -> this.select(id)).bounds(x, y, BUTTON_WIDTH, 20).build());
            y += 22;
        }
        Button previous = Button.builder(Component.literal("<"), b -> {
            this.page--;
            this.rebuild();
        }).bounds(x, 80 + this.perPage() * 22 + 6, 40, 20).build();
        previous.active = this.page > 0;
        Button next = Button.builder(Component.literal(">"), b -> {
            this.page++;
            this.rebuild();
        }).bounds(x + BUTTON_WIDTH - 40, 80 + this.perPage() * 22 + 6, 40, 20).build();
        next.active = this.page < pages - 1;
        this.addRenderableWidget(previous);
        this.addRenderableWidget(next);
        this.setInitialFocus(this.search);
    }

    private void switchMode(boolean biomes) {
        this.biomeMode = biomes;
        this.page = 0;
        this.rebuild();
    }

    private void select(ResourceLocation id) {
        NetheryXNetwork.CHANNEL.sendToServer(new AdminTeleportPacket(id, this.biomeMode));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFD36B);
        int pages = Math.max(1, (this.filtered.size() + this.perPage() - 1) / this.perPage());
        graphics.drawCenteredString(this.font, Component.translatable("screen.netheryx.admin_compass.page", this.page + 1, pages,
                this.filtered.size()), this.width / 2, 80 + this.perPage() * 22 + 12, 0xAAAAAA);
    }

    /** Nombre de lignes par page selon la hauteur de la fenetre (les boutons de navigation restent visibles). */
    private int perPage() {
        return Math.max(3, (this.height - 80 - 40) / 22);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int pages = Math.max(1, (this.filtered.size() + this.perPage() - 1) / this.perPage());
        int target = Math.max(0, Math.min(pages - 1, this.page - (int) Math.signum(delta)));
        if (target != this.page) {
            this.page = target;
            this.rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
