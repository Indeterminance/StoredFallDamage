package net.indeterminance.storedfalldamage.compat;

import net.indeterminance.storedfalldamage.render.CrackedHeartType;
import nonamecrackers2.witherstormmod.common.init.WitherStormModEffects;
import org.joml.Vector2i;

import static net.indeterminance.storedfalldamage.registries.ModResources.COMPAT_HEARTS_LOC;

public class CompatWitherStorm {
    public static void LoadFeatures() {
        CrackedHeartType.Definitions.INNER_HEARTS.add(
            new CrackedHeartType(COMPAT_HEARTS_LOC, new Vector2i(18,0), CrackedHeartType.FLASHING_OFFSET, true, true, 50, p -> p.hasEffect(WitherStormModEffects.WITHER_SICKNESS.get()), false)
        );
        CrackedHeartType.Definitions.CONTAINERS.add(
                new CrackedHeartType(COMPAT_HEARTS_LOC, new Vector2i(0,0), new Vector2i(9,0), true, false, 10, p -> p.hasEffect(WitherStormModEffects.WITHER_SICKNESS.get()), true)
        );
    }
}
