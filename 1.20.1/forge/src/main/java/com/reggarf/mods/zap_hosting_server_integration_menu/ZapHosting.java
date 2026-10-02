package com.reggarf.mods.zap_hosting_server_integration_menu;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.ZHClientSetup;
import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHMessageHandler;
import com.reggarf.mods.zap_hosting_server_integration_menu.network.ZHWelcomePopupPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(ZapHosting.MOD_ID)
public class ZapHosting {
    public static final String MOD_ID = "zap_hosting_server_integration_menu";
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            clientVersion -> true,
            serverVersion -> true
    );

    public static ZHConfig CONFIG;

    public ZapHosting() {
        init();

        // Register network payload for Forge 1.20.1 SimpleChannel
        CHANNEL.messageBuilder(ZHWelcomePopupPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ZHWelcomePopupPayload::encode)
                .decoder(ZHWelcomePopupPayload::new)
                .consumerMainThread((msg, ctxSupplier) -> {
                    ctxSupplier.get().setPacketHandled(true);
                    if (FMLEnvironment.dist.isClient()) {
                        ZHClientSetup.handleWelcomePacketOnClient(msg);
                    }
                })
                .add();

        // Register ZHMessageHandler on Forge EVENT_BUS (handles server-side PlayerLoggedInEvent)
        MinecraftForge.EVENT_BUS.register(ZHMessageHandler.class);

        // Only register client-only screens and providers when on the physical client
        if (FMLEnvironment.dist.isClient()) {
            ZHClientSetup.initClient();
        }
    }

    public static void init() {
        CONFIG = ZHConfig.load();
    }
}
