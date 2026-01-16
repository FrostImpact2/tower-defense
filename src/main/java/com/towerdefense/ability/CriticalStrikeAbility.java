package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Critical Strike Ability
 * Next attack deals 3x damage
 * Cooldown: 6 seconds
 */
public class CriticalStrikeAbility extends AbstractTowerAbility {
    
    private static final float DAMAGE_MULTIPLIER = 3.0f;
    private boolean critReady = false;
    
    public CriticalStrikeAbility() {
        super("critical_strike", "Critical Strike", "Next attack deals 3x damage", 120); // 6 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide()) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Mark critical strike as ready
        critReady = true;
        
        // Ready particles
        for (int i = 0; i < 20; i++) {
            double offsetX = (tower.getRandom().nextDouble() - 0.5) * 2;
            double offsetY = tower.getRandom().nextDouble() * 2;
            double offsetZ = (tower.getRandom().nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                tower.getX() + offsetX,
                tower.getY() + 1 + offsetY,
                tower.getZ() + offsetZ,
                1, 0, 0, 0, 0.1
            );
        }
        
        return true;
    }
    
    /**
     * Apply critical strike damage if ready
     * Should be called from tower's attack method
     */
    public float applyCriticalStrike(BaseTowerEntity tower, LivingEntity target, float baseDamage) {
        if (!critReady) {
            return baseDamage;
        }
        
        critReady = false;
        float critDamage = baseDamage * DAMAGE_MULTIPLIER;
        
        // Critical strike particles
        if (!tower.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) tower.level();
            for (int i = 0; i < 30; i++) {
                double offsetX = (tower.getRandom().nextDouble() - 0.5) * 2;
                double offsetY = tower.getRandom().nextDouble() * target.getBbHeight();
                double offsetZ = (tower.getRandom().nextDouble() - 0.5) * 2;
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    1, 0, 0, 0, 0.3
                );
            }
        }
        
        return critDamage;
    }
    
    public boolean isCritReady() {
        return critReady;
    }
}
