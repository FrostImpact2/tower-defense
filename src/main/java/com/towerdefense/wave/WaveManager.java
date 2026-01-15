package com.towerdefense.wave;

import com.towerdefense.entity.enemy.BaseEnemyEntity;
import com.towerdefense.entity.enemy.ZombieEnemyEntity;
import com.towerdefense.path.PathData;
import com.towerdefense.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages enemy wave spawning for paths
 * Handles wave timing, enemy counts, and spawn intervals
 */
public class WaveManager {

    private static final Map<UUID, WaveData> ACTIVE_WAVES = new HashMap<>();

    /**
     * Start a wave spawning system for a path
     */
    public static UUID startWaves(ServerLevel level, PathData path, BlockPos spawnLocation, List<BlockPos> waypoints) {
        UUID waveId = UUID.randomUUID();
        WaveData waveData = new WaveData(level, path, spawnLocation, waypoints);
        ACTIVE_WAVES.put(waveId, waveData);
        return waveId;
    }

    /**
     * Stop waves for a given ID
     */
    public static void stopWaves(UUID waveId) {
        ACTIVE_WAVES.remove(waveId);
    }

    /**
     * Tick all active waves
     * Should be called from a server tick event
     */
    public static void tickWaves() {
        List<UUID> toRemove = new ArrayList<>();
        
        for (Map.Entry<UUID, WaveData> entry : ACTIVE_WAVES.entrySet()) {
            WaveData waveData = entry.getValue();
            waveData.tick();
            
            // Remove completed waves
            if (waveData.isComplete()) {
                toRemove.add(entry.getKey());
            }
        }
        
        toRemove.forEach(ACTIVE_WAVES::remove);
    }

    /**
     * Get active wave count
     */
    public static int getActiveWaveCount() {
        return ACTIVE_WAVES.size();
    }

    /**
     * Data for a single wave spawning system
     */
    private static class WaveData {
        private final ServerLevel level;
        private final PathData path;
        private final BlockPos spawnLocation;
        private final List<BlockPos> waypoints;
        
        private int currentWave;
        private int ticksSinceLastSpawn;
        private int enemiesSpawnedThisWave;
        private int ticksUntilNextWave;
        private boolean waveActive;
        
        // Wave configuration
        // SPAWN_INTERVAL: Time in ticks (20 ticks = 1 second) between individual enemy spawns
        private static final int SPAWN_INTERVAL = 40; // 2 seconds between spawns
        // WAVE_DELAY: Time in ticks between waves completing and next wave starting
        private static final int WAVE_DELAY = 600; // 30 seconds between waves
        // MAX_WAVES: Total number of waves before wave system stops
        private static final int MAX_WAVES = 10;

        public WaveData(ServerLevel level, PathData path, BlockPos spawnLocation, List<BlockPos> waypoints) {
            this.level = level;
            this.path = path;
            this.spawnLocation = spawnLocation;
            this.waypoints = waypoints;
            this.currentWave = 1;
            this.ticksSinceLastSpawn = 0;
            this.enemiesSpawnedThisWave = 0;
            this.ticksUntilNextWave = 100; // 5 second delay before first wave
            this.waveActive = false;
        }

        public void tick() {
            if (!waveActive) {
                // Waiting for next wave
                ticksUntilNextWave--;
                
                if (ticksUntilNextWave <= 0) {
                    startWave();
                }
                return;
            }

            // Active wave - spawn enemies
            ticksSinceLastSpawn++;
            
            if (ticksSinceLastSpawn >= SPAWN_INTERVAL) {
                spawnEnemy();
                ticksSinceLastSpawn = 0;
                enemiesSpawnedThisWave++;
                
                // Check if wave is complete
                if (enemiesSpawnedThisWave >= getEnemiesForWave(currentWave)) {
                    endWave();
                }
            }
        }

        private void startWave() {
            waveActive = true;
            enemiesSpawnedThisWave = 0;
            ticksSinceLastSpawn = 0;
            
            // Broadcast wave start
            level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal("§6Wave " + currentWave + " started!"),
                false
            );
        }

        private void endWave() {
            waveActive = false;
            currentWave++;
            ticksUntilNextWave = WAVE_DELAY;
            
            // Broadcast wave complete
            level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal("§aWave " + (currentWave - 1) + " complete!"),
                false
            );
        }

        private void spawnEnemy() {
            // Create zombie enemy
            ZombieEnemyEntity enemy = new ZombieEnemyEntity(ModEntities.ZOMBIE_ENEMY.get(), level);
            
            // Set spawn position (slightly randomized)
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetZ = (Math.random() - 0.5) * 2;
            enemy.setPos(
                spawnLocation.getX() + 0.5 + offsetX,
                spawnLocation.getY() + 1,
                spawnLocation.getZ() + 0.5 + offsetZ
            );
            
            // Set path waypoints
            enemy.setPathWaypoints(waypoints);
            
            // Add to world
            level.addFreshEntity(enemy);
        }

        private int getEnemiesForWave(int wave) {
            // Scale enemy count with wave number
            return 5 + (wave * 2);
        }

        public boolean isComplete() {
            return currentWave > MAX_WAVES;
        }
    }
}
