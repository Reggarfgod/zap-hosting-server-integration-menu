package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.zap_hosting_server_integration_menu.Zap_Hosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.screens.ZHChooseLauncherScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.screens.ZHPlayerSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class ZHTopImageEvent {

    private static final ResourceLocation IMAGE =
            ResourceLocation.fromNamespaceAndPath(Zap_Hosting.MOD_ID, "textures/gui/wo_bg_overlay_2.png");

    private static final int IMAGE_WIDTH = 223;
    private static final int IMAGE_HEIGHT = 29;

    private static String playMultiplayerText = "";  //Zap_Hosting.CONFIG.common.code; can use config for codes
    private static int textX;
    private static int textY = 24;
    private static float textSize = 0.8f;

    private int imageX, imageY;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (!Zap_Hosting.CONFIG.common.enableOverlay) return;

        Screen screen = event.getScreen();
        if (screen instanceof JoinMultiplayerScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            imageX = (screen.width - IMAGE_WIDTH) / 2;
            imageY = 1;

            RenderSystem.setShaderTexture(0, IMAGE);
            guiGraphics.blit(IMAGE, imageX, imageY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

            textX = ((screen.width - Minecraft.getInstance().font.width(playMultiplayerText)) / 2) + 5;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(textSize, textSize, textSize);

            // Original white text
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    playMultiplayerText,
                    (int) (textX / textSize),
                    (int) (textY / textSize),
                    0xFFFFFF,
                    false
            );

            // New gray text
            String extraText = "Click me to get your own server!";
            int extraTextX = (screen.width - Minecraft.getInstance().font.width(extraText)) / 2+22;
            int extraTextY = textY - 8; // adjust spacing if needed
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    extraText,
                    (int) (extraTextX / textSize),
                    (int) (extraTextY / textSize),
                    0xAAAAAA,
                    false
            );

            // New gray text
            String serverText = "Need a Server?";
            int serverTextX = (screen.width - Minecraft.getInstance().font.width(serverText)) / 2-19;
            int serverTextY = textY - 18; // adjust spacing if needed
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    serverText,
                    (int) (serverTextX / textSize),
                    (int) (serverTextY / textSize),
                    0xFFFFFF,
                    false
            );

            guiGraphics.pose().popPose();

            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            if (mouseX >= imageX && mouseX <= imageX + IMAGE_WIDTH &&
                    mouseY >= imageY && mouseY <= imageY + IMAGE_HEIGHT) {
                guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.literal("Click me to get your own server"),
                        (int) mouseX, (int) mouseY
                );
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
                Minecraft.getInstance().setScreen(new ZHPlayerSlot());
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
