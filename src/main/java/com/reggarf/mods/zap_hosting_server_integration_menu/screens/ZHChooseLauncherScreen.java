package com.reggarf.mods.zap_hosting_server_integration_menu.screens;

import com.reggarf.mods.zap_hosting_server_integration_menu.apicall.ZHBudgetPlanScreen;
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
            Minecraft.getInstance().setScreen(new ZHPlayerSlot());
        }).pos(centerX - 50, centerY + 100).size(100, 20).build());

        // Only Budget Plan button
        this.addRenderableWidget(Button.builder(Component.literal("Select"), b -> {
            Minecraft.getInstance().setScreen(new ZHChoosePlanScreen());
        }).pos(centerX - 60, centerY + 70).size(120, 20).build());

        // Budget plan features scrollable list
        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;

        Map<String, Component> launcherMap = Map.of(
                "minecraft", Component.translatable("screen.config.Minecraft"),
                "curseforge", Component.translatable("screen.config.Curse_Forge"),
                "ftb", Component.translatable("screen.config.Feed_the_Beast"),
                "atlauncher", Component.translatable("screen.config.AT_Launcher"),
                "technic", Component.translatable("screen.config.Technic_Launcher"),
                "voidswrath", Component.translatable("screen.config.Voids_Wrath_Launcher"),
                "minigames", Component.translatable("screen.config.Minecraft_Minigames"),
                "adventure", Component.translatable("screen.config.Minecraft Adventure")
        );

        List<Component> launchers = List.copyOf(launcherMap.values());


        ZHClickableTextList launcherList = new ZHClickableTextList(
                centerX - (boxWidth / 2), boxY, boxWidth, boxHeight, launchers
        );

        // Set icons for each launcher (make sure these textures exist)
        launcherList.setImageForEntry(launcherMap.get("minecraft"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/minecraft.png"));
        launcherList.setImageForEntry(launcherMap.get("curseforge"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/curseforge.png"));
        launcherList.setImageForEntry(launcherMap.get("ftb"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/ftb.png"));
        launcherList.setImageForEntry(launcherMap.get("atlauncher"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/atlauncher.png"));
        launcherList.setImageForEntry(launcherMap.get("technic"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/technic.png"));
        launcherList.setImageForEntry(launcherMap.get("voidswrath"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/voidswrath.png"));
        launcherList.setImageForEntry(launcherMap.get("minigames"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/minigames.png"));
        launcherList.setImageForEntry(launcherMap.get("adventure"), ResourceLocation.fromNamespaceAndPath("zap_hosting_server_integration_menu", "textures/gui/launchers/adventure.png"));

        this.addRenderableWidget(launcherList);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        // 1. Now draw widgets (icons/text)

        //this.renderBackground(graphics);

        int centerX = this.width / 2;
        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;
        int boxPadding = 10;

        // 2. Draw dark shaded background box
        graphics.fill(centerX - (boxWidth / 2) - boxPadding, boxY - boxPadding,
                centerX + (boxWidth / 2) + boxPadding, boxY + boxHeight + boxPadding,
                0xAA000000);
        super.render(graphics, mouseX, mouseY, delta);
        // 3. Draw title
        graphics.drawCenteredString(this.font, Component.translatable("screen.config.title"), centerX, 30, 0xFFFFFF);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.config.Choose_Launcher").copy().withStyle(style -> style.withBold(true)),
                centerX, boxY - 22, 0xFFFFFF);


    }

}
