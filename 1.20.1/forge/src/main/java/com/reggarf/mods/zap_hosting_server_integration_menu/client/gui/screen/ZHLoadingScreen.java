package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHBaseWizardScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHStep1LauncherScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.api.ZHOrderLinkGenerator;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.net.URI;

public class ZHLoadingScreen extends Screen {

    public enum Mode {
        PRELOAD,
        ORDER,
        CONNECT
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation LOGO =
            new ResourceLocation(ZapHosting.MOD_ID, "textures/gui/logo.png");

    private final ZHOrderConfig config;
    private final Mode mode;
    private final Screen parentScreen;
    private final String serverName;
    private final String serverIp;

    private float currentProgress = 0.0f;
    private float targetProgress = 0.05f;
    private String statusMessage = "Connecting to ZAP-Hosting...";
    private Component connectStatus = Component.translatable("connect.connecting");
    private long startTime;
    private boolean completed = false;
    private boolean isOrderReady = false;
    private boolean isOrderFailed = false;
    private String generatedOrderUrl = null;

    private ZHBaseWizardScreen.ZHCustomButton continueButton;
    private ZHBaseWizardScreen.ZHCustomButton cancelButton;

    public ZHLoadingScreen() {
        this(new ZHOrderConfig(), Mode.PRELOAD);
    }

    public ZHLoadingScreen(ZHOrderConfig config) {
        this(config, Mode.PRELOAD);
    }

    public ZHLoadingScreen(ZHOrderConfig config, Mode mode) {
        super(Component.literal("Loading ZAP-Hosting"));
        this.config = config;
        this.mode = mode;
        this.parentScreen = null;
        this.serverName = null;
        this.serverIp = null;
    }

    public ZHLoadingScreen(Screen parentScreen, String serverName, String serverIp) {
        super(Component.literal("Connecting to " + serverName));
        this.config = null;
        this.mode = Mode.CONNECT;
        this.parentScreen = parentScreen;
        this.serverName = serverName;
        this.serverIp = serverIp;
    }

    /**
     * Safely delegates server connection via ConnectScreen on Forge 1.20.1
     */
    public static void openAndConnect(Screen parentScreen, String serverName, String serverIp) {
        Minecraft minecraft = Minecraft.getInstance();
        ServerData serverData = new ServerData(serverName, serverIp, false);
        ConnectScreen.startConnecting(
                parentScreen != null ? parentScreen : new JoinMultiplayerScreen(null),
                minecraft,
                ServerAddress.parseString(serverIp),
                serverData,
                false
        );
    }

    @Override
    protected void init() {
        super.init();
        this.startTime = Util.getMillis();

        int centerX = this.width / 2;
        int btnW = 85;
        int btnH = 20;
        int btnY = this.height - 34;

        if (mode == Mode.CONNECT) {
            cancelButton = new ZHBaseWizardScreen.ZHCustomButton(
                    centerX - btnW / 2, btnY, btnW, btnH,
                    Component.literal("Cancel"),
                    b -> handleCancel(),
                    ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
            );
            addRenderableWidget(cancelButton);
            return;
        }

        if (mode == Mode.ORDER) {
            int actionW = 160;
            int actionH = 22;
            int actionY = this.height / 2 + 65;
            continueButton = new ZHBaseWizardScreen.ZHCustomButton(
                    centerX - actionW / 2, actionY, actionW, actionH,
                    Component.literal("Click here to continue"),
                    b -> handleContinue(),
                    ZHBaseWizardScreen.ZHCustomButton.Style.PRIMARY
            );
            continueButton.active = false;
            addRenderableWidget(continueButton);

            cancelButton = new ZHBaseWizardScreen.ZHCustomButton(
                    centerX - btnW / 2, btnY, btnW, btnH,
                    Component.literal("Back"),
                    b -> handleCancel(),
                    ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
            );
            addRenderableWidget(cancelButton);

            this.statusMessage = "Generating server cart...";
            ZHOrderLinkGenerator.generateAndOpenOrder(this.config, success -> {
                this.isOrderReady = true;
                this.isOrderFailed = !success;
                this.targetProgress = 1.0f;
                if (success) {
                    this.statusMessage = "Server cart opened in browser!";
                } else {
                    this.statusMessage = "Order link failed. Click Back to retry.";
                }
                if (continueButton != null) {
                    continueButton.active = true;
                }
            });
            return;
        }

        // PRELOAD mode
        cancelButton = new ZHBaseWizardScreen.ZHCustomButton(
                centerX - btnW / 2, btnY, btnW, btnH,
                Component.literal("Cancel"),
                b -> Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(null)),
                ZHBaseWizardScreen.ZHCustomButton.Style.SECONDARY
        );
        addRenderableWidget(cancelButton);

        // Start background preloading all live data
        ZHLiveDataProvider.preloadAll(
                status -> this.statusMessage = status,
                progress -> this.targetProgress = progress
        ).whenComplete((res, err) -> {
            this.targetProgress = 1.0f;
            this.statusMessage = "Ready!";
        });
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return mode != Mode.CONNECT;
    }

    private void handleContinue() {
        if (mode == Mode.ORDER) {
            if (generatedOrderUrl != null && !generatedOrderUrl.isEmpty()) {
                Util.getPlatform().openUri(URI.create(generatedOrderUrl));
            } else {
                Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(null));
            }
        } else {
            completed = true;
            Minecraft.getInstance().setScreen(new ZHStep1LauncherScreen(this.config));
        }
    }

    private void handleCancel() {
        if (this.parentScreen != null) {
            Minecraft.getInstance().setScreen(this.parentScreen);
        } else {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(null));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (mode == Mode.CONNECT) {
            renderJoinServerCard(graphics, this.width, this.height, this.font, this.connectStatus, this.serverName, this.serverIp, this.startTime);
            return;
        }

        // Smooth progress interpolation
        currentProgress = Mth.lerp(0.12f, currentProgress, targetProgress);

        // Completion transition check for PRELOAD
        if (mode == Mode.PRELOAD) {
            if (targetProgress >= 0.99f && (currentProgress >= 0.96f || Util.getMillis() - startTime > 1200L) && !completed) {
                completed = true;
                Minecraft.getInstance().setScreen(new ZHStep1LauncherScreen(this.config));
                return;
            }

            // Safety fallback timeout: after 5 seconds, proceed regardless
            if (Util.getMillis() - startTime > 5000L && !completed) {
                completed = true;
                Minecraft.getInstance().setScreen(new ZHStep1LauncherScreen(this.config));
                return;
            }
        }

        // 1. Central Card
        int cardW = Math.min(270, this.width - 40);
        int cardH = 135;
        int cardX = centerX - cardW / 2;
        int cardY = centerY - cardH / 2 - 12;

        // Card backdrop & subtle border
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xDD1E2325);
        graphics.renderOutline(cardX, cardY, cardW, cardH, 0xFF353A3D);

        // 2. Animated / Centered Logo
        int logoSize = 34;
        int logoX = centerX - logoSize / 2;
        int logoY = cardY + 16;

        // Logo background frame
        graphics.fill(logoX - 3, logoY - 3, logoX + logoSize + 3, logoY + logoSize + 3, 0xFF263228);
        graphics.renderOutline(logoX - 3, logoY - 3, logoSize + 6, logoSize + 6, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        RenderSystem.enableBlend();
        graphics.blit(LOGO, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);
        RenderSystem.disableBlend();

        // 3. Titles
        String title = mode == Mode.ORDER ? "ZAP-Hosting Order" : "ZAP-Hosting Server";
        String subtitle = mode == Mode.ORDER ? "Preparing Your Server..." : "Preparing Configurator...";

        graphics.drawCenteredString(this.font, title, centerX, logoY + logoSize + 8, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        graphics.drawCenteredString(this.font, subtitle, centerX, logoY + logoSize + 20, 0xFFFFFFFF);

        // 4. Progress Bar
        int barW = cardW - 40;
        int barH = 6;
        int barX = centerX - barW / 2;
        int barY = logoY + logoSize + 36;

        // Track
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF141618);
        graphics.renderOutline(barX, barY, barW, barH, 0xFF2F3438);

        // Fill
        int fillW = (int) (barW * Mth.clamp(currentProgress, 0.0f, 1.0f));
        if (fillW > 0) {
            graphics.fill(barX + 1, barY + 1, barX + fillW, barY + barH - 1, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
            graphics.fill(barX + 1, barY + 1, barX + fillW, barY + 2, 0xFF8AE58A);
        }

        // 5. Dynamic Status text with animated dots
        long elapsed = (Util.getMillis() - startTime) / 350L;
        String dots = ".".repeat((int) (elapsed % 4));
        String fullStatus = statusMessage.endsWith(".") ? statusMessage : (statusMessage + dots);

        graphics.drawCenteredString(this.font, fullStatus, centerX, barY + 11, 0xFFAAAAAA);
    }

    public static void renderJoinServerCard(GuiGraphics graphics, int width, int height, Font font, Component status, String serverName, String serverIp) {
        renderJoinServerCard(graphics, width, height, font, status, serverName, serverIp, 0L);
    }

    public static void renderJoinServerCard(GuiGraphics graphics, int width, int height, Font font, Component status, String serverName, String serverIp, long startTime) {
        int centerX = width / 2;
        int centerY = height / 2;

        int cardW = Math.min(270, width - 40);
        int cardH = 135;
        int cardX = centerX - cardW / 2;
        int cardY = centerY - cardH / 2 - 12;

        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xDD1E2325);
        graphics.renderOutline(cardX, cardY, cardW, cardH, 0xFF353A3D);

        int logoSize = 34;
        int logoX = centerX - logoSize / 2;
        int logoY = cardY + 16;

        graphics.fill(logoX - 3, logoY - 3, logoX + logoSize + 3, logoY + logoSize + 3, 0xFF263228);
        graphics.renderOutline(logoX - 3, logoY - 3, logoSize + 6, logoSize + 6, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        RenderSystem.enableBlend();
        graphics.blit(LOGO, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);
        RenderSystem.disableBlend();

        graphics.drawCenteredString(font, "Connecting to Server", centerX, logoY + logoSize + 8, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        graphics.drawCenteredString(font, serverName != null ? serverName : "ZAP-Hosting Official Server", centerX, logoY + logoSize + 20, 0xFFFFFFFF);

        int barW = cardW - 40;
        int barH = 6;
        int barX = centerX - barW / 2;
        int barY = logoY + logoSize + 36;

        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF141618);
        graphics.renderOutline(barX, barY, barW, barH, 0xFF2F3438);

        long time = startTime > 0 ? (Util.getMillis() - startTime) : Util.getMillis();
        float pulse = 0.5f + 0.5f * (float) Math.sin(time / 250.0);
        int pulseW = (int) (barW * pulse);
        if (pulseW > 0) {
            graphics.fill(barX + 1, barY + 1, barX + pulseW, barY + barH - 1, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
            graphics.fill(barX + 1, barY + 1, barX + pulseW, barY + 2, 0xFF8AE58A);
        }

        Component effectiveStatus = status != null ? status : Component.translatable("connect.connecting");
        graphics.drawCenteredString(font, effectiveStatus, centerX, barY + 11, 0xFFAAAAAA);

        if (serverIp != null && !serverIp.isEmpty()) {
            graphics.drawCenteredString(font, serverIp, centerX, cardY + cardH - 14, 0xFF666666);
        }
    }
}
