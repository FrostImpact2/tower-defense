package com.towerdefense.ability;

import com.towerdefense.entity.enemy.BaseEnemyEntity;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Explosive Shot Ability
 * Fires a projectile that explodes on impact, dealing AoE damage
 * Cooldown: 8 seconds
 */
public class ExplosiveShotAbility extends AbstractTowerAbility {
    
    private static final float AOE_RADIUS = 5.0f;
    private static final float DAMAGE_MULTIPLIER = 1.5f;
    
    public ExplosiveShotAbility() {
        super("explosive_shot", "Explosive Shot", "Projectile explodes dealing AoE damage", 160); // 8 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide() || target == null) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Damage primary target
        float damage = tower.getStats().getDamage() * DAMAGE_MULTIPLIER;
        target.hurt(serverLevel.damageSources().explosion(tower, tower), damage);
        
        // Explosion particles
        for (int i = 0; i < 50; i++) {
            double offsetX = (tower.getRandom().nextDouble() - 0.5) * AOE_RADIUS * 2;
            double offsetY = tower.getRandom().nextDouble() * AOE_RADIUS;
            double offsetZ = (tower.getRandom().nextDouble() - 0.5) * AOE_RADIUS * 2;
            serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                target.getX() + offsetX,
                target.getY() + offsetY,
                target.getZ() + offsetZ,
                1, 0, 0, 0, 0
            );
            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                target.getX() + offsetX,
                target.getY() + offsetY,
                target.getZ() + offsetZ,
                2, 0.1, 0.1, 0.1, 0.05
            );
        }
        
        // Find and damage nearby enemies
        AABB explosionBox = new AABB(
            target.getX() - AOE_RADIUS, target.getY() - AOE_RADIUS, target.getZ() - AOE_RADIUS,
            target.getX() + AOE_RADIUS, target.getY() + AOE_RADIUS, target.getZ() + AOE_RADIUS
        );
        
        List<BaseEnemyEntity> nearbyEnemies = serverLevel.getEntitiesOfClass(BaseEnemyEntity.class, explosionBox);
        for (BaseEnemyEntity enemy : nearbyEnemies) {
            if (enemy != target && enemy.isAlive()) {
                float aoeDamage = damage * 0.5f; // Half damage to nearby enemies
                enemy.hurt(serverLevel.damageSources().explosion(tower, tower), aoeDamage);
            }
        }
        
        return true;
    }
}
