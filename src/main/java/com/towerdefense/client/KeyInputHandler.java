package com.towerdefense.client;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.client.gui.GuiModeManager;
import com.towerdefense.client.gui.SideGUIRenderer;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handles keybinding input and GUI mode interactions
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Toggle GUI mode
        if (KeyBindings.TOGGLE_GUI_MODE.consumeClick()) {
            GuiModeManager.toggleGuiMode();
            
            // If entering GUI mode without a tower selected, try to select the one being looked at
            if (GuiModeManager.isInGuiMode() && !GuiModeManager.hasTowerSelected()) {
                HitResult hitResult = mc.hitResult;
                if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hitResult;
                    Entity entity = entityHit.getEntity();
                    if (entity instanceof BaseTowerEntity tower) {
                        GuiModeManager.selectTower(tower);
                    }
                }
            }
            
            String mode = GuiModeManager.isInGuiMode() ? "GUI Control Mode" : "Piloting Mode";
            mc.gui.setOverlayMessage(
                net.minecraft.network.chat.Component.literal("Switched to " + mode),
                false
            );
        }

        // Open side GUI (alternative way to enter GUI mode)
        if (KeyBindings.OPEN_SIDE_GUI.consumeClick()) {
            if (!GuiModeManager.isInGuiMode()) {
                // Try to select tower being looked at
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
                    }
                }
            } else {
                GuiModeManager.setGuiMode(false);
                mc.gui.setOverlayMessage(
                    net.minecraft.network.chat.Component.literal("Exited GUI Control Mode"),
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
        if (GuiModeManager.isInGuiMode() && event.getButton() == 0) { // Left click
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
            
            if (SideGUIRenderer.handleClick(mouseX, mouseY)) {
                event.setCanceled(true);
                return;
            }
        }

        // In GUI mode, clicking on a tower selects it
        if (GuiModeManager.isInGuiMode() && event.getButton() == 0) { // Left click
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

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Disable movement in GUI mode
        if (GuiModeManager.isInGuiMode() && event.getEntity().level().isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // This is handled by the input system, but we keep track of mode here
                // Movement input will be blocked by checking GuiModeManager.isInGuiMode() elsewhere
            }
        }
    }
}
