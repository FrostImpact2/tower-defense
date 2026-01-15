package com.towerdefense.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores configuration data for a path
 * Includes waypoints, spawn location, and visual block type
 */
public class PathData {

    private final String name;
    private final List<BlockPos> waypoints;
    private final BlockPos spawnLocation;
    private final Block pathBlockType;
    private final int pathWidth;

    public PathData(String name, List<BlockPos> waypoints, BlockPos spawnLocation, Block pathBlockType, int pathWidth) {
        this.name = name;
        this.waypoints = new ArrayList<>(waypoints);
        this.spawnLocation = spawnLocation;
        this.pathBlockType = pathBlockType;
        this.pathWidth = pathWidth;
    }

    /**
     * Create a simple straight path for testing
     */
    public static PathData createSimplePath(String name, BlockPos start, int length) {
        List<BlockPos> waypoints = new ArrayList<>();
        
        // Create waypoints along X axis
        for (int i = 0; i <= length; i += 5) {
            waypoints.add(start.offset(i, 0, 0));
        }
        
        return new PathData(name, waypoints, start, Blocks.STONE_BRICKS, 3);
    }

    /**
     * Create a zigzag path
     */
    public static PathData createZigzagPath(String name, BlockPos start) {
        List<BlockPos> waypoints = new ArrayList<>();
        
        waypoints.add(start);
        waypoints.add(start.offset(10, 0, 0));
        waypoints.add(start.offset(10, 0, 10));
        waypoints.add(start.offset(20, 0, 10));
        waypoints.add(start.offset(20, 0, 0));
        waypoints.add(start.offset(30, 0, 0));
        
        return new PathData(name, waypoints, start, Blocks.COBBLESTONE, 3);
    }

    /**
     * Create a curved path
     */
    public static PathData createCurvedPath(String name, BlockPos start) {
        List<BlockPos> waypoints = new ArrayList<>();
        
        // Create a curved path using multiple waypoints
        waypoints.add(start);
        waypoints.add(start.offset(5, 0, 0));
        waypoints.add(start.offset(10, 0, 2));
        waypoints.add(start.offset(15, 0, 5));
        waypoints.add(start.offset(20, 0, 8));
        waypoints.add(start.offset(25, 0, 10));
        waypoints.add(start.offset(30, 0, 10));
        
        return new PathData(name, waypoints, start, Blocks.SMOOTH_STONE, 3);
    }

    /**
     * Create a long winding path
     */
    public static PathData createLongPath(String name, BlockPos start) {
        List<BlockPos> waypoints = new ArrayList<>();
        
        waypoints.add(start);
        waypoints.add(start.offset(15, 0, 0));
        waypoints.add(start.offset(15, 0, 15));
        waypoints.add(start.offset(0, 0, 15));
        waypoints.add(start.offset(0, 0, 30));
        waypoints.add(start.offset(15, 0, 30));
        waypoints.add(start.offset(15, 0, 45));
        waypoints.add(start.offset(30, 0, 45));
        
        return new PathData(name, waypoints, start, Blocks.POLISHED_ANDESITE, 3);
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<BlockPos> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    public BlockPos getSpawnLocation() {
        return spawnLocation;
    }

    public Block getPathBlockType() {
        return pathBlockType;
    }

    public int getPathWidth() {
        return pathWidth;
    }

    public BlockPos getEndLocation() {
        if (waypoints.isEmpty()) {
            return spawnLocation;
        }
        return waypoints.get(waypoints.size() - 1);
    }
}
