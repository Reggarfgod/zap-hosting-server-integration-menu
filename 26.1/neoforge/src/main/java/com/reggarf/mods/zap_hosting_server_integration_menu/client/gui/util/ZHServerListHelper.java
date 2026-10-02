package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.util;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry.ZHPublicServerListEntry;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry.ZHServerListEntry;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.ArrayList;
import java.util.List;

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

        if ((showBanner && !hasBanner) || (showPublic && !hasPublic)) {
            List<ServerSelectionList.Entry> entries = new ArrayList<>();
            if (showBanner) {
                entries.add(new ZHServerListEntry(screen, list));
            }
            if (showPublic) {
                entries.add(new ZHPublicServerListEntry(screen, list));
            }
            for (ServerSelectionList.Entry entry : list.children()) {
                if (!(entry instanceof ZHServerListEntry) && !(entry instanceof ZHPublicServerListEntry)) {
                    entries.add(entry);
                }
            }
            list.replaceEntries(entries);
        }
    }
}
