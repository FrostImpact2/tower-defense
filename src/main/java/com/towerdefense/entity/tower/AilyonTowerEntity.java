package com.towerdefense.entity.tower;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.towerdefense.ability.BlinkAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

/**
 * Ailyon, the Skirmisher - A ranged DPS assassin tower
 * 
 * Role: Ranged DPS, Assassin | Difficulty: 6/7
 * - High mobility with Blink
 * - Single target focus (aggro limit: 1)
 * - Attack while moving capability
 * - Spectral charge mechanic for empowered shots
 * 
 * Appearance: Spectral/ethereal theme with purple-cyan leather armor and custom skull
 * Abilities:
 * 1. Basic Attack: Piercing Bolt - Standard projectile attack
 * 2. Blink (Move Skill) - Teleport to destination if within 8 blocks
 * 3. Recollection (Passive) - Drop spectral charges on kill, empower next attack
 * 4. Skilled Shooting (Passive) - Can attack while moving, 50% slower attack speed when moving
 */
public class AilyonTowerEntity extends BaseTowerEntity {

    // Base stats constants
    private static final float BASE_DAMAGE = 12.0f;
    private static final float BASE_RANGE = 8.0f;
    private static final float BASE_ATTACK_SPEED = 1.2f;
    private static final float BASE_MAX_HEALTH = 70.0f;
    private static final int BASE_AGGRO_LIMIT = 1;
    private static final double BASE_MOVEMENT_SPEED = 0.32D;

    // Custom skull texture for spectral/ethereal appearance (spectral skull from Minecraft texture database)
    private static final String AILYON_SKULL_TEXTURE = 
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y" +
        "3NjU3NzQ4MjM5OTBmNzQ3ZmNhYWE2YWZhZjJmN2QyZDI1ZWMxYTJhYzc0OGE3NWU0ZjNmZGI5YzU3MjYyZiJ9fX0=";
    
    // Spectral charge tracking
    private int spectralCharges = 0;
    private static final int MAX_SPECTRAL_CHARGES = 4;
    
    // Track if next attack is empowered
    private boolean isNextAttackEmpowered = false;
    
    public AilyonTowerEntity(EntityType<? extends AilyonTowerEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected TowerStats createDefaultStats() {
        return new TowerStats(
            BASE_DAMAGE,        // damage - high single target damage
            BASE_RANGE,         // range - medium range for assassin playstyle
            BASE_ATTACK_SPEED,  // attacks per second - moderate attack speed
            BASE_MAX_HEALTH,    // max health - low health, glass cannon
            BASE_AGGRO_LIMIT    // aggro limit - assassin focuses on single targets
        );
    }

    @Override
    protected void initializeAbilities() {
        // Add Blink as a passive ability
        abilities.add(new BlinkAbility());
        // Note: Recollection and Skilled Shooting are implemented as passive mechanics
        // in the entity logic rather than as separate ability objects
        // This provides tighter integration with movement and combat systems
    }
    
    @Override
    protected boolean handleCustomMovement(BlockPos target) {
        // Blink: If destination is within 8 blocks, teleport instead of walking
        if (BlinkAbility.canBlinkTo(this, target)) {
            BlinkAbility.performBlink(this, target);
            return true;
        }
        return false;
    }

    @Override
    protected void setupAppearance() {
        // Spectral-themed skull head
        ItemStack helmet = createCustomSkullHead();
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        // Apply purple-cyan spectral color to leather armor
        // Mix of purple (RGB: 138, 43, 226) and cyan (RGB: 0, 255, 255)
        // Result: Spectral purple-cyan (RGB: 100, 70, 220)
        int spectralColor = (100 << 16) | (70 << 8) | 220; // 0x6446DC

        chest.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(spectralColor, true));
        legs.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(spectralColor, true));
        boots.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(spectralColor, true));

        this.setItemSlot(EquipmentSlot.HEAD, helmet);
        this.setItemSlot(EquipmentSlot.CHEST, chest);
        this.setItemSlot(EquipmentSlot.LEGS, legs);
        this.setItemSlot(EquipmentSlot.FEET, boots);
        
        // Crossbow in main hand for ranged attacks
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    /**
     * Create a player head with custom spectral texture
     */
    private ItemStack createCustomSkullHead() {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        
        // Create a game profile with the texture
        GameProfile profile = new GameProfile(UUID.randomUUID(), "AilyonTower");
        profile.getProperties().put("textures", new Property("textures", AILYON_SKULL_TEXTURE));
        
        // Set the profile on the skull
        skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        
        return skull;
    }

    @Override
    public String getTowerName() {
        return "Ailyon, the Skirmisher";
    }

    @Override
    public String getTowerRole() {
        return "Assassin";
    }

    @Override
    public void tick() {
        super.tick();
        
        // Spawn spectral charge particles if we have charges
        if (level().isClientSide() && spectralCharges > 0 && tickCount % 10 == 0) {
            spawnSpectralChargeParticles();
        }
    }
    
    /**
     * Spawn particles showing spectral charges
     */
    private void spawnSpectralChargeParticles() {
        for (int i = 0; i < spectralCharges; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SPECTRAL_CHARGES;
            double radius = 0.5;
            double xOffset = Math.cos(angle + tickCount * 0.05) * radius;
            double zOffset = Math.sin(angle + tickCount * 0.05) * radius;
            
            level().addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                getX() + xOffset,
                getY() + 1.5,
                getZ() + zOffset,
                0, 0.02, 0
            );
        }
    }

    @Override
    public void performAttack(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        
        // Skilled Shooting: Adjust attack cooldown if moving
        int baseAttackInterval = (int)(20 / stats.getAttackSpeed());
        int attackInterval = baseAttackInterval;
        
        if (isMoving()) {
            // 50% slower when moving (multiply by 1.5)
            attackInterval = (int)(baseAttackInterval * 1.5f);
        }
        
        attackCooldown = attackInterval;
        
        // Calculate damage
        float damage = stats.getDamage();
        
        // Recollection: Empower attack if we have charges
        if (spectralCharges > 0) {
            isNextAttackEmpowered = true;
            damage *= 1.5f; // 150% damage
            spectralCharges--;
            
            // If we had extra charges (more than 1), reset cooldowns
            if (spectralCharges > 0) {
                attackCooldown = 0; // Reset attack cooldown
                // Note: Move skill cooldown would also be reset here
            }
        }
        
        // Apply damage
        if (isNextAttackEmpowered && !level().isClientSide()) {
            // Empowered shot: Pierce through multiple enemies
            performPiercingAttack(target, damage);
            isNextAttackEmpowered = false;
        } else {
            // Normal attack: Single target
            target.hurt(damageSources().mobAttack(this), damage);
        }
        
        // Trigger attack animation
        entityData.set(ATTACK_ANIMATION_TICK, 10);
        
        // Spawn attack particles (spectral themed)
        spawnSpectralAttackParticles(target);
    }
    
    /**
     * Perform a piercing attack that hits multiple enemies in a line
     */
    private void performPiercingAttack(LivingEntity primaryTarget, float damage) {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) return;
        
        // Get direction to primary target
        double dx = primaryTarget.getX() - getX();
        double dy = primaryTarget.getY() - getY();
        double dz = primaryTarget.getZ() - getZ();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (length == 0) return;
        
        dx /= length;
        dy /= length;
        dz /= length;
        
        // Hit all enemies in a line
        var enemies = serverLevel.getEntitiesOfClass(
            net.minecraft.world.entity.Mob.class,
            getBoundingBox().inflate(stats.getRange()),
            entity -> entity != this && entity.isAlive()
        );
        
        for (var enemy : enemies) {
            // Check if enemy is roughly in the line of fire
            double ex = enemy.getX() - getX();
            double ey = enemy.getY() - getY();
            double ez = enemy.getZ() - getZ();
            double elen = Math.sqrt(ex * ex + ey * ey + ez * ez);
            
            if (elen == 0) continue;
            
            ex /= elen;
            ey /= elen;
            ez /= elen;
            
            // Dot product to check alignment
            double dot = dx * ex + dy * ey + dz * ez;
            
            if (dot > 0.8) { // Within cone
                enemy.hurt(damageSources().mobAttack(this), damage);
                spawnSpectralAttackParticles(enemy);
            }
        }
    }
    
    /**
     * Spawn spectral-themed attack particles
     */
    @Override
    protected void spawnAttackParticles(LivingEntity target) {
        spawnSpectralAttackParticles(target);
    }
    
    private void spawnSpectralAttackParticles(LivingEntity target) {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) return;
        
        // Line of spectral particles from tower to target
        var start = position().add(0, 1.5, 0);
        var end = target.position().add(0, target.getBbHeight() / 2, 0);
        var direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        for (double d = 0; d < distance; d += 0.3) {
            var pos = start.add(direction.scale(d));
            serverLevel.sendParticles(
                ParticleTypes.ENCHANTED_HIT,
                pos.x, pos.y, pos.z,
                2, 0.05, 0.05, 0.05, 0
            );
        }
    }
    
    /**
     * Called when this tower kills an enemy
     * Implements Recollection: Drop spectral charge
     */
    public void onKillEnemy() {
        if (level().isClientSide()) return;
        
        if (spectralCharges < MAX_SPECTRAL_CHARGES) {
            spectralCharges++;
            
            // Visual feedback
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY() + 1, getZ(),
                    10, 0.3, 0.5, 0.3, 0.05
                );
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BaseTowerEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED) // Fast movement speed
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SpectralCharges", spectralCharges);
        compound.putBoolean("IsNextAttackEmpowered", isNextAttackEmpowered);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        spectralCharges = compound.getInt("SpectralCharges");
        isNextAttackEmpowered = compound.getBoolean("IsNextAttackEmpowered");
    }
}
