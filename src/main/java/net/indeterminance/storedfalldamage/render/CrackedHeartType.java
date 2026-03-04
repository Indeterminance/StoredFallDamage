package net.indeterminance.storedfalldamage.render;

import net.indeterminance.storedfalldamage.registries.ModResources;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static net.indeterminance.storedfalldamage.registries.ModResources.CRACKED_HEARTS_LOC;

public class CrackedHeartType {
    public static final int CRACK_STAGES = 7;
    public static final Vector2i HARDCORE_OFFSET = new Vector2i(0, 9 * CRACK_STAGES);
    public static final Vector2i HALFHEART_OFFSET = new Vector2i(9, 0);
    public static final Vector2i FLASHING_OFFSET = new Vector2i(18, 0);

    public static class Definitions {
        public static CrackedHeartType CONTAINER = new CrackedHeartType(CRACKED_HEARTS_LOC, new Vector2i(0,0), FLASHING_OFFSET, false, false, 0, p -> true, false);
        public static CrackedHeartType NORMAL = new CrackedHeartType(CRACKED_HEARTS_LOC, new Vector2i(36,0), FLASHING_OFFSET, true, true, 10, p -> true, false);
        public static CrackedHeartType FROZEN = new CrackedHeartType(CRACKED_HEARTS_LOC, new Vector2i(162,0), FLASHING_OFFSET, false, true, 20, Entity::isFullyFrozen, false);
        public static CrackedHeartType WITHER = new CrackedHeartType(CRACKED_HEARTS_LOC, new Vector2i(108,0), FLASHING_OFFSET, true, true, 30, p -> p.hasEffect(MobEffects.WITHER), true);
        public static CrackedHeartType POISON = new CrackedHeartType(CRACKED_HEARTS_LOC, new Vector2i(72,0), FLASHING_OFFSET, false, true, 40, p -> p.hasEffect(MobEffects.POISON), true);
        public static CrackedHeartType ABSORPTION = new CrackedHeartType(CRACKED_HEARTS_LOC, new Vector2i(144,0), FLASHING_OFFSET, false, true, 20, p -> true, false);

        public static List<CrackedHeartType> INNER_HEARTS = new ArrayList<>(List.of(
                NORMAL,
                FROZEN,
                WITHER,
                POISON
        ));

        public static List<CrackedHeartType> CONTAINERS = new ArrayList<>(List.of(
                CONTAINER
        ));
    }

    ResourceLocation location;
    Vector2i position;
    Vector2i flashingOffset;
    boolean canBlink;
    boolean hasHalfHeart;
    int priority;
    Function<Player,Boolean> requirements;
    boolean overridesAbsorption;

    public CrackedHeartType(ResourceLocation loc, Vector2i pos, Vector2i flashingOffset, boolean canBlink, boolean hasHalfHeart, int priority, Function<Player, Boolean> reqs, boolean overridesAbsorption) {
        this.location = loc;
        this.position = pos;
        this.flashingOffset = flashingOffset;
        this.canBlink = canBlink;
        this.hasHalfHeart = hasHalfHeart;
        this.priority = priority;
        this.requirements = reqs;
        this.overridesAbsorption = overridesAbsorption;
    }

    public boolean isApplicableHeart(Player player) {
        return this.requirements.apply(player);
    }

    public Vector2i CalculateFinalPosition(boolean isHalfHeart, boolean renderHighlight, boolean isHardcore) {
        Vector2i finalPos = new Vector2i(position);
        if (isHalfHeart) finalPos = finalPos.add(HALFHEART_OFFSET);
        if (isHardcore) finalPos = finalPos.add(HARDCORE_OFFSET);
        if (renderHighlight) finalPos.add(FLASHING_OFFSET);
        return finalPos;
    }

    public void RenderHeart(GuiGraphics graphics, int screenX, int screenY, int stage, boolean isHalfHeart, boolean renderHighlight, boolean redHighlight, boolean isHardcore) {
        Vector2i sheetPos = CalculateFinalPosition(isHalfHeart, renderHighlight, isHardcore);
        graphics.blit(location, screenX, screenY, sheetPos.x, sheetPos.y + stage * 9, 9, 9);
    }

    public enum StoringShieldSprite {
        LEFT,
        MIDDLE,
        RIGHT
    }

    public void RenderStoringShield(GuiGraphics graphics, int screenX, int screenY, StoringShieldSprite mode) {
        int xPos;
        int width;
        if (mode == StoringShieldSprite.LEFT) {
            xPos = 0;
            width = 12;
        }
        else if (mode == StoringShieldSprite.RIGHT) {
            xPos = 19;
            width = 12;
        }
        else {
            xPos = 11;
            width = 9;
        }
        graphics.blit(ModResources.SHIELD_EFFECT_LOC, screenX, screenY, xPos, 0, width, 13);
    }

    public static CrackedHeartType GetCorrectHeartForPlayer(Player player) {
        CrackedHeartType currentHeartType = Definitions.NORMAL;
        for (CrackedHeartType heart : Definitions.INNER_HEARTS) {
            if (!heart.isApplicableHeart(player)) continue;
            if (heart.priority > currentHeartType.priority) {
                currentHeartType = heart;
            }
        }
        return currentHeartType;
    }

    public static CrackedHeartType GetCorrectContainerForPlayer(Player player) {
        CrackedHeartType currentContainerType = Definitions.CONTAINER;
        for (CrackedHeartType heart : Definitions.CONTAINERS) {
            if (!heart.isApplicableHeart(player)) continue;
            if (heart.priority > currentContainerType.priority) {
                currentContainerType = heart;
            }
        }
        return currentContainerType;
    }

    public static CrackedHeartType GetAbsorbHeartForPlayer(Player player) {
        CrackedHeartType currentAbsorptionType = Definitions.ABSORPTION;
        for (CrackedHeartType heart : Definitions.INNER_HEARTS) {
            if (!heart.isApplicableHeart(player) || !heart.overridesAbsorption) continue;
            if (heart.priority > currentAbsorptionType.priority) {
                currentAbsorptionType = heart;
            }
        }
        return currentAbsorptionType;
    }
}
