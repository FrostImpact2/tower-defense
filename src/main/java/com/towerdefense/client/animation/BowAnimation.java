package com.towerdefense.client.animation;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.util.Mth;

/**
 * Bow-specific animation for archer towers
 * Handles bow drawing and shooting animations
 */
public class BowAnimation {

    /**
     * Apply bow pulling animation for archer towers
     * @param model The armor stand model
     * @param entity The tower entity
     * @param attackProgress Attack animation progress (0.0 to 1.0)
     */
    public static void applyBowAnimation(ArmorStandModel model, BaseTowerEntity entity, float attackProgress) {
        if (attackProgress <= 0.0F) {
            // Idle bow pose - arms at ready position
            applyIdleBowPose(model);
            return;
        }

        // Animation phases:
        // 0.0 - 0.5: Pull bow back
        // 0.5 - 0.7: Hold at full draw
        // 0.7 - 1.0: Release and return to idle

        if (attackProgress < 0.5F) {
            // Drawing phase
            float drawProgress = attackProgress / 0.5F;
            applyBowDrawing(model, drawProgress);
        } else if (attackProgress < 0.7F) {
            // Full draw hold
            applyBowDrawing(model, 1.0F);
        } else {
            // Release phase
            float releaseProgress = (attackProgress - 0.7F) / 0.3F;
            applyBowRelease(model, releaseProgress);
        }
    }

    /**
     * Idle bow pose - arms ready to draw
     */
    private static void applyIdleBowPose(ArmorStandModel model) {
        // Right arm holds bow (slightly forward)
        model.rightArm.xRot = -0.3F; // ~17 degrees down
        model.rightArm.yRot = 0.0F;
        model.rightArm.zRot = 0.0F;

        // Left arm at side ready position
        model.leftArm.xRot = -0.2F;
        model.leftArm.yRot = 0.1F;
        model.leftArm.zRot = 0.0F;
    }

    /**
     * Apply bow drawing motion
     * @param drawProgress 0.0 (start) to 1.0 (fully drawn)
     */
    private static void applyBowDrawing(ArmorStandModel model, float drawProgress) {
        // Right arm (bow hand) - extends forward and slightly up
        model.rightArm.xRot = Mth.lerp(drawProgress, -0.3F, -1.5F); // Pull forward
        model.rightArm.yRot = Mth.lerp(drawProgress, 0.0F, -0.1F);
        model.rightArm.zRot = 0.0F;

        // Left arm (draw hand) - pulls back
        model.leftArm.xRot = Mth.lerp(drawProgress, -0.2F, -1.5F);
        model.leftArm.yRot = Mth.lerp(drawProgress, 0.1F, 0.3F);
        model.leftArm.zRot = Mth.lerp(drawProgress, 0.0F, -0.2F);

        // Body leans slightly forward while drawing
        model.body.xRot = Mth.lerp(drawProgress, 0.0F, -0.05F);

        // Head tilts down to aim
        model.head.xRot = Mth.lerp(drawProgress, 0.0F, -0.15F);
    }

    /**
     * Apply bow release motion and recoil
     * @param releaseProgress 0.0 (release) to 1.0 (return to idle)
     */
    private static void applyBowRelease(ArmorStandModel model, float releaseProgress) {
        // Quick recoil then return to idle
        float recoil = Mth.sin(releaseProgress * (float) Math.PI);

        // Right arm (bow hand) - recoils backward slightly
        model.rightArm.xRot = -1.5F + (recoil * 0.3F);
        model.rightArm.yRot = -0.1F;
        model.rightArm.zRot = 0.0F;

        // Left arm (draw hand) - snaps forward after release
        float leftArmReturn = Mth.lerp(releaseProgress, -1.5F, -0.2F);
        model.leftArm.xRot = leftArmReturn - (recoil * 0.2F);
        model.leftArm.yRot = Mth.lerp(releaseProgress, 0.3F, 0.1F);
        model.leftArm.zRot = Mth.lerp(releaseProgress, -0.2F, 0.0F);

        // Body returns to neutral
        model.body.xRot = Mth.lerp(releaseProgress, -0.05F, 0.0F);

        // Head returns to neutral
        model.head.xRot = Mth.lerp(releaseProgress, -0.15F, 0.0F);
    }

    /**
     * Check if entity is an archer tower that should use bow animation
     */
    public static boolean shouldUseBowAnimation(BaseTowerEntity entity) {
        // Check if entity has "Archer" in its tower name
        return entity.getTowerName().contains("Archer");
    }
}
