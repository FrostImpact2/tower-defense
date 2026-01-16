package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Rapid Fire Ability
 * Temporarily increases attack speed by 200% for 5 seconds
 * Cooldown: 15 seconds
 */
public class RapidFireAbility extends AbstractTowerAbility {
    
    private static final int DURATION = 100; // 5 seconds
    private static final float SPEED_MULTIPLIER = 3.0f;
    
    private int remainingDuration = 0;
    private float originalAttackSpeed = 0;
    
    public RapidFireAbility() {
        super("rapid_fire", "Rapid Fire", "Increases attack speed by 200% for 5 seconds", 300); // 15 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide()) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Store original attack speed
        originalAttackSpeed = tower.getStats().getAttackSpeed();
        
        // Apply rapid fire buff
        tower.getStats().setAttackSpeed(originalAttackSpeed * SPEED_MULTIPLIER);
        remainingDuration = DURATION;
        
        // Spawn particles
        for (int i = 0; i < 30; i++) {
            double offsetX = (tower.getRandom().nextDouble() - 0.5) * 2;
            double offsetY = tower.getRandom().nextDouble() * 2;
            double offsetZ = (tower.getRandom().nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(
                ParticleTypes.FIREWORK,
                tower.getX() + offsetX,
                tower.getY() + 1 + offsetY,
                tower.getZ() + offsetZ,
                1, 0, 0, 0, 0.1
            );
        }
        
        return true;
    }
    
    @Override
    public void tick(BaseTowerEntity tower) {
        super.tick(tower);
        
        if (remainingDuration > 0) {
            remainingDuration--;
            
            // Spawn continuous particles during rapid fire
            if (!tower.level().isClientSide() && tower.getRandom().nextFloat() < 0.3f) {
                ServerLevel serverLevel = (ServerLevel) tower.level();
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    tower.getX(),
                    tower.getY() + 1,
                    tower.getZ(),
                    2, 0.3, 0.5, 0.3, 0
                );
            }
            
            // End rapid fire
            if (remainingDuration == 0 && originalAttackSpeed > 0) {
                tower.getStats().setAttackSpeed(originalAttackSpeed);
            }
        }
    }
}
