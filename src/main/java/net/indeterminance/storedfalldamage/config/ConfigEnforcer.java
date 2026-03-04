package net.indeterminance.storedfalldamage.config;

import net.indeterminance.storedfalldamage.registries.ModResources;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class ConfigEnforcer {
    public static boolean IsHoldingClutchItem(Player player, DamageSource source) {
        TagKey<Item> thisDamageClutches = ModResources.LoadClutchItemData(source);
        return player.getMainHandItem().is(thisDamageClutches) || player.getOffhandItem().is(thisDamageClutches);
    }

    public static boolean ShouldSavePlayer(Player player, DamageSource source) {
        StoredDamageConfig.ENABLE_FALLBREAK_VALUES shouldBreakSeverity = StoredDamageConfig.ENABLE_FALLBREAK.get();
        if (shouldBreakSeverity == StoredDamageConfig.ENABLE_FALLBREAK_VALUES.ALWAYS) {
            return true;
        }
        else if (shouldBreakSeverity == StoredDamageConfig.ENABLE_FALLBREAK_VALUES.HOLDING_CLUTCH_ITEM) {
            return IsHoldingClutchItem(player, source);
        }
        else return false;
    }

    public static boolean DoesDamageExceedStoreable(float value) {
        float limit = StoredDamageConfig.STORED_LIMIT.get().floatValue();
        return value > limit;
    }

    public static boolean IsStoreShieldedFromType(String breakReason, DamageSource source) {
        TagKey<DamageType> tag = ModResources.LoadShieldedDamageTypes(breakReason);
        return source.is(tag);

    }
}
