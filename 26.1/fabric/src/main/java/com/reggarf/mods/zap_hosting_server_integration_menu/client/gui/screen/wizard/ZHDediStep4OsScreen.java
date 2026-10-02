package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ZHDediStep4OsScreen extends ZHBaseWizardScreen {

    public record OsOption(String name, String badge, String desc) {}

    private static final List<OsOption> OS_OPTIONS = List.of(
            new OsOption("Debian 12 64-bit", "RECOMMENDED", "Maximum bare-metal throughput with rock-solid server stability."),
            new OsOption("Ubuntu 24.04 LTS", "LATEST LTS", "Modern package ecosystem, ideal for complex Docker & Java setups."),
            new OsOption("Windows Server 2022", "ENTERPRISE", "Complete Windows Server environment with Remote Desktop (RDP) access.")
    );

    private int scrollOffset = 0;
    private final int CARD_HEIGHT = 34;
    private final int GAP = 6;

    public ZHDediStep4OsScreen(ZHOrderConfig config) {
        super(config, 4, getTotalSteps(config), "Dedicated Server Operating System");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int cardW = Math.min(340, this.width - 24);
        int startX = centerX - cardW / 2;
        int listY = 48;
        int listH = this.height - listY - 34;

        int visibleItems = Math.max(1, listH / (CARD_HEIGHT + GAP));
        int maxOffset = Math.max(0, OS_OPTIONS.size() - visibleItems);
        scrollOffset = Math.min(scrollOffset, maxOffset);

        graphics.enableScissor(startX - 2, listY - 1, startX + cardW + 2, listY + listH + 1);

        for (int i = 0; i < OS_OPTIONS.size(); i++) {
            OsOption os = OS_OPTIONS.get(i);
            int cy = listY + (i - scrollOffset) * (CARD_HEIGHT + GAP);

            if (cy + CARD_HEIGHT < listY || cy > listY + listH) continue;

            boolean sel = os.name().equals(config.dediOs);
            boolean hov = mouseX >= startX && mouseX <= startX + cardW && mouseY >= cy && mouseY <= cy + CARD_HEIGHT && mouseY >= listY && mouseY <= listY + listH;

            drawCard(graphics, startX, cy, cardW, CARD_HEIGHT, sel, hov);

            graphics.drawString(this.font, os.name(), startX + 8, cy + 5, sel ? 0xFFFFFFFF : 0xFFDDDDDD, false);

            int badgeColor = "RECOMMENDED".equals(os.badge()) ? COLOR_ZAP_GREEN : 0xFF888888;
            graphics.drawString(this.font, "[" + os.badge() + "]", startX + 8 + this.font.width(os.name()) + 8, cy + 5, badgeColor, false);

            graphics.drawString(this.font, os.desc(), startX + 8, cy + 18, 0xFFAAAAAA, false);
        }

        graphics.disableScissor();

        if (OS_OPTIONS.size() > visibleItems) {
            int scrollBarH = Math.max(15, (visibleItems * listH) / OS_OPTIONS.size());
            int scrollBarY = listY + (scrollOffset * (listH - scrollBarH)) / maxOffset;
            graphics.fill(startX + cardW + 2, scrollBarY, startX + cardW + 5, scrollBarY + scrollBarH, COLOR_ZAP_GREEN);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listY = 48;
        int listH = this.height - listY - 34;
        int visibleItems = Math.max(1, listH / (CARD_HEIGHT + GAP));
        int maxOffset = Math.max(0, OS_OPTIONS.size() - visibleItems);

        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int cardW = Math.min(340, this.width - 24);
            int startX = centerX - cardW / 2;
            int listY = 48;
            int listH = this.height - listY - 34;

            for (int i = 0; i < OS_OPTIONS.size(); i++) {
                int cy = listY + (i - scrollOffset) * (CARD_HEIGHT + GAP);
                if (mouseX >= startX && mouseX <= startX + cardW && mouseY >= cy && mouseY <= cy + CARD_HEIGHT && mouseY >= listY && mouseY <= listY + listH) {
                    OsOption os = OS_OPTIONS.get(i);
                    boolean wasSelected = os.name().equals(config.dediOs);
                    config.dediOs = os.name();
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
