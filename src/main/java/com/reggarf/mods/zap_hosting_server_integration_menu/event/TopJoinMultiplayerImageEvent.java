package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.zap_hosting_server_integration_menu.Zap_Hosting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TopJoinMultiplayerImageEvent {
    private static final ResourceLocation IMAGE = new ResourceLocation("zap_hosting_server_integration_menu", "textures/gui/overlay_1.png");
    private static final int IMAGE_WIDTH = 225;
    private static final int IMAGE_HEIGHT = 31;
    private static final String LINK = Zap_Hosting.CONFIG.common.link;

    private static String playMultiplayerText = Zap_Hosting.CONFIG.common.code;
    private static int textX;
    private static int textY = 25;
    private static float textSize = 0.8f;

    private int imageX, imageY;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onScreenRender(ScreenEvent.Render event) {
        if (!Zap_Hosting.CONFIG.common.enableOverlay) return;

        Screen screen = event.getScreen();
        if (screen instanceof JoinMultiplayerScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            imageX = (screen.width - IMAGE_WIDTH) / 2;
            imageY = 1;

            guiGraphics.fill(imageX - 2, imageY - 2, imageX + IMAGE_WIDTH + 2, imageY + IMAGE_HEIGHT + 2, 0x66000000);

            RenderSystem.setShaderTexture(0, IMAGE);
            guiGraphics.blit(IMAGE, imageX, imageY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

            textX = ((screen.width - Minecraft.getInstance().font.width(playMultiplayerText)) / 2) + 5;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(textSize, textSize, textSize);
            guiGraphics.drawString(Minecraft.getInstance().font, playMultiplayerText, (int) (textX / textSize), (int) (textY / textSize), 0xFFFFFF, false);
            guiGraphics.pose().popPose();

            // Show tooltip if the mouse is over the image
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            if (mouseX >= imageX && mouseX <= imageX + IMAGE_WIDTH &&
                    mouseY >= imageY && mouseY <= imageY + IMAGE_HEIGHT) {
                guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal("Click me to buy server"), (int) mouseX, (int) mouseY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!Zap_Hosting.CONFIG.common.enableOverlay) return;

        Screen screen = event.getScreen();
        if (screen instanceof JoinMultiplayerScreen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            if (mouseX >= imageX && mouseX <= imageX + IMAGE_WIDTH &&
                    mouseY >= imageY && mouseY <= imageY + IMAGE_HEIGHT &&
                    event.getButton() == 0) {
                Minecraft.getInstance().setScreen(null);
                Util.getPlatform().openUri(LINK);
                event.setCanceled(true);
            }
        }
    }

    public static void setPlayMultiplayerText(String newText) {
        playMultiplayerText = newText;
    }

    public static void setPlayMultiplayerPosition(int x, int y) {
        textX = x;
        textY = y;
    }

    public static void setPlayMultiplayerTextSize(float size) {
        textSize = size;
    }
}