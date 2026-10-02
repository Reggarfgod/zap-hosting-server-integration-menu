package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.util;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry.ZHPublicServerListEntry;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry.ZHServerListEntry;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

public class ZHServerListHelper {

    public static void ensureCustomEntries(JoinMultiplayerScreen screen, ServerSelectionList list) {
        if (screen == null || list == null) return;

        boolean showBanner = true;
        boolean showPublic = ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null
                && Boolean.TRUE.equals(ZapHosting.CONFIG.common.enablePublicServer)
                && ZapHosting.CONFIG.common.publicServerIp != null
                && !ZapHosting.CONFIG.common.publicServerIp.trim().isEmpty();

        boolean hasBanner = false;
        boolean hasPublic = false;
        for (ServerSelectionList.Entry entry : list.children()) {
            if (entry instanceof ZHServerListEntry) hasBanner = true;
            if (entry instanceof ZHPublicServerListEntry) hasPublic = true;
        }

        int insertIndex = 0;
        if (showBanner && !hasBanner) {
            list.children().add(insertIndex++, new ZHServerListEntry(screen, list));
        }
        if (showPublic && !hasPublic) {
            list.children().add(insertIndex, new ZHPublicServerListEntry(screen, list));
        }
    }
}
