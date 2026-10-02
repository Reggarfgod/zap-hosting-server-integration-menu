package com.reggarf.mods.zap_hosting_server_integration_menu;

import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHMessageHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;

public class ZapHosting implements ModInitializer {
    public static final String MOD_ID = "zap_hosting_server_integration_menu";
    public static final ResourceLocation WELCOME_PACKET_ID = new ResourceLocation(MOD_ID, "welcome_popup");

    public static ZHConfig CONFIG;

    @Override
    public void onInitialize() {
        init();
        ZHMessageHandler.init();
    }

    public static void init() {
        CONFIG = ZHConfig.load();
    }
}
