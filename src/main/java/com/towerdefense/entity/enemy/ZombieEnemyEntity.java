package com.towerdefense.entity.enemy;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Zombie Enemy - A basic melee enemy
 * 
 * Type: Basic/Melee
 * - Slow movement speed
 * - Medium health
 * - Low damage
 * - Standard appearance (using zombie model)
 * 
 * Behavior:
 * - Follows the path
 * - Attacks towers blocking its path
 */
public class ZombieEnemyEntity extends BaseEnemyEntity {

    public ZombieEnemyEntity(EntityType<? extends ZombieEnemyEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void initializeStats() {
        this.damage = 5.0f;
        this.attackSpeed = 1.0f; // 1 attack per second
        this.reward = 10; // Gold given when killed
    }

    @Override
    public String getEnemyName() {
        return "Zombie";
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseEnemyEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }
}
