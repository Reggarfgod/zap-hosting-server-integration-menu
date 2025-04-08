package com.reggarf.mods.zap_hosting_server_integration_menu.tool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZHClickableTextList extends AbstractWidget {
    private final List<Component> items;
    private final int itemHeight = 20;
    private int scrollOffset = 0;
    private final Font font;
    private int selectedIndex = -1;
    private final Map<Component, ResourceLocation> imageMap = new HashMap<>();

    public ZHClickableTextList(int x, int y, int width, int height, List<Component> items) {
        super(x, y, width, height, Component.empty());
        this.items = items;
        this.font = Minecraft.getInstance().font;
    }

    public void setImageForEntry(Component entry, ResourceLocation image) {
        this.imageMap.put(entry, image);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int visibleCount = this.height / itemHeight;
        int startY = getY();

        for (int i = 0; i < visibleCount; i++) {
            int index = i + scrollOffset;
            if (index >= items.size()) break;

            Component entry = items.get(index);
            int itemY = startY + i * itemHeight;
            boolean hovered = mouseX >= getX() && mouseX <= getX() + getWidth()
                    && mouseY >= itemY && mouseY <= itemY + itemHeight;

            // Background
            if (index == selectedIndex) {
                graphics.fill(getX(), itemY, getX() + getWidth(), itemY + itemHeight, 0xFF777777);
            } else if (hovered) {
                graphics.fill(getX(), itemY, getX() + getWidth(), itemY + itemHeight, 0xFF555555);
            }

            // Draw icon if available
            ResourceLocation icon = imageMap.get(entry);
            int iconSize = 16;
            int textOffsetX = getX() + 5;
            if (icon != null) {
                graphics.blit(icon, getX() + 5, itemY + 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
                textOffsetX += iconSize + 4;
            }

            int textColor = (index == selectedIndex) ? 0xFFFFAA00 : 0xFFFFFF;
            graphics.drawString(font, entry, textOffsetX, itemY + 5, textColor);
        }

        // Scrollbar rendering
        if (items.size() * itemHeight > height) {
            int maxScroll = Math.max(1, items.size() - visibleCount);
            int scrollbarHeight = Math.max(10, (int) ((float) visibleCount / items.size() * height));
            int scrollbarY = getY() + (int) ((float) scrollOffset / maxScroll * (height - scrollbarHeight));

            int scrollbarX = getX() + getWidth() - 4;
            graphics.fill(scrollbarX, getY(), scrollbarX + 4, getY() + height, 0xFF333333);
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= getX() && mouseX <= getX() + getWidth()
                && mouseY >= getY() && mouseY <= getY() + getHeight()) {

            int visibleCount = this.height / itemHeight;
            int maxScroll = Math.max(0, items.size() - visibleCount);
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(verticalAmount), 0, maxScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int visibleCount = this.height / itemHeight;
        for (int i = 0; i < visibleCount; i++) {
            int index = i + scrollOffset;
            if (index >= items.size()) break;

            int itemY = getY() + i * itemHeight;
            if (mouseX >= getX() && mouseX <= getX() + getWidth()
                    && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                onItemClick(index);
                return true;
            }
        }
        return false;
    }

    private void onItemClick(int index) {
        selectedIndex = index;
        Minecraft.getInstance().player.sendSystemMessage(
                Component.literal("Clicked: " + items.get(index).getString())
        );
        // TODO: Add your actual logic here
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Optional: accessibility support
    }
}
