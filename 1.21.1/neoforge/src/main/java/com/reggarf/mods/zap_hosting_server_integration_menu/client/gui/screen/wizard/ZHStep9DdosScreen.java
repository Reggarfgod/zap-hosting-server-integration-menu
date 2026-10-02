package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;

public class ZHStep9DdosScreen extends ZHBaseWizardScreen {

    public ZHStep9DdosScreen(ZHOrderConfig config) {
        super(config, 9, "DDoS Manager Overview");
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

        // Card 1: No overview
        boolean noAccessSelected = !config.hasDdosOverview;
        boolean noAccessHovered = mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, leftX, cardY, cardW, cardH, noAccessSelected, noAccessHovered);

        graphics.drawString(this.font, "🛡 No access to DDoS", leftX + 10, cardY + 10, noAccessSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.drawString(this.font, " overview", leftX + 10, cardY + 22, noAccessSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.drawString(this.font, "Automated filter active", leftX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.drawString(this.font, "$0.00 / Month", leftX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Card 2: Access to DDoS Overview
        boolean accessSelected = config.hasDdosOverview;
        boolean accessHovered = mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, rightX, cardY, cardW, cardH, accessSelected, accessHovered);

        graphics.drawString(this.font, "👁 Access to DDoS", rightX + 10, cardY + 10, accessSelected ? 0xFFFFFFFF : 0xFFFFAA00);
        graphics.drawString(this.font, " overview", rightX + 10, cardY + 22, accessSelected ? 0xFFFFFFFF : 0xFFFFAA00);
        graphics.drawString(this.font, "Live attack logs & graphs", rightX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.drawString(this.font, "+ $1.16 / Month", rightX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Explanation text
        graphics.drawCenteredString(this.font, "DDoS protection is always active. The overview grants live dashboard analytics.", centerX, cardY + cardH + 10, 0xFF888888);
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
            config.hasDdosOverview = false;
            return true;
        }

        if (mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
            config.hasDdosOverview = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
