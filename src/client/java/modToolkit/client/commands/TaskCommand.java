package modToolkit.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import modToolkit.client.util.TaskManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import static modToolkit.client.ModToolkitClient.sendError;
import static modToolkit.client.ModToolkitClient.sendInfo;
import static modToolkit.client.ModToolkitClient.sendSuccess;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static net.minecraft.command.CommandSource.suggestMatching;

public class TaskCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("task")
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.word())
                                        .then(argument("description", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String desc = StringArgumentType.getString(ctx, "description");
                                                    TaskManager.addTask(name, desc);
                                                    sendSuccess(ctx.getSource(), "Added task '" + name + "': " + desc);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(literal("info")
                                .then(argument("name", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestMatching(TaskManager.getAllTasks().keySet(), builder))
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            String desc = TaskManager.getTask(name);
                                            if (desc != null) {
                                                sendInfo(ctx.getSource(), "Task '" + name + "': " + desc);
                                            } else {
                                                sendError(ctx.getSource(), "Task not found.");
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("name", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestMatching(TaskManager.getAllTasks().keySet(), builder))
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            if (TaskManager.removeTask(name)) {
                                                sendSuccess(ctx.getSource(), "Removed task '" + name + "'.");
                                            } else {
                                                sendError(ctx.getSource(), "Task not found.");
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("list")
                                .executes(ctx -> {
                                    if (!TaskManager.hasTasks()) {
                                        sendInfo(ctx.getSource(), "No tasks available.");
                                    } else {
                                        String joined = String.join(", ", TaskManager.getAllTasks().keySet());
                                        sendInfo(ctx.getSource(), "Tasks: " + joined);
                                    }
                                    return 1;
                                })
                        )
                        .then(literal("clear")
                                .executes(ctx -> {
                                    TaskManager.clearTasks();
                                    sendSuccess(ctx.getSource(), "All tasks cleared.");
                                    return 1;
                                })
                        )
        );
    }
}