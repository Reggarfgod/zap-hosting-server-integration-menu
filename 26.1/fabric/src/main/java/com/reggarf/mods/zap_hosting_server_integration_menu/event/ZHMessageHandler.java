package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ZHMessageHandler {

    private static final String JOIN_TAG = "zap_hosting_has_joined";

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ZapHosting.CONFIG == null || ZapHosting.CONFIG.common == null || !ZapHosting.CONFIG.common.enableOverlay) {
                return;
            }

            ServerPlayer serverPlayer = handler.getPlayer();
            if (isFirstJoin(serverPlayer)) {
                markPlayerAsJoined(serverPlayer);
                ServerPlayNetworking.send(serverPlayer, new ZHWelcomePopupPayload(20));
            }
        });
    }

    public static boolean isFirstJoin(ServerPlayer player) {
        return !player.entityTags().contains(JOIN_TAG);
    }

    public static void markPlayerAsJoined(ServerPlayer player) {
        player.addTag(JOIN_TAG);
    }
}
