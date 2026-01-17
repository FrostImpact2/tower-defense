package com.towerdefense.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.towerdefense.TowerDefenseMod;
import com.towerdefense.ability.TowerAbility;
import com.towerdefense.entity.tower.BaseTowerEntity;
import com.towerdefense.entity.tower.TowerStats;
import com.towerdefense.network.ModNetwork;
import com.towerdefense.network.TowerActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

/**
 * Side panel GUI renderer for tower control
 * Displays on the left side of the screen when in GUI Control Mode
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SideGUIRenderer {

    private static final int PANEL_WIDTH = 170;
    private static final int PANEL_PADDING = 10;
    private static final int PANEL_MARGIN_TOP = 20;
    
    // Icon items for actions
    private static final ItemStack UPGRADE_ICON = new ItemStack(Items.EXPERIENCE_BOTTLE);
    private static final ItemStack SELL_ICON = new ItemStack(Items.GOLD_INGOT);
    private static final ItemStack MOVE_ICON = new ItemStack(Items.ENDER_PEARL);
    private static final ItemStack ABILITY_ICON = new ItemStack(Items.BLAZE_POWDER);

    // Button bounds for click detection
    private static int upgradeButtonX, upgradeButtonY, upgradeButtonW, upgradeButtonH;
    private static int sellButtonX, sellButtonY, sellButtonW, sellButtonH;
    private static int moveButtonX, moveButtonY, moveButtonW, moveButtonH;
    private static int[] abilityButtonX = new int[10];
    private static int[] abilityButtonY = new int[10];
    private static int[] abilityButtonW = new int[10];
    private static int[] abilityButtonH = new int[10];
    private static int abilityButtonCount = 0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        // Show GUI automatically when a tower is selected
        if (!GuiModeManager.hasTowerSelected()) {
            return;
        }

        BaseTowerEntity tower = GuiModeManager.getSelectedTower();
        if (tower == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Calculate panel position (left side of screen)
        int panelX = PANEL_PADDING;
        int panelY = PANEL_MARGIN_TOP;
        int panelHeight = screenHeight - (PANEL_MARGIN_TOP * 2);

        // Render the panel
        renderPanel(guiGraphics, panelX, panelY, PANEL_WIDTH, panelHeight, tower, mc);
    }

    private static void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, BaseTowerEntity tower, Minecraft mc) {
        // Background with transparency
        guiGraphics.fill(x, y, x + width, y + height, 0xD0000000);
        
        // Border
        guiGraphics.fill(x, y, x + width, y + 2, 0xFF4A90E2); // Top
        guiGraphics.fill(x, y + height - 2, x + width, y + height, 0xFF4A90E2); // Bottom
        guiGraphics.fill(x, y, x + 2, y + height, 0xFF4A90E2); // Left
        guiGraphics.fill(x + width - 2, y, x + width, y + height, 0xFF4A90E2); // Right

        int contentX = x + 10;
        int contentY = y + 10;
        int contentWidth = width - 20;

        TowerStats stats = tower.getStats();

        // Tower name and level (smaller)
        String titleText = tower.getTowerName() + " §7(Lv. " + stats.getLevel() + ")";
        guiGraphics.drawString(mc.font, titleText, contentX, contentY, 0xFFD700, false);
        contentY += 12;

        // Tower role (smaller)
        String roleText = "§6" + tower.getTowerRole();
        guiGraphics.drawString(mc.font, roleText, contentX, contentY, 0xFFFFFF, false);
        contentY += 10;

        // Stats section
        guiGraphics.drawString(mc.font, "§e--- Stats ---", contentX, contentY, 0xFFFFFF, false);
        contentY += 10;

        // Health bar (smaller)
        float healthPercent = stats.getCurrentHealth() / stats.getMaxHealth();
        int barWidth = contentWidth;
        int barHeight = 8;
        
        // Background
        guiGraphics.fill(contentX, contentY, contentX + barWidth, contentY + barHeight, 0xFF333333);
        
        // Health bar color
        int healthColor = getHealthColor(healthPercent);
        guiGraphics.fill(contentX, contentY, contentX + (int)(barWidth * healthPercent), contentY + barHeight, healthColor);
        
        // Health text (smaller)
        String healthText = String.format("HP: %.0f/%.0f", stats.getCurrentHealth(), stats.getMaxHealth());
        guiGraphics.drawString(mc.font, healthText, contentX + 2, contentY + 0, 0xFFFFFF, false);
        contentY += barHeight + 5;

        // Other stats (more compact)
        guiGraphics.drawString(mc.font, String.format("§7DMG: §f%.1f", stats.getDamage()), contentX, contentY, 0xFFFFFF, false);
        contentY += 9;
        guiGraphics.drawString(mc.font, String.format("§7RNG: §f%.1f", stats.getRange()), contentX, contentY, 0xFFFFFF, false);
        contentY += 9;
        guiGraphics.drawString(mc.font, String.format("§7SPD: §f%.2f/s", stats.getAttackSpeed()), contentX, contentY, 0xFFFFFF, false);
        contentY += 9;
        guiGraphics.drawString(mc.font, String.format("§7AGG: §f%d", stats.getAggroLimit()), contentX, contentY, 0xFFFFFF, false);
        contentY += 12;

        // Upgrade preview
        guiGraphics.drawString(mc.font, "§e--- Next Level ---", contentX, contentY, 0xFFFFFF, false);
        contentY += 10;
        
        // Calculate next level stats (using multipliers: 1.2 DMG, 1.15 HP, 1.1 RNG)
        float nextDamage = stats.getDamage() * 1.2f;
        float nextHealth = stats.getMaxHealth() * 1.15f;
        float nextRange = stats.getRange() * 1.1f;
        
        guiGraphics.drawString(mc.font, String.format("§a+20%% DMG §7(%.1f)", nextDamage), contentX, contentY, 0xFFFFFF, false);
        contentY += 9;
        guiGraphics.drawString(mc.font, String.format("§a+15%% HP §7(%.0f)", nextHealth), contentX, contentY, 0xFFFFFF, false);
        contentY += 9;
        guiGraphics.drawString(mc.font, String.format("§a+10%% RNG §7(%.1f)", nextRange), contentX, contentY, 0xFFFFFF, false);
        contentY += 12;

        // Abilities section
        List<TowerAbility> abilities = tower.getAbilities();
        abilityButtonCount = 0;
        if (!abilities.isEmpty()) {
            guiGraphics.drawString(mc.font, "§e--- Abilities ---", contentX, contentY, 0xFFFFFF, false);
            contentY += 10;
            
            for (int i = 0; i < abilities.size(); i++) {
                TowerAbility ability = abilities.get(i);
                
                // Store button bounds
                abilityButtonX[abilityButtonCount] = contentX;
                abilityButtonY[abilityButtonCount] = contentY;
                abilityButtonW[abilityButtonCount] = contentWidth;
                abilityButtonH[abilityButtonCount] = 16;
                abilityButtonCount++;
                
                // Render as button (smaller)
                int bgColor = ability.isOnCooldown() ? 0xFF2C2C2C : 0xFF1A4D1A;
                guiGraphics.fill(contentX, contentY, contentX + contentWidth, contentY + 16, bgColor);
                guiGraphics.fill(contentX, contentY, contentX + contentWidth, contentY + 1, 0xFF555555);
                guiGraphics.fill(contentX, contentY + 15, contentX + contentWidth, contentY + 16, 0xFF555555);
                guiGraphics.fill(contentX, contentY, contentX + 1, contentY + 16, 0xFF555555);
                guiGraphics.fill(contentX + contentWidth - 1, contentY, contentX + contentWidth, contentY + 16, 0xFF555555);
                
                // Ability name and cooldown (smaller)
                String abilityText = ability.getName();
                if (ability.isOnCooldown()) {
                    float cooldownSeconds = ability.getCurrentCooldown() / 20.0f;
                    abilityText += String.format(" §7(%.1fs)", cooldownSeconds);
                    guiGraphics.drawString(mc.font, abilityText, contentX + 3, contentY + 4, 0x888888, false);
                } else {
                    guiGraphics.drawString(mc.font, "§a" + abilityText, contentX + 3, contentY + 4, 0xFFFFFF, false);
                }
                
                contentY += 17;
            }
        }

        contentY += 3;

        // Action buttons
        guiGraphics.drawString(mc.font, "§e--- Actions ---", contentX, contentY, 0xFFFFFF, false);
        contentY += 10;

        int buttonWidth = (contentWidth - 5) / 2;
        int buttonHeight = 22;

        // Upgrade button with preview
        upgradeButtonX = contentX;
        upgradeButtonY = contentY;
        upgradeButtonW = buttonWidth;
        upgradeButtonH = buttonHeight;
        renderUpgradeButton(guiGraphics, contentX, contentY, buttonWidth, buttonHeight, stats, mc);

        // Sell button
        sellButtonX = contentX + buttonWidth + 5;
        sellButtonY = contentY;
        sellButtonW = buttonWidth;
        sellButtonH = buttonHeight;
        renderActionButton(guiGraphics, contentX + buttonWidth + 5, contentY, buttonWidth, buttonHeight,
                          SELL_ICON, "Sell", stats.getSellValue() + "g", mc);

        contentY += buttonHeight + 4;

        // Move button
        moveButtonX = contentX;
        moveButtonY = contentY;
        moveButtonW = contentWidth;
        moveButtonH = buttonHeight;
        renderActionButton(guiGraphics, contentX, contentY, contentWidth, buttonHeight,
                          MOVE_ICON, "Move", "", mc);
    }

    private static void renderUpgradeButton(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                           TowerStats stats, Minecraft mc) {
        // Button background
        guiGraphics.fill(x, y, x + width, y + height, 0xFF2C2C2C);
        
        // Button border
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF555555);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);

        // Icon
        guiGraphics.renderItem(UPGRADE_ICON, x + 3, y + 3);

        // Label
        guiGraphics.drawString(mc.font, "Upgrade", x + 22, y + 3, 0xFFFFFF, false);
        
        // Cost
        guiGraphics.drawString(mc.font, "§6" + stats.getUpgradeCost() + "g", x + 22, y + 12, 0xFFFFFF, false);
    }

    private static void renderActionButton(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                          ItemStack icon, String label, String sublabel, Minecraft mc) {
        // Button background
        guiGraphics.fill(x, y, x + width, y + height, 0xFF2C2C2C);
        
        // Button border (lighter on hover would be ideal, but simplified here)
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF555555);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);

        // Icon
        guiGraphics.renderItem(icon, x + 3, y + 4);

        // Label
        guiGraphics.drawString(mc.font, label, x + 22, y + 4, 0xFFFFFF);
        
        // Sublabel (cost/value)
        if (!sublabel.isEmpty()) {
            guiGraphics.drawString(mc.font, "§6" + sublabel, x + 22, y + 13, 0xFFFFFF);
        }
    }

    private static int getHealthColor(float healthPercent) {
        if (healthPercent > 0.66f) {
            return 0xFF00AA00; // Green
        } else if (healthPercent > 0.33f) {
            return 0xFFFFAA00; // Yellow/Orange
        } else {
            return 0xFFAA0000; // Red
        }
    }

    /**
     * Handle mouse clicks on the side panel
     */
    public static boolean handleClick(double mouseX, double mouseY) {
        if (!GuiModeManager.hasTowerSelected()) {
            return false;
        }

        BaseTowerEntity tower = GuiModeManager.getSelectedTower();
        if (tower == null) {
            return false;
        }

        // Check upgrade button
        if (isInBounds(mouseX, mouseY, upgradeButtonX, upgradeButtonY, upgradeButtonW, upgradeButtonH)) {
            ModNetwork.sendToServer(new TowerActionPacket(tower.getId(), TowerActionPacket.Action.UPGRADE, null, -1));
            return true;
        }

        // Check sell button
        if (isInBounds(mouseX, mouseY, sellButtonX, sellButtonY, sellButtonW, sellButtonH)) {
            ModNetwork.sendToServer(new TowerActionPacket(tower.getId(), TowerActionPacket.Action.SELL, null, -1));
            GuiModeManager.clearSelection();
            return true;
        }

        // Check move button
        if (isInBounds(mouseX, mouseY, moveButtonX, moveButtonY, moveButtonW, moveButtonH)) {
            // Enter move mode
            com.towerdefense.client.TowerMoveHandler.enterMoveMode(tower.getId());
            GuiModeManager.clearSelection();
            return true;
        }
        
        // Check ability buttons
        for (int i = 0; i < abilityButtonCount; i++) {
            if (isInBounds(mouseX, mouseY, abilityButtonX[i], abilityButtonY[i], abilityButtonW[i], abilityButtonH[i])) {
                // Don't activate if on cooldown
                if (!tower.getAbilities().get(i).isOnCooldown()) {
                    ModNetwork.sendToServer(new TowerActionPacket(tower.getId(), TowerActionPacket.Action.USE_ABILITY, null, i));
                }
                return true;
            }
        }

        return false;
    }

    private static boolean isInBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
