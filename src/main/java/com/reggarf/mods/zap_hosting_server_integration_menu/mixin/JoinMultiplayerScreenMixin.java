package com.reggarf.mods.zap_hosting_server_integration_menu.mixin;


import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void removeTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Allow normal rendering, only modify the title
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"), cancellable = true)
    private void preventTitleRendering(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Prevent only the title from rendering
        ci.cancel();
    }
}