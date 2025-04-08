package com.reggarf.mods.zap_hosting_server_integration_menu.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModpackSelectScreen extends Screen {

    private final List<Button> allButtons = new ArrayList<>();
    private final int buttonHeight = 35;
    private final int visibleRows = 5;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private String selected = "Minecraft";

    // Navigation buttons
    private Button nextButton;
    private Button backButton;

    public ModpackSelectScreen() {
        super(Component.literal("Select Platform"));
    }

    @Override
    protected void init() {
        super.init();
        allButtons.clear();
        this.children().clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Calculate vertical positioning for scroll area
        int scrollAreaHeight = visibleRows * buttonHeight;
        int scrollTopY = centerY - scrollAreaHeight / 2;

        int currentY = 0;

        // Add platform buttons
        addPlatformButton(centerX - 190, currentY, "Minecraft");
        addPlatformButton(centerX + 10, currentY, "Feed the Beast");

        currentY += buttonHeight;
        addPlatformButton(centerX - 190, currentY, "AT Launcher");
        addPlatformButton(centerX + 10, currentY, "Technic Launcher");

        currentY += buttonHeight;
        addPlatformButton(centerX - 190, currentY, "Voids Wrath Launcher");
        addPlatformButton(centerX + 10, currentY, "Minecraft Minigames");

        currentY += buttonHeight;
        addPlatformButton(centerX - 190, currentY, "Minecraft Adventure");
        addPlatformButton(centerX + 10, currentY, "Curse / Twitch");

        maxScroll = Math.max(0, currentY + buttonHeight - scrollAreaHeight);

        // Add "Back" and "Next" buttons at bottom center
        backButton = Button.builder(Component.literal("Back"), btn -> {
            // Handle back logic
            Minecraft.getInstance().setScreen(null); // Close screen for now
        }).bounds(centerX - 100 - 10, this.height - 40, 100, 20).build();

        nextButton = Button.builder(Component.literal("Next"), btn -> {
            // Handle next logic
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Selected: " + selected));
        }).bounds(centerX + 10, this.height - 40, 100, 20).build();

        addRenderableWidget(backButton);
        addRenderableWidget(nextButton);
    }

    private void addPlatformButton(int x, int y, String label) {
        Button button = Button.builder(Component.literal(label), btn -> {
            selected = label;
        }).bounds(x, y, 180, 30).build();

        allButtons.add(button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //this.renderBackground(graphics);

        int scrollY = this.scrollOffset;
        int centerX = this.width / 2;
        int scrollTopY = this.height / 2 - (visibleRows * buttonHeight) / 2;
        int scrollBottomY = scrollTopY + (visibleRows * buttonHeight);

        graphics.enableScissor(0, scrollTopY, this.width, scrollBottomY);

        for (Button btn : allButtons) {
            int realY = btn.getY() - scrollY + scrollTopY;
            if (realY + btn.getHeight() > scrollTopY && realY < scrollBottomY) {
                btn.setY(realY);
                btn.render(graphics, mouseX, mouseY, partialTick);

                if (btn.getMessage().getString().equals(selected)) {
                    graphics.fill(btn.getX() - 2, btn.getY() - 2, btn.getX() + btn.getWidth() + 2, btn.getY() + btn.getHeight() + 2, 0x884CAF50);
                }
            }
        }

        graphics.disableScissor();

        // Draw navigation buttons
        backButton.render(graphics, mouseX, mouseY, partialTick);
        nextButton.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * 10;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        for (Button b : allButtons) {
            if (b.isMouseOver(x, y)) {
                b.onClick(x, y);
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }
}
