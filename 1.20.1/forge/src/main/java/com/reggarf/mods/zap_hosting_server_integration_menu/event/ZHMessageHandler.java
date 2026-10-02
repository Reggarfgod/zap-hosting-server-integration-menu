package com.reggarf.mods.zap_hosting_server_integration_menu.event;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;

public class ZHMessageHandler {

    /**
     * Triggered on server login (Singleplayer worlds and Dedicated Servers with the mod installed).
     * Tracks player persistent NBT data ('hasJoinedBefore') on the server so the welcome popup
     * only ever triggers on the 1st join per world/server.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ZapHosting.CONFIG == null || ZapHosting.CONFIG.common == null || !ZapHosting.CONFIG.common.enableOverlay) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (isFirstJoin(serverPlayer)) {
                markPlayerAsJoined(serverPlayer);

                if (FMLEnvironment.dist.isClient()) {
                    // Integrated singleplayer server: trigger locally
                    ZHClientTicker.triggerWelcomePopup(20);
                } else {
                    // Dedicated server: send packet to client
                    ZapHosting.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ZHWelcomePopupPayload(20));
                }
            }
        }
    }

    public static boolean isFirstJoin(ServerPlayer player) {
        return !player.getPersistentData().getBoolean("hasJoinedBefore");
    }

    public static void markPlayerAsJoined(ServerPlayer player) {
        player.getPersistentData().putBoolean("hasJoinedBefore", true);
    }
}
