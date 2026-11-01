package com.netheryx.client.screen;

import com.netheryx.world.NetherAtlasData;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.Locale;

/** Atlas du Nether : carte des cases explorees, a faire glisser et a zoomer, avec les structures visitees. */
public class NetherAtlasScreen extends Screen {
    private static final int MARGIN = 20;
    private final Long2IntMap cells;
    private final List<String> biomes;
    private final List<Integer> colors;
    private final List<NetherAtlasData.Marker> markers;
    private final int playerX;
    private final int playerZ;
    private int zoom = 4;
    private double panX;
    private double panZ;

    public NetherAtlasScreen(Long2IntMap cells, List<String> biomes, List<Integer> colors, List<NetherAtlasData.Marker> markers, int playerX, int playerZ) {
        super(Component.translatable("item.netheryx.nether_atlas"));
        this.cells = cells;
        this.biomes = biomes;
        this.colors = colors;
        this.markers = markers;
        this.playerX = playerX;
        this.playerZ = playerZ;
    }

    private double cellToScreenX(double blockX) {
        return this.width / 2.0D + (blockX - this.playerX) / NetherAtlasData.CELL * this.zoom + this.panX;
    }

    private double cellToScreenY(double blockZ) {
        return this.height / 2.0D + (blockZ - this.playerZ) / NetherAtlasData.CELL * this.zoom + this.panZ;
    }

    /** Le brouillard des biomes est sombre : on l'eclaircit pour que la carte se lise. */
    private static int brighten(int rgb) {
        int r = Math.min(255, ((rgb >> 16) & 255) * 3 / 2 + 24);
        int g = Math.min(255, ((rgb >> 8) & 255) * 3 / 2 + 24);
        int b = Math.min(255, (rgb & 255) * 3 / 2 + 24);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int left = MARGIN;
        int top = MARGIN + 10;
        int right = this.width - MARGIN;
        int bottom = this.height - MARGIN;
        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, 0xFF2A1A12);
        graphics.fill(left, top, right, bottom, 0xFF120A08);
        graphics.enableScissor(left, top, right, bottom);
        String hovered = null;
        for (Long2IntMap.Entry entry : this.cells.long2IntEntrySet()) {
            int cx = ChunkPos.getX(entry.getLongKey());
            int cz = ChunkPos.getZ(entry.getLongKey());
            int x = (int) Math.floor(this.cellToScreenX(cx * (double) NetherAtlasData.CELL));
            int y = (int) Math.floor(this.cellToScreenY(cz * (double) NetherAtlasData.CELL));
            if (x + this.zoom < left || x > right || y + this.zoom < top || y > bottom) {
                continue;
            }
            int index = entry.getIntValue();
            graphics.fill(x, y, x + this.zoom, y + this.zoom, brighten(index < this.colors.size() ? this.colors.get(index) : 0x552222));
            if (mouseX >= x && mouseX < x + this.zoom && mouseY >= y && mouseY < y + this.zoom && index < this.biomes.size()) {
                hovered = this.biomes.get(index);
            }
        }
        NetherAtlasData.Marker hoveredMarker = null;
        for (NetherAtlasData.Marker marker : this.markers) {
            int x = (int) this.cellToScreenX(marker.x());
            int y = (int) this.cellToScreenY(marker.z());
            graphics.fill(x - 3, y - 3, x + 3, y + 3, 0xFF000000);
            graphics.fill(x - 2, y - 2, x + 2, y + 2, 0xFFFFD36B);
            if (Math.abs(mouseX - x) <= 4 && Math.abs(mouseY - y) <= 4) {
                hoveredMarker = marker;
            }
        }
        int px = (int) this.cellToScreenX(this.playerX);
        int py = (int) this.cellToScreenY(this.playerZ);
        graphics.fill(px - 2, py - 2, px + 2, py + 2, 0xFFFFFFFF);
        graphics.fill(px - 1, py - 1, px + 1, py + 1, 0xFFFF2020);
        graphics.disableScissor();
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFE0C0);
        if (hoveredMarker != null) {
            graphics.renderTooltip(this.font, Component.literal(prettyName(hoveredMarker.structure())), mouseX, mouseY);
        } else if (hovered != null) {
            ResourceLocation id = new ResourceLocation(hovered);
            graphics.renderTooltip(this.font, Component.translatable("biome." + id.getNamespace() + "." + id.getPath()), mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static String prettyName(String structure) {
        String path = new ResourceLocation(structure).getPath().replace('_', ' ');
        return path.isEmpty() ? structure : path.substring(0, 1).toUpperCase(Locale.ROOT) + path.substring(1);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.panX += dragX;
        this.panZ += dragY;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int previous = this.zoom;
        this.zoom = Mth.clamp(this.zoom + (delta > 0 ? 1 : -1), 1, 12);
        double ratio = (double) this.zoom / previous;
        this.panX *= ratio;
        this.panZ *= ratio;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
