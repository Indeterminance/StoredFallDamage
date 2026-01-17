package net.indeterminance.storedfalldamage.networking;

import net.indeterminance.storedfalldamage.networking.packet.FallBreakPacketS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }


    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath("storedfalldamage", "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE.messageBuilder(FallBreakPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FallBreakPacketS2C::new)
                .encoder(FallBreakPacketS2C::toBytes)
                .consumerMainThread(FallBreakPacketS2C::handle).add();;
    }

    public static <V> void sendToServer(V msg) {
        INSTANCE.sendToServer(msg);
    }

    public static <V> void sendToClient(V msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
