package net.indeterminance.storedfalldamage.compat;

import net.minecraftforge.fml.ModList;

public class CompatManager {
    public static final boolean CWSM_LOADED = isModLoaded("witherstormmod");

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    public static void LoadCompatFeatures() {
        if (CWSM_LOADED) CompatWitherStorm.LoadFeatures();
    }


}
