package net.indeterminance.storedfalldamage;

import com.mojang.logging.LogUtils;
import net.indeterminance.storedfalldamage.compat.CompatManager;
import net.indeterminance.storedfalldamage.config.StoredDamageConfig;
import net.indeterminance.storedfalldamage.networking.FallBreakPacketS2C;
import net.indeterminance.storedfalldamage.particle.CrackedHeartParticle;
import net.indeterminance.storedfalldamage.particle.ShieldParticle;
import net.indeterminance.storedfalldamage.registries.ModDataAttachments;
import net.indeterminance.storedfalldamage.registries.ModEffects;
//import net.indeterminance.storedfalldamage.networking.PacketHandler;
import net.indeterminance.storedfalldamage.registries.ModParticles;
import net.indeterminance.storedfalldamage.registries.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(StoredFallDamage.MOD_ID)
public class StoredFallDamage
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "storedfalldamage";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation res(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }

    public StoredFallDamage(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloadHandlers);
        modEventBus.addListener(this::registerParticleFactories);

        modContainer.registerConfig(ModConfig.Type.COMMON, StoredDamageConfig.CONFIG, "storedfalldamage.toml");
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        ModEffects.register(modEventBus);
        ModDataAttachments.register(modEventBus);
        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);

        CompatManager.LoadCompatFeatures();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        //PacketHandler.register();
    }


    private void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                FallBreakPacketS2C.TYPE,
                FallBreakPacketS2C.STREAM_CODEC,
                FallBreakPacketS2C::handle
        );
    }

    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SHIELD_PARTICLE.get(), ShieldParticle.Provider::new);
        event.registerSpriteSet(ModParticles.CRACKED_HEART_PARTICLE.get(), CrackedHeartParticle.Provider::new);
    }
}
