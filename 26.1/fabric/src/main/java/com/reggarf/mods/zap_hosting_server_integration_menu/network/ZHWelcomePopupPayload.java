package com.reggarf.mods.zap_hosting_server_integration_menu.network;

import com.reggarf.mods.zap_hosting_server_integration_menu.ZapHosting;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ZHWelcomePopupPayload(int delayTicks) implements CustomPacketPayload {

    public static final Type<ZHWelcomePopupPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ZapHosting.MOD_ID, "welcome_popup"));

    public static final StreamCodec<ByteBuf, ZHWelcomePopupPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ZHWelcomePopupPayload::delayTicks,
            ZHWelcomePopupPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
