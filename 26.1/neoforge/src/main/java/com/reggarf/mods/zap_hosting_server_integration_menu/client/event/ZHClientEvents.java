package com.reggarf.mods.zap_hosting_server_integration_menu.client.event;

/**
 * Previously rendered a top banner overlay at the top of the multiplayer screen.
 * The integration has now been relocated to a pinned, undeletable entry at the top of the multiplayer server list (ZHServerListEntry).
 */
public class ZHClientEvents {

    private static String playMultiplayerText = "";
    private static float textSize = 0.8f;

    public static void setPlayMultiplayerText(String newText) {
        playMultiplayerText = newText;
    }

    public static void setPlayMultiplayerTextSize(float size) {
        textSize = size;
    }
}
