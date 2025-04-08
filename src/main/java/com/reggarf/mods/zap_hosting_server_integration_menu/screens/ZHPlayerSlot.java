package com.reggarf.mods.zap_hosting_server_integration_menu.screens;




import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

public class ZHPlayerSlot extends Screen {

    private int playerCount = 10;
    private PlayerCountSlider playerSlider;

    public ZHPlayerSlot() {
        super(Component.literal("Let's create your Minecraft Server."));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Create and add player count slider
        playerSlider = new PlayerCountSlider(centerX - 100, centerY - 20, 200, 20, playerCount);
        addRenderableWidget(playerSlider);

        // Next button
        addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            Minecraft.getInstance().setScreen(new ZHChooseLauncherScreen()); // <-- Go to Plan selection screen
        }).bounds(centerX - 100, centerY + 10, 200, 20).build());

        // Back button
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(this)); //go to multiplayer screen
        }).bounds(centerX - 40, centerY + 40, 80, 20).build());
    }

//    @Override
//    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(guiGraphics);
//        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
//        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.config.modules."), this.width / 2, 50, 0xFFFFFF);
//        super.render(guiGraphics, mouseX, mouseY, partialTick);
//    }
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, Component.translatable("screen.config.title"), this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.config.playercount"), this.width / 2, 90, 0xFFFFFF);
    }
    // Custom slider class
    private class PlayerCountSlider extends AbstractSliderButton {

        public PlayerCountSlider(int x, int y, int width, int height, int initialValue) {
            super(x, y, width, height, Component.literal(""), (initialValue - 1) / 99.0);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            int value = (int) (this.value * 99.0) + 1;
            this.setMessage(Component.literal(String.valueOf(value)));
        }

        @Override
        protected void applyValue() {
            playerCount = (int) (this.value * 99.0) + 1;
        }
    }
}
