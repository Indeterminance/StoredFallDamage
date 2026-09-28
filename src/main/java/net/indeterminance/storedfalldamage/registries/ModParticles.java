package net.indeterminance.storedfalldamage.registries;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.effect.StoringShieldEffect;
import net.indeterminance.storedfalldamage.effect.UnstableHeartsEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static DeferredRegister<ParticleType<?>> PARTICLES;
    public static Supplier<SimpleParticleType> SHIELD_PARTICLE;
    public static Supplier<SimpleParticleType> CRACKED_HEART_PARTICLE;

    static {
        PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, StoredFallDamage.MOD_ID);
        SHIELD_PARTICLE = PARTICLES.register("storing_shield_particle", () -> new SimpleParticleType(false));
        CRACKED_HEART_PARTICLE = PARTICLES.register("cracked_heart_particle", () -> new SimpleParticleType(false));
    }

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }

}
