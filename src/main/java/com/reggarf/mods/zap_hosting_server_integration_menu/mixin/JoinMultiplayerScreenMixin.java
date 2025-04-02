package com.reggarf.mods.zap_hosting_server_integration_menu.mixin;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {

    @Shadow private Screen lastScreen;

    @Unique
    private static final Component CUSTOM_TITLE = Component.translatable("custom.multiplayer.title");

    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modifyTitle(Screen p_99688_, CallbackInfo ci) {
        try {
            Field titleField = Screen.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(this, CUSTOM_TITLE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
