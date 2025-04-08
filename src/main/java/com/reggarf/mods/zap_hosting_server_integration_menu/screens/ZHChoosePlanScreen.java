package com.reggarf.mods.zap_hosting_server_integration_menu.screens;

import com.reggarf.mods.zap_hosting_server_integration_menu.apicall.ZHBudgetPlanScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.apicall.ZHPremiumPlanScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.tool.ZHScrollableTextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ZHChoosePlanScreen extends Screen {

    public ZHChoosePlanScreen() {
        super(Component.literal("Choose Plan Type"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            Minecraft.getInstance().setScreen(new ZHChooseLauncherScreen());
        }).pos(centerX - 50, centerY + 90).size(100, 20).build());

        // Choose Premium button
        this.addRenderableWidget(Button.builder(Component.literal("Choose Premium"), b -> {
            Minecraft.getInstance().setScreen(new ZHPremiumPlanScreen());
        }).pos(centerX - 160, centerY + 70).size(120, 20).build());

        // Choose Budget button
        this.addRenderableWidget(Button.builder(Component.literal("Choose Budget"), b -> {
            Minecraft.getInstance().setScreen(new ZHBudgetPlanScreen());
        }).pos(centerX + 40, centerY + 70).size(120, 20).build());

        // Scrollable text boxes (without titles)
        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;

        this.addRenderableWidget(new ZHScrollableTextList(
                centerX - 160, boxY,
                boxWidth, boxHeight,
                List.of(
                        Component.translatable("screen.config.AllBudgetFeatures"),
                        Component.translatable("screen.config.FreeDedicatedIP"),
                        Component.translatable("screen.config.FreeUnlimitedSlots"),
                        Component.translatable("screen.config.MoreLocations")
                )
        ));

        this.addRenderableWidget(new ZHScrollableTextList(
                centerX + 40, boxY,
                boxWidth, boxHeight,
                List.of(
                        Component.translatable("screen.config.InstantSetup"),
                        Component.translatable("screen.config.FreeMySQLDatabase"),
                        Component.translatable("screen.config.24/7Support"),
                        Component.translatable("screen.config.FullFTPAccess"),
                        Component.translatable("screen.config.UnlimitedNVMeSSDSpace"),
                        Component.translatable("screen.config.Free Modpack/ForgeInstallation")
                )
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        // Headers
        graphics.drawCenteredString(this.font, Component.translatable("screen.config.title"), this.width / 2, 30, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.config.Choose_plan_type"), this.width / 2, 50, 0xFFFFFF);

        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;
        int boxPadding = 10;

        // Titles above scroll areas (now bold)
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.config.PremiumPlans").copy().withStyle(style -> style.withBold(true)),
                this.width / 2 - 160 + (boxWidth / 2), boxY - 20, 0xFFFFFF);

        graphics.drawCenteredString(this.font,
                Component.translatable("screen.config.BudgetPlans").copy().withStyle(style -> style.withBold(true)),
                this.width / 2 + 40 + (boxWidth / 2), boxY - 20, 0xFFFFFF);


        // Background boxes
        graphics.fill(this.width / 2 - 160 - boxPadding, boxY - boxPadding,
                this.width / 2 - 160 + boxWidth + boxPadding, boxY + boxHeight + boxPadding,
                0xAA000000);

        graphics.fill(this.width / 2 + 40 - boxPadding, boxY - boxPadding,
                this.width / 2 + 40 + boxWidth + boxPadding, boxY + boxHeight + boxPadding,
                0xAA000000);
    }
}
