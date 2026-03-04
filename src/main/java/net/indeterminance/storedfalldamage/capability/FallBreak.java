package net.indeterminance.storedfalldamage.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FallBreak {
    private float storedFallDamage = 0f;
    private static final float MIN_DAMAGE = 0f;
    private static String reason = "";

    public float getStoredFallDamage() {
        return storedFallDamage;
    }

    public void beginStoreDamage(float amount) {
        storedFallDamage += amount;
    }

    public void storeReason(DamageSource source) {
        Optional<ResourceKey<DamageType>> damageOptional =  source.typeHolder().unwrap().left();
        AtomicReference<String> result = new AtomicReference<String>();
        damageOptional.ifPresent(key -> result.set(key.location().toString()));
        reason = result.get();
    }

    public void clearReason() {
        reason = "";
    }

    public void storeDamage(float amount) {
        storedFallDamage += amount;
    }

    public float healStoredFallDamage(float amount) {
        float healedAmount = storedFallDamage - amount;
        storedFallDamage = Math.max(healedAmount, MIN_DAMAGE);
        return Math.max(-healedAmount, 0);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putFloat("storedFallDamage", storedFallDamage);
        nbt.putString("reason", reason);
    }

    public void loadNBTData (CompoundTag nbt) {
        storedFallDamage = nbt.getFloat("storedFallDamage");
        reason = nbt.getString("reason");
    }

    public String getReason() {
        return reason;
    }
}
