package com.towerdefense.renderer;

import com.towerdefense.entity.enemy.BaseEnemyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for enemy entities
 * Uses a humanoid model for zombie-type enemies
 */
public class EnemyRenderer extends MobRenderer<BaseEnemyEntity, HumanoidModel<BaseEnemyEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public EnemyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BaseEnemyEntity entity) {
        return TEXTURE;
    }
}