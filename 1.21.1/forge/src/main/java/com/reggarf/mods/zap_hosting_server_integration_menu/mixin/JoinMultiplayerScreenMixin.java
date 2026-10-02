package com.reggarf.mods.zap_hosting_server_integration_menu.mixin;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.screen.ZHLoadingScreen;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry.ZHPublicServerListEntry;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.entry.ZHServerListEntry;
import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.util.ZHServerListHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {

    @Shadow private Screen lastScreen;
    @Shadow private ServerSelectionList serverSelectionList;
    @Shadow private Button selectButton;
    @Shadow private Button editButton;
    @Shadow private Button deleteButton;

    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (this.serverSelectionList != null) {
            ZHServerListHelper.ensureCustomEntries((JoinMultiplayerScreen) (Object) this, this.serverSelectionList);
        }
    }

    @Inject(method = "joinSelectedServer", at = @At("HEAD"), cancellable = true)
    private void onJoinSelectedServer(CallbackInfo ci) {
        if (this.serverSelectionList != null) {
            ServerSelectionList.Entry selected = this.serverSelectionList.getSelected();
            if (selected instanceof ZHServerListEntry) {
                Minecraft.getInstance().setScreen(new ZHLoadingScreen());
                ci.cancel();
            } else if (selected instanceof ZHPublicServerListEntry publicEntry) {
                publicEntry.joinServer();
                ci.cancel();
            }
        }
    }

    @Inject(method = "onSelectedChange", at = @At("RETURN"))
    private void onSelectedChange(CallbackInfo ci) {
        if (this.serverSelectionList != null) {
            ServerSelectionList.Entry selected = this.serverSelectionList.getSelected();
            if (selected instanceof ZHServerListEntry) {
                if (this.selectButton != null) this.selectButton.active = false;
                if (this.editButton != null) this.editButton.active = false;
                if (this.deleteButton != null) this.deleteButton.active = false;
            } else if (selected instanceof ZHPublicServerListEntry) {
                if (this.selectButton != null) this.selectButton.active = true;
                if (this.editButton != null) this.editButton.active = false;
                if (this.deleteButton != null) this.deleteButton.active = false;
            }
        }
    }
}
