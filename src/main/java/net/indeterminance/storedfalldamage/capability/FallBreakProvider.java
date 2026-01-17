package net.indeterminance.storedfalldamage.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FallBreakProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<FallBreak> fallBreakCapability = CapabilityManager.get(new CapabilityToken<>() {});
    private FallBreak breaker = null;
    private final LazyOptional<FallBreak> optional = LazyOptional.of(this::createFallBreaker);

    private FallBreak createFallBreaker() {
        if (this.breaker == null) {
            breaker = new FallBreak();
        }
        return this.breaker;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == fallBreakCapability) return optional.cast();
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createFallBreaker().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createFallBreaker().loadNBTData(nbt);

    }
}
