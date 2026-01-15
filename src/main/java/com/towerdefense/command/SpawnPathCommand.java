package com.towerdefense.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.towerdefense.path.PathData;
import com.towerdefense.path.PathManager;
import com.towerdefense.wave.WaveManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Command to spawn pre-built paths with enemy waves
 * Usage: /tdspawn [pathName]
 */
public class SpawnPathCommand {

    private static final SuggestionProvider<CommandSourceStack> PATH_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(PathManager.getPathNames(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tdspawn")
                        .requires(source -> source.hasPermission(2)) // Requires OP or creative mode
                        .then(Commands.argument("pathName", StringArgumentType.string())
                                .suggests(PATH_SUGGESTIONS)
                                .executes(context -> {
                                    String pathName = StringArgumentType.getString(context, "pathName");
                                    return spawnPath(context.getSource(), pathName);
                                })
                        )
                        .executes(context -> {
                            // No path name provided, show available paths
                            return listPaths(context.getSource());
                        })
        );
    }

    private static int spawnPath(CommandSourceStack source, String pathName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        PathData path = PathManager.getPath(pathName);

        if (path == null) {
            source.sendFailure(Component.literal("Path '" + pathName + "' not found. Use /tdspawn to see available paths."));
            return 0;
        }

        // Get player's position
        BlockPos playerPos = player.blockPosition();

        // Spawn the path at player's location
        PathManager.spawnPath(level, playerPos, pathName);

        // Get adjusted waypoints for this spawned path
        List<BlockPos> waypoints = PathManager.getAdjustedWaypoints(path, playerPos);

        // Start wave spawning
        WaveManager.startWaves(level, path, playerPos, waypoints);

        // Success message
        source.sendSuccess(() -> Component.literal("§aSpawned path: §e" + path.getName() + " §aat your location!"), true);
        source.sendSuccess(() -> Component.literal("§6Enemy waves will now spawn. Good luck!"), false);

        return 1;
    }

    private static int listPaths(CommandSourceStack source) {
        String[] pathNames = PathManager.getPathNames();

        if (pathNames.length == 0) {
            source.sendFailure(Component.literal("No paths available"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6Available paths:"), false);
        for (String pathName : pathNames) {
            PathData path = PathManager.getPath(pathName);
            source.sendSuccess(() -> Component.literal("§e- " + pathName + " §7(" + path.getName() + ")"), false);
        }
        source.sendSuccess(() -> Component.literal("§7Usage: /tdspawn <pathName>"), false);

        return 1;
    }
}
