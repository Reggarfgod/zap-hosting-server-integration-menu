package com.reggarf.mods.zap_hosting_server_integration_menu.apicall;

import com.reggarf.mods.zap_hosting_server_integration_menu.Zap_Hosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.screens.ZHChoosePlanScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;

public class ZHPremiumPlanScreen extends Screen {

    private PlanSlider slider;
    private String selectedPlan = "Premium Plan - 8GB RAM $39.92/Month";
    private final String launcherKey;

    public ZHPremiumPlanScreen(String launcherKey) {
        super(Component.literal("Let's create your Minecraft Server."));
        this.launcherKey = launcherKey;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Slider to choose plan
        slider = new PlanSlider(centerX - 100, centerY + 10, 200, 20);
        addRenderableWidget(slider);

        // Order button
        addRenderableWidget(Button.builder(Component.literal("Order Plan"), b -> {
            try {
                String url = "https://zap-hosting.com/en/shop/product/cloud-gameserver/" + launcherKey + "/?ref="+Zap_Hosting.CONFIG.common.link;
                Util.getPlatform().openUri(new URI(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).bounds(centerX - 75, centerY + 40, 150, 20).build());

        // Back button
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            Minecraft.getInstance().setScreen(new ZHChoosePlanScreen(launcherKey));
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
                "Premium Plan - 4GB RAM $9.99/Month",
                "Premium Plan - 6GB RAM $19.99/Month",
                "Premium Plan - 8GB RAM $39.92/Month",
                "Premium Plan - 12GB RAM $59.99/Month"
        };

        public PlanSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), 2 / 3.0); // default to Premium
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
