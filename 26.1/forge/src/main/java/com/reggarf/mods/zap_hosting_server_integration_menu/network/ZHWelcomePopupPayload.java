package com.reggarf.mods.zap_hosting_server_integration_menu.network;

import net.minecraft.network.FriendlyByteBuf;

public record ZHWelcomePopupPayload(int delayTicks) {

    public ZHWelcomePopupPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(delayTicks);
    }
}
