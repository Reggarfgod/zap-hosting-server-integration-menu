package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ZHStep6DiskSpaceScreen extends ZHBaseWizardScreen {

    private List<ZHLiveDataProvider.OptionStep> diskSteps;
    private DiskSlider slider;

    public ZHStep6DiskSpaceScreen(ZHOrderConfig config) {
        super(config, 6, "Additional Disk Space");
        this.diskSteps = ZHLiveDataProvider.getDiskSteps();
    }

    @Override
    protected void init() {
        super.init();
        this.diskSteps = ZHLiveDataProvider.getDiskSteps();

        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int initialIndex = 0;
        for (int i = 0; i < diskSteps.size(); i++) {
            if (diskSteps.get(i).id().equals(config.diskOptionId) || diskSteps.get(i).numericValue() == config.diskSpaceGB) {
                initialIndex = i;
                break;
            }
        }

        int sliderW = Math.min(280, this.width - 60);
        slider = new DiskSlider(centerX - sliderW / 2, centerY - 10, sliderW, 20, (double) initialIndex / Math.max(1, diskSteps.size() - 1));
        addWidget(slider);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int boxW = Math.min(320, this.width - 30);
        int boxH = Math.min(130, bottomLimit - topY - 8);
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        // 1. Draw card background first
        drawCard(graphics, boxX, boxY, boxW, boxH, false, false);

        int sliderY = centerY - 10;

        // 2. Draw card labels and information
        String minStr = "0 Gigabyte";
        String maxStr = diskSteps.isEmpty() ? "200 Gigabyte" : diskSteps.get(diskSteps.size() - 1).label();
        graphics.drawString(this.font, minStr, boxX + 15, sliderY + 24, 0xFF888888);
        graphics.drawString(this.font, maxStr, boxX + boxW - this.font.width(maxStr) - 15, sliderY + 24, 0xFF888888);

        String diskInfo = config.diskSpaceGB == 0 ? "Fast NVMe SSD storage included by default!" :
                "+" + config.diskSpaceGB + " GB NVMe SSD High-Speed Storage Added";
        graphics.drawCenteredString(this.font, diskInfo, centerX, sliderY + 38, 0xFFCCCCCC);

        double price = config.getDiskPriceMonthly();
        String priceFormatted = price > 0 ? String.format("+ $%.2f / Month", price) : "$0.00";
        graphics.drawCenteredString(this.font, priceFormatted, centerX, sliderY + 52, COLOR_ZAP_GREEN);

        // 3. Render custom slider ON TOP of the card
        if (slider != null) {
            slider.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private class DiskSlider extends ZHCustomSlider {
        public DiskSlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, Component.empty(), initialValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (diskSteps.isEmpty()) return;
            int stepIndex = (int) Math.round(this.value * (diskSteps.size() - 1));
            ZHLiveDataProvider.OptionStep step = diskSteps.get(stepIndex);
            config.diskSpaceGB = step.numericValue();
            config.diskOptionId = step.id();
            setMessage(Component.literal(step.label()));
            setBubbleText(config.diskSpaceGB + " GB NVMe");
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }
    }
}
