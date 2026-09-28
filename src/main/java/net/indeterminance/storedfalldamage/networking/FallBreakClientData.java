package net.indeterminance.storedfalldamage.networking;

import net.minecraft.client.player.LocalPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record FallBreakClientData(float storedDamage, UUID playerUUID, String reason) {
    public static List<FallBreakClientData> playersStoredDamage = new ArrayList<>();

    public static float getPlayerStoredDamage(LocalPlayer player) {

        return getPlayerData(player).map(fallBreakClientData -> fallBreakClientData.storedDamage).orElse(0F);
    }

    public static Optional<FallBreakClientData> getPlayerData(LocalPlayer player) {
        return playersStoredDamage.stream().filter(data -> data.compareUUID(player.getStringUUID())).findFirst();
    }

    public boolean compareUUID(String uuidStr) {
        return UUID.fromString(uuidStr).compareTo(playerUUID) == 0;
    }
}