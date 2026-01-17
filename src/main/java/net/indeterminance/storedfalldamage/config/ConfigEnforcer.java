package net.indeterminance.storedfalldamage.config;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class ConfigEnforcer {
    public static final TagKey<Item> clutch_items = ItemTags.create(ResourceLocation.fromNamespaceAndPath("storedfalldamage","clutch_items"));

    public static boolean IsHoldingClutchItem(Player player) {
        return player.getMainHandItem().is(clutch_items) || player.getOffhandItem().is(clutch_items);
    }

    public static boolean ShouldBreakFall(Player player) {
        StoredDamageConfig.ENABLE_FALLBREAK_VALUES shouldBreakSeverity = StoredDamageConfig.ENABLE_FALLBREAK.get();
        if (shouldBreakSeverity == StoredDamageConfig.ENABLE_FALLBREAK_VALUES.ALWAYS) {
            return true;
        }
        else if (shouldBreakSeverity == StoredDamageConfig.ENABLE_FALLBREAK_VALUES.HOLDING_CLUTCH_ITEM) {
            return IsHoldingClutchItem(player);
        }
        else return false;
    }

    public static boolean DoesFallExceedStoreable(float value) {
        float limit = StoredDamageConfig.STORED_LIMIT.get().floatValue();
        return value > limit;
    }
}
