package com.reggarf.mods.zap_hosting_server_integration_menu.event;




import com.reggarf.mods.zap_hosting_server_integration_menu.Zap_Hosting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.network.chat.TextColor.fromRgb;


@Mod.EventBusSubscriber(modid = Zap_Hosting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZHMessageHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Zap_Hosting.CONFIG.common.enableOverlay) {
            // Check if the player is joining for the first time
            if (isFirstJoin(player)) {
                Component message = createClickableMessage();
                player.sendSystemMessage(message);
                markPlayerAsJoined(player);
            }
        }
    }

    private static boolean isFirstJoin(ServerPlayer player) {
        // Example logic: Use persistent data to track if the player has joined before
        return !player.getPersistentData().getBoolean("hasJoinedBefore");
    }

    private static void markPlayerAsJoined(ServerPlayer player) {
        // Mark the player as having joined before
        player.getPersistentData().putBoolean("hasJoinedBefore", true);
    }

    private static Component createClickableMessage() {
        // Parse the RGB colors from configuration
       TextColor textColor = parseTextColor("#FFFFFF");
        TextColor clickableTextColor = parseTextColor("#00FFAA");

         //int color
         //TextColor textColor = fromRgb(WFJMessage.CONFIG.common.welcomeMessageColor);
         //TextColor clickableTextColor = fromRgb(WFJMessage.CONFIG.common.clickableTextColor);

        return Component.literal("THANKS TO OUR SPONSORS, ZAP-HOSTING! Support us And Save BIG - USE CODE "+Zap_Hosting.CONFIG.common.code +"FOR 20% OFF Your ORDER!")
                .setStyle(Style.EMPTY.withColor(textColor)) // Main message color
                .append(" ")
                .append(Component.literal("[Click here]")
                        .setStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Zap_Hosting.CONFIG.common.link))
                                .withUnderlined(true)
                                .withColor(clickableTextColor) // Clickable text color
                        ));
    }

    private static TextColor parseTextColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1); // Remove '#' if present
            }
            int rgb = Integer.parseInt(hex, 16); // Convert hex to int
            return fromRgb(rgb);
        } catch (NumberFormatException e) {
            System.err.println("Invalid color format: " + hex);
            return fromRgb(0xFFFFFF); // Default to white if invalid
        }
    }
}