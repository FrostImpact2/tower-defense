package com.towerdefense.client.gui;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Manages the selected tower for GUI display
 * Simplified - no mode toggling, GUI appears automatically when tower is selected
 */
public class GuiModeManager {

    private static int selectedTowerId = -1;
    private static BaseTowerEntity selectedTower = null;

    /**
     * Select a tower for GUI control
     * This automatically shows the side GUI
     */
    public static void selectTower(BaseTowerEntity tower) {
        if (tower != null) {
            selectedTowerId = tower.getId();
            selectedTower = tower;
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

    /**
     * @deprecated Mode toggle concept removed - GUI shows automatically on tower selection
     */
    @Deprecated
    public static boolean isInGuiMode() {
        return hasTowerSelected();
    }

    /**
     * @deprecated Mode toggle concept removed - use selectTower() or clearSelection() instead
     */
    @Deprecated
    public static void setGuiMode(boolean enabled) {
        if (!enabled) {
            clearSelection();
        }
    }

    /**
     * @deprecated Mode toggle concept removed
     */
    @Deprecated
    public static void toggleGuiMode() {
        // No-op
    }
}
