package com.towerdefense.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.towerdefense.TowerDefenseMod;
import com.towerdefense.entity.enemy.BaseEnemyEntity;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

/**
 * Renders health bars above tower and enemy entities
 * Features:
 * - Billboard effect (always faces camera)
 * - Color-coded health (green/yellow/red)
 * - Entity name display
 * - Health text (current/max)
 * - 32 block visibility range
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class EntityHealthBarRenderer {

    private static final float BAR_WIDTH = 1.0F;
    private static final float BAR_HEIGHT = 0.12F;
    private static final float BAR_Y_OFFSET = 0.5F;
    private static final float NAME_Y_OFFSET = 0.3F;
    private static final int MAX_RENDER_DISTANCE = 32;

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        Entity entity = event.getEntity();

        // Only render for towers and enemies
        if (!(entity instanceof BaseTowerEntity || entity instanceof BaseEnemyEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) entity;

        // Check distance
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double distance = livingEntity.distanceToSqr(mc.player);
        if (distance > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            return;
        }

        // Get entity name
        String name = entity.getName().getString();

        // Render custom health bar with name
        renderHealthBar(
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                livingEntity,
                name,
                mc.font
        );
    }

    /**
     * Render health bar with billboard effect
     */
    private static void renderHealthBar(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                        LivingEntity entity, String name, Font font) {
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = health / maxHealth;

        poseStack.pushPose();

        // Position above entity
        poseStack.translate(0.0, entity.getBbHeight() + BAR_Y_OFFSET, 0.0);

        // Billboard effect - face the camera
        Minecraft mc = Minecraft.getInstance();
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        // Scale
        float scale = 0.025F;
        poseStack.scale(-scale, -scale, scale);

        Matrix4f matrix = poseStack.last().pose();

        // Render entity name
        float nameWidth = font.width(name);
        font.drawInBatch(
                name,
                -nameWidth / 2.0F,
                -NAME_Y_OFFSET / scale,
                0xFFFFFFFF,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        // Render health bar background
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float barWidthPixels = BAR_WIDTH / scale;
        float barHeightPixels = BAR_HEIGHT / scale;
        float barLeft = -barWidthPixels / 2.0F;
        float barRight = barWidthPixels / 2.0F;
        float barTop = 0.0F;
        float barBottom = barHeightPixels;

        // Background (dark gray)
        drawQuad(bufferBuilder, matrix, barLeft - 1, barTop - 1, barRight + 1, barBottom + 1,
                0.0F, 0.0F, 0.0F, 0.8F);

        // Health bar (color based on health percentage)
        float healthBarWidth = barWidthPixels * healthPercent;
        int healthColor = getHealthColor(healthPercent);
        float r = ((healthColor >> 16) & 0xFF) / 255.0F;
        float g = ((healthColor >> 8) & 0xFF) / 255.0F;
        float b = (healthColor & 0xFF) / 255.0F;

        drawQuad(bufferBuilder, matrix, barLeft, barTop, barLeft + healthBarWidth, barBottom,
                r, g, b, 1.0F);

        // Border
        drawQuad(bufferBuilder, matrix, barLeft - 1, barTop - 1, barRight + 1, barTop,
                0.5F, 0.5F, 0.5F, 1.0F);
        drawQuad(bufferBuilder, matrix, barLeft - 1, barBottom, barRight + 1, barBottom + 1,
                0.5F, 0.5F, 0.5F, 1.0F);
        drawQuad(bufferBuilder, matrix, barLeft - 1, barTop - 1, barLeft, barBottom + 1,
                0.5F, 0.5F, 0.5F, 1.0F);
        drawQuad(bufferBuilder, matrix, barRight, barTop - 1, barRight + 1, barBottom + 1,
                0.5F, 0.5F, 0.5F, 1.0F);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        // Render health text
        String healthText = String.format("%.0f / %.0f", health, maxHealth);
        float healthTextWidth = font.width(healthText);
        font.drawInBatch(
                healthText,
                -healthTextWidth / 2.0F,
                (barHeightPixels - 8) / 2.0F,
                0xFFFFFFFF,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                0x80000000,
                packedLight
        );

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    /**
     * Draw a colored quad
     */
    private static void drawQuad(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float x2, float y2,
                                 float r, float g, float b, float a) {
        builder.addVertex(matrix, x1, y1, 0.0F).setColor(r, g, b, a);
        builder.addVertex(matrix, x1, y2, 0.0F).setColor(r, g, b, a);
        builder.addVertex(matrix, x2, y2, 0.0F).setColor(r, g, b, a);
        builder.addVertex(matrix, x2, y1, 0.0F).setColor(r, g, b, a);
    }

    /**
     * Get health bar color based on health percentage
     * Green (>66%), Yellow (33-66%), Red (<33%)
     */
    private static int getHealthColor(float healthPercent) {
        if (healthPercent > 0.66F) {
            return 0x00FF00; // Green
        } else if (healthPercent > 0.33F) {
            return 0xFFAA00; // Yellow/Orange
        } else {
            return 0xFF0000; // Red
        }
    }
}