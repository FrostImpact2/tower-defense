package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Heal Aura Ability
 * Heals nearby towers over time
 * Passive effect with cooldown
 */
public class HealAuraAbility extends AbstractTowerAbility {
    
    private static final float HEAL_RADIUS = 10.0f;
    private static final float HEAL_AMOUNT = 20.0f;
    
    public HealAuraAbility() {
        super("heal_aura", "Heal Aura", "Heals nearby towers", 400); // 20 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide()) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Find nearby towers
        AABB healBox = new AABB(
            tower.getX() - HEAL_RADIUS, tower.getY() - HEAL_RADIUS, tower.getZ() - HEAL_RADIUS,
            tower.getX() + HEAL_RADIUS, tower.getY() + HEAL_RADIUS, tower.getZ() + HEAL_RADIUS
        );
        
        List<BaseTowerEntity> nearbyTowers = serverLevel.getEntitiesOfClass(BaseTowerEntity.class, healBox);
        
        int healed = 0;
        for (BaseTowerEntity nearbyTower : nearbyTowers) {
            if (nearbyTower.isAlive() && nearbyTower.getHealth() < nearbyTower.getMaxHealth()) {
                nearbyTower.heal(HEAL_AMOUNT);
                healed++;
                
                // Healing particles
                for (int i = 0; i < 10; i++) {
                    double offsetX = (tower.getRandom().nextDouble() - 0.5) * 1;
                    double offsetY = tower.getRandom().nextDouble() * 2;
                    double offsetZ = (tower.getRandom().nextDouble() - 0.5) * 1;
                    serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        nearbyTower.getX() + offsetX,
                        nearbyTower.getY() + offsetY,
                        nearbyTower.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
        
        // Aura particles from caster
        for (int i = 0; i < 30; i++) {
            double angle = (2 * Math.PI * i) / 30;
            double x = tower.getX() + Math.cos(angle) * HEAL_RADIUS;
            double z = tower.getZ() + Math.sin(angle) * HEAL_RADIUS;
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                x, tower.getY() + 1, z,
                1, 0, 0.5, 0, 0
            );
        }
        
        return healed > 0;
    }
}
