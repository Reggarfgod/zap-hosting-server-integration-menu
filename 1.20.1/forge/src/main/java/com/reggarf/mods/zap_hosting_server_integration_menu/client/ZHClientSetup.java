package com.reggarf.mods.zap_hosting_server_integration_menu.client;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.event.ZHClientEvents;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHConfigScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHWelcomePopupScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHClientTicker;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nullable;

/**
 * Isolated client-only setup and helper methods to ensure no client GUI/Screen classes
 * are referenced or loaded on dedicated servers.
 */
public class ZHClientSetup {

    public static void initClient() {
        MinecraftForge.EVENT_BUS.register(new ZHClientEvents());
        MinecraftForge.EVENT_BUS.register(ZHClientTicker.class);

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new ZHConfigScreen(parent)));

        // Preload live data from Zap-Hosting in background upon startup
        ZHLiveDataProvider.ensureLoaded();
    }

    public static void handleWelcomePacketOnClient(ZHWelcomePopupPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                ZHClientTicker.triggerWelcomePopup(payload.delayTicks());
            });
        }
    }

    public static void openWelcomePopup(@Nullable Screen parentScreen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new ZHWelcomePopupScreen(parentScreen));
        }
    }

    public static boolean checkAndOpenWelcomePopupIfReady() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && mc.level != null) {
            if (mc.screen == null) {
                mc.setScreen(new ZHWelcomePopupScreen(null));
                return true;
            }
        }
        return false;
    }
}
