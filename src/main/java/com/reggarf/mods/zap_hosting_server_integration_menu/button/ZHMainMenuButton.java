//package com.reggarf.mods.zap_hosting_server_integration_menu.button;
//
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.screens.TitleScreen;
//import net.minecraftforge.client.event.ScreenEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//
//import net.minecraft.network.chat.Component;
//
//
//@Mod.EventBusSubscriber
//public class ZHMainMenuButton {
//
//    @SubscribeEvent
//    public static void onScreenInit(ScreenEvent.Init.Post event) {
//        if (event.getScreen() instanceof TitleScreen titleScreen) {
//            event.addListener(Button.builder(Component.translatable("New_Button"),
//                            button -> {
//                                System.out.println("Button Clicked!");
//                            })
//                    .bounds(10, 10, 100, 20) // Set position and size
//                    .build());
//        }
//    }
//}