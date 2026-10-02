package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class ZHMessageHandler {

    private static final String JOIN_TAG = "zap_hosting_has_joined";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ZapHosting.CONFIG == null || ZapHosting.CONFIG.common == null || !ZapHosting.CONFIG.common.enableOverlay) {
                return;
            }

            if (isFirstJoin(player)) {
                markPlayerAsJoined(player);
                ZapHosting.CHANNEL.send(new ZHWelcomePopupPayload(20), PacketDistributor.PLAYER.with(player));
            }
        }
    }

    public static boolean isFirstJoin(ServerPlayer player) {
        return !player.entityTags().contains(JOIN_TAG);
    }

    public static void markPlayerAsJoined(ServerPlayer player) {
        player.addTag(JOIN_TAG);
    }
}
