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

    // Teleport selected tower to player position
    public static final KeyMapping TELEPORT_TOWER = new KeyMapping(
            "key." + TowerDefenseMod.MOD_ID + ".teleport_tower",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, // Default to 'V' key
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TELEPORT_TOWER);
    }
}
