package net.indeterminance.storedfalldamage.registries;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.effect.StoringShieldEffect;
import net.indeterminance.storedfalldamage.effect.UnstableHeartsEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static DeferredRegister<MobEffect> EFFECTS;
    public static Holder<MobEffect> STORING_SHIELD;
    public static Holder<MobEffect> UNSTABLE_HEARTS;

    static {
        EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, StoredFallDamage.MOD_ID);
        STORING_SHIELD = EFFECTS.register("storing_shield", () -> new StoringShieldEffect(MobEffectCategory.BENEFICIAL, 11721470));
        UNSTABLE_HEARTS = EFFECTS.register("unstable_hearts", () -> new UnstableHeartsEffect(MobEffectCategory.HARMFUL, 13663350));
    }

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
