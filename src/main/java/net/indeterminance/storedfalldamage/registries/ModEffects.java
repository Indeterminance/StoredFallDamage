package net.indeterminance.storedfalldamage.registries;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.effect.StoringShieldEffect;
import net.indeterminance.storedfalldamage.effect.UnstableHeartsEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static DeferredRegister<MobEffect> EFFECTS;
    public static RegistryObject<StoringShieldEffect> STORING_SHIELD;
    public static RegistryObject<UnstableHeartsEffect> UNSTABLE_HEARTS;

    static {
        EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, StoredFallDamage.MOD_ID);
        STORING_SHIELD = EFFECTS.register("storing_shield", () -> new StoringShieldEffect(MobEffectCategory.BENEFICIAL, 11721470));
        UNSTABLE_HEARTS = EFFECTS.register("unstable_hearts", () -> new UnstableHeartsEffect(MobEffectCategory.HARMFUL, 13663350));
    }

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
