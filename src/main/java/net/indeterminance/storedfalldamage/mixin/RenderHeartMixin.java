package net.indeterminance.storedfalldamage.mixin;

import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.client.FallBreakClientData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Gui.class)
public class RenderHeartMixin {
    @Unique
    @Final
    private static final ResourceLocation CRACKED_HEARTS_LOC = new ResourceLocation(StoredFallDamage.MOD_ID, "textures/gui/cracked_hearts.png");

    @Unique
    private static final int CRACKED_HEARTS_VARIANTS = 6;

    @Shadow
    @Final
    protected RandomSource random;

    @Unique
    private void storedFallDamage$renderCrackedHeart(GuiGraphics pGuiGraphics, Gui.HeartType pHeartType, int pX, int pY, int pYOffset, boolean pRenderHighlight, boolean pHalfHeart) {

        pGuiGraphics.blit(CRACKED_HEARTS_LOC, pX, pY, pHeartType.getX(pHalfHeart, pRenderHighlight) - 16, pYOffset, 9, 9);
    }

    /*
        This is mostly a copy of the vanilla renderHearts event,
        but with some extra calculations to handle stored damage.
     */
    @Inject(
            method = "renderHearts",
            at = @At("HEAD"),
            cancellable = true
    )
    public void renderHearts(GuiGraphics pGuiGraphics, Player pPlayer, int pX, int pY, int pHeight, int pOffsetHeartIndex, float pMaxHealth, int pCurrentHealth, int pDisplayHealth, int pAbsorptionAmount, boolean pRenderHighlight, CallbackInfo ci) {
        //pPlayer.sendSystemMessage(Component.literal("client fall damage : {}".format(String.valueOf(FallBreakClientData.clientStoredFallDamage))));
        if (FallBreakClientData.clientStoredFallDamage == 0) return;
        ci.cancel();
        Gui.HeartType playerHeartType = Gui.HeartType.forPlayer(pPlayer);
        int hardcoreOffset = pPlayer.level().getLevelData().isHardcore() ? 54 : 0;
        int heartCount = Math.min(Mth.ceil((double)pMaxHealth / 2.0D),10);
        int absorbHeartCount = Mth.ceil((double)pAbsorptionAmount / 2.0D);
        int halfHeartCount = heartCount * 2;

        float fallDamageToHeal = FallBreakClientData.clientStoredFallDamage + pAbsorptionAmount;

        for(int combinedHeartCount = heartCount + absorbHeartCount - 1; combinedHeartCount >= 0; --combinedHeartCount) {
            int textureYOffset = combinedHeartCount >= heartCount ? 0 : 9 * (int) Math.min(CRACKED_HEARTS_VARIANTS,Math.ceil(fallDamageToHeal / 20)) + hardcoreOffset;
            fallDamageToHeal -= 2;
            int combinedRowCount = combinedHeartCount / 10;
            int lastRowHeartCount = combinedHeartCount % 10;
            int lastRowHeartXPos = pX + lastRowHeartCount * 8;
            int lastRowHeartYPos = pY - combinedRowCount * pHeight;
            if (pCurrentHealth + pAbsorptionAmount <= 4) {
                lastRowHeartYPos += this.random.nextInt(2);
            }

            if (combinedHeartCount < heartCount && combinedHeartCount == pOffsetHeartIndex) {
                lastRowHeartYPos -= 2;
            }

            storedFallDamage$renderCrackedHeart(pGuiGraphics, Gui.HeartType.CONTAINER, lastRowHeartXPos, lastRowHeartYPos, textureYOffset, pRenderHighlight, false);
            int combinedHalfHeartCount = combinedHeartCount * 2;
            boolean hasAbsorbHearts = combinedHeartCount >= heartCount;
            if (hasAbsorbHearts) {
                int absorbHalfHeartCount = combinedHalfHeartCount - halfHeartCount;
                if (absorbHalfHeartCount < pAbsorptionAmount) {
                    boolean flag1 = absorbHalfHeartCount + 1 == pAbsorptionAmount;
                    storedFallDamage$renderCrackedHeart(pGuiGraphics, playerHeartType == Gui.HeartType.WITHERED ? playerHeartType : Gui.HeartType.ABSORBING, lastRowHeartXPos, lastRowHeartYPos, textureYOffset, false, flag1);
                }
            }

            if (pRenderHighlight && combinedHalfHeartCount < pDisplayHealth) {
                boolean flag2 = combinedHalfHeartCount + 1 == pDisplayHealth;
                storedFallDamage$renderCrackedHeart(pGuiGraphics, playerHeartType, lastRowHeartXPos, lastRowHeartYPos, textureYOffset, true, flag2);
            }

            if (combinedHalfHeartCount < pCurrentHealth) {
                boolean flag3 = combinedHalfHeartCount + 1 == pCurrentHealth;
                storedFallDamage$renderCrackedHeart(pGuiGraphics, playerHeartType, lastRowHeartXPos, lastRowHeartYPos, textureYOffset, false, flag3);
            }
        }

    }
}
