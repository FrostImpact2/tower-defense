package com.towerdefense.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.towerdefense.client.animation.BowAnimation;
import com.towerdefense.client.animation.TowerAnimation;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for tower entities
 * Renders towers as humanoid entities with armor and animations
 */
public class TowerRenderer extends MobRenderer<BaseTowerEntity, HumanoidModel<BaseTowerEntity>> {

    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

    public TowerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);

        // Add armor layers
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));

        // Add item in hand layer
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));

        // Add custom head layer for player heads
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));

        // Add elytra layer
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(BaseTowerEntity entity) {
        return DEFAULT_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(BaseTowerEntity entity) {
        // Don't show default name tag
        return false;
    }

    @Override
    public void render(BaseTowerEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // Apply animations to model before rendering
        HumanoidModel<BaseTowerEntity> model = this.getModel();

        // Get attack animation progress
        float attackProgress = TowerAnimation.getAttackProgress(entity);
        float ageInTicks = entity.tickCount + partialTicks;

        // Reset arms to default
        model.rightArm.xRot = 0.0F;
        model.rightArm.yRot = 0.0F;
        model.rightArm.zRot = 0.0F;
        model.leftArm.xRot = 0.0F;
        model.leftArm.yRot = 0.0F;
        model.leftArm.zRot = 0.0F;

        // Apply bow animation for archer towers
        if (BowAnimation.shouldUseBowAnimation(entity) && attackProgress > 0.0F) {
            applyBowAnimation(model, attackProgress);
        } else if (attackProgress > 0.0F) {
            // Generic attack animation
            float swingAngle = (float) Math.sin(attackProgress * Math.PI) * -1.5F;
            model.rightArm.xRot = swingAngle;
        }

        // Call parent render
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(BaseTowerEntity entity, PoseStack poseStack, float partialTicks) {
        // Make towers slightly smaller (0.9 scale)
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }

    /**
     * Apply bow animation to humanoid model
     */
    private void applyBowAnimation(HumanoidModel<BaseTowerEntity> model, float attackProgress) {
        if (attackProgress < 0.5F) {
            // Drawing phase
            float drawProgress = attackProgress / 0.5F;

            // Right arm (bow hand) - extends forward
            model.rightArm.xRot = -1.5F * drawProgress;
            model.rightArm.yRot = -0.1F * drawProgress;

            // Left arm (draw hand) - pulls back
            model.leftArm.xRot = -1.5F * drawProgress;
            model.leftArm.yRot = 0.3F * drawProgress;
            model.leftArm.zRot = -0.2F * drawProgress;
        } else if (attackProgress < 0.7F) {
            // Full draw hold
            model.rightArm.xRot = -1.5F;
            model.rightArm.yRot = -0.1F;
            model.leftArm.xRot = -1.5F;
            model.leftArm.yRot = 0.3F;
            model.leftArm.zRot = -0.2F;
        } else {
            // Release phase
            float releaseProgress = (attackProgress - 0.7F) / 0.3F;
            float recoil = (float) Math.sin(releaseProgress * Math.PI);

            model.rightArm.xRot = -1.5F + (recoil * 0.3F);
            model.rightArm.yRot = -0.1F;

            float leftArmReturn = -1.5F + (1.3F * releaseProgress);
            model.leftArm.xRot = leftArmReturn - (recoil * 0.2F);
            model.leftArm.yRot = 0.3F * (1.0F - releaseProgress);
            model.leftArm.zRot = -0.2F * (1.0F - releaseProgress);
        }
    }
}