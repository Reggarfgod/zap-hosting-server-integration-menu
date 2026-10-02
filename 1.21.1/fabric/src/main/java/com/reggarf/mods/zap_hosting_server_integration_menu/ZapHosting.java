package com.reggarf.mods.zap_hosting_server_integration_menu;

import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHMessageHandler;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ZapHosting implements ModInitializer {
    public static final String MOD_ID = "zap_hosting_server_integration_menu";

    public static ZHConfig CONFIG;

    @Override
    public void onInitialize() {
        init();
        PayloadTypeRegistry.playS2C().register(ZHWelcomePopupPayload.TYPE, ZHWelcomePopupPayload.STREAM_CODEC);
        ZHMessageHandler.init();
    }

    public static void init() {
        CONFIG = ZHConfig.load();
    }
}
