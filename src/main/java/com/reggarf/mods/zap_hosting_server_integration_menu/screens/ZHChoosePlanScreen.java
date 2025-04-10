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

    private final String launcherKey;

    public ZHChoosePlanScreen(String launcherKey) {
        super(Component.literal("Choose Plan Type"));
        this.launcherKey = launcherKey;
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
            Minecraft.getInstance().setScreen(new ZHPremiumPlanScreen(launcherKey));
        }).pos(centerX - 160, centerY + 60).size(120, 20).build());

        // Choose Budget button
        this.addRenderableWidget(Button.builder(Component.literal("Choose Budget"), b -> {
            Minecraft.getInstance().setScreen(new ZHBudgetPlanScreen(launcherKey));
        }).pos(centerX + 40, centerY + 60).size(120, 20).build());

        // Scrollable text boxes (without titles)
        int boxWidth = 150;
        int boxHeight = 90;
        int boxY = 80;

        this.addRenderableWidget(new ZHScrollableTextList(
                centerX - 160, boxY,
                boxWidth, boxHeight,
                List.of(
                        Component.translatable("screen.config.1"),
                        Component.translatable("screen.config.2"),
                        Component.translatable("screen.config.3"),
                        Component.translatable("screen.config.4")
                )
        ));

        this.addRenderableWidget(new ZHScrollableTextList(
                centerX + 40, boxY,
                boxWidth, boxHeight,
                List.of(
                        Component.translatable("screen.config.1.1"),
                        Component.translatable("screen.config.1.2"),
                        Component.translatable("screen.config.1.3"),
                        Component.translatable("screen.config.1.4"),
                        Component.translatable("screen.config.1.5"),
                        Component.translatable("screen.config.1.6")
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
