package com.towerdefense.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates tower defense maps with paths, spawn points, and buildable platforms
 */
public class MapGenerator {
    
    private static final BlockState PATH_BLOCK = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState PLATFORM_BLOCK = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState SPAWN_BLOCK = Blocks.REDSTONE_BLOCK.defaultBlockState();
    private static final BlockState END_BLOCK = Blocks.EMERALD_BLOCK.defaultBlockState();
    private static final BlockState BARRIER_BLOCK = Blocks.OAK_FENCE.defaultBlockState();
    
    private static final int DEFAULT_MAP_SIZE = 50;
    private static final int PATH_WIDTH = 3;
    private static final int PLATFORM_SIZE = 5;
    private static final int CLEAR_HEIGHT = 10;
    private static final int DECORATION_COUNT = 10;
    private static final double DECORATION_PATH_CLEARANCE_SQ = 25.0; // 5 blocks squared
    
    /**
     * Generate a tower defense map at the specified location
     */
    public static GeneratedMap generateMap(ServerLevel level, BlockPos center, int mapSize) {
        GeneratedMap map = new GeneratedMap();
        map.center = center;
        map.size = mapSize;
        
        // Clear the area
        clearArea(level, center, mapSize);
        
        // Create path
        List<BlockPos> pathPoints = createPath(level, center, mapSize);
        map.pathPoints = pathPoints;
        
        // Set spawn and end points
        if (!pathPoints.isEmpty()) {
            map.spawnPoint = pathPoints.get(0);
            map.endPoint = pathPoints.get(pathPoints.size() - 1);
            
            // Mark spawn with redstone block
            level.setBlock(map.spawnPoint.below(), SPAWN_BLOCK, 3);
            
            // Mark end with emerald block
            level.setBlock(map.endPoint.below(), END_BLOCK, 3);
        }
        
        // Create buildable platforms
        map.platformPositions = createPlatforms(level, center, pathPoints, mapSize);
        
        // Add decorations
        addDecorations(level, center, pathPoints, mapSize);
        
        return map;
    }
    
    /**
     * Clear the area for the map
     */
    private static void clearArea(ServerLevel level, BlockPos center, int size) {
        int halfSize = size / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                BlockPos pos = center.offset(x, 0, z);
                
                // Find ground level
                BlockPos ground = findGroundLevel(level, pos);
                
                // Clear vertical space
                for (int y = 0; y < CLEAR_HEIGHT; y++) {
                    level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 3);
                }
                
                // Set ground to grass
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * Create a winding path through the map
     */
    private static List<BlockPos> createPath(ServerLevel level, BlockPos center, int size) {
        List<BlockPos> pathPoints = new ArrayList<>();
        int halfSize = size / 2;
        
        // Start point (left side)
        BlockPos start = center.offset(-halfSize + 5, 0, 0);
        start = findGroundLevel(level, start).above();
        
        // End point (right side)
        BlockPos end = center.offset(halfSize - 5, 0, 0);
        end = findGroundLevel(level, end).above();
        
        // Create a winding path
        pathPoints.add(start);
        
        // Add waypoints
        int numWaypoints = 4;
        int currentX = start.getX();
        int currentZ = start.getZ();
        
        for (int i = 1; i <= numWaypoints; i++) {
            int targetX = start.getX() + (end.getX() - start.getX()) * i / (numWaypoints + 1);
            int targetZ = currentZ + ((i % 2 == 0) ? 10 : -10);
            
            BlockPos waypoint = new BlockPos(targetX, start.getY(), targetZ);
            waypoint = findGroundLevel(level, waypoint).above();
            pathPoints.add(waypoint);
            
            currentX = targetX;
            currentZ = targetZ;
        }
        
        pathPoints.add(end);
        
        // Build the actual path blocks
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            buildPathSegment(level, pathPoints.get(i), pathPoints.get(i + 1));
        }
        
        return pathPoints;
    }
    
    /**
     * Build a path segment between two points
     */
    private static void buildPathSegment(ServerLevel level, BlockPos from, BlockPos to) {
        int steps = Math.max(Math.abs(to.getX() - from.getX()), Math.abs(to.getZ() - from.getZ()));
        
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) (from.getX() + (to.getX() - from.getX()) * t);
            int z = (int) (from.getZ() + (to.getZ() - from.getZ()) * t);
            BlockPos pos = new BlockPos(x, from.getY(), z);
            
            // Place path blocks
            for (int dx = -PATH_WIDTH / 2; dx <= PATH_WIDTH / 2; dx++) {
                for (int dz = -PATH_WIDTH / 2; dz <= PATH_WIDTH / 2; dz++) {
                    BlockPos pathPos = pos.offset(dx, 0, dz);
                    level.setBlock(pathPos, PATH_BLOCK, 3);
                }
            }
        }
    }
    
    /**
     * Create buildable platforms along the path
     */
    private static List<BlockPos> createPlatforms(ServerLevel level, BlockPos center, List<BlockPos> pathPoints, int size) {
        List<BlockPos> platforms = new ArrayList<>();
        
        if (pathPoints.isEmpty()) return platforms;
        
        // Create platforms near each path segment
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            BlockPos p1 = pathPoints.get(i);
            BlockPos p2 = pathPoints.get(i + 1);
            
            // Calculate perpendicular offset
            int dx = p2.getX() - p1.getX();
            int dz = p2.getZ() - p1.getZ();
            
            // Normalize and rotate 90 degrees
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 0) {
                int offsetX = (int) (-dz / length * 8);
                int offsetZ = (int) (dx / length * 8);
                
                // Place platforms on both sides
                BlockPos left = p1.offset(offsetX, 0, offsetZ);
                BlockPos right = p1.offset(-offsetX, 0, -offsetZ);
                
                createPlatform(level, left);
                createPlatform(level, right);
                
                platforms.add(left);
                platforms.add(right);
            }
        }
        
        return platforms;
    }
    
    /**
     * Create a single platform
     */
    private static void createPlatform(ServerLevel level, BlockPos center) {
        center = findGroundLevel(level, center).above();
        
        int halfSize = PLATFORM_SIZE / 2;
        for (int dx = -halfSize; dx <= halfSize; dx++) {
            for (int dz = -halfSize; dz <= halfSize; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                level.setBlock(pos, PLATFORM_BLOCK, 3);
            }
        }
    }
    
    /**
     * Add decorative elements
     */
    private static void addDecorations(ServerLevel level, BlockPos center, List<BlockPos> pathPoints, int size) {
        // Add some trees and barriers for visual appeal
        int halfSize = size / 2;
        
        for (int i = 0; i < DECORATION_COUNT; i++) {
            int x = center.getX() + level.random.nextInt(size) - halfSize;
            int z = center.getZ() + level.random.nextInt(size) - halfSize;
            BlockPos pos = findGroundLevel(level, new BlockPos(x, center.getY(), z)).above();
            
            // Check if not on path
            boolean onPath = false;
            for (BlockPos pathPoint : pathPoints) {
                if (pos.distSqr(pathPoint) < DECORATION_PATH_CLEARANCE_SQ) {
                    onPath = true;
                    break;
                }
            }
            
            if (!onPath && level.random.nextBoolean()) {
                // Place a tree or fence
                if (level.random.nextFloat() < 0.3f) {
                    level.setBlock(pos, Blocks.OAK_LOG.defaultBlockState(), 3);
                    level.setBlock(pos.above(), Blocks.OAK_LEAVES.defaultBlockState(), 3);
                } else {
                    level.setBlock(pos, BARRIER_BLOCK, 3);
                }
            }
        }
    }
    
    /**
     * Find the ground level at a position
     */
    private static BlockPos findGroundLevel(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        
        // Go down to find solid ground
        while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos).isAir()) {
            pos.move(0, -1, 0);
        }
        
        // Go up if underground
        while (pos.getY() < level.getMaxBuildHeight() && !level.getBlockState(pos.above()).isAir()) {
            pos.move(0, 1, 0);
        }
        
        return pos.immutable();
    }
    
    /**
     * Data class for generated map information
     */
    public static class GeneratedMap {
        public BlockPos center;
        public int size;
        public BlockPos spawnPoint;
        public BlockPos endPoint;
        public List<BlockPos> pathPoints;
        public List<BlockPos> platformPositions;
        
        public GeneratedMap() {
            pathPoints = new ArrayList<>();
            platformPositions = new ArrayList<>();
        }
    }
}
