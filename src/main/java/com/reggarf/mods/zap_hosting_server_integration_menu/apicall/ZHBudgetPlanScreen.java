package com.reggarf.mods.zap_hosting_server_integration_menu.apicall;

import com.reggarf.mods.zap_hosting_server_integration_menu.screens.ZHChoosePlanScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;

public class ZHBudgetPlanScreen extends Screen {

    private PlanSlider slider;
    private String selectedPlan = "Budget Plan - 6GB RAM $17.94/Month";

    public ZHBudgetPlanScreen() {
        super(Component.literal("Let's create your Minecraft Server."));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Slider
        slider = new PlanSlider(centerX - 100, centerY + 10, 200, 20);
        addRenderableWidget(slider);

        // Order Button
        addRenderableWidget(Button.builder(Component.literal("Order Plan"), b -> {
            try {
                String url = switch (selectedPlan) {
                    case "Budget Plan - 4GB RAM $9.99/Month" -> "https://zap-hosting.com/en/shop/product/cloud-gameserver/minecraft/";
                    case "Budget Plan - 6GB RAM $17.94/Month" -> "https://zap-hosting.com/en/shop/product/cloud-gameserver/minecraft/";
                    case "Budget Plan - 8GB RAM $22.99/Month" -> "https://zap-hosting.com/en/shop/product/cloud-gameserver/minecraft/";
                    default -> "https://zap-hosting.com/en/shop/product/cloud-gameserver/minecraft/";
                };
                Util.getPlatform().openUri(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).bounds(centerX - 75, centerY + 40, 150, 20).build());

        // Back Button
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            Minecraft.getInstance().setScreen(new ZHChoosePlanScreen());
        }).bounds(centerX - 40, centerY + 70, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        graphics.drawCenteredString(this.font, Component.literal("Let's create your Minecraft Server."), centerX, 40, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("We've selected the perfect plan for you!"), centerX, 60, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal(selectedPlan), centerX, 85, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Move the slider to choose a different plan"), centerX, 115, 0xFFFFFF);
    }

    private class PlanSlider extends AbstractSliderButton {

        private final String[] plans = {
                "Budget Plan - 4GB RAM $9.99/Month",
                "Budget Plan - 6GB RAM $17.94/Month",
                "Budget Plan - 8GB RAM $22.99/Month"
        };

        public PlanSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), 1 / 2.0); // Defaults to 6GB
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int index = (int) (value * (plans.length - 1));
            selectedPlan = plans[index];
        }

        @Override
        protected void applyValue() {
            updateMessage();
        }
    }
}
