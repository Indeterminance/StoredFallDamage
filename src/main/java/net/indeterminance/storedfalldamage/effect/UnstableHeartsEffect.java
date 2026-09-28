package net.indeterminance.storedfalldamage.effect;

import net.indeterminance.storedfalldamage.registries.ModResources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.extensions.IMobEffectExtension;

import java.util.List;
import java.util.Set;

public class UnstableHeartsEffect extends MobEffect implements IMobEffectExtension {
    public UnstableHeartsEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // Nothing, Storing Shield doesn't need a "cure".
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }


    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(ModResources.source(entity.level().registryAccess(), ModResources.UNSTABLE_HEARTS_RESKEY), Float.MAX_VALUE);
        return true;
    }
}
