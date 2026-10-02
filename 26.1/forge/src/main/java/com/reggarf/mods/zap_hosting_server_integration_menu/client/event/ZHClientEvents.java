package com.reggarf.mods.zap_hosting_server_integration_menu.client.event;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Previously rendered a top banner overlay at the top of the multiplayer screen.
 * The integration has now been relocated to a pinned, undeletable entry at the top of the multiplayer server list (ZHServerListEntry).
 */
public class ZHClientEvents {

    private static String playMultiplayerText = "";
    private static float textSize = 0.8f;

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        // No-op: Top bar overlay removed. Relocated to pinned server list entry (ZHServerListEntry).
    }

    public static void setPlayMultiplayerText(String newText) {
        playMultiplayerText = newText;
    }

    public static void setPlayMultiplayerTextSize(float size) {
        textSize = size;
    }
}
