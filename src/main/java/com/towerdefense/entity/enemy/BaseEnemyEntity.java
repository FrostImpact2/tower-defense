package com.towerdefense.entity.enemy;

import com.towerdefense.entity.tower.BaseTowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Base class for all enemy entities in tower defense.
 * 
 * Enemies follow a predefined path and will attack towers that block their way.
 * They don't "target" towers traditionally - instead:
 * 1. They follow their path normally
 * 2. If blocked by a tower, they attack it
 * 3. Each enemy can only aggro one tower at a time
 */
public abstract class BaseEnemyEntity extends PathfinderMob {

    // Synced data
    private static final EntityDataAccessor<Boolean> IS_ATTACKING_TOWER = SynchedEntityData.defineId(BaseEnemyEntity.class, EntityDataSerializers.BOOLEAN);

    // Enemy state
    protected float damage;
    protected float attackSpeed;
    protected int reward; // Currency given when killed
    protected List<BlockPos> pathWaypoints;
    protected int currentWaypointIndex;
    protected BaseTowerEntity blockingTower;
    protected int attackCooldown;

    public BaseEnemyEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.pathWaypoints = new ArrayList<>();
        this.currentWaypointIndex = 0;
        this.attackCooldown = 0;
        
        initializeStats();
    }

    /**
     * Initialize stats for this enemy type
     */
    protected abstract void initializeStats();

    /**
     * Get the enemy's display name
     */
    public abstract String getEnemyName();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ATTACKING_TOWER, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new EnemyAttackTowerGoal(this));
        this.goalSelector.addGoal(2, new FollowPathGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide()) {
            // Update attack cooldown
            if (attackCooldown > 0) {
                attackCooldown--;
            }
            
            // Check for blocking towers
            checkForBlockingTower();
            
            // Attack blocking tower if present
            if (blockingTower != null && blockingTower.isAlive() && attackCooldown <= 0) {
                attackTower();
            }
        }
    }

    /**
     * Check if there's a tower blocking our path
     */
    protected void checkForBlockingTower() {
        if (blockingTower != null && blockingTower.isAlive()) {
            // Already have a valid blocking tower
            return;
        }
        
        // Clear old blocking tower
        if (blockingTower != null) {
            blockingTower.removeAggroedEnemy(this);
            blockingTower = null;
        }
        
        // Look for towers in our immediate path
        Vec3 lookDirection = getLookAngle().normalize();
        AABB searchBox = getBoundingBox().inflate(2.0);
        
        List<BaseTowerEntity> nearbyTowers = level().getEntitiesOfClass(
            BaseTowerEntity.class, 
            searchBox,
            tower -> tower.isAlive() && canTargetTower(tower)
        );
        
        if (!nearbyTowers.isEmpty()) {
            // Find the closest tower that can accept aggro
            for (BaseTowerEntity tower : nearbyTowers) {
                if (tower.addAggroedEnemy(this)) {
                    blockingTower = tower;
                    entityData.set(IS_ATTACKING_TOWER, true);
                    break;
                }
            }
        }
    }

    /**
     * Check if we can target this tower
     */
    protected boolean canTargetTower(BaseTowerEntity tower) {
        // Tower must be alive and close enough
        return tower.isAlive() && distanceToSqr(tower) < 9.0; // Within 3 blocks
    }

    /**
     * Attack the blocking tower
     */
    protected void attackTower() {
        if (blockingTower == null || !blockingTower.isAlive()) {
            clearBlockingTower();
            return;
        }
        
        // Calculate attack interval
        int attackInterval = (int)(20 / attackSpeed);
        attackCooldown = attackInterval;
        
        // Deal damage
        blockingTower.hurt(damageSources().mobAttack(this), damage);
        
        // Check if tower died
        if (!blockingTower.isAlive()) {
            clearBlockingTower();
        }
    }

    /**
     * Clear the blocking tower reference
     */
    protected void clearBlockingTower() {
        if (blockingTower != null) {
            blockingTower.removeAggroedEnemy(this);
        }
        blockingTower = null;
        entityData.set(IS_ATTACKING_TOWER, false);
    }

    /**
     * Set the path waypoints for this enemy
     */
    public void setPathWaypoints(List<BlockPos> waypoints) {
        this.pathWaypoints = new ArrayList<>(waypoints);
        this.currentWaypointIndex = 0;
    }

    /**
     * Add a waypoint to the path
     */
    public void addWaypoint(BlockPos waypoint) {
        this.pathWaypoints.add(waypoint);
    }

    /**
     * Get the current waypoint
     */
    public BlockPos getCurrentWaypoint() {
        if (pathWaypoints.isEmpty() || currentWaypointIndex >= pathWaypoints.size()) {
            return null;
        }
        return pathWaypoints.get(currentWaypointIndex);
    }

    /**
     * Move to the next waypoint
     */
    public void advanceWaypoint() {
        currentWaypointIndex++;
    }

    /**
     * Check if reached the current waypoint
     */
    public boolean hasReachedWaypoint() {
        BlockPos waypoint = getCurrentWaypoint();
        if (waypoint == null) return true;
        
        double dist = distanceToSqr(
            waypoint.getX() + 0.5,
            waypoint.getY(),
            waypoint.getZ() + 0.5
        );
        return dist < 2.0;
    }

    /**
     * Check if reached the end of the path
     */
    public boolean hasReachedEnd() {
        return pathWaypoints.isEmpty() || currentWaypointIndex >= pathWaypoints.size();
    }

    @Override
    public void die(DamageSource source) {
        // Clear aggro before dying
        clearBlockingTower();
        super.die(source);
    }

    // Getters
    public float getDamageAmount() { return damage; }
    public int getReward() { return reward; }
    public boolean isAttackingTower() { return entityData.get(IS_ATTACKING_TOWER); }
    public BaseTowerEntity getBlockingTower() { return blockingTower; }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        
        compound.putFloat("Damage", damage);
        compound.putFloat("AttackSpeed", attackSpeed);
        compound.putInt("Reward", reward);
        compound.putInt("CurrentWaypoint", currentWaypointIndex);
        
        // Save waypoints
        ListTag waypointList = new ListTag();
        for (BlockPos pos : pathWaypoints) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            waypointList.add(posTag);
        }
        compound.put("PathWaypoints", waypointList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        
        if (compound.contains("Damage")) {
            damage = compound.getFloat("Damage");
        }
        if (compound.contains("AttackSpeed")) {
            attackSpeed = compound.getFloat("AttackSpeed");
        }
        if (compound.contains("Reward")) {
            reward = compound.getInt("Reward");
        }
        if (compound.contains("CurrentWaypoint")) {
            currentWaypointIndex = compound.getInt("CurrentWaypoint");
        }
        
        // Load waypoints
        if (compound.contains("PathWaypoints")) {
            pathWaypoints.clear();
            ListTag waypointList = compound.getList("PathWaypoints", 10);
            for (int i = 0; i < waypointList.size(); i++) {
                CompoundTag posTag = waypointList.getCompound(i);
                int x = posTag.getInt("X");
                int y = posTag.getInt("Y");
                int z = posTag.getInt("Z");
                pathWaypoints.add(new BlockPos(x, y, z));
            }
        }
    }

    /**
     * Goal for following the path
     */
    static class FollowPathGoal extends Goal {
        private final BaseEnemyEntity enemy;

        public FollowPathGoal(BaseEnemyEntity enemy) {
            this.enemy = enemy;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Can follow path if not attacking a tower and path exists
            return !enemy.isAttackingTower() && !enemy.hasReachedEnd();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            if (enemy.hasReachedWaypoint()) {
                enemy.advanceWaypoint();
            }
            
            BlockPos waypoint = enemy.getCurrentWaypoint();
            if (waypoint != null) {
                PathNavigation nav = enemy.getNavigation();
                if (nav.isDone()) {
                    nav.moveTo(
                        waypoint.getX() + 0.5,
                        waypoint.getY(),
                        waypoint.getZ() + 0.5,
                        1.0
                    );
                }
            }
        }
    }

    /**
     * Goal for attacking blocking towers
     */
    static class EnemyAttackTowerGoal extends Goal {
        private final BaseEnemyEntity enemy;

        public EnemyAttackTowerGoal(BaseEnemyEntity enemy) {
            this.enemy = enemy;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return enemy.blockingTower != null && enemy.blockingTower.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            enemy.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (enemy.blockingTower != null) {
                enemy.getLookControl().setLookAt(enemy.blockingTower);
                
                // Move closer if needed
                if (enemy.distanceToSqr(enemy.blockingTower) > 4.0) {
                    enemy.getNavigation().moveTo(enemy.blockingTower, 1.0);
                } else {
                    enemy.getNavigation().stop();
                }
            }
        }

        @Override
        public void stop() {
            enemy.clearBlockingTower();
        }
    }
}
