package com.towerdefense.ability;

import com.towerdefense.entity.tower.AilyonTowerEntity;
import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Blink Ability for Ailyon, the Skirmisher
 * 
 * On Move: If the destination is within 8 blocks (tower's range),
 * instantly teleport to that location instead of walking.
 * 
 * This is a passive ability that modifies the tower's movement behavior.
 */
public class BlinkAbility extends AbstractTowerAbility {
    
    private static final double BLINK_RANGE = 8.0;
    
    public BlinkAbility() {
        super("blink", "Blink", "Teleport to destination if within 8 blocks", 0);
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        // This ability is passive and modifies movement behavior
        // Not directly activated
        return false;
    }
    
    /**
     * Check if the tower can blink to the target position
     */
    public static boolean canBlinkTo(BaseTowerEntity tower, BlockPos target) {
        if (!(tower instanceof AilyonTowerEntity)) {
            return false;
        }
        
        double distance = tower.position().distanceTo(
            new Vec3(target.getX() + 0.5, target.getY(), target.getZ() + 0.5)
        );
        
        return distance <= BLINK_RANGE;
    }
    
    /**
     * Perform the blink teleport
     */
    public static void performBlink(BaseTowerEntity tower, BlockPos target) {
        if (tower.level().isClientSide() || !(tower instanceof AilyonTowerEntity)) {
            return;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Store old position for particles
        Vec3 oldPos = tower.position();
        
        // Teleport to target
        tower.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        
        // Spawn particles at old location (departure)
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            oldPos.x, oldPos.y + 1, oldPos.z,
            30, 0.3, 0.5, 0.3, 0.5
        );
        
        serverLevel.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            oldPos.x, oldPos.y + 1, oldPos.z,
            15, 0.2, 0.3, 0.2, 0.1
        );
        
        // Spawn particles at new location (arrival)
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            tower.getX(), tower.getY() + 1, tower.getZ(),
            30, 0.3, 0.5, 0.3, 0.5
        );
        
        serverLevel.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            tower.getX(), tower.getY() + 1, tower.getZ(),
            15, 0.2, 0.3, 0.2, 0.1
        );
        
        // Stop movement since we've already arrived
        tower.stopMoving();
    }
}
