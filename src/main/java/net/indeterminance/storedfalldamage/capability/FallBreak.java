package net.indeterminance.storedfalldamage.capability;

import net.minecraft.nbt.CompoundTag;

public class FallBreak {
    private float storedFallDamage = 0f;
    private static final float MIN_DAMAGE = 0f;

    public float getStoredFallDamage() {
        return storedFallDamage;
    }

    public void setStoredFallDamage(float amount) {
        storedFallDamage = amount;
    }

    public float healStoredFallDamage(float amount) {
        float healedAmount = storedFallDamage - amount;
        storedFallDamage = Math.max(healedAmount, MIN_DAMAGE);
        return Math.max(-healedAmount, 0);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putFloat("storedFallDamage", storedFallDamage);
    }

    public void loadNBTData (CompoundTag nbt) {
        storedFallDamage = nbt.getFloat("storedFallDamage");
    }
}
