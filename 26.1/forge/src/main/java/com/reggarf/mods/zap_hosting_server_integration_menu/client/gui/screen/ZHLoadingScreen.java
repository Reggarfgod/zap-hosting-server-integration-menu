package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHBaseWizardScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHStep1LauncherScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHStep10BillingScreen;
import com.mojang.logging.LogUtils;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.api.ZHOrderLinkGenerator;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHLiveDataProvider;
import com.reggarf.mods.zap_hosting_server_integration_menu.model.ZHOrderConfig;
import io.netty.channel.ChannelFuture;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ZHLoadingScreen extends Screen {

    public enum Mode {
        PRELOAD,
        ORDER,
        CONNECT
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Identifier LOGO =
            Identifier.fromNamespaceAndPath(ZapHosting.MOD_ID, "textures/gui/logo.png");

    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);

    private final ZHOrderConfig config;
    private final Mode mode;
    private final Screen parentScreen;
    private final String serverName;
    private final String serverIp;

    @Nullable private volatile Connection connection;
    @Nullable private ChannelFuture channelFuture;
    private volatile boolean aborted = false;
    private volatile Component connectStatus = Component.literal("Connecting to server...");

    private volatile String generatedOrderUrl = null;
    private volatile boolean isOrderReady = false;
    private volatile boolean isOrderFailed = false;

    private volatile float targetProgress = 0.05f;
    private float currentProgress = 0.05f;
    private volatile String statusMessage = "Connecting to ZAP-Hosting...";
    private long startTime;
    private boolean completed = false;

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
     * Safely opens ZHLoadingScreen in CONNECT mode without triggering Minecraft's default ProgressScreen.
     */
    public static void openAndConnect(Screen parentScreen, String serverName, String serverIp) {
        Minecraft minecraft = Minecraft.getInstance();
        ZHLoadingScreen loadingScreen = new ZHLoadingScreen(parentScreen, serverName, serverIp);
        minecraft.disconnect(loadingScreen, false);
        minecraft.prepareForMultiplayer();
        minecraft.updateReportEnvironment(ReportEnvironment.thirdParty(serverIp));
        minecraft.quickPlayLog().setWorldData(QuickPlayLog.Type.MULTIPLAYER, serverIp, serverName);
        minecraft.setScreen(loadingScreen);
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

            startServerConnection();
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

    private void startServerConnection() {
        Minecraft minecraft = Minecraft.getInstance();
        ServerAddress serverAddress = ServerAddress.parseString(this.serverIp);
        ServerData serverData = new ServerData(this.serverName, this.serverIp, ServerData.Type.OTHER);

        Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                InetSocketAddress inetSocketAddress = null;
                try {
                    if (ZHLoadingScreen.this.aborted) return;

                    Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress)
                            .map(ResolvedServerAddress::asInetSocketAddress);

                    if (ZHLoadingScreen.this.aborted) return;

                    if (optional.isEmpty()) {
                        minecraft.execute(() -> minecraft.setScreen(
                                new DisconnectedScreen(
                                        ZHLoadingScreen.this.parentScreen != null ? ZHLoadingScreen.this.parentScreen : new JoinMultiplayerScreen(null),
                                        CommonComponents.CONNECT_FAILED,
                                        Component.translatable("disconnect.genericReason", Component.translatable("disconnect.unknownHost"))
                                )
                        ));
                        return;
                    }

                    inetSocketAddress = optional.get();
                    Connection conn;
                    synchronized (ZHLoadingScreen.this) {
                        if (ZHLoadingScreen.this.aborted) return;
                        conn = new Connection(PacketFlow.CLIENTBOUND);
                        conn.setBandwidthLogger(minecraft.getDebugOverlay().getBandwidthLogger());
                        ZHLoadingScreen.this.channelFuture = Connection.connect(
                                inetSocketAddress,
                                EventLoopGroupHolder.remote(minecraft.options.useNativeTransport()),
                                conn
                        );
                    }

                    ZHLoadingScreen.this.channelFuture.syncUninterruptibly();
                    synchronized (ZHLoadingScreen.this) {
                        if (ZHLoadingScreen.this.aborted) {
                            conn.disconnect(Component.translatable("connect.aborted"));
                            return;
                        }
                        ZHLoadingScreen.this.connection = conn;
                        minecraft.getDownloadedPackSource().configureForServerControl(conn, ServerPackManager.PackPromptStatus.ALLOWED);
                    }

                    ZHLoadingScreen.this.connection.initiateServerboundPlayConnection(
                            inetSocketAddress.getHostName(),
                            inetSocketAddress.getPort(),
                            LoginProtocols.SERVERBOUND,
                            LoginProtocols.CLIENTBOUND,
                            new ClientHandshakePacketListenerImpl(
                                    ZHLoadingScreen.this.connection,
                                    minecraft,
                                    serverData,
                                    ZHLoadingScreen.this.parentScreen != null ? ZHLoadingScreen.this.parentScreen : new JoinMultiplayerScreen(null),
                                    false,
                                    null,
                                    statusComponent -> ZHLoadingScreen.this.connectStatus = statusComponent,
                                    new LevelLoadTracker(),
                                    null
                            ),
                            false
                    );
                    ZHLoadingScreen.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getUser().getProfileId()));
                } catch (Exception ex) {
                    if (ZHLoadingScreen.this.aborted) return;

                    Exception realEx = (ex.getCause() instanceof Exception c) ? c : ex;
                    String msg = inetSocketAddress == null ? realEx.getMessage() : realEx.getMessage()
                            .replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "")
                            .replaceAll(inetSocketAddress.toString(), "");

                    minecraft.execute(() -> minecraft.setScreen(
                            new DisconnectedScreen(
                                    ZHLoadingScreen.this.parentScreen != null ? ZHLoadingScreen.this.parentScreen : new JoinMultiplayerScreen(null),
                                    CommonComponents.CONNECT_FAILED,
                                    Component.translatable("disconnect.genericReason", msg)
                            )
                    ));
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mode == Mode.CONNECT && this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }
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
        if (mode == Mode.CONNECT) {
            synchronized (this) {
                this.aborted = true;
                if (this.channelFuture != null) {
                    this.channelFuture.cancel(true);
                    this.channelFuture = null;
                }
                if (this.connection != null) {
                    this.connection.disconnect(Component.translatable("connect.aborted"));
                }
            }
            Minecraft.getInstance().setScreen(this.parentScreen != null ? this.parentScreen : new JoinMultiplayerScreen(null));
            return;
        }

        if (mode == Mode.ORDER) {
            Minecraft.getInstance().setScreen(new ZHStep10BillingScreen(this.config));
        } else {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(null));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fillGradient(0, 0, this.width, this.height, 0xEE121415, 0xF90A0B0C);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

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
        graphics.outline(cardX, cardY, cardW, cardH, 0xFF353A3D);

        // 2. Animated / Centered Logo
        int logoSize = 34;
        int logoX = centerX - logoSize / 2;
        int logoY = cardY + 16;

        // Logo background frame
        graphics.fill(logoX - 3, logoY - 3, logoX + logoSize + 3, logoY + logoSize + 3, 0xFF263228);
        graphics.outline(logoX - 3, logoY - 3, logoSize + 6, logoSize + 6, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);

        // 3. Titles
        String title = mode == Mode.ORDER ? "ZAP-Hosting Order" : "ZAP-Hosting Server";
        String subtitle = mode == Mode.ORDER ? "Preparing Your Server..." : "Preparing Configurator...";

        graphics.centeredText(this.font, title, centerX, logoY + logoSize + 8, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        graphics.centeredText(this.font, subtitle, centerX, logoY + logoSize + 20, 0xFFFFFFFF);

        // 4. Progress Bar
        int barW = cardW - 40;
        int barH = 6;
        int barX = centerX - barW / 2;
        int barY = logoY + logoSize + 36;

        // Track
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF141618);
        graphics.outline(barX, barY, barW, barH, 0xFF2F3438);

        // Fill
        int fillW = (int) (barW * Mth.clamp(currentProgress, 0.0f, 1.0f));
        if (fillW > 0) {
            graphics.fill(barX + 1, barY + 1, barX + fillW, barY + barH - 1, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
            // Highlight shine line
            graphics.fill(barX + 1, barY + 1, barX + fillW, barY + 2, 0xFF8AE58A);
        }

        // 5. Dynamic Status text with animated dots
        long elapsed = (Util.getMillis() - startTime) / 350L;
        String dots = ".".repeat((int) (elapsed % 4));
        String fullStatus = statusMessage.endsWith(".") ? statusMessage : (statusMessage + dots);

        graphics.centeredText(this.font, fullStatus, centerX, barY + 11, 0xFFAAAAAA);
    }

    public static void renderJoinServerCard(GuiGraphicsExtractor graphics, int width, int height, Font font, Component status, String serverName, String serverIp) {
        renderJoinServerCard(graphics, width, height, font, status, serverName, serverIp, 0L);
    }

    /**
     * Dedicated method for rendering custom join screen card when connecting to the public server.
     */
    public static void renderJoinServerCard(GuiGraphicsExtractor graphics, int width, int height, Font font, Component status, String serverName, String serverIp, long startTime) {
        int centerX = width / 2;
        int centerY = height / 2;

        // 1. Central Card
        int cardW = Math.min(270, width - 40);
        int cardH = 135;
        int cardX = centerX - cardW / 2;
        int cardY = centerY - cardH / 2 - 12;

        // Card backdrop & subtle border
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xDD1E2325);
        graphics.outline(cardX, cardY, cardW, cardH, 0xFF353A3D);

        // 2. Animated / Centered Logo
        int logoSize = 34;
        int logoX = centerX - logoSize / 2;
        int logoY = cardY + 16;

        // Logo background frame
        graphics.fill(logoX - 3, logoY - 3, logoX + logoSize + 3, logoY + logoSize + 3, 0xFF263228);
        graphics.outline(logoX - 3, logoY - 3, logoSize + 6, logoSize + 6, ZHBaseWizardScreen.COLOR_ZAP_GREEN);

        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);

        // 3. Titles
        String displayTitle = (serverName != null && !serverName.isEmpty()) ? serverName : "ZAP-Hosting Server";
        String displayIp = (serverIp != null && !serverIp.isEmpty()) ? serverIp : "play.zap-hosting.com";

        graphics.centeredText(font, displayTitle, centerX, logoY + logoSize + 8, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        graphics.centeredText(font, "Connecting to " + displayIp + "...", centerX, logoY + logoSize + 20, 0xFFFFFFFF);

        // 4. Animated Connecting Progress Bar (sweeping bar effect)
        int barW = cardW - 40;
        int barH = 6;
        int barX = centerX - barW / 2;
        int barY = logoY + logoSize + 36;

        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF141618);
        graphics.outline(barX, barY, barW, barH, 0xFF2F3438);

        float cycle = ((Util.getMillis() - startTime) % 1800L) / 1800.0f;
        int sweepW = Math.max(30, barW / 3);
        int sweepX = barX + 1 + (int) (cycle * (barW - sweepW - 2));

        graphics.fill(sweepX, barY + 1, sweepX + sweepW, barY + barH - 1, ZHBaseWizardScreen.COLOR_ZAP_GREEN);
        graphics.fill(sweepX, barY + 1, sweepX + sweepW, barY + 2, 0xFF8AE58A);

        // 5. Dynamic Status text with animated dots
        long elapsed = (Util.getMillis() - startTime) / 350L;
        String dots = ".".repeat((int) (elapsed % 4));
        String statusStr = status != null ? status.getString() : "Connecting to server...";
        String fullStatus = statusStr.endsWith(".") ? statusStr : (statusStr + dots);

        graphics.centeredText(font, fullStatus, centerX, barY + 11, 0xFFAAAAAA);
    }
}
