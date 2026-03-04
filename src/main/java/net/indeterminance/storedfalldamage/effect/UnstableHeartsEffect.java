package net.indeterminance.storedfalldamage.effect;

import net.indeterminance.storedfalldamage.registries.ModResources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class UnstableHeartsEffect extends MobEffect {
    public UnstableHeartsEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration == 1;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.hurt(ModResources.source(pLivingEntity.level().registryAccess(), ModResources.UNSTABLE_HEARTS_RESKEY), Integer.MAX_VALUE);
    }
}
