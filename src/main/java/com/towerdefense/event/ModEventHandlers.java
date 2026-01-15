package com.towerdefense.event;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.entity.enemy.ZombieEnemyEntity;
import com.towerdefense.entity.tower.ArcherTowerEntity;
import com.towerdefense.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * Handles entity attribute registration for the mod
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandlers {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // Register tower attributes
        event.put(ModEntities.ARCHER_TOWER.get(), ArcherTowerEntity.createAttributes().build());
        
        // Register enemy attributes
        event.put(ModEntities.ZOMBIE_ENEMY.get(), ZombieEnemyEntity.createAttributes().build());
    }
}
