package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;

public class ZHStep7CpuScreen extends ZHBaseWizardScreen {

    public ZHStep7CpuScreen(ZHOrderConfig config) {
        super(config, 7, "CPU & Host Server");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

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

        graphics.drawString(this.font, "⚡ Standard", leftX + 10, cardY + 10, stdSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.drawString(this.font, "(SSD, 2.0-3.4 GHz,", leftX + 10, cardY + 24, 0xFFAAAAAA);
        graphics.drawString(this.font, " DDR3/4 memory)", leftX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.drawString(this.font, "$0.00 / Month", leftX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Card 2: Premium
        boolean premSelected = config.isPremiumCpu;
        boolean premHovered = mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, rightX, cardY, cardW, cardH, premSelected, premHovered);

        graphics.drawString(this.font, "🚀 ++ Premium ++", rightX + 10, cardY + 10, premSelected ? 0xFFFFFFFF : 0xFFFFAA00);
        graphics.drawString(this.font, "(M.2 SSD, Gaming CPU", rightX + 10, cardY + 24, 0xFFAAAAAA);
        graphics.drawString(this.font, " 3.4-4.4GHz, DDR4)", rightX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.drawString(this.font, "+ $4.50 / Month", rightX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Explanation text
        graphics.drawCenteredString(this.font, "Choose Premium for modpacks with heavy tick rates and large worlds.", centerX, cardY + cardH + 10, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

        if (mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
            config.isPremiumCpu = false;
            return true;
        }

        if (mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
            config.isPremiumCpu = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
