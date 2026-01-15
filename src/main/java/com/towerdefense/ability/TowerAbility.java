package com.towerdefense.ability;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Base interface for tower abilities.
 * Abilities can be active (triggered by player) or passive (automatic effects).
 */
public interface TowerAbility {
    
    /**
     * @return The unique identifier for this ability
     */
    String getId();
    
    /**
     * @return The display name for this ability
     */
    String getName();
    
    /**
     * @return Description of what this ability does
     */
    String getDescription();
    
    /**
     * @return Cooldown time in ticks (20 ticks = 1 second)
     */
    int getCooldown();
    
    /**
     * @return Whether this ability is currently on cooldown
     */
    boolean isOnCooldown();
    
    /**
     * @return Current cooldown remaining in ticks
     */
    int getCurrentCooldown();
    
    /**
     * Activate this ability
     * @param tower The tower using this ability
     * @param target Optional target for the ability (can be null for AoE)
     * @return true if the ability was successfully activated
     */
    boolean activate(BaseTowerEntity tower, LivingEntity target);
    
    /**
     * Called every tick to update cooldowns and passive effects
     * @param tower The tower with this ability
     */
    void tick(BaseTowerEntity tower);
    
    /**
     * Reset the ability state (e.g., when tower is upgraded)
     */
    void reset();
}
