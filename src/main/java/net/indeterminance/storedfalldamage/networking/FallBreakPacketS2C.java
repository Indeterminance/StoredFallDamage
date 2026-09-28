package net.indeterminance.storedfalldamage.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static net.indeterminance.storedfalldamage.registries.ModResources.loc;

public record FallBreakPacketS2C(float storedFallDamage, String uuidStr, String reason) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FallBreakPacketS2C> TYPE = new CustomPacketPayload.Type<>(loc("client_stored_damage"));

    public static final StreamCodec<ByteBuf, FallBreakPacketS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            FallBreakPacketS2C::storedFallDamage,
            ByteBufCodecs.STRING_UTF8,
            FallBreakPacketS2C::uuidStr,
            ByteBufCodecs.STRING_UTF8,
            FallBreakPacketS2C::reason,
            FallBreakPacketS2C::new
    );

    public static void handle(final FallBreakPacketS2C packet, final IPayloadContext context) {
        FallBreakClientData.playersStoredDamage.removeIf(data -> data.compareUUID(packet.uuidStr));
        FallBreakClientData.playersStoredDamage.add(new FallBreakClientData(packet.storedFallDamage, UUID.fromString(packet.uuidStr), packet.reason));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}