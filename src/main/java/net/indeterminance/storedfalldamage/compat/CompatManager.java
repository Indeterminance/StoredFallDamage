package net.indeterminance.storedfalldamage.compat;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CompatManager {
    public static final Map<String, Supplier<ModCompat>> potentialCompatFeatures;
    public static final Map<String, ModCompat> loadedCompatFeatures = new HashMap<>();

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    public static void LoadCompatFeatures() {
        for (Map.Entry<String, Supplier<ModCompat>> compatEntry : potentialCompatFeatures.entrySet()) {
            String modId = compatEntry.getKey();
            if (isModLoaded(modId)) {
                StoredFallDamage.LOGGER.debug("Found mod {} with compat feature, enabling...", modId);
                Supplier<ModCompat> supplier = compatEntry.getValue();
                ModCompat compat = supplier.get();
                loadedCompatFeatures.put(modId, compat);
                compat.LoadFeatures();
            }
        }
    }

    static {
        potentialCompatFeatures = new HashMap<>() {{
            put("witherstormmod", CompatWitherStorm::new);
            put("raised", CompatRaised::new);
        }};
    }


}
