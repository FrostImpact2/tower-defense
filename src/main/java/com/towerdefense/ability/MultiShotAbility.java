package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Example ability: Multi-Shot
 * Fires multiple arrows at once to hit multiple targets.
 */
public class MultiShotAbility extends AbstractTowerAbility {
    
    private final int arrowCount;
    
    public MultiShotAbility() {
        super("multi_shot", "Multi-Shot", "Fire multiple arrows at nearby enemies", 200); // 10 second cooldown
        this.arrowCount = 3;
    }
    
    @Override
    protected boolean doActivate(BaseTowerEntity tower, LivingEntity target) {
        if (tower.level().isClientSide()) {
            return false;
        }
        
        ServerLevel serverLevel = (ServerLevel) tower.level();
        
        // Spawn particles for visual effect
        for (int i = 0; i < 20; i++) {
            double offsetX = (tower.getRandom().nextDouble() - 0.5) * 2;
            double offsetY = tower.getRandom().nextDouble() * 2;
            double offsetZ = (tower.getRandom().nextDouble() - 0.5) * 2;
            serverLevel.sendParticles(
                ParticleTypes.CRIT,
                tower.getX() + offsetX,
                tower.getY() + 1 + offsetY,
                tower.getZ() + offsetZ,
                1, 0, 0, 0, 0.1
            );
        }
        
        // Attack multiple targets
        tower.performMultiAttack(arrowCount);
        
        return true;
    }
}
