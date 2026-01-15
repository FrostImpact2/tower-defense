package com.towerdefense.client.gui;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Manages the GUI control mode state
 * Handles switching between Piloting Mode (normal play) and GUI Control Mode
 */
public class GuiModeManager {

    private static boolean isInGuiMode = false;
    private static int selectedTowerId = -1;
    private static BaseTowerEntity selectedTower = null;

    /**
     * Toggle between Piloting Mode and GUI Control Mode
     */
    public static void toggleGuiMode() {
        isInGuiMode = !isInGuiMode;
        
        if (!isInGuiMode) {
            // Exiting GUI mode, clear selection
            selectedTowerId = -1;
            selectedTower = null;
        }
    }

    /**
     * Check if currently in GUI Control Mode
     */
    public static boolean isInGuiMode() {
        return isInGuiMode;
    }

    /**
     * Set GUI mode state
     */
    public static void setGuiMode(boolean enabled) {
        isInGuiMode = enabled;
        if (!enabled) {
            selectedTowerId = -1;
            selectedTower = null;
        }
    }

    /**
     * Select a tower for GUI control
     */
    public static void selectTower(BaseTowerEntity tower) {
        if (tower != null) {
            selectedTowerId = tower.getId();
            selectedTower = tower;
            isInGuiMode = true;
        }
    }

    /**
     * Select a tower by ID
     */
    public static void selectTower(int towerId) {
        selectedTowerId = towerId;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(towerId);
            if (entity instanceof BaseTowerEntity tower) {
                selectedTower = tower;
                isInGuiMode = true;
            }
        }
    }

    /**
     * Get the currently selected tower
     */
    public static BaseTowerEntity getSelectedTower() {
        // Update reference if needed
        if (selectedTower == null && selectedTowerId != -1) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(selectedTowerId);
                if (entity instanceof BaseTowerEntity tower) {
                    selectedTower = tower;
                } else {
                    // Tower no longer exists
                    selectedTowerId = -1;
                }
            }
        }

        // Validate tower is still alive
        if (selectedTower != null && !selectedTower.isAlive()) {
            selectedTower = null;
            selectedTowerId = -1;
            isInGuiMode = false;
        }

        return selectedTower;
    }

    /**
     * Get the selected tower ID
     */
    public static int getSelectedTowerId() {
        return selectedTowerId;
    }

    /**
     * Clear the selected tower
     */
    public static void clearSelection() {
        selectedTowerId = -1;
        selectedTower = null;
    }

    /**
     * Check if a tower is currently selected
     */
    public static boolean hasTowerSelected() {
        return getSelectedTower() != null;
    }
}
