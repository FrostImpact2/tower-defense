package com.towerdefense.entity.tower;

import com.towerdefense.ability.TowerAbility;
import com.towerdefense.entity.enemy.BaseEnemyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Base class for all tower entities.
 * Towers are PathfinderMob entities that look like armor stands and can:
 * - Attack enemies within range
 * - Be moved by clicking a "Move" button then clicking a destination
 * - Be upgraded, sold, and have abilities
 * - Aggro enemies when blocking their path
 */
public abstract class BaseTowerEntity extends PathfinderMob {

    // Synced data
    private static final EntityDataAccessor<Boolean> IS_MOVING = SynchedEntityData.defineId(BaseTowerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_TICK = SynchedEntityData.defineId(BaseTowerEntity.class, EntityDataSerializers.INT);

    // Tower state
    protected TowerStats stats;
    protected BlockPos moveTarget;
    protected boolean isInMoveMode;
    protected int attackCooldown;
    protected LivingEntity currentTarget;
    protected final Set<UUID> aggroedEnemies;
    protected final List<TowerAbility> abilities;

    public BaseTowerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.stats = createDefaultStats();
        this.aggroedEnemies = new HashSet<>();
        this.abilities = new ArrayList<>();
        this.isInMoveMode = false;
        this.attackCooldown = 0;
        
        // Initialize abilities
        initializeAbilities();
        
        // Make the entity look like an armor stand by setting equipment
        setupAppearance();
    }

    /**
     * Create the default stats for this tower type
     */
    protected abstract TowerStats createDefaultStats();

    /**
     * Initialize abilities for this tower type
     */
    protected abstract void initializeAbilities();

    /**
     * Setup the armor and held items for this tower's appearance
     */
    protected abstract void setupAppearance();

    /**
     * Get the tower's display name
     */
    public abstract String getTowerName();

    /**
     * Get the tower's type/role (e.g., "Archer", "Tank", "Mage")
     */
    public abstract String getTowerRole();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_MOVING, false);
        builder.define(ATTACK_ANIMATION_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TowerMoveGoal(this));
        this.goalSelector.addGoal(2, new TowerAttackGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide()) {
            // Update attack cooldown
            if (attackCooldown > 0) {
                attackCooldown--;
            }
            
            // Update abilities
            for (TowerAbility ability : abilities) {
                ability.tick(this);
            }
            
            // Find and attack targets
            if (attackCooldown <= 0) {
                findAndAttackTarget();
            }
            
            // Update animation
            int animTick = entityData.get(ATTACK_ANIMATION_TICK);
            if (animTick > 0) {
                entityData.set(ATTACK_ANIMATION_TICK, animTick - 1);
            }
        } else {
            // Client-side animation updates
            updateClientAnimation();
        }
    }

    /**
     * Find enemies in range and attack them
     */
    protected void findAndAttackTarget() {
        if (level().isClientSide()) return;
        
        ServerLevel serverLevel = (ServerLevel) level();
        AABB searchBox = new AABB(
            getX() - stats.getRange(), getY() - 2, getZ() - stats.getRange(),
            getX() + stats.getRange(), getY() + 4, getZ() + stats.getRange()
        );
        
        List<BaseEnemyEntity> enemies = serverLevel.getEntitiesOfClass(BaseEnemyEntity.class, searchBox);
        
        if (!enemies.isEmpty()) {
            // Sort by distance, attack closest
            enemies.sort((a, b) -> Double.compare(
                a.distanceToSqr(this),
                b.distanceToSqr(this)
            ));
            
            currentTarget = enemies.get(0);
            performAttack(currentTarget);
        }
    }

    /**
     * Perform an attack on the target
     */
    public void performAttack(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        
        // Calculate attack interval in ticks (20 ticks = 1 second)
        int attackInterval = (int)(20 / stats.getAttackSpeed());
        attackCooldown = attackInterval;
        
        // Apply damage
        target.hurt(damageSources().mobAttack(this), stats.getDamage());
        
        // Trigger attack animation
        entityData.set(ATTACK_ANIMATION_TICK, 10);
        
        // Spawn attack particles
        spawnAttackParticles(target);
    }

    /**
     * Perform a multi-attack hitting multiple targets
     */
    public void performMultiAttack(int targetCount) {
        if (level().isClientSide()) return;
        
        ServerLevel serverLevel = (ServerLevel) level();
        AABB searchBox = new AABB(
            getX() - stats.getRange(), getY() - 2, getZ() - stats.getRange(),
            getX() + stats.getRange(), getY() + 4, getZ() + stats.getRange()
        );
        
        List<BaseEnemyEntity> enemies = serverLevel.getEntitiesOfClass(BaseEnemyEntity.class, searchBox);
        
        int attacked = 0;
        for (BaseEnemyEntity enemy : enemies) {
            if (attacked >= targetCount) break;
            
            enemy.hurt(damageSources().mobAttack(this), stats.getDamage());
            spawnAttackParticles(enemy);
            attacked++;
        }
        
        entityData.set(ATTACK_ANIMATION_TICK, 15);
    }

    /**
     * Spawn particles for attack animation
     */
    protected void spawnAttackParticles(LivingEntity target) {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) return;
        
        // Line of particles from tower to target
        Vec3 start = position().add(0, 1.5, 0);
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        for (double d = 0; d < distance; d += 0.5) {
            Vec3 pos = start.add(direction.scale(d));
            serverLevel.sendParticles(
                ParticleTypes.CRIT,
                pos.x, pos.y, pos.z,
                1, 0.1, 0.1, 0.1, 0
            );
        }
    }

    /**
     * Client-side animation update
     */
    protected void updateClientAnimation() {
        int animTick = entityData.get(ATTACK_ANIMATION_TICK);
        if (animTick > 0) {
            // Animate arm swing
            // This would be implemented with custom rendering
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Tower selection is now handled client-side via KeyInputHandler
        // No GUI opens server-side anymore - side panel is client-only
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    /**
     * Start moving to a target block position
     */
    public void startMovingTo(BlockPos target) {
        this.moveTarget = target;
        this.isInMoveMode = true;
        entityData.set(IS_MOVING, true);
        
        // Use pathfinding to navigate
        PathNavigation nav = getNavigation();
        nav.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
    }

    /**
     * Stop movement
     */
    public void stopMoving() {
        this.moveTarget = null;
        this.isInMoveMode = false;
        entityData.set(IS_MOVING, false);
        getNavigation().stop();
    }

    /**
     * Check if movement is complete
     */
    public boolean hasReachedMoveTarget() {
        if (moveTarget == null) return true;
        
        double dist = distanceToSqr(
            moveTarget.getX() + 0.5,
            moveTarget.getY(),
            moveTarget.getZ() + 0.5
        );
        return dist < 1.5;
    }

    /**
     * Add an enemy to the aggro list
     * @return true if the enemy was added, false if aggro limit reached
     */
    public boolean addAggroedEnemy(BaseEnemyEntity enemy) {
        // Clean up dead enemies periodically (only on server)
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            aggroedEnemies.removeIf(uuid -> {
                Entity entity = serverLevel.getEntity(uuid);
                return entity == null || !entity.isAlive();
            });
        }
        
        if (aggroedEnemies.size() >= stats.getAggroLimit()) {
            return false;
        }
        
        aggroedEnemies.add(enemy.getUUID());
        return true;
    }

    /**
     * Remove an enemy from the aggro list
     */
    public void removeAggroedEnemy(BaseEnemyEntity enemy) {
        aggroedEnemies.remove(enemy.getUUID());
    }

    /**
     * Check if we can aggro more enemies
     */
    public boolean canAggroMore() {
        return aggroedEnemies.size() < stats.getAggroLimit();
    }

    /**
     * Upgrade the tower
     */
    public void upgrade() {
        stats.applyUpgrade(1.2f, 1.15f, 1.1f);
        
        // Visual feedback
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                getX(), getY() + 1, getZ(),
                30, 0.5, 1, 0.5, 0.1
            );
        }
    }

    /**
     * Get the sell value
     */
    public int getSellValue() {
        return stats.getSellValue();
    }

    /**
     * Sell the tower (remove and return currency)
     */
    public void sell() {
        // Spawn particles
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                getX(), getY() + 1, getZ(),
                20, 0.5, 0.5, 0.5, 0.1
            );
        }
        
        // Remove entity
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Towers take reduced knockback
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            stats.takeDamage(amount);
            
            if (stats.getCurrentHealth() <= 0) {
                this.kill();
            }
        }
        return hurt;
    }

    // Getters
    public TowerStats getStats() { return stats; }
    public List<TowerAbility> getAbilities() { return abilities; }
    public boolean isMoving() { return entityData.get(IS_MOVING); }
    public BlockPos getMoveTarget() { return moveTarget; }
    public int getAttackAnimationTick() { return entityData.get(ATTACK_ANIMATION_TICK); }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        
        // Save tower stats
        CompoundTag statsTag = new CompoundTag();
        statsTag.putFloat("damage", stats.getDamage());
        statsTag.putFloat("range", stats.getRange());
        statsTag.putFloat("attackSpeed", stats.getAttackSpeed());
        statsTag.putFloat("maxHealth", stats.getMaxHealth());
        statsTag.putFloat("currentHealth", stats.getCurrentHealth());
        statsTag.putInt("aggroLimit", stats.getAggroLimit());
        statsTag.putInt("level", stats.getLevel());
        statsTag.putInt("upgradeCost", stats.getUpgradeCost());
        statsTag.putInt("sellValue", stats.getSellValue());
        compound.put("TowerStats", statsTag);
        
        // Save move target
        if (moveTarget != null) {
            compound.putInt("MoveTargetX", moveTarget.getX());
            compound.putInt("MoveTargetY", moveTarget.getY());
            compound.putInt("MoveTargetZ", moveTarget.getZ());
        }
        
        // Save aggroed enemies
        ListTag aggroList = new ListTag();
        for (UUID uuid : aggroedEnemies) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("UUID", uuid);
            aggroList.add(uuidTag);
        }
        compound.put("AggroedEnemies", aggroList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        
        // Load tower stats
        if (compound.contains("TowerStats")) {
            CompoundTag statsTag = compound.getCompound("TowerStats");
            stats.setDamage(statsTag.getFloat("damage"));
            stats.setRange(statsTag.getFloat("range"));
            stats.setAttackSpeed(statsTag.getFloat("attackSpeed"));
            stats.setMaxHealth(statsTag.getFloat("maxHealth"));
            stats.setCurrentHealth(statsTag.getFloat("currentHealth"));
            stats.setAggroLimit(statsTag.getInt("aggroLimit"));
            stats.setLevel(statsTag.getInt("level"));
            stats.setUpgradeCost(statsTag.getInt("upgradeCost"));
            stats.setSellValue(statsTag.getInt("sellValue"));
        }
        
        // Load move target
        if (compound.contains("MoveTargetX")) {
            moveTarget = new BlockPos(
                compound.getInt("MoveTargetX"),
                compound.getInt("MoveTargetY"),
                compound.getInt("MoveTargetZ")
            );
        }
        
        // Load aggroed enemies
        if (compound.contains("AggroedEnemies")) {
            ListTag aggroList = compound.getList("AggroedEnemies", 10);
            for (int i = 0; i < aggroList.size(); i++) {
                CompoundTag uuidTag = aggroList.getCompound(i);
                aggroedEnemies.add(uuidTag.getUUID("UUID"));
            }
        }
    }

    /**
     * Goal for moving to a target position
     */
    static class TowerMoveGoal extends Goal {
        private final BaseTowerEntity tower;

        public TowerMoveGoal(BaseTowerEntity tower) {
            this.tower = tower;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return tower.isInMoveMode && tower.moveTarget != null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && !tower.hasReachedMoveTarget();
        }

        @Override
        public void start() {
            if (tower.moveTarget != null) {
                tower.getNavigation().moveTo(
                    tower.moveTarget.getX() + 0.5,
                    tower.moveTarget.getY(),
                    tower.moveTarget.getZ() + 0.5,
                    1.0
                );
            }
        }

        @Override
        public void stop() {
            tower.stopMoving();
        }

        @Override
        public void tick() {
            if (tower.hasReachedMoveTarget()) {
                tower.stopMoving();
            }
        }
    }

    /**
     * Goal for attacking enemies
     */
    static class TowerAttackGoal extends Goal {
        private final BaseTowerEntity tower;

        public TowerAttackGoal(BaseTowerEntity tower) {
            this.tower = tower;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return tower.currentTarget != null && tower.currentTarget.isAlive();
        }

        @Override
        public void tick() {
            if (tower.currentTarget != null && tower.currentTarget.isAlive()) {
                tower.getLookControl().setLookAt(tower.currentTarget);
            }
        }
    }
}
