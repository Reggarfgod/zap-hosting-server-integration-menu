package com.reggarf.mods.zap_hosting_server_integration_menu.tool;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ZHScrollableTextList extends AbstractWidget {
    private final List<Component> entries;
    private final int entryHeight = 12;
    private int scrollOffset = 0;
    private final Font font;

    public ZHScrollableTextList(int x, int y, int width, int height, List<Component> entries) {
        super(x, y, width, height, Component.empty());
        this.entries = entries;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int visibleCount = this.height / entryHeight;
        int scrollY = getY();

        for (int i = 0; i < visibleCount; i++) {
            int index = i + scrollOffset;
            if (index < entries.size()) {
                graphics.drawString(font, entries.get(index), getX() + 4, scrollY + i * entryHeight, 0xCCCCCC);
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Only scroll if the mouse is inside the widget area
        if (mouseX >= getX() && mouseX <= getX() + getWidth() &&
                mouseY >= getY() && mouseY <= getY() + getHeight()) {

            int visibleCount = this.height / entryHeight;
            int maxScroll = Math.max(0, entries.size() - visibleCount);

            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll);
            return true;
        }
        return false;
    }



    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // No narration needed for now
    }
}
