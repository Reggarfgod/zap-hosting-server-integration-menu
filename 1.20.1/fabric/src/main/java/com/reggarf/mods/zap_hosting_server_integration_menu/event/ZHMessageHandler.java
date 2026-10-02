package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ZHMessageHandler {

    private static final String JOIN_TAG = "zap_hosting_has_joined";

    /**
     * Initializes Fabric server-side player login handling.
     * Tracks scoreboard tag on the server player so the welcome popup
     * only ever triggers on the 1st join per world/server.
     */
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ZapHosting.CONFIG == null || ZapHosting.CONFIG.common == null || !ZapHosting.CONFIG.common.enableOverlay) {
                return;
            }

            ServerPlayer serverPlayer = handler.getPlayer();
            if (isFirstJoin(serverPlayer)) {
                markPlayerAsJoined(serverPlayer);

                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                new ZHWelcomePopupPayload(20).encode(buf);
                ServerPlayNetworking.send(serverPlayer, ZapHosting.WELCOME_PACKET_ID, buf);
            }
        });
    }

    public static boolean isFirstJoin(ServerPlayer player) {
        return !player.getTags().contains(JOIN_TAG);
    }

    public static void markPlayerAsJoined(ServerPlayer player) {
        player.addTag(JOIN_TAG);
    }
}
