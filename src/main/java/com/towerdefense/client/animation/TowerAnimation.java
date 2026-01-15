package com.towerdefense.client.animation;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.util.Mth;

/**
 * Base animation system for tower entities
 * Handles generic tower animations like idle pose and attack animations
 */
public class TowerAnimation {

    // Duration of attack animation in ticks (20 ticks = 1 second)
    private static final int ATTACK_ANIMATION_DURATION = 10;

    /**
     * Apply idle animation to the tower model
     */
    public static void applyIdleAnimation(ArmorStandModel model, BaseTowerEntity entity, float ageInTicks) {
        // Slight idle bob
        float bob = Mth.sin(ageInTicks * 0.067F) * 0.05F;
        model.body.y += bob;
        model.head.y += bob;
    }

    /**
     * Apply attack animation to the tower model
     */
    public static void applyAttackAnimation(ArmorStandModel model, BaseTowerEntity entity, float attackProgress) {
        // attackProgress: 0.0 (start) to 1.0 (complete)
        // Generic arm swing for melee towers
        if (attackProgress > 0.0F) {
            float swingAngle = Mth.sin(attackProgress * (float) Math.PI) * -1.5F;
            model.rightArm.xRot = swingAngle;
        }
    }

    /**
     * Reset model to default pose
     */
    public static void resetPose(ArmorStandModel model) {
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        model.head.zRot = 0.0F;

        model.body.xRot = 0.0F;
        model.body.yRot = 0.0F;
        model.body.zRot = 0.0F;

        model.rightArm.xRot = 0.0F;
        model.rightArm.yRot = 0.0F;
        model.rightArm.zRot = 0.0F;

        model.leftArm.xRot = 0.0F;
        model.leftArm.yRot = 0.0F;
        model.leftArm.zRot = 0.0F;

        model.rightLeg.xRot = 0.0F;
        model.rightLeg.yRot = 0.0F;
        model.rightLeg.zRot = 0.0F;

        model.leftLeg.xRot = 0.0F;
        model.leftLeg.yRot = 0.0F;
        model.leftLeg.zRot = 0.0F;
    }

    /**
     * Get attack animation progress from entity data
     * Returns 0.0 to 1.0 based on attack animation tick
     */
    public static float getAttackProgress(BaseTowerEntity entity) {
        int animTick = entity.getAttackAnimationTick();
        if (animTick <= 0) return 0.0F;

        return 1.0F - ((float) animTick / ATTACK_ANIMATION_DURATION);
    }
}
