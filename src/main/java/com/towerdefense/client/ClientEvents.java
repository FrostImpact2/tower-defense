package com.towerdefense.client;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.renderer.EnemyRenderer;
import com.towerdefense.renderer.TowerRenderer;
import com.towerdefense.gui.TowerMenu;
import com.towerdefense.gui.TowerScreen;
import com.towerdefense.registry.ModEntities;
import com.towerdefense.registry.ModMenuTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register entity renderers
            EntityRenderers.register(ModEntities.ARCHER_TOWER.get(), TowerRenderer::new);
            EntityRenderers.register(ModEntities.ZOMBIE_ENEMY.get(), EnemyRenderer::new);
        });
    }
}