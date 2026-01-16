package com.towerdefense.ability;

import com.towerdefense.entity.enemy.BaseEnemyEntity;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain Lightning Ability
 * Damage bounces to nearby enemies
 * Cooldown: 10 seconds
 */
public class ChainLightningAbility extends AbstractTowerAbility {
    
    private static final int MAX_BOUNCES = 4;
    private static final float BOUNCE_RANGE = 8.0f;
    private static final float DAMAGE_MULTIPLIER = 1.2f;
    private static final float DAMAGE_REDUCTION = 0.7f; // Each bounce does 70% of previous
    
    public ChainLightningAbility() {
        super("chain_lightning", "Chain Lightning", "Damage bounces to nearby enemies", 200); // 10 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide() || target == null) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Start chain lightning
        List<LivingEntity> hitTargets = new ArrayList<>();
        LivingEntity currentTarget = target;
        float currentDamage = tower.getStats().getDamage() * DAMAGE_MULTIPLIER;
        
        for (int bounce = 0; bounce < MAX_BOUNCES && currentTarget != null; bounce++) {
            // Damage current target
            currentTarget.hurt(serverLevel.damageSources().lightningBolt(), currentDamage);
            hitTargets.add(currentTarget);
            
            // Lightning particles
            spawnLightningParticles(serverLevel, tower, currentTarget);
            
            // Find next target
            LivingEntity nextTarget = findNextTarget(serverLevel, currentTarget, hitTargets);
            
            if (nextTarget != null) {
                // Chain particles between targets
                spawnChainParticles(serverLevel, currentTarget, nextTarget);
            }
            
            currentTarget = nextTarget;
            currentDamage *= DAMAGE_REDUCTION;
        }
        
        return !hitTargets.isEmpty();
    }
    
    private LivingEntity findNextTarget(ServerLevel level, LivingEntity current, List<LivingEntity> hitTargets) {
        AABB searchBox = new AABB(
            current.getX() - BOUNCE_RANGE, current.getY() - BOUNCE_RANGE, current.getZ() - BOUNCE_RANGE,
            current.getX() + BOUNCE_RANGE, current.getY() + BOUNCE_RANGE, current.getZ() + BOUNCE_RANGE
        );
        
        List<BaseEnemyEntity> nearby = level.getEntitiesOfClass(BaseEnemyEntity.class, searchBox);
        
        // Find closest enemy that hasn't been hit
        BaseEnemyEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        
        for (BaseEnemyEntity enemy : nearby) {
            if (!hitTargets.contains(enemy) && enemy.isAlive()) {
                double dist = enemy.distanceToSqr(current);
                if (dist < closestDist) {
                    closest = enemy;
                    closestDist = dist;
                }
            }
        }
        
        return closest;
    }
    
    private void spawnLightningParticles(ServerLevel level, BaseTowerEntity tower, LivingEntity target) {
        // Lightning strike particles on target
        for (int i = 0; i < 15; i++) {
            double offsetX = (tower.getRandom().nextDouble() - 0.5) * 0.5;
            double offsetY = tower.getRandom().nextDouble() * target.getBbHeight();
            double offsetZ = (tower.getRandom().nextDouble() - 0.5) * 0.5;
            level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX() + offsetX,
                target.getY() + offsetY,
                target.getZ() + offsetZ,
                3, 0.2, 0.2, 0.2, 0.1
            );
        }
    }
    
    private void spawnChainParticles(ServerLevel level, LivingEntity from, LivingEntity to) {
        // Line of particles between targets
        Vec3 start = from.position().add(0, from.getBbHeight() / 2, 0);
        Vec3 end = to.position().add(0, to.getBbHeight() / 2, 0);
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        for (double d = 0; d < distance; d += 0.3) {
            Vec3 pos = start.add(direction.scale(d));
            level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.x, pos.y, pos.z,
                1, 0.1, 0.1, 0.1, 0
            );
        }
    }
}
