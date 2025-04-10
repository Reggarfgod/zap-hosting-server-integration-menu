package com.reggarf.mods.zap_hosting_server_integration_menu.screens;

import com.reggarf.mods.zap_hosting_server_integration_menu.tool.ZHClickableTextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class ZHChooseLauncherScreen extends Screen {

    public ZHChooseLauncherScreen() {
        super(Component.literal("Choose Launcher"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(this));
        }).pos(centerX - 50, centerY + 100).size(100, 20).build());

        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;

        Map<String, Component> launcherMap = Map.of(
                "minecraft", Component.translatable("screen.config.Minecraft"),
                "curse-twitch", Component.translatable("screen.config.Curse_Forge"),
                "feed-the-beast", Component.translatable("screen.config.Feed_the_Beast"),
                "at-launcher", Component.translatable("screen.config.AT_Launcher"),
                "technic-launcher", Component.translatable("screen.config.Technic_Launcher"),
                "voids-wrath-launcher", Component.translatable("screen.config.Voids_Wrath_Launcher"),
                "minecraft-minigames", Component.translatable("screen.config.Minecraft_Minigames"),
                "minecraft-adventure", Component.translatable("screen.config.Minecraft_Adventure")
        );

        List<Component> launchers = List.copyOf(launcherMap.values());

        ZHClickableTextList launcherList = new ZHClickableTextList(
                centerX - (boxWidth / 2), boxY, boxWidth, boxHeight, launchers
        );

        launcherMap.forEach((key, component) -> {
            launcherList.setKeyForEntry(component, key);
            launcherList.setImageForEntry(component, ResourceLocation.fromNamespaceAndPath(
                    "zap_hosting_server_integration_menu", "textures/gui/launchers/" + key + ".png"
            ));
        });

        launcherList.setItemClickListener(key -> {
            // For now, "minecraft" opens multiplayer menu — all others open plan screen with launcher key
            if (key.equals("minecraft")) {
                Minecraft.getInstance().setScreen(new ZHChoosePlanScreen(key));
            } else {
                Minecraft.getInstance().setScreen(new ZHChoosePlanScreen(key));
            }
        });

        this.addRenderableWidget(launcherList);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;
        int boxPadding = 10;

        graphics.fill(centerX - (boxWidth / 2) - boxPadding, boxY - boxPadding,
                centerX + (boxWidth / 2) + boxPadding, boxY + boxHeight + boxPadding,
                0xAA000000);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(this.font, Component.translatable("screen.config.title"), centerX, 30, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.config.Choose_Launcher").copy().withStyle(style -> style.withBold(true)),
                centerX, boxY - 22, 0xFFFFFF);
    }
}
