package net.indeterminance.storedfalldamage.compat;

import dev.yurisuika.raised.api.RaisedApi;
import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.render.HeartRenderer;
import net.minecraft.resources.ResourceLocation;

public class CompatRaised implements ModCompat {
    public static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hotbar");

    @Override
    public void LoadFeatures() {
        StoredFallDamage.LOGGER.debug("Adding Raised offsets!");
        HeartRenderer.offsetXFunctions.add(this::modifyX);
        HeartRenderer.offsetYFunctions.add(this::modifyY);
    }

    public int modifyX(int x) {
        return x + RaisedApi.getX(HOTBAR);
    }

    public int modifyY(int y) {
        return y + RaisedApi.getY(HOTBAR);
    }
}
