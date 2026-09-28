package net.indeterminance.storedfalldamage.render;

import net.indeterminance.storedfalldamage.networking.FallBreakClientData;
import net.indeterminance.storedfalldamage.registries.ModEffects;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HeartRenderer {
    public static boolean fallFlashing = false;

    public static float lastStoredDamage = 0;
    private static Gui gui = Minecraft.getInstance().gui;
    private static Minecraft instance = Minecraft.getInstance();

    public static List<Function<Integer, Integer>> offsetXFunctions = new ArrayList<>();
    public static List<Function<Integer, Integer>> offsetYFunctions = new ArrayList<>();

    public static boolean shouldRenderHeartDisplay(RenderGuiLayerEvent.Pre event) {
        boolean cancelledEvent = event.isCanceled();
        boolean noStoredDamage = FallBreakClientData.getPlayerStoredDamage(instance.player) == 0;
        boolean badGui = instance.options.hideGui;
        boolean notPlayer = !(instance.getCameraEntity() instanceof Player);
        return !(cancelledEvent || noStoredDamage || badGui || notPlayer);
    }

    public static boolean renderHeartDisplay(RenderGuiLayerEvent.Pre event) {
        if (instance == null) instance = Minecraft.getInstance();
        if (gui == null) gui = instance.gui;

        if (!shouldRenderHeartDisplay(event)) return false;

        //gui.setupOverlayRenderState(true, false);
        renderHealth(event.getGuiGraphics());

        return true;
    }

    public static void renderHealth(GuiGraphics guiGraphics) {
        Player player = instance.player;
        if (player != null) {
            int currentHealth = Mth.ceil(player.getHealth());
            boolean shouldRenderHighlight = gui.healthBlinkTime > (long) gui.tickCount && (gui.healthBlinkTime - (long) gui.tickCount) / 3L % 2L == 1L;

            ProcessFlashing(player, currentHealth);

            int displayHealth = gui.displayHealth;
            gui.random.setSeed(gui.tickCount * 312871L);
            int x = guiGraphics.guiWidth() / 2 - 91;
            int y = guiGraphics.guiHeight() - gui.leftHeight;for (Function<Integer, Integer> modifier : offsetXFunctions) {
                x = modifier.apply(x);
            }
            for (Function<Integer, Integer> modifier : offsetYFunctions) {
                y = modifier.apply(y);
            }
            float maxHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), (float) Math.max(displayHealth, currentHealth));
            int currentAbsorption = Mth.ceil(player.getAbsorptionAmount());
            int l1 = Mth.ceil((maxHealth + (float) currentAbsorption) / 2.0F / 10.0F);
            int height = Math.max(10 - (l1 - 2), 3);
            gui.leftHeight += (l1 - 1) * height + 10;
            instance.getProfiler().push("health");
            renderHearts(guiGraphics, player, x, y, height, maxHealth, currentHealth, displayHealth, currentAbsorption, shouldRenderHighlight);
            instance.getProfiler().pop();
        }
    }

    public static void renderHearts(GuiGraphics graphics, Player player, int x, int y, int height, float maxHealth, int currentHealth, int displayHealth, int currentAbsorption, boolean shouldRenderHighlight) {
        CrackedHeartType heartToDisplay = CrackedHeartType.GetCorrectHeartForPlayer(player);
        boolean isHardcore = player.level().getLevelData().isHardcore();
        int heartCount = Math.min(Mth.ceil((double)maxHealth / 2.0D), 10);
        int absorbHeartCount = Mth.ceil((double)currentAbsorption / 2.0D);
        int halfHeartCount = heartCount * 2;

        float fallDamageToHeal = FallBreakClientData.getPlayerStoredDamage((LocalPlayer)player) + currentAbsorption;

        // Setup values for regen shake
        MobEffectInstance regenEffect = player.getEffect(MobEffects.REGENERATION);
        boolean isRegenHeartRaised = gui.tickCount % 10 < 5 && regenEffect != null;
        int regenLevel =  regenEffect == null ? 1 : regenEffect.getAmplifier() + 1;


        int startingHeartX = (heartCount + absorbHeartCount - 1) % 10;
        int startingHeartY = (heartCount + absorbHeartCount - 1) / 10;
        for(int thisHeartIndex = heartCount + absorbHeartCount - 1; thisHeartIndex >= 0; --thisHeartIndex) {
            int crackStage = thisHeartIndex >= heartCount ? 0 : (int) Math.min(CrackedHeartType.CRACK_STAGES - 1,Math.ceil(fallDamageToHeal / 20));
            fallDamageToHeal -= 2;

            boolean thisHeartShake = (gui.tickCount / heartCount) % (10 / regenLevel) == thisHeartIndex % (heartCount / regenLevel);
            boolean isShake = isRegenHeartRaised && thisHeartShake;

            int thisHeartRowIndex = thisHeartIndex / 10;
            int thisHeartRowPosition = thisHeartIndex % 10;
            int thisHeartPosX = x + thisHeartRowPosition * 8;
            int thisHeartPosY = y - thisHeartRowIndex * height - (isShake ? 1 : 0);
            int thisShieldPosY = y - thisHeartRowIndex * height;
            int thisHalfHeartCount = thisHeartIndex * 2;

            // Leftmost heart
            if (thisHeartIndex == 0) thisHeartPosY += gui.random.nextInt(2);

            // Render bg container (ie. our cracked hearts)
            CrackedHeartType.GetCorrectContainerForPlayer(player).RenderHeart(graphics, thisHeartPosX, thisHeartPosY, crackStage, false, shouldRenderHighlight, false);

            if (thisHeartIndex >= heartCount) {
                int absorbHalfHeartCount = thisHalfHeartCount - halfHeartCount;
                if (absorbHalfHeartCount < currentAbsorption) {
                    boolean isHalfAbsorbHeart = absorbHalfHeartCount + 1 == currentAbsorption;
                    CrackedHeartType.GetAbsorbHeartForPlayer(player).RenderHeart(graphics, thisHeartPosX, thisHeartPosY, crackStage, isHalfAbsorbHeart, false, isHardcore);
                }
            }

            boolean isHalfHeart = thisHalfHeartCount + 1 == displayHealth;
            if (shouldRenderHighlight && thisHalfHeartCount < displayHealth) {
                heartToDisplay.RenderHeart(graphics, thisHeartPosX, thisHeartPosY, crackStage, isHalfHeart, shouldRenderHighlight, isHardcore);
            }

            if (thisHalfHeartCount < currentHealth) {
                heartToDisplay.RenderHeart(graphics, thisHeartPosX, thisHeartPosY, crackStage, isHalfHeart, shouldRenderHighlight, isHardcore);
            }

            MobEffectInstance storingShieldEffect = player.getEffect(ModEffects.STORING_SHIELD);
            if (storingShieldEffect != null) {
                int duration = storingShieldEffect.getDuration();
                if ((duration <= 100 && duration % 6 < 3)) continue;
                CrackedHeartType.StoringShieldSprite mode = CrackedHeartType.StoringShieldSprite.MIDDLE;
                int shieldXOffset = 0;
                if (thisHeartRowPosition == 0) {
                    shieldXOffset = 3;
                    mode = CrackedHeartType.StoringShieldSprite.LEFT;
                }
                else if ((thisHeartIndex % 10 == startingHeartX && thisHeartRowIndex == startingHeartY) || thisHeartRowPosition == 9) {
                    mode = CrackedHeartType.StoringShieldSprite.RIGHT;
                }
                heartToDisplay.RenderStoringShield(graphics, thisHeartPosX - shieldXOffset, thisShieldPosY - 3, mode);
            }
        }
    }

    public static void ProcessFlashing(Player player, int health) {
        float storedDamage = FallBreakClientData.getPlayerStoredDamage((LocalPlayer)player);
        if (health < gui.lastHealth && player.invulnerableTime > 0)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = gui.tickCount + 20L;
        }
        else if (health > gui.lastHealth && player.invulnerableTime > 0)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = gui.tickCount + 10L;
        }
        else if (storedDamage < lastStoredDamage)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = gui.tickCount + 10L;
            fallFlashing = true;
        }
        else if (storedDamage == 0) {
            fallFlashing = false;
        }

        if (Util.getMillis() - gui.lastHealthTime > 1000L)
        {
            gui.lastHealth = health;
            gui.displayHealth = health;
            gui.lastHealthTime = Util.getMillis();
            fallFlashing = false;
        }

        gui.lastHealth = health;
        lastStoredDamage = storedDamage;
    }
}
