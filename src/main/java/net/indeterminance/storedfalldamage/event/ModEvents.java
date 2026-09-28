package net.indeterminance.storedfalldamage.event;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.networking.FallBreakClientData;
import net.indeterminance.storedfalldamage.networking.FallBreakPacketS2C;
import net.indeterminance.storedfalldamage.registries.ModDataAttachments;
import net.indeterminance.storedfalldamage.config.ConfigEnforcer;
import net.indeterminance.storedfalldamage.config.StoredDamageConfig;
import net.indeterminance.storedfalldamage.registries.ModEffects;
import net.indeterminance.storedfalldamage.registries.ModParticles;
import net.indeterminance.storedfalldamage.registries.ModTags;
import net.indeterminance.storedfalldamage.render.HeartRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@EventBusSubscriber(modid = StoredFallDamage.MOD_ID)
public class ModEvents {
    /*
        We're going to check if the player takes a lethal amount of fall damage, and if so
        save them at half a heart (consuming all absorption hearts) and store all the damage that
        would've killed them.

        This method returns early if it's either not a player, not fall damage, or the fall doesn't
        match the requirements set in configuration.
     */
    @net.neoforged.bus.api.SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DamageSource currentDamageSource = event.getSource();
        String previousStoringSource = player.getData(ModDataAttachments.STORED_DAMAGE_SOURCE);
        boolean matchesStoringDamageTypes = currentDamageSource.is(ModTags.STORED_DAMAGE_TYPES) && ConfigEnforcer.ShouldSavePlayer(player, currentDamageSource);
        boolean isStoringShield = player.hasEffect(ModEffects.STORING_SHIELD) && ConfigEnforcer.IsStoreShieldedFromType(previousStoringSource, currentDamageSource);

        if (!matchesStoringDamageTypes && !isStoringShield) return;

        float damage = event.getOriginalDamage();
        float currentHealth = event.getEntity().getHealth();
        float currentAbsorption = event.getEntity().getAbsorptionAmount();
        float toStoreBase = (damage - currentHealth - currentAbsorption + 1);
        float storeScaling = StoredDamageConfig.STORED_SCALING.get().floatValue();
        float shieldScaling = StoredDamageConfig.SHIELD_SCALING.get().floatValue();
        float scaling = isStoringShield & !matchesStoringDamageTypes ? shieldScaling : storeScaling;
        if (ConfigEnforcer.DoesDamageExceedStoreable(toStoreBase * scaling)) return;
        if (currentHealth + currentAbsorption - 1 >= damage) return;

        float storedDamage = player.getData(ModDataAttachments.STORED_DAMAGE);
        if (storedDamage > 0 && !isStoringShield || ConfigEnforcer.DoesDamageExceedStoreable(storedDamage + toStoreBase * shieldScaling)) {
            // Player took non-shielded damage whilst having damage stored (instant death)
            event.setNewDamage(Float.MAX_VALUE);
            PacketDistributor.sendToAllPlayers(new FallBreakPacketS2C(storedDamage, player.getStringUUID(), previousStoringSource));
        }
        else if (matchesStoringDamageTypes && !isStoringShield) {
            // Player is about to store damage
            event.setNewDamage(currentHealth + currentAbsorption - 1);
            String reasonForStoring = storeReason(player, currentDamageSource);
            player.setData(ModDataAttachments.STORED_DAMAGE, storedDamage + toStoreBase * storeScaling);
            PacketDistributor.sendToAllPlayers(new FallBreakPacketS2C(storedDamage, player.getStringUUID(), reasonForStoring));
            if (StoredDamageConfig.STORING_SHIELD_ENABLED.get()) {
                // Give the player Storing Shield if we've enabled that
                player.addEffect(new MobEffectInstance(ModEffects.STORING_SHIELD, StoredDamageConfig.SHIELD_DURATION.get()));
            }
            if (StoredDamageConfig.UNSTABLE_HEARTS_ENABLED.get()) {
                // Give the player Unstable Hearts if we've enabled that
                int duration = StoredDamageConfig.UnstableHeartsDuration((int) (toStoreBase * shieldScaling));
                player.addEffect(new MobEffectInstance(ModEffects.UNSTABLE_HEARTS, duration));
            }
        }
        else {
            // Player is protected via Storing Shield
            event.setNewDamage(0);
            event.getEntity().setAbsorptionAmount(0);
            player.setData(ModDataAttachments.STORED_DAMAGE, storedDamage + toStoreBase * shieldScaling);
            String storeReason = player.getData(ModDataAttachments.STORED_DAMAGE_SOURCE);
            PacketDistributor.sendToAllPlayers(new FallBreakPacketS2C(storedDamage, player.getStringUUID(), storeReason));
        }
    }

    public static String storeReason(Player player, DamageSource source) {
        Optional<ResourceKey<DamageType>> damageOptional =  source.typeHolder().unwrap().left();
        AtomicReference<String> result = new AtomicReference<String>("");
        damageOptional.ifPresent(key -> result.set(key.location().toString()));

        player.setData(ModDataAttachments.STORED_DAMAGE_SOURCE, result.get());
        return result.get();
    }


    /*
        If the player has stored damage, use any healing to reduce stored damage instead.
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        float storedDamage = player.getData(ModDataAttachments.STORED_DAMAGE);
        if (storedDamage == 0) {
            StabilizeHearts(player);
            return;
        }

        float healedAmount = storedDamage - event.getAmount();
        player.setData(ModDataAttachments.STORED_DAMAGE, Math.max(healedAmount, 0f));
        event.setAmount(Math.max(-healedAmount, 0));

        if (healedAmount > 0) {
            StabilizeHearts(player);
        }
    }

    public static void StabilizeHearts(Player player) {
        if (player.hasEffect(ModEffects.UNSTABLE_HEARTS)) {
            // Take away the Unstable Heart countdown since we managed to recover
            player.removeEffect(ModEffects.UNSTABLE_HEARTS);
        }
        if (player.hasEffect(ModEffects.STORING_SHIELD)) {
            // Take away the Unstable Heart countdown since we managed to recover
            player.removeEffect(ModEffects.STORING_SHIELD);
        }
        player.setData(ModDataAttachments.STORED_DAMAGE_SOURCE, "");
    }

    // Get this before any other modded event, so that we can stop them rendering special
    // heart displays and show cracked hearts instead
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void renderHearts(RenderGuiLayerEvent.Pre event) {
        boolean isHealthLayer = event.getName().equals(ResourceLocation.withDefaultNamespace("player_health"));
        boolean isSurvival = Minecraft.getInstance().gameMode.canHurtPlayer() ;
        if (!isHealthLayer || !isSurvival ) return;
        boolean hasRendered = HeartRenderer.renderHeartDisplay(event);
        event.setCanceled(event.isCanceled() || hasRendered);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.StartTracking event) {
        ServerPlayer player = (ServerPlayer)event.getEntity();
        float storedDamage = player.getData(ModDataAttachments.STORED_DAMAGE);
        PacketDistributor.sendToAllPlayers(new FallBreakPacketS2C(storedDamage, player.getStringUUID(), ""));
    }


    @SubscribeEvent
    public static void entityTickEvent(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        Level level = player.level();
        if (FallBreakClientData.getPlayerStoredDamage(player) > 0) {
            if (player.random.nextInt(15) == 0 && !player.isInvisible()) {
                level.addParticle(
                        ModParticles.CRACKED_HEART_PARTICLE.get(),
                        player.getRandomX(2),
                        player.getRandomY(),
                        player.getRandomZ(2),
                        0.0, -1.0, 0.0);
            }
        }
    }
}

