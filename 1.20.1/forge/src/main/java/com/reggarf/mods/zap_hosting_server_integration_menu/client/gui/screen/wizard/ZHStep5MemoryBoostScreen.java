package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ZHStep5MemoryBoostScreen extends ZHBaseWizardScreen {

    private List<ZHLiveDataProvider.OptionStep> ramSteps;
    private MemorySlider slider;

    public ZHStep5MemoryBoostScreen(ZHOrderConfig config) {
        super(config, 5, "Memory Boost (RAM)");
        this.ramSteps = ZHLiveDataProvider.getRamSteps();
    }

    @Override
    protected void init() {
        super.init();
        this.ramSteps = ZHLiveDataProvider.getRamSteps();

        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int initialIndex = 0;
        for (int i = 0; i < ramSteps.size(); i++) {
            if (ramSteps.get(i).id().equals(config.ramOptionId) || ramSteps.get(i).numericValue() == config.ramBoostGB) {
                initialIndex = i;
                break;
            }
        }

        int sliderW = Math.min(280, this.width - 60);
        slider = new MemorySlider(centerX - sliderW / 2, centerY - 10, sliderW, 20, (double) initialIndex / Math.max(1, ramSteps.size() - 1));
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
        String minStr = "0 GB RAM";
        String maxStr = ramSteps.isEmpty() ? "32 GB RAM" : ramSteps.get(ramSteps.size() - 1).label();
        graphics.drawString(this.font, minStr, boxX + 15, sliderY + 24, 0xFF888888);
        graphics.drawString(this.font, maxStr, boxX + boxW - this.font.width(maxStr) - 15, sliderY + 24, 0xFF888888);

        int totalRam = config.getTotalRamGB();
        String totalRamText = "Total " + totalRam + " GB RAM (Base " + config.getBaseRamGB() + "GB + Boost " + config.ramBoostGB + "GB)";
        graphics.drawCenteredString(this.font, totalRamText, centerX, sliderY + 38, 0xFFCCCCCC);

        double price = config.getRamPriceMonthly();
        String priceFormatted = price > 0 ? String.format("+ $%.2f / Month", price) : "$0.00";
        graphics.drawCenteredString(this.font, priceFormatted, centerX, sliderY + 52, COLOR_ZAP_GREEN);

        // 3. Render custom slider ON TOP of the card
        if (slider != null) {
            slider.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private class MemorySlider extends ZHCustomSlider {
        public MemorySlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, Component.empty(), initialValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (ramSteps.isEmpty()) return;
            int stepIndex = (int) Math.round(this.value * (ramSteps.size() - 1));
            ZHLiveDataProvider.OptionStep step = ramSteps.get(stepIndex);
            config.ramBoostGB = step.numericValue();
            config.ramOptionId = step.id();
            setMessage(Component.literal(step.label()));
            setBubbleText(config.ramBoostGB + " GB Boost");
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }
    }
}
