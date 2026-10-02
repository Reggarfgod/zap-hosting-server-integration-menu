package com.reggarf.mods.zap_hosting_server_integration_menu.client;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ZHModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ZHConfigScreen(parent);
    }
}
