package com.reggarf.mods.zap_hosting_server_integration_menu.mixin;

import com.reggarf.mods.zap_hosting_server_integration_menu.client.gui.util.ZHServerListHelper;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin {

    @Shadow private JoinMultiplayerScreen screen;

    @Inject(method = "refreshEntries", at = @At("RETURN"))
    private void onRefreshEntries(CallbackInfo ci) {
        ZHServerListHelper.ensureCustomEntries(this.screen, (ServerSelectionList) (Object) this);
    }
}
