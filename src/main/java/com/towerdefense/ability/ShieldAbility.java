package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shield Ability
 * Absorbs damage for 5 seconds
 * Cooldown: 20 seconds
 */
public class ShieldAbility extends AbstractTowerAbility {
    
    private static final int DURATION = 100; // 5 seconds
    private static final float SHIELD_AMOUNT = 50.0f;
    
    private int remainingDuration = 0;
    private float remainingShield = 0;
    
    public ShieldAbility() {
        super("shield", "Shield", "Absorbs damage for 5 seconds", 400); // 20 second cooldown
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide()) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Activate shield
        remainingDuration = DURATION;
        remainingShield = SHIELD_AMOUNT;
        
        // Shield activation particles
        for (int i = 0; i < 40; i++) {
            double angle = (2 * Math.PI * i) / 40;
            double radius = 1.5;
            double x = tower.getX() + Math.cos(angle) * radius;
            double z = tower.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                x, tower.getY() + 1, z,
                1, 0, 0, 0, 0
            );
        }
        
        return true;
    }
    
    @Override
    public void tick(BaseTowerEntity tower) {
        super.tick(tower);
        
        if (remainingDuration > 0) {
            remainingDuration--;
            
            // Shield particles
            if (!tower.level().isClientSide() && tower.getRandom().nextFloat() < 0.5f) {
                ServerLevel serverLevel = (ServerLevel) tower.level();
                double angle = tower.getRandom().nextDouble() * 2 * Math.PI;
                double radius = 1.2;
                double x = tower.getX() + Math.cos(angle) * radius;
                double z = tower.getZ() + Math.sin(angle) * radius;
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    x, tower.getY() + 1, z,
                    1, 0, 0.3, 0, 0
                );
            }
            
            if (remainingDuration == 0) {
                remainingShield = 0;
            }
        }
    }
    
    /**
     * Absorb damage with shield
     * Should be called from tower's hurt method
     */
    public float absorbDamage(BaseTowerEntity tower, float damage) {
        if (remainingShield <= 0 || remainingDuration <= 0) {
            return damage;
        }
        
        float absorbed = Math.min(damage, remainingShield);
        remainingShield -= absorbed;
        float remaining = damage - absorbed;
        
        // Shield damage particles
        if (!tower.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) tower.level();
            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    tower.getX() + (tower.getRandom().nextDouble() - 0.5),
                    tower.getY() + 1 + tower.getRandom().nextDouble(),
                    tower.getZ() + (tower.getRandom().nextDouble() - 0.5),
                    1, 0, 0, 0, 0.1
                );
            }
        }
        
        if (remainingShield <= 0) {
            remainingDuration = 0;
        }
        
        return remaining;
    }
    
    public boolean isShieldActive() {
        return remainingDuration > 0 && remainingShield > 0;
    }
    
    public float getRemainingShield() {
        return remainingShield;
    }
}
