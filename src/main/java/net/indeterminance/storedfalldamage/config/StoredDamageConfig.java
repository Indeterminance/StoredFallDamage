package net.indeterminance.storedfalldamage.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class StoredDamageConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG;

    public static final ForgeConfigSpec.ConfigValue<ENABLE_FALLBREAK_VALUES> ENABLE_FALLBREAK;
    public static final ForgeConfigSpec.ConfigValue<Double> STORED_SCALING;
    public static final ForgeConfigSpec.ConfigValue<Double> STORED_LIMIT;

    public static final ForgeConfigSpec.ConfigValue<Boolean> STORING_SHIELD_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHIELD_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Double> SHIELD_SCALING;

    public static final ForgeConfigSpec.ConfigValue<Boolean> UNSTABLE_HEARTS_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Integer> UNSTABLE_HEARTS_BASE;
    public static final ForgeConfigSpec.ConfigValue<Integer> UNSTABLE_HEARTS_MULT;

    public enum ENABLE_FALLBREAK_VALUES {
        NEVER,
        HOLDING_CLUTCH_ITEM,
        ALWAYS
    }

    static {
        BUILDER.push("Stored Fall Damage configs");

        BUILDER.push("Storing Damage");

        ENABLE_FALLBREAK = BUILDER.comment("When should damage be stored?").defineEnum("activation_requirement", ENABLE_FALLBREAK_VALUES.HOLDING_CLUTCH_ITEM);
        STORED_SCALING = BUILDER.comment("When damage is stored, multiply by the following amount (must be positive)").defineInRange("scaling", 1d, 0.1d, 1024d );
        STORED_LIMIT = BUILDER.comment("The amount of damage that can be stored prior to scaling without the player dying outright (in half-hearts)").defineInRange("limit", 120d, 1d, 1024d);

        BUILDER.pop();
        BUILDER.push("Storing Shield");
        STORING_SHIELD_ENABLED = BUILDER.comment("When storing damage, should the player receive a \"Storing Shield\" effect that prevents damage from certain sources (such as fire) from finishing the player off for a short period of time?").define("use_storing_shield", true);
        SHIELD_DURATION = BUILDER.comment("How long should \"Storing Shield\" last for in ticks?").defineInRange("storing_shield_duration", 100, 1, Integer.MAX_VALUE);
        SHIELD_SCALING = BUILDER.comment("When nullifying damage with \"Storing Shield\", also store this damage with the following multiplier:").defineInRange("storing_shield_multiplier", 0, 0, 1024d);
        BUILDER.pop();
        BUILDER.push("Unstable Hearts");
        UNSTABLE_HEARTS_ENABLED = BUILDER.comment("When storing damage, should the player gain a \"Unstable Hearts\" effect that kills the player later if they haven't recovered their hearts?").define("use_unstable_hearts", false);
        UNSTABLE_HEARTS_BASE = BUILDER.comment("How long (in ticks) should \"Unstable Hearts\" last for?").defineInRange("unstable_hearts_base", 600, 0, Integer.MAX_VALUE);
        UNSTABLE_HEARTS_MULT = BUILDER.comment("How many additional ticks of duration should \"Unstable Hearts\" start with for each half-heart of stored damage?").defineInRange("unstable_hearts_mult", 60, 0, Integer.MAX_VALUE);

        BUILDER.pop(2);
        CONFIG = BUILDER.build();
    }
    public static int UnstableHeartsDuration(int damage) {
        int base = UNSTABLE_HEARTS_BASE.get();
        int mult = UNSTABLE_HEARTS_MULT.get() * damage;
        return base + mult;
    }
}
