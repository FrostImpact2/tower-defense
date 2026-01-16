package com.towerdefense.ability;

import com.towerdefense.entity.enemy.BaseEnemyEntity;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Slow Field Ability
 * Enemies in range are slowed by 50%
 * Cooldown: 12 seconds, Duration: 6 seconds
 */
public class SlowFieldAbility extends AbstractTowerAbility {
    
    private static final float SLOW_RADIUS = 12.0f;
    private static final int SLOW_DURATION = 120; // 6 seconds
    
    public SlowFieldAbility() {
        super("slow_field", "Slow Field", "Slows enemies in range by 50%", 240); // 12 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide()) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Find enemies in range
        AABB slowBox = new AABB(
            tower.getX() - SLOW_RADIUS, tower.getY() - SLOW_RADIUS, tower.getZ() - SLOW_RADIUS,
            tower.getX() + SLOW_RADIUS, tower.getY() + SLOW_RADIUS, tower.getZ() + SLOW_RADIUS
        );
        
        List<BaseEnemyEntity> enemies = serverLevel.getEntitiesOfClass(BaseEnemyEntity.class, slowBox);
        
        int slowed = 0;
        for (BaseEnemyEntity enemy : enemies) {
            if (enemy.isAlive()) {
                // Apply slowness effect (level 2 = 50% slower)
                enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION, 2));
                slowed++;
                
                // Slow particles on enemy
                for (int i = 0; i < 5; i++) {
                    double offsetX = (tower.getRandom().nextDouble() - 0.5);
                    double offsetY = tower.getRandom().nextDouble() * enemy.getBbHeight();
                    double offsetZ = (tower.getRandom().nextDouble() - 0.5);
                    serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        enemy.getX() + offsetX,
                        enemy.getY() + offsetY,
                        enemy.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
        
        // Field particles
        for (int i = 0; i < 50; i++) {
            double angle = (2 * Math.PI * i) / 50;
            double x = tower.getX() + Math.cos(angle) * SLOW_RADIUS;
            double z = tower.getZ() + Math.sin(angle) * SLOW_RADIUS;
            serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                x, tower.getY() + 0.5, z,
                1, 0, 0.2, 0, 0
            );
        }
        
        return slowed > 0;
    }
}
