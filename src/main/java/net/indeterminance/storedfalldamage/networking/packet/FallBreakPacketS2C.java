package net.indeterminance.storedfalldamage.networking.packet;

import net.indeterminance.storedfalldamage.client.FallBreakClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FallBreakPacketS2C {
    private final float storedFallDamage;

    public FallBreakPacketS2C(float damage) {
        this.storedFallDamage = damage;
    }

    public FallBreakPacketS2C(FriendlyByteBuf buf) {
        this.storedFallDamage = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(storedFallDamage);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            FallBreakClientData.clientStoredFallDamage = storedFallDamage;
        });
        return true;
    }
}
