//package com.reggarf.mods.zap_hosting_server_integration_menu.event;
//
//import net.minecraft.Util;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
//import net.minecraft.client.multiplayer.ServerData;
//import net.minecraft.client.multiplayer.ServerList;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.client.event.ScreenEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//
//public class ZHServerListOverlay {
//    private static final ResourceLocation SERVER_IMAGE = new ResourceLocation("zap_hosting_server_integration_menu", "textures/gui/server_image.png");
//
//    private static final int IMAGE_WIDTH = 200;
//    private static final int IMAGE_HEIGHT = 40;
//
//    private static final String LINK = "https://zap-hosting.com"; // Change to desired link
//
//    @SubscribeEvent
//    public void onServerListRender(ScreenEvent.Render event) {
//        if (event.getScreen() instanceof JoinMultiplayerScreen screen) {
//            GuiGraphics guiGraphics = event.getGuiGraphics();
//            Minecraft mc = Minecraft.getInstance();
//            ServerList serverList = new ServerList(mc);
//            serverList.load();
//
//            int serverCount = serverList.size();
//            int yOffset = 32;
//            int entryHeight = 36;
//
//            // Add space for the fake server at the top
//            int fakeServerX = (screen.width / 2) - (IMAGE_WIDTH / 2);
//            int fakeServerY = yOffset - entryHeight; // Places it before actual servers
//
//            guiGraphics.blit(SERVER_IMAGE, fakeServerX, fakeServerY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
//        }
//    }
//
//    @SubscribeEvent
//    public void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
//        if (event.getScreen() instanceof JoinMultiplayerScreen screen) {
//            double mouseX = event.getMouseX();
//            double mouseY = event.getMouseY();
//
//            int fakeServerX = (screen.width / 2) - (IMAGE_WIDTH / 2);
//            int fakeServerY = 32 - 36; // Same position as in render
//
//            if (mouseX >= fakeServerX && mouseX <= fakeServerX + IMAGE_WIDTH &&
//                    mouseY >= fakeServerY && mouseY <= fakeServerY + IMAGE_HEIGHT &&
//                    event.getButton() == 0) {
//                Util.getPlatform().openUri(LINK);
//                event.setCanceled(true);
//            }
//        }
//    }
//}
