package net.indeterminance.storedfalldamage.event;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.capability.FallBreak;
import net.indeterminance.storedfalldamage.capability.FallBreakProvider;
import net.indeterminance.storedfalldamage.config.ConfigEnforcer;
import net.indeterminance.storedfalldamage.config.StoredDamageConfig;
import net.indeterminance.storedfalldamage.networking.PacketHandler;
import net.indeterminance.storedfalldamage.networking.packet.FallBreakPacketS2C;
import net.indeterminance.storedfalldamage.render.HeartRenderer;
import net.minecraft.server.level.ServerPlayer;
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
        if (!(event.getEntity() instanceof Player player) || !event.getSource().is(ConfigEnforcer.STORED_DAMAGE_TYPES)) return;
        if (!ConfigEnforcer.ShouldBreakFall(player)) return;

        float damage = event.getAmount();
        float currentHealth = event.getEntity().getHealth();
        float currentAbsorption = event.getEntity().getAbsorptionAmount();
        float toStoreBase = (damage - currentHealth - currentAbsorption + 1);
        float scaling = StoredDamageConfig.STORED_SCALING.get().floatValue();
        if (ConfigEnforcer.DoesFallExceedStoreable(toStoreBase)) return;
        if (currentHealth + currentAbsorption - 1 >= damage) return;

        LazyOptional<FallBreak> cap = player.getCapability(FallBreakProvider.fallBreakCapability);
        cap.ifPresent(fallBreak -> {
            if (fallBreak.getStoredFallDamage() > 0) {
                event.setAmount(Float.MAX_VALUE);
                PacketHandler.sendToClient(new FallBreakPacketS2C(fallBreak.getStoredFallDamage()), (ServerPlayer)player);
            }
            else {
                event.setAmount(currentHealth + currentAbsorption - 1);
                event.getEntity().setAbsorptionAmount(0);
                fallBreak.setStoredFallDamage(toStoreBase * scaling);
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
            if (fallBreak.getStoredFallDamage() == 0) return;
            float healAmount = fallBreak.healStoredFallDamage(event.getAmount());
            event.setAmount(healAmount);
            PacketHandler.sendToClient(new FallBreakPacketS2C(fallBreak.getStoredFallDamage()), (ServerPlayer)player);
        });
    }

    // Get this before any other modded event, so that we can stop them rendering special
    // heart displays and show cracked hearts instead
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void renderHearts(RenderGuiOverlayEvent.Pre event) {
        boolean hasRendered = HeartRenderer.renderHeartDisplay(event);
        event.setCanceled(event.isCanceled() || hasRendered);
    }
}

