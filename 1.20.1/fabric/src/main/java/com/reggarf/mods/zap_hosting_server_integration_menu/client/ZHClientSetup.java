package com.reggarf.mods.zap_hosting_server_integration_menu.client;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHConfigScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHWelcomePopupScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHClientTicker;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.Nullable;

/**
 * Isolated client-only setup and helper methods for Fabric.
 */
public class ZHClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ZHClientTicker.init();

        // Register incoming packet from server
        ClientPlayNetworking.registerGlobalReceiver(ZapHosting.WELCOME_PACKET_ID, (client, handler, buf, responseSender) -> {
            ZHWelcomePopupPayload payload = new ZHWelcomePopupPayload(buf);
            client.execute(() -> handleWelcomePacketOnClient(payload));
        });

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
