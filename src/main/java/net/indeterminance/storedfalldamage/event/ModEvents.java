package net.indeterminance.storedfalldamage.event;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.capability.FallBreak;
import net.indeterminance.storedfalldamage.capability.FallBreakProvider;
import net.indeterminance.storedfalldamage.config.ConfigEnforcer;
import net.indeterminance.storedfalldamage.config.StoredDamageConfig;
import net.indeterminance.storedfalldamage.registries.ModEffects;
import net.indeterminance.storedfalldamage.networking.PacketHandler;
import net.indeterminance.storedfalldamage.networking.packet.FallBreakPacketS2C;
import net.indeterminance.storedfalldamage.registries.ModTags;
import net.indeterminance.storedfalldamage.render.HeartRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = StoredFallDamage.MOD_ID)
public class ModEvents {
    /*
        We're going to check if the player takes a lethal amount of fall damage, and if so
        save them at half a heart (consuming all absorption hearts) and store all the damage that
        would've killed them.

        The client also needs to know this happened so it can render the player's hearts correctly.

        This method returns early if it's either not a player, not fall damage, or the fall doesn't
        match the requirements set in configuration.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        DamageSource source = event.getSource();
        LazyOptional<FallBreak> cap = player.getCapability(FallBreakProvider.fallBreakCapability);
        cap.ifPresent(fallBreak -> {
            boolean storedDamageType = source.is(ModTags.STORED_DAMAGE_TYPES) && ConfigEnforcer.ShouldSavePlayer(player, source);
            boolean isStoringShield = player.hasEffect(ModEffects.STORING_SHIELD.get()) && ConfigEnforcer.IsStoreShieldedFromType(fallBreak.getReason(), source);
            if (!storedDamageType && !isStoringShield) return;

            float damage = event.getAmount();
            float currentHealth = event.getEntity().getHealth();
            float currentAbsorption = event.getEntity().getAbsorptionAmount();
            float toStoreBase = (damage - currentHealth - currentAbsorption + 1);
            float storeScaling = StoredDamageConfig.STORED_SCALING.get().floatValue();
            float shieldScaling = StoredDamageConfig.SHIELD_SCALING.get().floatValue();
            float scaling = isStoringShield & !storedDamageType ? shieldScaling : storeScaling;
            if (ConfigEnforcer.DoesDamageExceedStoreable(toStoreBase * scaling)) return;
            if (currentHealth + currentAbsorption - 1 >= damage) return;

            if (fallBreak.getStoredFallDamage() > 0 && !isStoringShield) {
                // Player took non-shielded damage whilst having damage stored (instant death)
                event.setAmount(Float.MAX_VALUE);
                PacketHandler.sendToClient(new FallBreakPacketS2C(fallBreak.getStoredFallDamage()), (ServerPlayer)player);
            }
            else if (storedDamageType && !isStoringShield) {
                // Player is storing damage
                event.setAmount(currentHealth + currentAbsorption - 1);

                fallBreak.storeReason(source);
                fallBreak.beginStoreDamage(toStoreBase * storeScaling);
                PacketHandler.sendToClient(new FallBreakPacketS2C(fallBreak.getStoredFallDamage()), (ServerPlayer)player);
                if (StoredDamageConfig.STORING_SHIELD_ENABLED.get()) {
                    // Give the player Storing Shield if we've enabled that
                    player.addEffect(new MobEffectInstance(ModEffects.STORING_SHIELD.get(), StoredDamageConfig.SHIELD_DURATION.get()));
                }
                if (StoredDamageConfig.UNSTABLE_HEARTS_ENABLED.get()) {
                    // Give the player Unstable Hearts if we've enabled that
                    int duration = StoredDamageConfig.UnstableHeartsDuration((int) (toStoreBase * shieldScaling));
                    player.addEffect(new MobEffectInstance(ModEffects.UNSTABLE_HEARTS.get(), duration));
                }
            }
            else {
                // Player is protected via Storing Shield
                event.setAmount(0);
                event.getEntity().setAbsorptionAmount(0);
                fallBreak.storeDamage(toStoreBase * shieldScaling);
                PacketHandler.sendToClient(new FallBreakPacketS2C(fallBreak.getStoredFallDamage()), (ServerPlayer)player);
            }
        });
    }

    /*
        If the player has stored damage, use any healing to reduce stored damage instead.
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        LazyOptional<FallBreak> cap = player.getCapability(FallBreakProvider.fallBreakCapability);
        cap.ifPresent(fallBreak -> {
            if (fallBreak.getStoredFallDamage() == 0) {
                    StabilizeHearts(player, fallBreak);
                    return;
            }
            float healAmount = fallBreak.healStoredFallDamage(event.getAmount());
            event.setAmount(healAmount);
            PacketHandler.sendToClient(new FallBreakPacketS2C(fallBreak.getStoredFallDamage()), (ServerPlayer)player);
            if (healAmount > 0) {
                StabilizeHearts(player, fallBreak);
            }
        });
    }

    public static void StabilizeHearts(Player player, FallBreak fallBreak) {
        if (player.hasEffect(ModEffects.UNSTABLE_HEARTS.get())) {
            // Take away the Unstable Heart countdown since we managed to recover
            player.removeEffect(ModEffects.UNSTABLE_HEARTS.get());
        }
        if (player.hasEffect(ModEffects.STORING_SHIELD.get())) {
            // Take away the Unstable Heart countdown since we managed to recover
            player.removeEffect(ModEffects.STORING_SHIELD.get());
        }
        fallBreak.clearReason();
    }

    // Get this before any other modded event, so that we can stop them rendering special
    // heart displays and show cracked hearts instead
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void renderHearts(RenderGuiOverlayEvent.Pre event) {
        boolean hasRendered = HeartRenderer.renderHeartDisplay(event);
        event.setCanceled(event.isCanceled() || hasRendered);
    }
}

