package com.towerdefense.entity.tower;

/**
 * Represents the stats of a tower.
 * Used for display in GUI and combat calculations.
 */
public class TowerStats {
    private float damage;
    private float range;
    private float attackSpeed; // attacks per second
    private float maxHealth;
    private float currentHealth;
    private int aggroLimit;
    private int level;
    private int upgradeCost;
    private int sellValue;

    public TowerStats(float damage, float range, float attackSpeed, float maxHealth, int aggroLimit) {
        this.damage = damage;
        this.range = range;
        this.attackSpeed = attackSpeed;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.aggroLimit = aggroLimit;
        this.level = 1;
        this.upgradeCost = 100;
        this.sellValue = 50;
    }

    // Copy constructor
    public TowerStats(TowerStats other) {
        this.damage = other.damage;
        this.range = other.range;
        this.attackSpeed = other.attackSpeed;
        this.maxHealth = other.maxHealth;
        this.currentHealth = other.currentHealth;
        this.aggroLimit = other.aggroLimit;
        this.level = other.level;
        this.upgradeCost = other.upgradeCost;
        this.sellValue = other.sellValue;
    }

    // Getters
    public float getDamage() { return damage; }
    public float getRange() { return range; }
    public float getAttackSpeed() { return attackSpeed; }
    public float getMaxHealth() { return maxHealth; }
    public float getCurrentHealth() { return currentHealth; }
    public int getAggroLimit() { return aggroLimit; }
    public int getLevel() { return level; }
    public int getUpgradeCost() { return upgradeCost; }
    public int getSellValue() { return sellValue; }

    // Setters
    public void setDamage(float damage) { this.damage = damage; }
    public void setRange(float range) { this.range = range; }
    public void setAttackSpeed(float attackSpeed) { this.attackSpeed = attackSpeed; }
    public void setMaxHealth(float maxHealth) { this.maxHealth = maxHealth; }
    public void setCurrentHealth(float currentHealth) { 
        this.currentHealth = Math.min(currentHealth, maxHealth); 
    }
    public void setAggroLimit(int aggroLimit) { this.aggroLimit = aggroLimit; }
    public void setLevel(int level) { this.level = level; }
    public void setUpgradeCost(int upgradeCost) { this.upgradeCost = upgradeCost; }
    public void setSellValue(int sellValue) { this.sellValue = sellValue; }

    /**
     * Apply upgrade multipliers to stats
     */
    public void applyUpgrade(float damageMultiplier, float healthMultiplier, float rangeMultiplier) {
        this.damage *= damageMultiplier;
        this.maxHealth *= healthMultiplier;
        this.currentHealth = this.maxHealth;
        this.range *= rangeMultiplier;
        this.level++;
        this.upgradeCost = (int)(this.upgradeCost * 1.5f);
        this.sellValue = (int)(this.sellValue * 1.3f);
    }

    /**
     * Take damage and return true if tower is destroyed
     */
    public boolean takeDamage(float amount) {
        this.currentHealth -= amount;
        return this.currentHealth <= 0;
    }

    /**
     * Heal the tower
     */
    public void heal(float amount) {
        this.currentHealth = Math.min(this.currentHealth + amount, this.maxHealth);
    }
}
