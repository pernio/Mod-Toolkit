package emcMod.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import emcMod.client.EmcModClient;
import emcMod.client.util.TaskManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static emcMod.client.util.FollowManager.getFollowedPlayers;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static net.minecraft.command.CommandSource.suggestMatching;

public class TaskCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("addtask")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("description", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            if (!EmcModClient.toolsEnabled) {
                                                sendError(ctx.getSource(), "Tools must be enabled to use this command.");
                                                return 0;
                                            }
                                            String name = StringArgumentType.getString(ctx, "name");
                                            String desc = StringArgumentType.getString(ctx, "description");
                                            TaskManager.addTask(name, desc);
                                            sendSuccess(ctx.getSource(), "Added task '" + name + "': " + desc);
                                            return 1;
                                        })))
        );

        dispatcher.register(
                literal("task")
                        .then(argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> suggestMatching(TaskManager.getAllTasks().keySet(), builder))
                                .executes(ctx -> {
                                    if (!EmcModClient.toolsEnabled) {
                                        sendError(ctx.getSource(), "Tools must be enabled to use this command.");
                                        return 0;
                                    }
                                    String name = StringArgumentType.getString(ctx, "name");
                                    String desc = TaskManager.getTask(name);
                                    if (desc != null) {
                                        sendNeutral(ctx.getSource(), "Task '" + name + "': " + desc);
                                    } else {
                                        sendError(ctx.getSource(), "Task not found.");
                                    }
                                    return 1;
                                }))
        );

        dispatcher.register(
                literal("removetask")
                        .then(argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> suggestMatching(TaskManager.getAllTasks().keySet(), builder))
                                .executes(ctx -> {
                                    if (!EmcModClient.toolsEnabled) {
                                        sendError(ctx.getSource(), "Tools must be enabled to use this command.");
                                        return 0;
                                    }
                                    String name = StringArgumentType.getString(ctx, "name");
                                    if (TaskManager.removeTask(name)) {
                                        sendSuccess(ctx.getSource(), "Removed task '" + name + "'.");
                                    } else {
                                        sendError(ctx.getSource(), "Task not found.");
                                    }
                                    return 1;
                                }))
        );

        dispatcher.register(
                literal("tasks")
                        .executes(ctx -> {
                            if (!EmcModClient.toolsEnabled) {
                                sendError(ctx.getSource(), "Tools must be enabled to use this command.");
                                return 0;
                            }
                            if (!TaskManager.hasTasks()) {
                                sendNeutral(ctx.getSource(), "No tasks available.");
                            } else {
                                String joined = String.join(", ", TaskManager.getAllTasks().keySet());
                                sendNeutral(ctx.getSource(), "Tasks: " + joined);
                            }
                            return 1;
                        })
        );

        // TODO: Remove duplicate code
        dispatcher.register(
                literal("seetask")
                        .executes(ctx -> {
                            if (!EmcModClient.toolsEnabled) {
                                sendError(ctx.getSource(), "Tools must be enabled to use this command.");
                                return 0;
                            }
                            if (!TaskManager.hasTasks()) {
                                sendNeutral(ctx.getSource(), "No tasks available.");
                            } else {
                                String joined = String.join(", ", TaskManager.getAllTasks().keySet());
                                sendNeutral(ctx.getSource(), "Tasks: " + joined);
                            }
                            return 1;
                        })
        );

        dispatcher.register(
                literal("purgetask")
                        .executes(ctx -> {
                            if (!EmcModClient.toolsEnabled) {
                                sendError(ctx.getSource(), "Tools must be enabled to use this command.");
                                return 0;
                            }
                            TaskManager.clearTasks();
                            sendSuccess(ctx.getSource(), "All tasks cleared.");
                            return 1;
                        })
        );
    }

    private static void sendSuccess(FabricClientCommandSource ctx, String msg) {
        ctx.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.GREEN)));
    }

    private static void sendError(FabricClientCommandSource ctx, String msg) {
        ctx.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.RED)));
    }

    private static void sendNeutral(FabricClientCommandSource ctx, String msg) {
        ctx.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.WHITE)));
    }
}