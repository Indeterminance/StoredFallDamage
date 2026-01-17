package net.indeterminance.storedfalldamage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.indeterminance.storedfalldamage.StoredFallDamage;
import net.indeterminance.storedfalldamage.client.FallBreakClientData;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class HeartRenderer {

    private static final ResourceLocation CRACKED_HEARTS_LOC = new ResourceLocation(StoredFallDamage.MOD_ID, "textures/gui/cracked_hearts.png");
    private static final int CRACKED_HEARTS_VARIANTS = 6;

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

        int healthRows = Mth.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        gui.random.setSeed((long) (gui.tickCount * 312871));

        int left = gui.screenWidth / 2 - 91;
        int top = gui.screenHeight - gui.leftHeight - (player.hasEffect(MobEffects.REGENERATION) ? 2 : 0);
        gui.leftHeight += (healthRows * rowHeight);
        if (rowHeight != 10) gui.leftHeight += 10 - rowHeight;

        renderHearts(graphics, player, left, top, rowHeight, healthMax, health, gui.displayHealth, absorb, highlight, fallFlashing);

        RenderSystem.disableBlend();
        instance.getProfiler().pop();
    }

    public static void renderHearts(GuiGraphics pGuiGraphics, Player player, int x, int y, int height, float maxHealth, int currentHealth, int displayHealth, int currentAbsorption, boolean shouldRenderHighlight, boolean redHighlight) {
        Gui.HeartType playerHeartType = Gui.HeartType.forPlayer(player);
        int hardcoreOffset = player.level().getLevelData().isHardcore() ? 54 : 0;
        int heartCount = Math.min(Mth.ceil((double)maxHealth / 2.0D),10);
        int absorbHeartCount = Mth.ceil((double)currentAbsorption / 2.0D);
        int halfHeartCount = heartCount * 2;

        float fallDamageToHeal = FallBreakClientData.clientStoredFallDamage + currentAbsorption;

        for(int thisHeartIndex = heartCount + absorbHeartCount - 1; thisHeartIndex >= 0; --thisHeartIndex) {
            int textureYOffset = thisHeartIndex >= heartCount ? 0 : 9 * (int) Math.min(CRACKED_HEARTS_VARIANTS,Math.ceil(fallDamageToHeal / 20)) + hardcoreOffset;
            fallDamageToHeal -= 2;
            int thisHeartRowIndex = thisHeartIndex / 10;
            int thisHeartRowPosition = thisHeartIndex % 10;
            int thisHeartPosX = x + thisHeartRowPosition * 8;
            int thisHeartPosY = y - thisHeartRowIndex * height;
            int thisHalfHeartCount = thisHeartIndex * 2;

            // Last heart
            if (thisHeartIndex == 0) thisHeartPosY += gui.random.nextInt(2);

            // Render bg container (ie. our cracked hearts)
            renderCrackedHeart(pGuiGraphics, Gui.HeartType.CONTAINER, thisHeartPosX, thisHeartPosY, textureYOffset, shouldRenderHighlight, false, redHighlight);

            if (thisHeartIndex >= heartCount) {
                int absorbHalfHeartCount = thisHalfHeartCount - halfHeartCount;
                if (absorbHalfHeartCount < currentAbsorption) {
                    boolean isHalfAbsorbHeart = absorbHalfHeartCount + 1 == currentAbsorption;
                    Gui.HeartType absorbFlavor = playerHeartType == Gui.HeartType.WITHERED ? playerHeartType : Gui.HeartType.ABSORBING;
                    renderCrackedHeart(pGuiGraphics, absorbFlavor, thisHeartPosX, thisHeartPosY, textureYOffset, false, isHalfAbsorbHeart, false);
                }
            }

            boolean isHalfHeart = thisHalfHeartCount + 1 == displayHealth;
            if (shouldRenderHighlight && thisHalfHeartCount < displayHealth) {
                renderCrackedHeart(pGuiGraphics, playerHeartType, thisHeartPosX, thisHeartPosY, textureYOffset, true, isHalfHeart, false);
            }

            if (thisHalfHeartCount < currentHealth) {
                renderCrackedHeart(pGuiGraphics, playerHeartType, thisHeartPosX, thisHeartPosY, textureYOffset, false, isHalfHeart, false);
            }
        }
    }

    private static void renderCrackedHeart(GuiGraphics graphics, Gui.HeartType heartType, int x, int y, int yOffset, boolean shouldRenderHighlight, boolean isHalfHeart, boolean redHighlight) {
        graphics.blit(CRACKED_HEARTS_LOC, x, y, GetHeartXFromSheet(heartType, isHalfHeart, shouldRenderHighlight, redHighlight), yOffset, 9, 9);
    }

    public static int GetHeartXFromSheet(Gui.HeartType heartType, boolean isHalfHeart, boolean shouldRenderHighlight, boolean redHighlight) {
        int i;
        if (heartType == Gui.HeartType.CONTAINER) {
            //StoredFallDamage.LOGGER.debug("{}", redHighlight);
            int j = redHighlight ? 2 : 1;
            i = j * (shouldRenderHighlight ? 1 : 0);
        } else {
            int j = isHalfHeart ? 1 : 0;
            int k = heartType.canBlink && shouldRenderHighlight ? 2 : 0;
            i = j + k;
        }

        return (heartType.index * 2 + i) * 9;
    }

    public static void ProcessFlashing(Player player, int health) {
        if (health < gui.lastHealth && player.invulnerableTime > 0)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = (long) (gui.tickCount + 20);
        }
        else if (health > gui.lastHealth && player.invulnerableTime > 0)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = (long) (gui.tickCount + 10);
        }
        else if (FallBreakClientData.clientStoredFallDamage < lastStoredDamage)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = (long) (gui.tickCount + 10);
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
