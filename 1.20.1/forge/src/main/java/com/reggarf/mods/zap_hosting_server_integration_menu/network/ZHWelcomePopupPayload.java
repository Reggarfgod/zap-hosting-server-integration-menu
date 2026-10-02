package com.reggarf.mods.zap_hosting_server_integration_menu.network;

import net.minecraft.network.FriendlyByteBuf;

public class ZHWelcomePopupPayload {

    private final int delayTicks;

    public ZHWelcomePopupPayload(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public ZHWelcomePopupPayload(FriendlyByteBuf buf) {
        this.delayTicks = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.delayTicks);
    }

    public int delayTicks() {
        return delayTicks;
    }
}
