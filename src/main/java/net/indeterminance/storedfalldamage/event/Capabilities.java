package net.indeterminance.storedfalldamage.event;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.capability.FallBreak;
import net.indeterminance.storedfalldamage.capability.FallBreakProvider;
import net.indeterminance.storedfalldamage.networking.PacketHandler;
import net.indeterminance.storedfalldamage.networking.packet.FallBreakPacketS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoredFallDamage.MOD_ID)
public class Capabilities {
    @SubscribeEvent
    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof Player) {
            if (entity.getCapability(FallBreakProvider.fallBreakCapability).isPresent()) return;
            event.addCapability(new ResourceLocation(StoredFallDamage.MOD_ID, "properties"), new FallBreakProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            LazyOptional<FallBreak> capability = event.getOriginal().getCapability(FallBreakProvider.fallBreakCapability);
            capability.ifPresent(oldStore -> {
                capability.ifPresent(fb -> {
                    PacketHandler.sendToClient(new FallBreakPacketS2C(fb.getStoredFallDamage()), (ServerPlayer) event.getOriginal());
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        LazyOptional<FallBreak> capability = event.getEntity().getCapability(FallBreakProvider.fallBreakCapability);
        capability.ifPresent(oldStore -> {
            capability.ifPresent(fb -> {
                //fb.setStoredFallDamage(0);
                PacketHandler.sendToClient(new FallBreakPacketS2C(fb.getStoredFallDamage()), (ServerPlayer) event.getEntity());
            });
        });
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(FallBreak.class);
    }
}
