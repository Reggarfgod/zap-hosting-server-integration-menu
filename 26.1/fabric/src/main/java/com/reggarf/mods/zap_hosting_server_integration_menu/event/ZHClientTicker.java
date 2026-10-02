package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.ZHClientSetup;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Client-only event handler for tick polling and popup display on Fabric.
 */
public class ZHClientTicker {

    private static volatile boolean pendingWelcomeScreen = false;
    private static volatile int delayTicks = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!pendingWelcomeScreen) {
                return;
            }

            delayTicks--;
            if (delayTicks <= 0) {
                if (ZHClientSetup.checkAndOpenWelcomePopupIfReady()) {
                    pendingWelcomeScreen = false;
                } else {
                    delayTicks = 20;
                }
            }
        });
    }

    public static void triggerWelcomePopup(int ticks) {
        pendingWelcomeScreen = true;
        delayTicks = ticks;
    }

    public static void showWelcomePopup(@Nullable Screen parentScreen) {
        ZHClientSetup.openWelcomePopup(parentScreen);
    }
}
