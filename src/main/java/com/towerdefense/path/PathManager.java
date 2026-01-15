package com.towerdefense.path;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all paths in the game
 * Stores pre-built path configurations and handles path spawning
 */
public class PathManager {

    private static final Map<String, PathData> PATHS = new HashMap<>();

    static {
        // Register pre-built paths
        registerDefaultPaths();
    }

    /**
     * Register default pre-built paths
     */
    private static void registerDefaultPaths() {
        // Simple straight path for testing
        BlockPos testStart = new BlockPos(0, 64, 0);
        PATHS.put("simple", PathData.createSimplePath("Simple Path", testStart, 30));

        // Zigzag path
        PATHS.put("zigzag", PathData.createZigzagPath("Zigzag Path", testStart));

        // Curved path
        PATHS.put("curved", PathData.createCurvedPath("Curved Path", testStart));

        // Long winding path
        PATHS.put("long", PathData.createLongPath("Long Path", testStart));
    }

    /**
     * Register a custom path
     */
    public static void registerPath(String name, PathData path) {
        PATHS.put(name.toLowerCase(), path);
    }

    /**
     * Get a path by name
     */
    public static PathData getPath(String name) {
        return PATHS.get(name.toLowerCase());
    }

    /**
     * Check if a path exists
     */
    public static boolean hasPath(String name) {
        return PATHS.containsKey(name.toLowerCase());
    }

    /**
     * Get all path names
     */
    public static String[] getPathNames() {
        return PATHS.keySet().toArray(new String[0]);
    }

    /**
     * Spawn a path in the world at the player's location
     */
    public static void spawnPath(ServerLevel level, BlockPos playerPos, String pathName) {
        PathData path = getPath(pathName);
        if (path == null) {
            return;
        }

        // Adjust path to start at player's location
        BlockPos offset = playerPos.subtract(path.getSpawnLocation());
        BlockState pathBlock = path.getPathBlockType().defaultBlockState();
        int width = path.getPathWidth();
        int halfWidth = width / 2;

        // Build the path blocks
        for (int i = 0; i < path.getWaypoints().size() - 1; i++) {
            BlockPos start = path.getWaypoints().get(i).offset(offset);
            BlockPos end = path.getWaypoints().get(i + 1).offset(offset);

            // Draw path segment
            drawPathSegment(level, start, end, pathBlock, halfWidth);
        }
    }

    /**
     * Draw a path segment between two waypoints
     */
    private static void drawPathSegment(ServerLevel level, BlockPos start, BlockPos end, BlockState block, int halfWidth) {
        int steps = (int) Math.sqrt(start.distSqr(end)) * 2;
        
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(start.getX() + (end.getX() - start.getX()) * t);
            int y = start.getY();
            int z = (int) Math.round(start.getZ() + (end.getZ() - start.getZ()) * t);

            // Find ground level
            BlockPos centerPos = new BlockPos(x, y, z);
            BlockPos groundPos = findGroundLevel(level, centerPos);

            // Place path blocks with width
            for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                    BlockPos placePos = groundPos.offset(dx, 0, dz);
                    level.setBlock(placePos, block, 3);
                }
            }
        }
    }

    /**
     * Find the ground level at a position
     */
    private static BlockPos findGroundLevel(ServerLevel level, BlockPos pos) {
        // Search downward for solid ground
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        
        for (int i = 0; i < 10; i++) {
            if (level.getBlockState(mutablePos.below()).isSolid()) {
                return mutablePos.immutable();
            }
            mutablePos.move(0, -1, 0);
        }

        // If no ground found, search upward
        mutablePos.set(pos);
        for (int i = 0; i < 10; i++) {
            if (level.getBlockState(mutablePos).isSolid()) {
                return mutablePos.above().immutable();
            }
            mutablePos.move(0, 1, 0);
        }

        return pos;
    }

    /**
     * Get waypoints for a spawned path (adjusted for player location)
     */
    public static java.util.List<BlockPos> getAdjustedWaypoints(PathData path, BlockPos playerPos) {
        BlockPos offset = playerPos.subtract(path.getSpawnLocation());
        java.util.List<BlockPos> adjustedWaypoints = new java.util.ArrayList<>();
        
        for (BlockPos waypoint : path.getWaypoints()) {
            adjustedWaypoints.add(waypoint.offset(offset));
        }
        
        return adjustedWaypoints;
    }
}
