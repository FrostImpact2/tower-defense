package com.towerdefense.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * Renderer for tower entities using armor stand model
 * Renders towers as small armor stands with arms visible and animation support
 */
public class ArmorStandTowerRenderer extends MobRenderer<BaseTowerEntity, ArmorStandModel> {

    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");

    public ArmorStandTowerRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);

        // Add armor layer so towers can display their armor
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
                new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
                context.getModelManager()
        ));

        // Add item in hand layer for weapons/tools
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));

        // Add custom head layer for player heads/skulls
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));

        // Add elytra layer for wings
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    @Override
    protected void scale(BaseTowerEntity entity, PoseStack poseStack, float partialTick) {
        // Make towers smaller - 0.9 scale for small armor stand appearance
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(BaseTowerEntity entity) {
        return DEFAULT_TEXTURE;
    }

    @Override
    protected boolean shouldShowName(BaseTowerEntity entity) {
        // Don't show name tag, we'll use custom health bar rendering instead
        return false;
    }
}
