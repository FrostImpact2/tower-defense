package com.towerdefense.client;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.gui.TowerMenu;
import com.towerdefense.gui.TowerScreen;
import com.towerdefense.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-side event handlers for the mod
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TOWER_MENU.get(), TowerScreen::new);
    }
}
