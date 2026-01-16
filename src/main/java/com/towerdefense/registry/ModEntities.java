package com.towerdefense.registry;

import com.towerdefense.TowerDefenseMod;
import com.towerdefense.entity.tower.ArcherTowerEntity;
import com.towerdefense.entity.tower.TankTowerEntity;
import com.towerdefense.entity.tower.MageTowerEntity;
import com.towerdefense.entity.enemy.ZombieEnemyEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TowerDefenseMod.MOD_ID);

    // Towers
    public static final DeferredHolder<EntityType<?>, EntityType<ArcherTowerEntity>> ARCHER_TOWER = 
            ENTITIES.register("archer_tower", () -> EntityType.Builder.<ArcherTowerEntity>of(ArcherTowerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("archer_tower"));

    public static final DeferredHolder<EntityType<?>, EntityType<TankTowerEntity>> TANK_TOWER = 
            ENTITIES.register("tank_tower", () -> EntityType.Builder.<TankTowerEntity>of(TankTowerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("tank_tower"));

    public static final DeferredHolder<EntityType<?>, EntityType<MageTowerEntity>> MAGE_TOWER = 
            ENTITIES.register("mage_tower", () -> EntityType.Builder.<MageTowerEntity>of(MageTowerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("mage_tower"));

    // Enemies
    public static final DeferredHolder<EntityType<?>, EntityType<ZombieEnemyEntity>> ZOMBIE_ENEMY = 
            ENTITIES.register("zombie_enemy", () -> EntityType.Builder.<ZombieEnemyEntity>of(ZombieEnemyEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("zombie_enemy"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
