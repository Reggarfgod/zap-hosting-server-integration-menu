package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ZHStep4SlotsScreen extends ZHBaseWizardScreen {

    private List<ZHLiveDataProvider.OptionStep> slotSteps;
    private SlotSlider slider;

    public ZHStep4SlotsScreen(ZHOrderConfig config) {
        super(config, 4, "Game Server Slots");
        this.slotSteps = ZHLiveDataProvider.getSlotSteps();
    }

    @Override
    protected void init() {
        super.init();
        this.slotSteps = ZHLiveDataProvider.getSlotSteps();

        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int initialIndex = 0;
        for (int i = 0; i < slotSteps.size(); i++) {
            if (slotSteps.get(i).numericValue() == config.slots) {
                initialIndex = i;
                break;
            }
        }

        int sliderW = Math.min(280, this.width - 60);
        slider = new SlotSlider(centerX - sliderW / 2, centerY - 10, sliderW, 20, (double) initialIndex / Math.max(1, slotSteps.size() - 1));
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
        if (!slotSteps.isEmpty()) {
            graphics.drawString(this.font, slotSteps.get(0).label(), boxX + 16, sliderY + 24, 0xFF888888);
            String maxLabel = slotSteps.get(slotSteps.size() - 1).label();
            graphics.drawString(this.font, maxLabel, boxX + boxW - 16 - this.font.width(maxLabel), sliderY + 24, 0xFF888888);
        }

        int baseRam = config.getBaseRamGB();
        String ramInfo = "Including " + baseRam + " GB RAM!";
        graphics.drawCenteredString(this.font, ramInfo, centerX, sliderY + 38, 0xFFCCCCCC);

        String priceFormatted = String.format("+ $%.2f / Month", config.getSlotPriceMonthly());
        graphics.drawCenteredString(this.font, priceFormatted, centerX, sliderY + 52, COLOR_ZAP_GREEN);

        // 3. Render custom slider ON TOP of the card so it is crisp, sharp, and never blurry
        if (slider != null) {
            slider.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private class SlotSlider extends ZHCustomSlider {
        public SlotSlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, Component.empty(), initialValue);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (slotSteps.isEmpty()) return;
            int stepIndex = (int) Math.round(this.value * (slotSteps.size() - 1));
            ZHLiveDataProvider.OptionStep step = slotSteps.get(stepIndex);
            config.slots = step.numericValue();
            setMessage(Component.literal(step.label()));
            setBubbleText(config.slots + " Slots");
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }
    }
}
