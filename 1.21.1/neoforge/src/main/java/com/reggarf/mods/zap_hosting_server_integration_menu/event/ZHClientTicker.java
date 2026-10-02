package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.ZHClientSetup;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import javax.annotation.Nullable;

/**
 * Client-only event handler for tick polling and popup display.
 * Kept completely separated from common/server code.
 */
public class ZHClientTicker {

    private static volatile boolean pendingWelcomeScreen = false;
    private static volatile int delayTicks = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
    }

    public static void triggerWelcomePopup(int ticks) {
        pendingWelcomeScreen = true;
        delayTicks = ticks;
    }

    public static void showWelcomePopup(@Nullable Screen parentScreen) {
        ZHClientSetup.openWelcomePopup(parentScreen);
    }
}
