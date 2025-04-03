//package com.reggarf.mods.zap_hosting_server_integration_menu.mainMenu;
//
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screens.TitleScreen;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraftforge.client.event.ScreenEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraft.resources.ResourceLocation;
//
//
//@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
//public class MainMenuOverlay {
//    private static final ResourceLocation CUSTOM_IMAGE = new ResourceLocation("zap_hosting_server_integration_menu", "textures/gui/logo.png");
//
//    @SubscribeEvent
//    public static void onMainMenuRender(ScreenEvent.Render event) {
//        if (event.getScreen() instanceof TitleScreen) {
//            GuiGraphics guiGraphics = event.getGuiGraphics();
//            Minecraft mc = Minecraft.getInstance();
//
//            // Image size (adjust to your image dimensions)
//            int width = 100;
//            int height = 100;
//
//            // Position: Right side, centered vertically
//            int x = mc.getWindow().getGuiScaledWidth() - width - 20; // Stick to the right with 20px padding
//            int y = (mc.getWindow().getGuiScaledHeight() - height) / 2; // Centered vertically
//
//            RenderSystem.setShaderTexture(0, CUSTOM_IMAGE);
//            guiGraphics.blit(CUSTOM_IMAGE, x, y, 0, 0, width, height, width, height);
//        }
//    }
//}