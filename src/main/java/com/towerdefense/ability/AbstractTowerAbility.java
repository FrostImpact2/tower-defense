package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Abstract base implementation for tower abilities.
 * Provides common cooldown management and basic structure.
 */
public abstract class AbstractTowerAbility implements TowerAbility {
    
    protected final String id;
    protected final String name;
    protected final String description;
    protected final int maxCooldown;
    protected int currentCooldown;
    
    public AbstractTowerAbility(String id, String name, String description, int maxCooldown) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxCooldown = maxCooldown;
        this.currentCooldown = 0;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getCooldown() {
        return maxCooldown;
    }
    
    @Override
    public boolean isOnCooldown() {
        return currentCooldown > 0;
    }
    
    @Override
    public int getCurrentCooldown() {
        return currentCooldown;
    }
    
    @Override
    public boolean activate(BaseTowerEntity tower, LivingEntity target) {
        if (isOnCooldown()) {
            return false;
        }
        
        boolean success = doActivate(tower, target);
        if (success) {
            currentCooldown = maxCooldown;
        }
        return success;
    }
    
    /**
     * Implement the actual ability effect
     * @param tower The tower using this ability
     * @param target Optional target for the ability
     * @return true if the ability was successfully executed
     */
    protected abstract boolean doActivate(BaseTowerEntity tower, LivingEntity target);
    
    @Override
    public void tick(BaseTowerEntity tower) {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }
    
    @Override
    public void reset() {
        currentCooldown = 0;
    }
}
