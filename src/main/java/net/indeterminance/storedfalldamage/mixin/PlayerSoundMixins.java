package net.indeterminance.storedfalldamage.mixin;

import net.indeterminance.storedfalldamage.config.ConfigEnforcer;
import net.indeterminance.storedfalldamage.networking.FallBreakClientData;
import net.indeterminance.storedfalldamage.registries.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
abstract class PlayerSoundMixins extends LivingEntity implements IAttachmentHolder, ILivingEntityExtension {
    protected PlayerSoundMixins(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "getHurtSound",
            at = @At("HEAD"),
            cancellable = true
    )
    public void modifySoundEvent(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> cir) {
        Entity entity = self();
        if (!(entity instanceof Player player)) return;
        boolean isStoring;
        String storingReason;
        if (player.level().isClientSide) {
            Optional<FallBreakClientData> data = FallBreakClientData.getPlayerData((LocalPlayer) player);
            if (data.isEmpty()) {
                return;
            }
            isStoring = data.get().storedDamage() > 0;
            storingReason = data.get().reason();
        }
        else {
            isStoring = player.getData(ModDataAttachments.STORED_DAMAGE) > 0;
            storingReason = player.getData(ModDataAttachments.STORED_DAMAGE_SOURCE);
        }
        boolean shieldedFromType = player.hasEffect(ModEffects.STORING_SHIELD) && ConfigEnforcer.IsStoreShieldedFromType(storingReason, damageSource);

        if (shieldedFromType) {
            cir.setReturnValue(ModSounds.SHIELD_HIT.get());
            cir.cancel();
        }
        else if (isStoring) {
            cir.setReturnValue(ModSounds.HEART_CRACK.get());
            cir.cancel();
        }
    }
}
