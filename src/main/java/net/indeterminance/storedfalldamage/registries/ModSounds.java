package net.indeterminance.storedfalldamage.registries;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.indeterminance.storedfalldamage.registries.ModResources.loc;

public class ModSounds {
    public static DeferredRegister<SoundEvent> SOUNDS;
    public static DeferredHolder<SoundEvent, SoundEvent> HEART_CRACK;
    public static DeferredHolder<SoundEvent, SoundEvent> SHIELD_HIT;

    static {
        SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, StoredFallDamage.MOD_ID);
        HEART_CRACK = SOUNDS.register("heart_crack", () -> SoundEvent.createVariableRangeEvent(loc("heart_crack")));
        SHIELD_HIT = SOUNDS.register("shield_hit", () -> SoundEvent.createVariableRangeEvent(loc("shield_hit")));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
