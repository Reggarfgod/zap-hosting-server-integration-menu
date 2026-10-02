package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

public class ZHVpsStep5ResourcesScreen extends ZHBaseWizardScreen {

    private int scrollOffset = 0;
    private int contentTotalHeight = 250;

    public ZHVpsStep5ResourcesScreen(ZHOrderConfig config) {
        super(config, 5, getTotalSteps(config), "VPS RAM & Storage");
    }

    private void drawChip(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean selected, boolean hovered) {
        int bg = selected ? 0xFF283B28 : (hovered ? COLOR_CARD_HOVER : COLOR_CARD_BG);
        int border = selected ? COLOR_CARD_SELECTED : (hovered ? 0xFF666666 : COLOR_CARD_BORDER);

        graphics.fill(x, y, x + width, y + height, bg);
        graphics.fill(x, y, x + width, y + 1, border);
        graphics.fill(x, y + height - 1, x + width, y + height, border);
        graphics.fill(x, y + 1, x + 1, y + height, border);
        graphics.fill(x + width - 1, y, x + width, y + height, border);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        List<ZHLiveDataProvider.OptionStep> ramSteps = ZHLiveDataProvider.getVpsRamSteps();
        List<ZHLiveDataProvider.OptionStep> diskSteps = ZHLiveDataProvider.getVpsDiskSteps();
        List<ZHLiveDataProvider.OptionStep> ipSteps = ZHLiveDataProvider.getVpsIpSteps();

        int centerX = this.width / 2;
        int panelW = Math.min(340, this.width - 24);
        int startX = centerX - panelW / 2;
        int listY = 46;
        int listH = this.height - listY - 34;

        int maxScroll = Math.max(0, contentTotalHeight - listH);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        graphics.enableScissor(startX - 2, listY - 1, startX + panelW + 2, listY + listH + 1);

        int curY = listY - scrollOffset;

        // 1. RAM Memory
        graphics.text(this.font, "RAM Memory Allocation (Live from ZAP-Hosting):", startX, curY, 0xFFFFFFFF, false);
        curY += 13;
        curY = renderChipGrid(graphics, startX, curY, panelW, 20, ramSteps, config.vpsRamGB, mouseX, mouseY, listY, listH);
        curY += 12;

        // 2. NVMe SSD Storage
        graphics.text(this.font, "NVMe SSD Storage Space (Live from ZAP-Hosting):", startX, curY, 0xFFFFFFFF, false);
        curY += 13;
        curY = renderChipGrid(graphics, startX, curY, panelW, 20, diskSteps, config.vpsDiskGB, mouseX, mouseY, listY, listH);
        curY += 12;

        // 3. Dedicated IPv4 Addresses
        graphics.text(this.font, "Dedicated IPv4 Addresses (Live from ZAP-Hosting):", startX, curY, 0xFFFFFFFF, false);
        curY += 13;
        curY = renderChipGrid(graphics, startX, curY, panelW, 20, ipSteps, config.vpsIps, mouseX, mouseY, listY, listH);
        curY += 14;

        // Live Config Summary Card
        int summaryH = 26;
        if (curY + summaryH >= listY && curY <= listY + listH) {
            graphics.fill(startX, curY, startX + panelW, curY + summaryH, 0x881E2224);
            graphics.outline(startX, curY, panelW, summaryH, 0xFF353C40);
            String specText = config.vpsCpuCores + " Cores | " + config.vpsRamGB + " GB RAM | " + config.vpsDiskGB + " GB NVMe | " + config.vpsIps + " IPv4";
            graphics.centeredText(this.font, specText, centerX, curY + 8, COLOR_ZAP_GREEN);
        }
        curY += summaryH + 10;

        contentTotalHeight = curY - (listY - scrollOffset);

        graphics.disableScissor();

        // Scrollbar if needed
        if (maxScroll > 0) {
            int scrollBarH = Math.max(15, (listH * listH) / contentTotalHeight);
            int scrollBarY = listY + (scrollOffset * (listH - scrollBarH)) / maxScroll;
            graphics.fill(startX + panelW + 2, scrollBarY, startX + panelW + 5, scrollBarY + scrollBarH, COLOR_ZAP_GREEN);
        }
    }

    private int renderChipGrid(GuiGraphicsExtractor graphics, int x, int y, int w, int h, List<ZHLiveDataProvider.OptionStep> steps, int currentVal, int mouseX, int mouseY, int listY, int listH) {
        if (steps == null || steps.isEmpty()) return y;

        int cols = steps.size() <= 4 ? steps.size() : (steps.size() <= 6 ? 3 : 4);
        int gap = 4;
        int chipW = (w - (cols - 1) * gap) / cols;

        int curY = y;
        for (int i = 0; i < steps.size(); i++) {
            int col = i % cols;
            if (col == 0 && i > 0) {
                curY += h + gap;
            }
            int cx = x + col * (chipW + gap);
            ZHLiveDataProvider.OptionStep step = steps.get(i);
            boolean sel = step.numericValue() == currentVal;

            if (curY + h >= listY && curY <= listY + listH) {
                boolean hov = mouseX >= cx && mouseX <= cx + chipW && mouseY >= curY && mouseY <= curY + h && mouseY >= listY && mouseY <= listY + listH;
                drawChip(graphics, cx, curY, chipW, h, sel, hov);

                String lbl = step.label();
                if (lbl.contains(" - ")) {
                    lbl = lbl.substring(0, lbl.indexOf(" - "));
                }
                graphics.centeredText(this.font, lbl, cx + chipW / 2, curY + (h - 8) / 2, sel ? 0xFFFFFFFF : 0xFFCCCCCC);
            }
        }
        return curY + h;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listY = 46;
        int listH = this.height - listY - 34;
        int maxScroll = Math.max(0, contentTotalHeight - listH);

        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 20);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 20);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int centerX = this.width / 2;
            int panelW = Math.min(340, this.width - 24);
            int startX = centerX - panelW / 2;
            int listY = 46;
            int listH = this.height - listY - 34;

            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseY < listY || mouseY > listY + listH) {
                return super.mouseClicked(event, doubleClick);
            }

            List<ZHLiveDataProvider.OptionStep> ramSteps = ZHLiveDataProvider.getVpsRamSteps();
            List<ZHLiveDataProvider.OptionStep> diskSteps = ZHLiveDataProvider.getVpsDiskSteps();
            List<ZHLiveDataProvider.OptionStep> ipSteps = ZHLiveDataProvider.getVpsIpSteps();

            int curY = listY - scrollOffset + 13;

            // 1. RAM
            if (checkGridClick(startX, curY, panelW, 20, ramSteps, mouseX, mouseY, (step) -> {
                config.vpsRamGB = step.numericValue();
                config.vpsRamOptionId = step.id();
            })) return true;
            curY = advanceGridY(curY, 20, ramSteps) + 12 + 13;

            // 2. Disk
            if (checkGridClick(startX, curY, panelW, 20, diskSteps, mouseX, mouseY, (step) -> {
                config.vpsDiskGB = step.numericValue();
                config.vpsDiskOptionId = step.id();
            })) return true;
            curY = advanceGridY(curY, 20, diskSteps) + 12 + 13;

            // 3. IPs
            if (checkGridClick(startX, curY, panelW, 20, ipSteps, mouseX, mouseY, (step) -> {
                config.vpsIps = step.numericValue();
                config.vpsIpOptionId = step.id();
            })) return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean checkGridClick(int x, int y, int w, int h, List<ZHLiveDataProvider.OptionStep> steps, double mouseX, double mouseY, java.util.function.Consumer<ZHLiveDataProvider.OptionStep> onSelect) {
        if (steps == null || steps.isEmpty()) return false;
        int cols = steps.size() <= 4 ? steps.size() : (steps.size() <= 6 ? 3 : 4);
        int gap = 4;
        int chipW = (w - (cols - 1) * gap) / cols;

        int curY = y;
        for (int i = 0; i < steps.size(); i++) {
            int col = i % cols;
            if (col == 0 && i > 0) curY += h + gap;
            int cx = x + col * (chipW + gap);
            if (mouseX >= cx && mouseX <= cx + chipW && mouseY >= curY && mouseY <= curY + h) {
                onSelect.accept(steps.get(i));
                return true;
            }
        }
        return false;
    }

    private int advanceGridY(int y, int h, List<ZHLiveDataProvider.OptionStep> steps) {
        if (steps == null || steps.isEmpty()) return y;
        int cols = steps.size() <= 4 ? steps.size() : (steps.size() <= 6 ? 3 : 4);
        int rows = (int) Math.ceil((double) steps.size() / cols);
        return y + rows * h + (rows - 1) * 4;
    }
}
