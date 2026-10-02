package com.reggarf.mods.zap_hosting_server_integration_menu.mixin;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {

    @Shadow private Component status;
    @Shadow @Final Screen parent;

    private static ServerData lastConnectingServerData = null;

    protected ConnectScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;Z)V", at = @At("HEAD"))
    private static void onStartConnecting(
            Screen parent, Minecraft minecraft,
            ServerAddress serverAddress, ServerData serverData,
            boolean isQuickPlay,
            CallbackInfo ci
    ) {
        lastConnectingServerData = serverData;
    }

    private boolean isConnectingToPublicServer() {
        if (ZapHosting.CONFIG == null || ZapHosting.CONFIG.common == null) return false;
        if (!ZapHosting.CONFIG.common.enablePublicServer) return false;
        if (!ZapHosting.CONFIG.common.enableCustomJoinScreen) return false;

        String targetIp = ZapHosting.CONFIG.common.publicServerIp;
        if (targetIp == null || targetIp.trim().isEmpty()) return false;

        if (lastConnectingServerData != null && lastConnectingServerData.ip != null) {
            String cleanTarget = targetIp.trim().toLowerCase();
            String cleanCurrent = lastConnectingServerData.ip.trim().toLowerCase();
            return cleanCurrent.equals(cleanTarget) || cleanCurrent.startsWith(cleanTarget + ":");
        }
        return false;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (isConnectingToPublicServer()) {
            int cancelY = this.height - 34;
            int btnW = 85;

            for (GuiEventListener listener : this.children()) {
                if (listener instanceof Button button) {
                    button.setWidth(btnW);
                    button.setX(this.width / 2 - btnW / 2);
                    button.setY(cancelY);
                }
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (isConnectingToPublicServer()) {
            guiGraphics.fillGradient(0, 0, this.width, this.height, 0xEE121415, 0xF90A0B0C);

            String serverName = (ZapHosting.CONFIG.common.publicServerName != null && !ZapHosting.CONFIG.common.publicServerName.trim().isEmpty())
                    ? ZapHosting.CONFIG.common.publicServerName.trim()
                    : "ZAP-Hosting Official Server";

            String serverIp = (ZapHosting.CONFIG.common.publicServerIp != null && !ZapHosting.CONFIG.common.publicServerIp.trim().isEmpty())
                    ? ZapHosting.CONFIG.common.publicServerIp.trim()
                    : "play.zap-hosting.com";

            ZHLoadingScreen.renderJoinServerCard(guiGraphics, this.width, this.height, this.font, this.status, serverName, serverIp);

            for (Renderable renderable : ((ScreenAccessor) this).getRenderables()) {
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            ci.cancel();
        }
    }
}
