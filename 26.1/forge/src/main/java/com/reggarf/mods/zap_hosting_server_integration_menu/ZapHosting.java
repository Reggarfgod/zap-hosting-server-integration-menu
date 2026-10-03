package com.reggarf.mods.zap_hosting_server_integration_menu;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.ZHClientSetup;
import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHMessageHandler;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

@Mod(ZapHosting.MOD_ID)
public class ZapHosting {
    public static final String MOD_ID = "zap_hosting_server_integration_menu";

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(Identifier.fromNamespaceAndPath(MOD_ID, "main"))
            .networkProtocolVersion(1)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .simpleChannel();

    public static ZHConfig CONFIG;

    public ZapHosting() {
        init();

        CHANNEL.messageBuilder(ZHWelcomePopupPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ZHWelcomePopupPayload::encode)
                .decoder(ZHWelcomePopupPayload::new)
                .consumerMainThread((msg, ctx) -> {
                    ctx.setPacketHandled(true);
                    if (FMLEnvironment.dist.isClient()) {
                        ZHClientSetup.handleWelcomePacketOnClient(msg);
                    }
                })
                .add();

        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(ZHMessageHandler::onPlayerLoggedIn);

        if (FMLEnvironment.dist.isClient()) {
            ZHClientSetup.initClient();
        }
    }

    public static void init() {
        CONFIG = ZHConfig.load();
    }
}
