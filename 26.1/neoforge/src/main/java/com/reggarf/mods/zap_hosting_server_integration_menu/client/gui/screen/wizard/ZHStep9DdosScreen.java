package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public class ZHStep9DdosScreen extends ZHBaseWizardScreen {

    public ZHStep9DdosScreen(ZHOrderConfig config) {
        super(config, 9, "DDoS Manager Overview");
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

        // Card 1: No overview
        boolean noAccessSelected = !config.hasDdosOverview;
        boolean noAccessHovered = mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, leftX, cardY, cardW, cardH, noAccessSelected, noAccessHovered);

        graphics.text(this.font, "\ud83d\udee1 No access to DDoS", leftX + 10, cardY + 10, noAccessSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.text(this.font, " overview", leftX + 10, cardY + 22, noAccessSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.text(this.font, "Automated filter active", leftX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.text(this.font, "$0.00 / Month", leftX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Card 2: Access to DDoS Overview
        boolean accessSelected = config.hasDdosOverview;
        boolean accessHovered = mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, rightX, cardY, cardW, cardH, accessSelected, accessHovered);

        graphics.text(this.font, "\ud83d\udc41 Access to DDoS", rightX + 10, cardY + 10, accessSelected ? 0xFFFFFFFF : 0xFFFFAA00);
        graphics.text(this.font, " overview", rightX + 10, cardY + 22, accessSelected ? 0xFFFFFFFF : 0xFFFFAA00);
        graphics.text(this.font, "Live attack logs & graphs", rightX + 10, cardY + 36, 0xFFAAAAAA);
        graphics.text(this.font, "+ $1.16 / Month", rightX + 10, cardY + cardH - 16, COLOR_ZAP_GREEN);

        // Explanation text
        graphics.centeredText(this.font, "DDoS protection is always active. The overview grants live dashboard analytics.", centerX, cardY + cardH + 10, 0xFF888888);
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
                config.hasDdosOverview = false;
                return true;
            }

            if (mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                config.hasDdosOverview = true;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}
