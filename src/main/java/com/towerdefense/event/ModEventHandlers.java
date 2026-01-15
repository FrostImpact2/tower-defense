package com.towerdefense.event;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.command.SpawnPathCommand;
import com.towerdefense.entity.enemy.ZombieEnemyEntity;
import com.towerdefense.entity.tower.ArcherTowerEntity;
import com.towerdefense.registry.ModEntities;
import com.towerdefense.wave.WaveManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

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

/**
 * Handles game events (commands, ticks, etc.)
 */
@EventBusSubscriber(modid = TowerDefenseMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
class GameEventHandlers {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SpawnPathCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Tick wave manager
        WaveManager.tickWaves();
    }
}
