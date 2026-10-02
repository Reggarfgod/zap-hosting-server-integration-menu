package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ZHVpsStep4CpuScreen extends ZHBaseWizardScreen {

    private int scrollOffset = 0;
    private final int CARD_HEIGHT = 28;
    private final int GAP = 5;

    public ZHVpsStep4CpuScreen(ZHOrderConfig config) {
        super(config, 4, getTotalSteps(config), "VPS CPU Cores");
    }

    private String getCpuDescription(int cores) {
        return switch (cores) {
            case 4 -> "Entry tier for utility servers & lightweight bots";
            case 6 -> "Balanced single-thread performance for small modpacks";
            case 8 -> "Recommended sweet spot for active community servers";
            case 10 -> "Strong computing power for medium-size worlds and plugins";
            case 12 -> "Multi-threaded power for complex mods and worlds";
            case 14 -> "High concurrency performance for popular community servers";
            case 16 -> "Heavy workloads, proxy networks, and Docker containers";
            case 32 -> "Extreme compute power for large networks & virtualization";
            case 64 -> "Maximum multi-core dedicated powerhouse";
            default -> cores + " High-performance dedicated CPU cores";
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        List<ZHLiveDataProvider.OptionStep> cpuTiers = ZHLiveDataProvider.getVpsCpuSteps();

        int centerX = this.width / 2;
        int cardW = Math.min(340, this.width - 24);
        int startX = centerX - cardW / 2;
        int listY = 46;
        int listH = this.height - listY - 34;

        int visibleItems = Math.max(1, listH / (CARD_HEIGHT + GAP));
        int maxOffset = Math.max(0, cpuTiers.size() - visibleItems);
        scrollOffset = Math.min(scrollOffset, maxOffset);

        graphics.enableScissor(startX - 2, listY - 1, startX + cardW + 2, listY + listH + 1);

        for (int i = 0; i < cpuTiers.size(); i++) {
            ZHLiveDataProvider.OptionStep tier = cpuTiers.get(i);
            int cy = listY + (i - scrollOffset) * (CARD_HEIGHT + GAP);

            if (cy + CARD_HEIGHT < listY || cy > listY + listH) continue;

            boolean sel = tier.numericValue() == config.vpsCpuCores;
            boolean hov = mouseX >= startX && mouseX <= startX + cardW && mouseY >= cy && mouseY <= cy + CARD_HEIGHT && mouseY >= listY && mouseY <= listY + listH;

            drawCard(graphics, startX, cy, cardW, CARD_HEIGHT, sel, hov);

            String title = tier.numericValue() + " CPU Cores";
            graphics.drawString(this.font, title, startX + 8, cy + 5, sel ? 0xFFFFFFFF : 0xFFDDDDDD, false);

            String price = "+$" + String.format("%.2f", tier.extraPrice()) + "/mo";
            int pw = this.font.width(price);
            graphics.drawString(this.font, price, startX + cardW - pw - (sel ? 18 : 8), cy + 5, COLOR_ZAP_GREEN, false);

            graphics.drawString(this.font, getCpuDescription(tier.numericValue()), startX + 8, cy + 16, 0xFFAAAAAA, false);
        }

        graphics.disableScissor();

        // Scrollbar if needed
        if (cpuTiers.size() > visibleItems) {
            int scrollBarH = Math.max(15, (visibleItems * listH) / cpuTiers.size());
            int scrollBarY = listY + (scrollOffset * (listH - scrollBarH)) / maxOffset;
            graphics.fill(startX + cardW + 2, scrollBarY, startX + cardW + 5, scrollBarY + scrollBarH, COLOR_ZAP_GREEN);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<ZHLiveDataProvider.OptionStep> cpuTiers = ZHLiveDataProvider.getVpsCpuSteps();
        int listY = 46;
        int listH = this.height - listY - 34;
        int visibleItems = Math.max(1, listH / (CARD_HEIGHT + GAP));
        int maxOffset = Math.max(0, cpuTiers.size() - visibleItems);

        if (delta > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (delta < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            List<ZHLiveDataProvider.OptionStep> cpuTiers = ZHLiveDataProvider.getVpsCpuSteps();
            int centerX = this.width / 2;
            int cardW = Math.min(340, this.width - 24);
            int startX = centerX - cardW / 2;
            int listY = 46;
            int listH = this.height - listY - 34;

            for (int i = 0; i < cpuTiers.size(); i++) {
                int cy = listY + (i - scrollOffset) * (CARD_HEIGHT + GAP);
                if (mouseX >= startX && mouseX <= startX + cardW && mouseY >= cy && mouseY <= cy + CARD_HEIGHT && mouseY >= listY && mouseY <= listY + listH) {
                    ZHLiveDataProvider.OptionStep tier = cpuTiers.get(i);
                    boolean wasSelected = tier.numericValue() == config.vpsCpuCores;
                    config.vpsCpuCores = tier.numericValue();
                    config.vpsCpuOptionId = tier.id();
                    if (wasSelected) {
                        onNext();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
