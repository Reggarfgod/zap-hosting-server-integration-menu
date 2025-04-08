package com.reggarf.mods.zap_hosting_server_integration_menu;

import com.reggarf.mods.zap_hosting_server_integration_menu.config.ZHConfig;


import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHMessageHandler;
import com.reggarf.mods.zap_hosting_server_integration_menu.event.ZHTopImageEvent;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Zap_Hosting.MOD_ID)
public class Zap_Hosting {
    public static final String MOD_ID = "zap_hosting_server_integration_menu";
    public static ZHConfig CONFIG;

    public Zap_Hosting(IEventBus modEventBus, ModContainer modContainer) {

        EVENT_BUS.register(this);
        init();
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(ZHMessageHandler.class);
        NeoForge.EVENT_BUS.register(new ZHTopImageEvent());

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (container, parent) -> {
            return AutoConfig.getConfigScreen(ZHConfig.class, parent).get();
        });


    }





    public static void init() {
        AutoConfig.register(ZHConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        CONFIG = AutoConfig.getConfigHolder(ZHConfig.class).getConfig();

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }
    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
   }
}
