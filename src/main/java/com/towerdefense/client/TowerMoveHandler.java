package com.towerdefense.client;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.entity.tower.BaseTowerEntity;
import com.towerdefense.network.ModNetwork;
import com.towerdefense.network.TowerMovePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * Handles tower move mode on the client
 * When player clicks Move in the tower GUI, they enter "move mode"
 * The next block they click becomes the tower's movement destination
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, value = Dist.CLIENT)
public class TowerMoveHandler {

    private static boolean inMoveMode = false;
    private static int movingTowerId = -1;

    /**
     * Enter move mode for a specific tower
     */
    public static void enterMoveMode(int towerId) {
        inMoveMode = true;
        movingTowerId = towerId;
        TowerDefenseMod.LOGGER.info("Entered move mode for tower " + towerId);
    }

    /**
     * Exit move mode
     */
    public static void exitMoveMode() {
        inMoveMode = false;
        movingTowerId = -1;
    }

    /**
     * Check if currently in move mode
     */
    public static boolean isInMoveMode() {
        return inMoveMode;
    }

    /**
     * Get the tower being moved
     */
    public static int getMovingTowerId() {
        return movingTowerId;
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Post event) {
        if (!inMoveMode) return;
        
        // Only handle left click (button 0) when pressed
        if (event.getButton() != 0 || event.getAction() != 1) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        // Cancel if player opens GUI or presses escape
        if (mc.screen != null) {
            exitMoveMode();
            return;
        }
        
        // Get what the player is looking at
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos targetPos = blockHit.getBlockPos().above(); // Move to the block above the clicked block
        
        // Verify the tower still exists
        Entity entity = mc.level.getEntity(movingTowerId);
        if (!(entity instanceof BaseTowerEntity)) {
            exitMoveMode();
            return;
        }
        
        // Send move packet to server
        ModNetwork.sendToServer(new TowerMovePacket(movingTowerId, targetPos));
        
        // Exit move mode
        exitMoveMode();
        
        // Cancel the event to prevent normal click behavior

    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!inMoveMode) return;
        
        // Cancel move mode on escape
        if (event.getKey() == 256) { // GLFW_KEY_ESCAPE
            exitMoveMode();
        }
    }
}
