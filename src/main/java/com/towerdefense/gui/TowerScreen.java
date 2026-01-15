package com.towerdefense.gui;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.ability.TowerAbility;
import com.towerdefense.client.TowerMoveHandler;
import com.towerdefense.entity.tower.BaseTowerEntity;
import com.towerdefense.entity.tower.TowerStats;
import com.towerdefense.network.ModNetwork;
import com.towerdefense.network.TowerActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Client-side screen for Tower GUI
 * Displays tower stats, abilities, and action buttons
 */
public class TowerScreen extends AbstractContainerScreen<TowerMenu> {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            TowerDefenseMod.MOD_ID, "textures/gui/tower_gui.png");

    private static final int GUI_WIDTH = 200;
    private static final int GUI_HEIGHT = 180;

    private Button upgradeButton;
    private Button sellButton;
    private Button moveButton;

    public TowerScreen(TowerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        
        // Calculate button positions
        int buttonWidth = 60;
        int buttonHeight = 20;
        int buttonY = topPos + imageHeight - 30;
        int spacing = 5;
        int totalWidth = (buttonWidth * 3) + (spacing * 2);
        int startX = leftPos + (imageWidth - totalWidth) / 2;

        // Upgrade button
        upgradeButton = Button.builder(Component.literal("Upgrade"), button -> {
            sendTowerAction(TowerActionPacket.Action.UPGRADE);
        }).bounds(startX, buttonY, buttonWidth, buttonHeight).build();
        addRenderableWidget(upgradeButton);

        // Sell button
        sellButton = Button.builder(Component.literal("Sell"), button -> {
            sendTowerAction(TowerActionPacket.Action.SELL);
        }).bounds(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight).build();
        addRenderableWidget(sellButton);

        // Move button
        moveButton = Button.builder(Component.literal("Move"), button -> {
            BaseTowerEntity tower = menu.getTower();
            if (tower != null) {
                TowerMoveHandler.enterMoveMode(tower.getId());
            }
            onClose(); // Close GUI and enter move mode
        }).bounds(startX + (buttonWidth + spacing) * 2, buttonY, buttonWidth, buttonHeight).build();
        addRenderableWidget(moveButton);
    }

    private void sendTowerAction(TowerActionPacket.Action action) {
        BaseTowerEntity tower = menu.getTower();
        if (tower != null) {
            ModNetwork.sendToServer(new TowerActionPacket(tower.getId(), action, null));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw background
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC000000);
        guiGraphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFF333333);
        
        // Border
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xFF666666);
        guiGraphics.fill(leftPos, topPos + imageHeight - 2, leftPos + imageWidth, topPos + imageHeight, 0xFF666666);
        guiGraphics.fill(leftPos, topPos, leftPos + 2, topPos + imageHeight, 0xFF666666);
        guiGraphics.fill(leftPos + imageWidth - 2, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF666666);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        BaseTowerEntity tower = menu.getTower();
        if (tower == null) return;

        TowerStats stats = tower.getStats();
        int y = 10;
        int lineHeight = 12;

        // Title
        String title = tower.getTowerName() + " (Lv. " + stats.getLevel() + ")";
        guiGraphics.drawString(font, title, (imageWidth - font.width(title)) / 2, y, 0xFFFFFF);
        y += lineHeight + 5;

        // Role
        String role = "Role: " + tower.getTowerRole();
        guiGraphics.drawString(font, role, 10, y, 0xAAAAAA);
        y += lineHeight + 5;

        // Stats section
        guiGraphics.drawString(font, "--- Stats ---", 10, y, 0xFFAA00);
        y += lineHeight;

        // Health bar
        float healthPercent = stats.getCurrentHealth() / stats.getMaxHealth();
        int barWidth = imageWidth - 20;
        int barHeight = 8;
        guiGraphics.fill(10, y, 10 + barWidth, y + barHeight, 0xFF333333);
        guiGraphics.fill(10, y, 10 + (int)(barWidth * healthPercent), y + barHeight, 0xFF00AA00);
        String healthText = String.format("HP: %.0f / %.0f", stats.getCurrentHealth(), stats.getMaxHealth());
        guiGraphics.drawString(font, healthText, 10, y + barHeight + 2, 0xFFFFFF);
        y += barHeight + lineHeight + 5;

        // Other stats
        guiGraphics.drawString(font, String.format("Damage: %.1f", stats.getDamage()), 10, y, 0xFFFFFF);
        y += lineHeight;
        guiGraphics.drawString(font, String.format("Range: %.1f", stats.getRange()), 10, y, 0xFFFFFF);
        y += lineHeight;
        guiGraphics.drawString(font, String.format("Attack Speed: %.2f/s", stats.getAttackSpeed()), 10, y, 0xFFFFFF);
        y += lineHeight;
        guiGraphics.drawString(font, String.format("Aggro Limit: %d", stats.getAggroLimit()), 10, y, 0xFFFFFF);
        y += lineHeight + 5;

        // Abilities section
        List<TowerAbility> abilities = tower.getAbilities();
        if (!abilities.isEmpty()) {
            guiGraphics.drawString(font, "--- Abilities ---", 10, y, 0xFFAA00);
            y += lineHeight;
            
            for (TowerAbility ability : abilities) {
                String abilityText = ability.getName();
                if (ability.isOnCooldown()) {
                    float cooldownSeconds = ability.getCurrentCooldown() / 20.0f;
                    abilityText += String.format(" (%.1fs)", cooldownSeconds);
                    guiGraphics.drawString(font, abilityText, 10, y, 0x888888);
                } else {
                    guiGraphics.drawString(font, abilityText, 10, y, 0x00FF00);
                }
                y += lineHeight;
            }
            y += 5;
        }

        // Cost info
        guiGraphics.drawString(font, "Upgrade Cost: " + stats.getUpgradeCost() + "g", 10, y, 0xFFFF00);
        y += lineHeight;
        guiGraphics.drawString(font, "Sell Value: " + stats.getSellValue() + "g", 10, y, 0xFFFF00);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
