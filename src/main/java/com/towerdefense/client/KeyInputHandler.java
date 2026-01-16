package com.towerdefense.client;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.client.gui.GuiModeManager;
import com.towerdefense.client.gui.SideGUIRenderer;
import com.towerdefense.entity.tower.BaseTowerEntity;
import com.towerdefense.network.ModNetwork;
import com.towerdefense.network.TowerMovePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * Handles keybinding input and tower interactions
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Teleport selected tower to player position
        if (KeyBindings.TELEPORT_TOWER.consumeClick()) {
            BaseTowerEntity tower = GuiModeManager.getSelectedTower();
            if (tower != null) {
                BlockPos playerPos = mc.player.blockPosition();
                ModNetwork.sendToServer(new TowerMovePacket(tower.getId(), playerPos));
                mc.gui.setOverlayMessage(
                    net.minecraft.network.chat.Component.literal("Teleporting " + tower.getTowerName() + " to your position"),
                    false
                );
            } else {
                mc.gui.setOverlayMessage(
                    net.minecraft.network.chat.Component.literal("No tower selected! Click on a tower first."),
                    false
                );
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Handle clicks on side GUI
        if (GuiModeManager.hasTowerSelected() && event.getButton() == 0) { // Left click
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
            
            if (SideGUIRenderer.handleClick(mouseX, mouseY)) {
                event.setCanceled(true);
                return;
            }
        }

        // Clicking on a tower selects it and shows GUI automatically
        if (event.getButton() == 0) { // Left click
            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                Entity entity = entityHit.getEntity();
                if (entity instanceof BaseTowerEntity tower) {
                    GuiModeManager.selectTower(tower);
                    mc.gui.setOverlayMessage(
                        net.minecraft.network.chat.Component.literal("Selected " + tower.getTowerName()),
                        false
                    );
                    event.setCanceled(true);
                }
            }
        }
    }
}
