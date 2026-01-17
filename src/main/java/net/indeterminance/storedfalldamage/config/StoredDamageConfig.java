package net.indeterminance.storedfalldamage.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class StoredDamageConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    public static final ForgeConfigSpec.ConfigValue<ENABLE_FALLBREAK_VALUES> ENABLE_FALLBREAK;
    public static final ForgeConfigSpec.ConfigValue<Double> STORED_SCALING;
    public static final ForgeConfigSpec.ConfigValue<Double> STORED_LIMIT;

    public enum ENABLE_FALLBREAK_VALUES {
        NEVER,
        HOLDING_CLUTCH_ITEM,
        ALWAYS
    }

    static {
        BUILDER.push("Stored Fall Damage configs");

        ENABLE_FALLBREAK = BUILDER.comment("When should a lethal fall be broken?").defineEnum("activation_requirement", ENABLE_FALLBREAK_VALUES.HOLDING_CLUTCH_ITEM);

        STORED_SCALING = BUILDER.comment("When fall damage is stored, multiply by the following amount (must be positive)").defineInRange("scaling", 1d, 0.1d, 1024d );
        CONFIG = BUILDER.build();

        STORED_LIMIT = BUILDER.comment("The amount of fall damage that can be stored prior to scaling without the player dying outright (in half-hearts)").defineInRange("limit", 120d, 1d, 1024d);

        BUILDER.pop();
        CONFIG = BUILDER.build();
    }
}
