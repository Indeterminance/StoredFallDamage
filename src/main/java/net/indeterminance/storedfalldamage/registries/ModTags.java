package net.indeterminance.storedfalldamage.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class ModTags {
    public static final TagKey<DamageType> STORED_DAMAGE_TYPES = TagKey.create(Registries.DAMAGE_TYPE, ModResources.loc("stored_damage_types"));
}
