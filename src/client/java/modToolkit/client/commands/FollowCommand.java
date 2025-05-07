package modToolkit.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import modToolkit.client.util.SettingsManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.util.List;

import static modToolkit.client.ModToolkitClient.colorsShown;
import static modToolkit.client.ModToolkitClient.followColor;
import static modToolkit.client.ModToolkitClient.sendError;
import static modToolkit.client.ModToolkitClient.sendInfo;
import static modToolkit.client.ModToolkitClient.sendSuccess;
import static modToolkit.client.util.FollowManager.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class FollowCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(
                literal("follow")
                        .then(literal("player")
                                .then(argument("playername", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            var client = MinecraftClient.getInstance();
                                            if (client.getNetworkHandler() != null) {
                                                List<String> names = client.getNetworkHandler().getPlayerList().stream()
                                                        .map(entry -> entry.getProfile().getName())
                                                        .toList();
                                                return suggestMatching(names, builder);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            var source = ctx.getSource();
                                            String name = StringArgumentType.getString(ctx, "playername");
                                            add(name);
                                            sendSuccess(source, "Now following " + name);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("playername", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestMatching(getFollowedPlayers(), builder))
                                        .executes(ctx -> {
                                            var source = ctx.getSource();
                                            String name = StringArgumentType.getString(ctx, "playername");
                                            if (remove(name)) {
                                                sendSuccess(source, "Removed " + name + " from followed list.");
                                            } else {
                                                sendError(source, name + " was not in your follow list.");
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("list")
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    if (isEmpty()) {
                                        sendInfo(source, "No players currently followed.");
                                    } else {
                                        sendInfo(source, "Following: " + String.join(", ", getFollowedPlayers()));
                                    }
                                    return 1;
                                })
                        )
                        .then(literal("clear")
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    clear();
                                    sendSuccess(source, "Follow list cleared.");
                                    return 1;
                                })
                        )
                        .then(literal("show")
                                .executes(ctx -> {
                                    colorsShown = !colorsShown;
                                    SettingsManager.save();
                                    if (colorsShown) {
                                        sendSuccess(ctx.getSource(), "Colors shown");
                                    } else {
                                        sendError(ctx.getSource(), "Colors hidden");
                                    }
                                    return 1;
                                })
                                .then(literal("on").executes(ctx -> {
                                    colorsShown = true;
                                    SettingsManager.save();
                                    sendSuccess(ctx.getSource(), "Colors shown");
                                    return 1;
                                }))
                                .then(literal("off").executes(ctx -> {
                                    colorsShown = false;
                                    SettingsManager.save();
                                    sendError(ctx.getSource(), "Colors hidden");
                                    return 1;
                                }))
                        )
                        .then(literal("color"))
                            .then(argument("color", StringArgumentType.word())
                                    .suggests((context, builder) -> suggestMatching(
                                            List.of("red", "blue", "green", "yellow", "aqua", "light_purple", "gray"),
                                            builder))
                                    .executes(ctx -> {
                                        String colorInput = StringArgumentType.getString(ctx, "color").toLowerCase();
                                        switch (colorInput) {
                                            case "red" -> followColor = Formatting.RED;
                                            case "blue" -> followColor = Formatting.BLUE;
                                            case "green" -> followColor = Formatting.GREEN;
                                            case "yellow" -> followColor = Formatting.YELLOW;
                                            case "aqua" -> followColor = Formatting.AQUA;
                                            case "light_purple" -> followColor = Formatting.LIGHT_PURPLE;
                                            case "gray" -> followColor = Formatting.GRAY;
                                            default -> {
                                                sendError(ctx.getSource(),"Invalid color. Choose a predefined color.");
                                                return 1;
                                            }
                                        }
                                        SettingsManager.save();
                                        sendSuccess(ctx.getSource(),"Set follow color to " + followColor.getName());
                                        return 1;
                                    }))
        );
    }
}