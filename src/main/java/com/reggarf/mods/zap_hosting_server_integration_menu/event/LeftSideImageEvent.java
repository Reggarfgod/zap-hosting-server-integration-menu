//package com.reggarf.mods.zap_hosting_server_integration_menu.event;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.Util;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.client.event.ScreenEvent;
//import net.minecraftforge.eventbus.api.EventPriority;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//
//public class LeftSideImageEvent {
//    private static final ResourceLocation IMAGE = new ResourceLocation("zap_hosting_server_integration_menu", "textures/gui/overlay_2.gif");
//    private static final int IMAGE_WIDTH = 100;
//    private static final int IMAGE_HEIGHT = 300;
//    private static final String LINK = "https://zap-hosting.com/reggarf-left"; // Replace with your link
//
//    private int imageX, imageY;
//
//    @SubscribeEvent(priority = EventPriority.NORMAL)
//    public void onScreenRender(ScreenEvent.Render event) {
//        Screen screen = event.getScreen();
//        if (screen instanceof JoinMultiplayerScreen) {
//            GuiGraphics guiGraphics = event.getGuiGraphics();
//            imageX = 10; // Fixed position at the left corner
//            imageY = 10; // Fixed position at the top-left corner
//
//            RenderSystem.setShaderTexture(0, IMAGE);
//            guiGraphics.blit(IMAGE, imageX, imageY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.NORMAL)
//    public void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
//        Screen screen = event.getScreen();
//        if (screen instanceof JoinMultiplayerScreen) {
//            double mouseX = event.getMouseX();
//            double mouseY = event.getMouseY();
//
//            if (mouseX >= imageX && mouseX <= imageX + IMAGE_WIDTH &&
//                    mouseY >= imageY && mouseY <= imageY + IMAGE_HEIGHT &&
//                    event.getButton() == 0) { // Left click only
//                Minecraft.getInstance().setScreen(null); // Close the GUI
//                Util.getPlatform().openUri(LINK); // Open the link
//                event.setCanceled(true); // Prevent further processing
//            }
//        }
//    }
//}