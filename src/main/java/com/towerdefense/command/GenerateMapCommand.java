package com.towerdefense.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.towerdefense.worldgen.MapGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command to generate tower defense maps
 * Usage: /td genmap [size]
 * or: /towerdefense generatemap [size]
 */
public class GenerateMapCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("td")
            .then(Commands.literal("genmap")
                .requires(source -> source.hasPermission(2))
                .executes(context -> generateMap(context, 50))
                .then(Commands.argument("size", IntegerArgumentType.integer(20, 100))
                    .executes(context -> generateMap(
                        context,
                        IntegerArgumentType.getInteger(context, "size")
                    ))
                )
            )
        );
        
        dispatcher.register(Commands.literal("towerdefense")
            .then(Commands.literal("generatemap")
                .requires(source -> source.hasPermission(2))
                .executes(context -> generateMap(context, 50))
                .then(Commands.argument("size", IntegerArgumentType.integer(20, 100))
                    .executes(context -> generateMap(
                        context,
                        IntegerArgumentType.getInteger(context, "size")
                    ))
                )
            )
        );
    }
    
    private static int generateMap(CommandContext<CommandSourceStack> context, int size) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player"));
            return 0;
        }
        
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        
        // Send initial message
        source.sendSuccess(() -> Component.literal(
            "§aGenerating tower defense map (size: " + size + "x" + size + ")..."
        ), true);
        
        // Generate the map
        try {
            MapGenerator.GeneratedMap map = MapGenerator.generateMap(level, playerPos, size);
            
            // Send success message with info
            source.sendSuccess(() -> Component.literal(
                "§aMap generated successfully!\n" +
                "§7Spawn Point: §f" + formatBlockPos(map.spawnPoint) + " §c(Red)\n" +
                "§7End Point: §f" + formatBlockPos(map.endPoint) + " §a(Green)\n" +
                "§7Platforms: §f" + map.platformPositions.size() + "\n" +
                "§7Path Length: §f" + map.pathPoints.size() + " waypoints\n" +
                "§ePlace towers on §6stone brick platforms§e!"
            ), true);
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cFailed to generate map: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
    
    private static String formatBlockPos(BlockPos pos) {
        return String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
    }
}
