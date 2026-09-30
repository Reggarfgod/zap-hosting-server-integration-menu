package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHLoadingScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.wizard.ZHStep1LauncherScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ZHServerListEntry extends ServerSelectionList.Entry {

    private static final ResourceLocation BANNER_IMAGE =
            ResourceLocation.fromNamespaceAndPath(ZapHosting.MOD_ID, "textures/gui/wo_bg_overlay_2.png");

    private static final int BANNER_WIDTH = 256;
    private static final int BANNER_HEIGHT = 33;

    private final JoinMultiplayerScreen screen;
    private final ServerSelectionList list;
    private final Minecraft minecraft;

    private int topPos;
    private int heightPos;
    private long lastClickTime = 0L;

    public ZHServerListEntry(JoinMultiplayerScreen screen, ServerSelectionList list) {
        this.screen = screen;
        this.list = list;
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        this.topPos = top;
        this.heightPos = height;

        int rowLeft = this.list.getRowLeft();
        int rowWidth = this.list.getRowWidth();
        boolean isSelected = this.list.getSelected() == this;

        // Native server select box around the entry row
        if (isSelected) {
            guiGraphics.fill(rowLeft - 2, top - 2, rowLeft + rowWidth + 2, top + height + 2, 0xFFFFFFFF);
            guiGraphics.fill(rowLeft - 1, top - 1, rowLeft + rowWidth + 1, top + height + 1, 0xFF000000);
        } else if (hovering) {
            guiGraphics.fill(rowLeft - 2, top - 2, rowLeft + rowWidth + 2, top + height + 2, 0x80FFFFFF);
            guiGraphics.fill(rowLeft - 1, top - 1, rowLeft + rowWidth + 1, top + height + 1, 0xFF000000);
        }

        int bannerX = left + (width - BANNER_WIDTH) / 2;
        int bannerY = top + (height - BANNER_HEIGHT) / 2;

        // Render clean banner graphic
        RenderSystem.enableBlend();
        guiGraphics.blit(BANNER_IMAGE, bannerX, bannerY, 0, 0, BANNER_WIDTH, BANNER_HEIGHT, BANNER_WIDTH, BANNER_HEIGHT);
        RenderSystem.disableBlend();

        // Banner text
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(bannerX + 53, bannerY + 1, 0);
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);

        // Line 1: Need a Server?
        guiGraphics.drawString(this.minecraft.font, "Need a Server?", 8, 6, 0xFFFFFFFF, false);

        // Line 2: Click me to get your own server!
        guiGraphics.drawString(this.minecraft.font, "Click me to get your own server!", 8, 18, 0xFF5CB85C, false);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int bannerX = this.list.getRowLeft() + (this.list.getRowWidth() - BANNER_WIDTH) / 2;
            int bannerY = this.topPos + (this.heightPos - BANNER_HEIGHT) / 2;
            boolean clickedBanner = mouseX >= bannerX && mouseX <= bannerX + BANNER_WIDTH
                    && mouseY >= bannerY && mouseY <= bannerY + BANNER_HEIGHT;

            boolean wasSelected = this.list.getSelected() == this;
            this.list.setSelected(this);

            long now = Util.getMillis();
            if (clickedBanner || wasSelected || (now - this.lastClickTime < 300L)) {
                this.minecraft.setScreen(new ZHLoadingScreen());
                return true;
            }
            this.lastClickTime = now;
            return true;
        }
        return false;
    }

    @Override
    public Component getNarration() {
        return Component.literal("Need a Server? Click me to get your own server!");
    }

    @Override
    public void close() {
    }
}
