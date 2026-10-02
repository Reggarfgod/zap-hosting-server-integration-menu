package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHLoadingScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public class ZHPublicServerListEntry extends ServerSelectionList.Entry {

    private static final ResourceLocation LOGO =
            new ResourceLocation(ZapHosting.MOD_ID, "textures/gui/logo.png");

    private final JoinMultiplayerScreen screen;
    private final ServerSelectionList list;
    private final Minecraft minecraft;
    private final ServerData serverData;
    private boolean pingStarted = false;

    private int topPos;
    private int heightPos;

    public ZHPublicServerListEntry(JoinMultiplayerScreen screen, ServerSelectionList list) {
        this.screen = screen;
        this.list = list;
        this.minecraft = Minecraft.getInstance();

        String ip = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.publicServerIp != null && !ZapHosting.CONFIG.common.publicServerIp.trim().isEmpty())
                ? ZapHosting.CONFIG.common.publicServerIp.trim()
                : "play.zap-hosting.com";

        String name = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.publicServerName != null && !ZapHosting.CONFIG.common.publicServerName.trim().isEmpty())
                ? ZapHosting.CONFIG.common.publicServerName.trim()
                : "ZAP-Hosting Official Server";

        this.serverData = new ServerData(name, ip, false);
    }

    public void joinServer() {
        boolean customScreen = ZapHosting.CONFIG == null || ZapHosting.CONFIG.common == null || ZapHosting.CONFIG.common.enableCustomJoinScreen;
        if (customScreen) {
            ZHLoadingScreen.openAndConnect(this.screen, this.serverData.name, this.serverData.ip);
        } else {
            ConnectScreen.startConnecting(
                    this.screen,
                    this.minecraft,
                    ServerAddress.parseString(this.serverData.ip),
                    this.serverData,
                    false
            );
        }
    }

    private void ensurePingStarted() {
        if (!this.pingStarted && this.serverData.ping < 0) {
            this.pingStarted = true;
            CompletableFuture.runAsync(() -> {
                try {
                    this.screen.getPinger().pingServer(
                            this.serverData,
                            () -> {}
                    );
                } catch (UnknownHostException e) {
                    this.serverData.ping = -1L;
                } catch (Exception e) {
                    this.serverData.ping = -1L;
                }
            }, Util.backgroundExecutor());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        this.topPos = top;
        this.heightPos = height;

        ensurePingStarted();

        int rowLeft = this.list.getRowLeft();
        int rowWidth = this.list.getRowWidth();
        boolean isSelected = this.list.getSelected() == this;

        // Native server select box around entry row
        if (isSelected) {
            guiGraphics.fill(rowLeft - 2, top - 2, rowLeft + rowWidth + 2, top + height + 2, 0xFFFFFFFF);
            guiGraphics.fill(rowLeft - 1, top - 1, rowLeft + rowWidth + 1, top + height + 1, 0xFF000000);
        } else if (hovering) {
            guiGraphics.fill(rowLeft - 2, top - 2, rowLeft + rowWidth + 2, top + height + 2, 0x80FFFFFF);
            guiGraphics.fill(rowLeft - 1, top - 1, rowLeft + rowWidth + 1, top + height + 1, 0xFF000000);
        }

        // Server row background
        guiGraphics.fill(rowLeft, top, rowLeft + rowWidth, top + height, isSelected ? 0x44263B26 : (hovering ? 0x2235393C : 0x181E2224));

        // 1. Left side 32x32 Server Icon with green border
        int iconX = left;
        int iconY = top + (height - 32) / 2;
        guiGraphics.fill(iconX - 1, iconY - 1, iconX + 33, iconY + 33, 0xFF2A3D2A);
        guiGraphics.renderOutline(iconX - 1, iconY - 1, 34, 34, 0xFF57BC54);

        RenderSystem.enableBlend();
        guiGraphics.blit(LOGO, iconX, iconY, 0, 0, 32, 32, 32, 32);
        RenderSystem.disableBlend();

        // Config / Live data
        String name = this.serverData.name != null ? this.serverData.name : "ZAP-Hosting Official Server";

        String motdFallback = (ZapHosting.CONFIG != null && ZapHosting.CONFIG.common != null && ZapHosting.CONFIG.common.publicServerMotd != null)
                ? ZapHosting.CONFIG.common.publicServerMotd
                : "Official Public Server hosted by ZAP-Hosting";

        Component motdComponent = (this.serverData.motd != null && !this.serverData.motd.getString().trim().isEmpty())
                ? this.serverData.motd
                : Component.literal(motdFallback);

        String ip = this.serverData.ip != null ? this.serverData.ip : "play.zap-hosting.com";

        int textX = iconX + 38;
        int rightEdge = rowLeft + rowWidth - 8;

        // --- LINE 1: Player count on top-right, Server Name + [OFFICIAL] on top-left ---
        String playerCountText = "";
        if (this.serverData.status != null) {
            playerCountText = this.serverData.status.getString();
        }

        int playerCountW = !playerCountText.isEmpty() ? this.minecraft.font.width(playerCountText) : 0;
        int playerCountX = rightEdge - playerCountW;
        if (!playerCountText.isEmpty()) {
            guiGraphics.drawString(this.minecraft.font, playerCountText, playerCountX, top + 3, 0xFFAAAAAA, false);
        }

        String badgeText = " [OFFICIAL]";
        int badgeW = this.minecraft.font.width(badgeText);
        int maxTitleW = (playerCountW > 0 ? playerCountX - 8 : rightEdge) - textX;

        String displayName = name;
        if (this.minecraft.font.width(displayName) + badgeW > maxTitleW) {
            int maxNameW = maxTitleW - badgeW;
            if (maxNameW > 30) {
                String dots = "...";
                int dotsW = this.minecraft.font.width(dots);
                displayName = this.minecraft.font.plainSubstrByWidth(name, maxNameW - dotsW) + dots;
            } else {
                badgeText = "";
                badgeW = 0;
                String dots = "...";
                int dotsW = this.minecraft.font.width(dots);
                displayName = this.minecraft.font.plainSubstrByWidth(name, maxTitleW - dotsW) + dots;
            }
        }

        guiGraphics.drawString(this.minecraft.font, displayName, textX, top + 3, 0xFFFFFFFF, false);
        if (!badgeText.isEmpty()) {
            int dispW = this.minecraft.font.width(displayName);
            guiGraphics.drawString(this.minecraft.font, badgeText, textX + dispW, top + 3, 0xFF57BC54, false);
        }

        // --- LINE 2: MOTD (live or configured) ---
        guiGraphics.drawString(this.minecraft.font, motdComponent, textX, top + 14, 0xFFAAAAAA, false);

        // --- LINE 3: IP Address on bottom-left, Status on bottom-right ---
        guiGraphics.drawString(this.minecraft.font, ip, textX, top + 24, 0xFF7CD47C, false);

        String statusText;
        int statusColor;
        if (this.serverData.ping < 0) {
            statusText = "\u25cf PINGING...";
            statusColor = 0xFFAAAAAA;
        } else {
            statusText = "\u25cf ONLINE";
            statusColor = 0xFF57BC54;
        }

        int statusW = this.minecraft.font.width(statusText);
        int statusX = rightEdge - statusW;
        int statusY = top + 24;
        guiGraphics.drawString(this.minecraft.font, statusText, statusX, statusY, statusColor, false);

        // Hover tooltip for latency or connection status
        int hoverTop = top;
        int hoverBottom = top + height;
        int hoverLeft = Math.min(statusX, playerCountX) - 4;
        if (hovering && mouseX >= hoverLeft && mouseX <= rightEdge + 4 && mouseY >= hoverTop && mouseY <= hoverBottom) {
            if (this.serverData.ping >= 0) {
                guiGraphics.renderTooltip(this.minecraft.font, Component.literal("Ping: " + this.serverData.ping + " ms"), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.minecraft.font, Component.literal("Pinging server..."), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.list.setSelected(this);
            joinServer();
            return true;
        }
        return false;
    }

    @Override
    public Component getNarration() {
        return Component.literal("Official Public Server. Press to join.");
    }

    @Override
    public void close() {
    }
}
