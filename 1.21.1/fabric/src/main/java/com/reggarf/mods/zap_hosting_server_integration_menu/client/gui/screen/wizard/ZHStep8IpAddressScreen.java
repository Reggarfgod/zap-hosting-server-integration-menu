package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;

public class ZHStep8IpAddressScreen extends ZHBaseWizardScreen {

    public ZHStep8IpAddressScreen(ZHOrderConfig config) {
        super(config, 8, "Own IPv4 Address");
    }

    private boolean isOwnIpDisabledAtCurrentLocation() {
        for (ZHLiveDataProvider.LocationEntry loc : ZHLiveDataProvider.getLocations()) {
            if (loc.siteId().equals(config.locationId)) {
                return loc.ownIpDisabled();
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        boolean disabledAtLocation = isOwnIpDisabledAtCurrentLocation();
        if (disabledAtLocation && config.hasDedicatedIp) {
            config.hasDedicatedIp = false;
        }

        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int cardW = Math.min(155, (this.width - 40) / 2);
        int cardH = Math.min(82, bottomLimit - topY - 26);
        int gap = 12;

        int leftX = centerX - cardW - gap / 2;
        int rightX = centerX + gap / 2;
        int cardY = centerY - cardH / 2 - 8;

        // Card 1: Shared IP (No own IPv4)
        boolean sharedSelected = !config.hasDedicatedIp;
        boolean sharedHovered = mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, leftX, cardY, cardW, cardH, sharedSelected, sharedHovered);

        graphics.drawString(this.font, "🔒 No own ipaddress", leftX + 8, cardY + 8, sharedSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
        graphics.drawString(this.font, "Connect with shared port", leftX + 8, cardY + 22, 0xFFAAAAAA);
        graphics.drawString(this.font, "(e.g. :25570)", leftX + 8, cardY + 34, 0xFFAAAAAA);
        graphics.drawString(this.font, "$0.00 / Month", leftX + 8, cardY + cardH - 14, COLOR_ZAP_GREEN);

        // Card 2: Dedicated IPv4
        boolean dedicatedSelected = config.hasDedicatedIp;
        boolean dedicatedHovered = !disabledAtLocation && mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH;
        drawCard(graphics, rightX, cardY, cardW, cardH, dedicatedSelected, dedicatedHovered);

        if (disabledAtLocation) {
            graphics.drawString(this.font, "❌ Own ipv4 address", rightX + 8, cardY + 8, 0xFF888888);
            graphics.drawString(this.font, "Not available at", rightX + 8, cardY + 22, 0xFFFF7777);
            graphics.drawString(this.font, config.locationName != null ? shorten(config.locationName, 18) : "selected location", rightX + 8, cardY + 34, 0xFFFF7777);
            graphics.drawString(this.font, "Unavailable", rightX + 8, cardY + cardH - 14, 0xFF888888);
        } else {
            graphics.drawString(this.font, "📍 Own ipv4 address", rightX + 8, cardY + 8, dedicatedSelected ? 0xFFFFFFFF : 0xFFFFAA00);
            graphics.drawString(this.font, "Default port :25565", rightX + 8, cardY + 22, 0xFFAAAAAA);
            graphics.drawString(this.font, "Direct domain connect", rightX + 8, cardY + 34, 0xFFAAAAAA);
            graphics.drawString(this.font, "+ $3.42 / Month", rightX + 8, cardY + cardH - 14, COLOR_ZAP_GREEN);
        }

        // Explanation text
        if (disabledAtLocation) {
            graphics.drawCenteredString(this.font, "Dedicated IPv4 is unavailable in " + config.locationName + ". Select Germany or USA for Own IP.", centerX, cardY + cardH + 8, 0xFFFFAA00);
        } else {
            graphics.drawCenteredString(this.font, "A dedicated IP allows you to connect directly with your custom domain without typing a port.", centerX, cardY + cardH + 8, 0xFF888888);
        }
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int topY = 44;
        int bottomLimit = this.height - 35;
        int centerY = topY + (bottomLimit - topY) / 2;

        int cardW = Math.min(155, (this.width - 40) / 2);
        int cardH = Math.min(82, bottomLimit - topY - 26);
        int gap = 12;

        int leftX = centerX - cardW - gap / 2;
        int rightX = centerX + gap / 2;
        int cardY = centerY - cardH / 2 - 8;

        if (mouseX >= leftX && mouseX <= leftX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
            config.hasDedicatedIp = false;
            return true;
        }

        if (!isOwnIpDisabledAtCurrentLocation()) {
            if (mouseX >= rightX && mouseX <= rightX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                config.hasDedicatedIp = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
