package com.towerdefense.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.towerdefense.TowerDefenseMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinding registration for the tower defense mod
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

    // Category for all tower defense keybindings
    public static final String CATEGORY = "key.categories." + TowerDefenseMod.MOD_ID;

    // Toggle between Piloting Mode and GUI Control Mode
    public static final KeyMapping TOGGLE_GUI_MODE = new KeyMapping(
            "key." + TowerDefenseMod.MOD_ID + ".toggle_gui_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // Default to 'G' key
            CATEGORY
    );

    // Open/close side GUI
    public static final KeyMapping OPEN_SIDE_GUI = new KeyMapping(
            "key." + TowerDefenseMod.MOD_ID + ".open_side_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_T, // Default to 'T' key
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_GUI_MODE);
        event.register(OPEN_SIDE_GUI);
    }
}
