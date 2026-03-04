package net.indeterminance.storedfalldamage.registries;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ModResources {
    public static final ResourceLocation CRACKED_HEARTS_LOC = loc("textures/gui/cracked_hearts.png");
    public static final ResourceLocation COMPAT_HEARTS_LOC = loc( "textures/gui/cracked_hearts_compat.png");
    public static final ResourceLocation UNSTABLE_HEARTS_RESLOC = loc("unstable_hearts_timeout");
    public static final ResourceLocation SHIELD_EFFECT_LOC = loc("textures/gui/storing_shield.png");
    public static ResourceKey<DamageType> UNSTABLE_HEARTS_RESKEY = ResourceKey.create(Registries.DAMAGE_TYPE, UNSTABLE_HEARTS_RESLOC);

    public static Map<DamageType, TagKey<Item>> STORING_ITEM_TAGS = new HashMap<>();
    public static Map<String, TagKey<DamageType>> STORING_SHIELD_NULLS = new HashMap<>();

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(StoredFallDamage.MOD_ID, path);
    }

    public static DamageSource source(RegistryAccess access, ResourceKey<DamageType> key) {
        return new DamageSource(access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }

    public static TagKey<Item> LoadClutchItemData(DamageSource source) {
        if (!STORING_ITEM_TAGS.containsKey(source.type())) {
            StoredFallDamage.LOGGER.debug("Getting clutch item for damage type : {}", source.type().msgId());
            source.typeHolder().unwrap().left().ifPresent(key -> {
                String namespace = key.location().getNamespace();
                String path = key.location().getPath();
                String key_path = String.format("storing_items/%s/%s", namespace, path);
                StoredFallDamage.LOGGER.debug("Found key path : {}", key_path);
                TagKey<Item> tag = TagKey.create(Registries.ITEM, loc(key_path));
                StoredFallDamage.LOGGER.debug("Found tag : {}", tag);
                STORING_ITEM_TAGS.put(source.type(), tag);
            });
        }
        return STORING_ITEM_TAGS.get(source.type());
    }

    public static TagKey<DamageType> LoadShieldedDamageTypes(String source) {
        ResourceLocation damageLoc = ResourceLocation.parse(source);

        if (!STORING_SHIELD_NULLS.containsKey(source)) {
            String key_path = String.format("storing_shield_nulls/%s/%s", damageLoc.getNamespace(), damageLoc.getPath());
            TagKey<DamageType> tag = TagKey.create(Registries.DAMAGE_TYPE, loc(key_path));
            STORING_SHIELD_NULLS.put(source, tag);
        }
        return STORING_SHIELD_NULLS.get(source);
    }
}
