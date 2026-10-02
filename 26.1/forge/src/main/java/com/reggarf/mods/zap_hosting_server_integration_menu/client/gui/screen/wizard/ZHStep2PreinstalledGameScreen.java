package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ZHStep2PreinstalledGameScreen extends ZHBaseWizardScreen {

    private List<ZHLiveDataProvider.GameItem> allGames = new ArrayList<>();
    private List<ZHLiveDataProvider.GameItem> filteredGames = new ArrayList<>();
    private EditBox searchBox;
    private int scrollOffset = 0;
    private final int ITEM_HEIGHT = 20;

    private static String getTitleForLauncher(ZHOrderConfig config) {
        if ("vps".equals(config.launcherKey)) return "Preinstalled Operating System";
        if ("dedicated-server".equals(config.launcherKey)) return "Select Dedicated Server Hardware";
        return "Preinstalled Game / Modpack";
    }

    public ZHStep2PreinstalledGameScreen(ZHOrderConfig config) {
        super(config, 2, getTotalSteps(config), getTitleForLauncher(config));
        refreshList();
    }

    private void refreshList() {
        allGames = ZHLiveDataProvider.getGames(config.launcherKey);
        filterList(searchBox != null ? searchBox.getValue() : "");
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int listW = Math.min(340, this.width - 30);
        searchBox = new EditBox(this.font, centerX - listW / 2, 44, listW, 16, Component.literal("Search..."));
        searchBox.setResponder(this::filterList);

        String hint = "vps".equals(config.launcherKey) ? "Type to filter operating systems..." :
                ("dedicated-server".equals(config.launcherKey) ? "Type to filter server hardware..." : "Type to filter games / modpacks...");
        searchBox.setHint(Component.literal(hint));
        addRenderableWidget(searchBox);

        refreshList();
    }

    private void filterList(String query) {
        filteredGames.clear();
        String lower = query.toLowerCase().trim();
        for (ZHLiveDataProvider.GameItem g : allGames) {
            if (lower.isEmpty() || g.displayName().toLowerCase().contains(lower)) {
                filteredGames.add(g);
            }
        }
        scrollOffset = 0;
    }

    private boolean isGameSelected(ZHLiveDataProvider.GameItem game) {
        if ("vps".equals(config.launcherKey)) {
            return game.displayName().equals(config.vpsOs) || game.optionId().equals(config.vpsOsId) || game.optionId().equals(config.pgid);
        }
        if ("dedicated-server".equals(config.launcherKey)) {
            return game.optionId().equals(config.dediModelId) || game.displayName().equals(config.dediModelName) || game.optionId().equals(config.pgid);
        }
        return game.optionId().equals(config.pgid) || game.displayName().equals(config.gameName);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int listW = Math.min(340, this.width - 30);
        int listX = centerX - listW / 2;
        int listY = 64;
        int listH = this.height - listY - 38;

        // Container background
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xEE1E2325);
        graphics.outline(listX, listY, listW, listH, 0xFF353A3D);

        int visibleItems = listH / ITEM_HEIGHT;
        int maxOffset = Math.max(0, filteredGames.size() - visibleItems);
        scrollOffset = Math.min(scrollOffset, maxOffset);

        if (filteredGames.isEmpty()) {
            graphics.centeredText(this.font, "No matching items found", centerX, listY + 20, 0xFFAAAAAA);
            return;
        }

        graphics.enableScissor(listX + 1, listY + 1, listX + listW - 1, listY + listH - 1);

        for (int i = 0; i < visibleItems; i++) {
            int index = scrollOffset + i;
            if (index >= filteredGames.size()) break;

            ZHLiveDataProvider.GameItem game = filteredGames.get(index);
            int itemY = listY + i * ITEM_HEIGHT;

            boolean isSelected = isGameSelected(game);
            boolean isHovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT && mouseY >= listY && mouseY <= listY + listH;

            if (isSelected) {
                graphics.fill(listX + 2, itemY + 1, listX + listW - 2, itemY + ITEM_HEIGHT - 1, 0xFF2D3A2F);
                graphics.outline(listX + 2, itemY + 1, listW - 4, ITEM_HEIGHT - 2, COLOR_ZAP_GREEN);
            } else if (isHovered) {
                graphics.fill(listX + 2, itemY + 1, listX + listW - 2, itemY + ITEM_HEIGHT - 1, 0xFF2A2F33);
            }

            // Green indicator icon
            graphics.fill(listX + 6, itemY + 8, listX + 10, itemY + 12, COLOR_ZAP_GREEN);

            String name = game.displayName();
            int maxChars = (listW - 32) / 6;
            if (name.length() > maxChars) {
                name = name.substring(0, Math.max(5, maxChars - 2)) + "..";
            }
            graphics.text(this.font, name, listX + 16, itemY + 6, isSelected ? 0xFFFFFFFF : 0xFFCCCCCC);

            if (isSelected) {
                graphics.text(this.font, "\u2713", listX + listW - 16, itemY + 6, COLOR_ZAP_GREEN);
            }
        }

        graphics.disableScissor();

        // Scrollbar indicator
        if (filteredGames.size() > visibleItems) {
            int scrollBarH = Math.max(15, (visibleItems * listH) / filteredGames.size());
            int scrollBarY = listY + (scrollOffset * (listH - scrollBarH)) / maxOffset;
            graphics.fill(listX + listW - 4, scrollBarY, listX + listW - 1, scrollBarY + scrollBarH, COLOR_ZAP_GREEN);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listY = 64;
        int listH = this.height - listY - 38;
        int visibleItems = listH / ITEM_HEIGHT;
        int maxOffset = Math.max(0, filteredGames.size() - visibleItems);

        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int centerX = this.width / 2;
            int listW = Math.min(340, this.width - 30);
            int listX = centerX - listW / 2;
            int listY = 64;
            int listH = this.height - listY - 38;

            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int clickedRow = (int) ((mouseY - listY) / ITEM_HEIGHT);
                int index = scrollOffset + clickedRow;
                if (index >= 0 && index < filteredGames.size()) {
                    ZHLiveDataProvider.GameItem chosen = filteredGames.get(index);
                    boolean wasSelected = isGameSelected(chosen);

                    config.pgid = chosen.optionId();
                    config.gameName = chosen.displayName();
                    try {
                        config.gameId = Integer.parseInt(chosen.optionId());
                    } catch (Exception ignored) {}

                    if ("vps".equals(config.launcherKey)) {
                        config.vpsOs = chosen.displayName();
                        config.vpsOsId = chosen.optionId();
                    } else if ("dedicated-server".equals(config.launcherKey)) {
                        config.dediModelName = chosen.displayName();
                        config.dediModelId = chosen.optionId();
                        if (chosen.displayName().contains("$67.15")) config.dediBasePrice = 67.15;
                        else if (chosen.displayName().contains("$79.00")) config.dediBasePrice = 79.00;
                        else if (chosen.displayName().contains("$89.00")) config.dediBasePrice = 89.00;
                        else if (chosen.displayName().contains("$119.00")) config.dediBasePrice = 119.00;
                        else if (chosen.displayName().contains("$149.00")) config.dediBasePrice = 149.00;
                        else if (chosen.displayName().contains("$189.00")) config.dediBasePrice = 189.00;
                        else if (chosen.displayName().contains("$249.00")) config.dediBasePrice = 249.00;
                        else if (chosen.displayName().contains("$299.00")) config.dediBasePrice = 299.00;
                        else if (chosen.displayName().contains("$499.00")) config.dediBasePrice = 499.00;
                    }

                    if (wasSelected) {
                        onNext();
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}
