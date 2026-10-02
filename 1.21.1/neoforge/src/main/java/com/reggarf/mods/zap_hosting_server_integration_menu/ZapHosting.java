package com.reggarf.mods.zap_hosting_server_integration_menu;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.ZHClientSetup;
import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHMessageHandler;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ZapHosting.MOD_ID)
public class ZapHosting {
    public static final String MOD_ID = "zap_hosting_server_integration_menu";
    public static ZHConfig CONFIG;

    public ZapHosting(IEventBus modEventBus, ModContainer modContainer) {
        init();

        // Register payload network channel on mod event bus
        modEventBus.addListener(this::registerPayloads);

        // Always register ZHMessageHandler on NeoForge event bus (handles server-side PlayerLoggedInEvent)
        NeoForge.EVENT_BUS.register(ZHMessageHandler.class);

        // Only register client-only screens and providers when on the physical client
        if (FMLEnvironment.dist.isClient()) {
            ZHClientSetup.initClient();
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                ZHWelcomePopupPayload.TYPE,
                ZHWelcomePopupPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist.isClient()) {
                        ZHClientSetup.handleWelcomePacketOnClient(payload, context);
                    }
                }
        );
    }

    public static void init() {
        CONFIG = ZHConfig.load();
    }
}
