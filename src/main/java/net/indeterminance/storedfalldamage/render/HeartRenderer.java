package net.indeterminance.storedfalldamage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.indeterminance.storedfalldamage.client.FallBreakClientData;
import net.indeterminance.storedfalldamage.registries.ModEffects;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class HeartRenderer {
    public static Minecraft instance;
    public static ForgeGui gui;
    public static boolean fallFlashing = false;

    public static float lastStoredDamage = 0;

    static {
        instance = Minecraft.getInstance();
        gui = (ForgeGui) instance.gui;

    }

    public static boolean shouldRenderHeartDisplay(RenderGuiOverlayEvent.Pre event) {
        boolean earlyFail = event.isCanceled() || event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type();
        boolean noStoredDamage = FallBreakClientData.clientStoredFallDamage == 0;
        boolean badGui = (gui == null) || instance.options.hideGui || !gui.shouldDrawSurvivalElements();
        boolean notPlayer = !(instance.getCameraEntity() instanceof Player);
        return !(earlyFail || noStoredDamage || badGui || notPlayer);
    }

    public static boolean renderHeartDisplay(RenderGuiOverlayEvent.Pre event) {
        if (!shouldRenderHeartDisplay(event)) return false;

        gui.setupOverlayRenderState(true, false);
        renderHealth(event.getGuiGraphics());

        return true;
    }

    public static void renderHealth(GuiGraphics graphics) {
        instance.getProfiler().push("health");
        RenderSystem.enableBlend();

        Player player = (Player) instance.getCameraEntity();
        int health = Mth.ceil(player.getHealth());
        boolean highlight = gui.healthBlinkTime > (long) gui.tickCount && (gui.healthBlinkTime - (long) gui.tickCount) / 3L % 2L == 1L;

        ProcessFlashing(player, health);

        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = Math.max((float) attrMaxHealth.getValue(), Math.max(gui.displayHealth, health));
        int absorb = Mth.ceil(player.getAbsorptionAmount());

        int healthRows = Mth.ceil((healthMax + absorb) / 20.0F);
        int rowHeight = Math.max(12 - healthRows, 3);

        gui.random.setSeed(gui.tickCount * 312871L);

        int left = gui.screenWidth / 2 - 91;
        int top = gui.screenHeight - gui.leftHeight;

        gui.leftHeight += (healthRows * rowHeight);
        if (rowHeight != 10) gui.leftHeight += 10 - rowHeight;

        renderHearts(graphics, player, left, top, rowHeight, healthMax, health, gui.displayHealth, absorb, highlight);

        RenderSystem.disableBlend();
        instance.getProfiler().pop();
    }

    public static void renderHearts(GuiGraphics graphics, Player player, int x, int y, int height, float maxHealth, int currentHealth, int displayHealth, int currentAbsorption, boolean shouldRenderHighlight) {
        CrackedHeartType heartToDisplay = CrackedHeartType.GetCorrectHeartForPlayer(player);
        boolean isHardcore = player.level().getLevelData().isHardcore();
        int heartCount = Math.min(Mth.ceil((double)maxHealth / 2.0D), 10);
        int absorbHeartCount = Mth.ceil((double)currentAbsorption / 2.0D);
        int halfHeartCount = heartCount * 2;

        float fallDamageToHeal = FallBreakClientData.clientStoredFallDamage + currentAbsorption;

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

            MobEffectInstance storingShieldEffect = player.getEffect(ModEffects.STORING_SHIELD.get());
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
        else if (FallBreakClientData.clientStoredFallDamage < lastStoredDamage)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = gui.tickCount + 10L;
            fallFlashing = true;
        }
        else if (FallBreakClientData.clientStoredFallDamage == 0) {
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
        lastStoredDamage = FallBreakClientData.clientStoredFallDamage;
    }
}
