package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public class ZHStep7CpuScreen extends ZHBaseWizardScreen {

    public ZHStep7CpuScreen(ZHOrderConfig config) {
        super(config, 7, "CPU & Host Server");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int cardW = Math.min(150, (this.width - 40) / 2);
        int cardH = Math.min(78, bottomLimit - topY - 26);
        int gap = 12;

        int leftX = centerX - cardW - gap / 2;
        int rightX = centerX + gap / 2;
        int cardY = centerY - cardH / 2 - 8;

        // Card 1: Standard
        boolean stdSelected = !config.isPremiumCpu;
        boolean stdHovered = mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, leftX, cardY, cardW, cardH, stdSelected, stdHovered);

        graphics.text(this.font, "\u26a1 Standard", leftX + 10, cardY + 10, stdSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.text(this.font, "(SSD, 2.0-3.4 GHz,", leftX + 10, cardY + 24, 0xFFAAAAAA);
        graphics.text(this.font, " DDR3/4 memory)", leftX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.text(this.font, "$0.00 / Month", leftX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Card 2: Premium
        boolean premSelected = config.isPremiumCpu;
        boolean premHovered = mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, rightX, cardY, cardW, cardH, premSelected, premHovered);

        graphics.text(this.font, "\ud83d\ude80 ++ Premium ++", rightX + 10, cardY + 10, premSelected ? 0xFFFFFFFF : 0xFFFFAA00);
        graphics.text(this.font, "(M.2 SSD, Gaming CPU", rightX + 10, cardY + 24, 0xFFAAAAAA);
        graphics.text(this.font, " 3.4-4.4GHz, DDR4)", rightX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.text(this.font, "+ $4.50 / Month", rightX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Explanation text
        graphics.centeredText(this.font, "Choose Premium for modpacks with heavy tick rates and large worlds.", centerX, cardY + cardH + 10, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int centerX = this.width / 2;
            int topY = 44;
            int bottomLimit = this.height - 35;
            int centerY = topY + (bottomLimit - topY) / 2;

            int cardW = Math.min(150, (this.width - 40) / 2);
            int cardH = Math.min(78, bottomLimit - topY - 26);
            int gap = 12;

            int leftX = centerX - cardW - gap / 2;
            int rightX = centerX + gap / 2;
            int cardY = centerY - cardH / 2 - 8;

            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                config.isPremiumCpu = false;
                return true;
            }

            if (mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                config.isPremiumCpu = true;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}
