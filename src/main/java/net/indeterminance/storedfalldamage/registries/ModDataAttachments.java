package net.indeterminance.storedfalldamage.registries;

import com.mojang.serialization.Codec;
import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES;
    public static final Supplier<AttachmentType<Float>> STORED_DAMAGE;
    public static final Supplier<AttachmentType<String>> STORED_DAMAGE_SOURCE;

    static {
        ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, StoredFallDamage.MOD_ID);
        STORED_DAMAGE = ATTACHMENT_TYPES.register("stored_damage", () -> AttachmentType.builder(() -> 0f).serialize(Codec.FLOAT).build());
        STORED_DAMAGE_SOURCE = ATTACHMENT_TYPES.register("stored_damage_reason", () -> AttachmentType.builder(() -> "").serialize(Codec.STRING).build());
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
