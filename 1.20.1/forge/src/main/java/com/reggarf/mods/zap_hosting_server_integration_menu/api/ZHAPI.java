package com.reggarf.mods.zap_hosting_server_integration_menu.api;

import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;

import java.util.function.Consumer;

public class ZHAPI {

    public static void generateAndOpenOrder(ZHOrderConfig config, Consumer<Boolean> onComplete) {
        ZHOrderLinkGenerator.generateAndOpenOrder(config, onComplete);
    }
}
