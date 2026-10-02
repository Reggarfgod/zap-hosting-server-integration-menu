package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ZHStep1LauncherScreen extends ZHBaseWizardScreen {

    private record LauncherIconInfo(ResourceLocation texture, int origW, int origH) {}

    private List<ZHLiveDataProvider.LauncherEntry> launchers;

    public ZHStep1LauncherScreen() {
        this(new ZHOrderConfig());
    }

    public ZHStep1LauncherScreen(ZHOrderConfig config) {
        super(config, 1, "Select Platform / Launcher");
        this.launchers = ZHLiveDataProvider.getLaunchers();
    }

    @Override
    protected void init() {
        super.init();
        this.launchers = ZHLiveDataProvider.getLaunchers();
    }

    private static LauncherIconInfo getIconInfo(String key) {
        String path = switch (key) {
            case "curse-twitch" -> "textures/gui/launchers/curse-twitch.png";
            case "feed-the-beast" -> "textures/gui/launchers/feed-the-beast.png";
            case "minecraft" -> "textures/gui/launchers/minecraft.png";
            case "at-launcher" -> "textures/gui/launchers/at-launcher.png";
            case "technic-launcher" -> "textures/gui/launchers/technic-launcher.png";
            case "voids-wrath-launcher" -> "textures/gui/launchers/voids-wrath-launcher.png";
            case "minecraft-adventure" -> "textures/gui/launchers/minecraft-adventure.png";
            case "minecraft-minigames" -> "textures/gui/launchers/minecraft-minigames.png";
            case "vps" -> "textures/gui/launchers/vps.png";
            case "dedicated-server" -> "textures/gui/launchers/dedicated-server.png";
            default -> "textures/gui/logo.png";
        };
        int w = switch (key) {
            case "curse-twitch" -> 512;
            case "minecraft" -> 200;
            case "voids-wrath-launcher" -> 48;
            case "vps", "dedicated-server" -> 64;
            case "feed-the-beast", "at-launcher", "technic-launcher", "minecraft-adventure", "minecraft-minigames" -> 50;
            default -> 48;
        };
        return new LauncherIconInfo(new ResourceLocation(ZapHosting.MOD_ID, path), w, w);
    }

    private void drawLauncherIcon(GuiGraphics graphics, String key, int iconX, int iconY, int iconSize) {
        LauncherIconInfo info = getIconInfo(key);
        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(iconX, iconY, 0);
        graphics.pose().scale((float) iconSize / info.origW(), (float) iconSize / info.origH(), 1.0F);
        graphics.blit(info.texture(), 0, 0, 0.0F, 0.0F, info.origW(), info.origH(), info.origW(), info.origH());
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    private record GridBounds(int startX, int startY, int cardW, int cardH, int gapX, int gapY, int cols, int rows) {}

    private GridBounds calculateGrid() {
        int centerX = this.width / 2;
        int topY = 46;
        int bottomLimit = this.height - 36;
        int availableH = Math.max(60, bottomLimit - topY);

        int cols = 3;
        int rows = Math.max(1, (launchers.size() + cols - 1) / cols);

        int maxGridW = Math.min(390, this.width - 24);
        int gapX = 8;
        int cardW = Math.max(80, (maxGridW - (cols - 1) * gapX) / cols);

        int gapY = Math.min(8, Math.max(4, (availableH - rows * 46) / (rows + 1)));
        int cardH = Math.min(48, Math.max(38, (availableH - (rows - 1) * gapY) / rows));

        int totalGridW = cols * cardW + (cols - 1) * gapX;
        int totalGridH = rows * cardH + (rows - 1) * gapY;

        int startX = centerX - totalGridW / 2;
        int startY = topY + (availableH - totalGridH) / 2;

        return new GridBounds(startX, startY, cardW, cardH, gapX, gapY, cols, rows);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        GridBounds grid = calculateGrid();
        ZHLiveDataProvider.LauncherEntry hoveredItem = null;

        for (int i = 0; i < launchers.size(); i++) {
            ZHLiveDataProvider.LauncherEntry item = launchers.get(i);
            int row = i / grid.cols();
            int col = i % grid.cols();

            int x = grid.startX() + col * (grid.cardW() + grid.gapX());
            int y = grid.startY() + row * (grid.cardH() + grid.gapY());

            boolean selected = item.key().equals(config.launcherKey);
            boolean hovered = mouseX >= x && mouseX <= x + grid.cardW() && mouseY >= y && mouseY <= y + grid.cardH();
            if (hovered) {
                hoveredItem = item;
            }

            drawCard(graphics, x, y, grid.cardW(), grid.cardH(), selected, hovered);

            // Icon square
            int iconSize = Math.min(26, grid.cardH() - 12);
            int iconX = x + 6;
            int iconY = y + (grid.cardH() - iconSize) / 2;

            graphics.fill(iconX - 1, iconY - 1, iconX + iconSize + 1, iconY + iconSize + 1, 0xFF353A3D);
            graphics.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xFF141618);

            drawLauncherIcon(graphics, item.key(), iconX, iconY, iconSize);

            // Text section
            int textX = iconX + iconSize + 6;
            int textW = x + grid.cardW() - textX - (selected ? 15 : 4);
            int titleY = y + 7;
            int priceY = y + grid.cardH() - 14;

            String name = item.displayName();
            int nameWidth = this.font.width(name);

            if (nameWidth > textW) {
                float scale = (float) textW / nameWidth;
                if (scale >= 0.76f) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(textX, titleY + 1, 0);
                    graphics.pose().scale(scale, scale, 1.0f);
                    graphics.drawString(this.font, name, 0, 0, selected ? 0xFFFFFFFF : 0xFFDDDDDD, false);
                    graphics.pose().popPose();
                } else {
                    String trimmed = this.font.plainSubstrByWidth(name, textW - 8) + "..";
                    graphics.drawString(this.font, trimmed, textX, titleY, selected ? 0xFFFFFFFF : 0xFFDDDDDD, false);
                }
            } else {
                graphics.drawString(this.font, name, textX, titleY, selected ? 0xFFFFFFFF : 0xFFDDDDDD, false);
            }

            // Price badge
            graphics.drawString(this.font, item.fromPrice(), textX, priceY, COLOR_ZAP_GREEN, false);
        }

        // Tooltip for hovered card
        if (hoveredItem != null) {
            graphics.renderTooltip(this.font, Component.literal(hoveredItem.displayName() + " (" + hoveredItem.fromPrice() + ")"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GridBounds grid = calculateGrid();

        for (int i = 0; i < launchers.size(); i++) {
            int row = i / grid.cols();
            int col = i % grid.cols();

            int x = grid.startX() + col * (grid.cardW() + grid.gapX());
            int y = grid.startY() + row * (grid.cardH() + grid.gapY());

            if (mouseX >= x && mouseX <= x + grid.cardW() && mouseY >= y && mouseY <= y + grid.cardH()) {
                ZHLiveDataProvider.LauncherEntry selectedLauncher = launchers.get(i);
                boolean wasSelected = selectedLauncher.key().equals(config.launcherKey);
                config.launcherKey = selectedLauncher.key();
                config.launcherName = selectedLauncher.displayName();

                if (wasSelected) {
                    onNext();
                    return true;
                }

                // Asynchronously pre-fetch in background without stalling GUI thread
                CompletableFuture.runAsync(() -> ZHLiveDataProvider.fetchGamesForLauncher(selectedLauncher.key()));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
