package com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class ZHServerListEntry extends ServerSelectionList.OnlineServerEntry {

    private static final Identifier BANNER_IMAGE =
            Identifier.fromNamespaceAndPath(ZapHosting.MOD_ID, "textures/gui/wo_bg_overlay_2.png");

    private static final int BANNER_WIDTH = 256;
    private static final int BANNER_HEIGHT = 33;

    private final JoinMultiplayerScreen screen;
    private final ServerSelectionList list;
    private final Minecraft minecraft;

    private int topPos;
    private int heightPos;
    private long lastClickTime = 0L;

    public ZHServerListEntry(JoinMultiplayerScreen screen, ServerSelectionList list) {
        list.super(screen, new ServerData("ZAP-Hosting Banner", "zap_banner", ServerData.Type.OTHER));
        this.screen = screen;
        this.list = list;
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void join() {
        this.minecraft.setScreen(new ZHLoadingScreen());
    }

    @Override
    public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int top = this.getY();
        int height = this.getHeight();
        this.topPos = top;
        this.heightPos = height;

        int rowLeft = this.list.getRowLeft();
        int rowWidth = this.list.getRowWidth();
        boolean isSelected = this.list.getSelected() == this;

        // Native server select box around the entry row
        if (isSelected) {
            guiGraphics.fill(rowLeft - 2, top - 2, rowLeft + rowWidth + 2, top + height + 2, 0xFFFFFFFF);
            guiGraphics.fill(rowLeft - 1, top - 1, rowLeft + rowWidth + 1, top + height + 1, 0xFF000000);
        }

        int bannerX = rowLeft + (rowWidth - BANNER_WIDTH) / 2;
        int bannerY = top + (height - BANNER_HEIGHT) / 2;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BANNER_IMAGE,
                bannerX, bannerY,
                0.0f, 0.0f,
                BANNER_WIDTH, BANNER_HEIGHT,
                BANNER_WIDTH, BANNER_HEIGHT
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int rowLeft = this.list.getRowLeft();
        int rowWidth = this.list.getRowWidth();
        int top = this.topPos;
        int height = this.heightPos;

        if (mouseX >= rowLeft && mouseX <= rowLeft + rowWidth && mouseY >= top && mouseY <= top + height) {
            this.list.setSelected(this);

            long currentTime = Util.getMillis();
            if (currentTime - this.lastClickTime < 250L) {
                join();
            }
            this.lastClickTime = currentTime;
            return true;
        }
        return false;
    }

    @Override
    public Component getNarration() {
        return Component.translatable("narrator.select", "ZAP-Hosting Server Integration Banner");
    }
}
