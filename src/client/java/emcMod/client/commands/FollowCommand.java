package emcMod.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static emcMod.client.EmcModClient.toolsEnabled;
import static emcMod.client.EmcModClient.followColor;
import static emcMod.client.util.FollowManager.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

import com.mojang.brigadier.CommandDispatcher;

public class FollowCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(
                literal("follow")
                        .then(argument("player", StringArgumentType.word())
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
                                    if (!toolsEnabled) {
                                        sendError(source, "Tools must be enabled to use this command.");
                                        return 0;
                                    }
                                    String name = StringArgumentType.getString(ctx, "player");
                                    add(name);
                                    sendSuccess(source, "Now following " + name);
                                    return 1;
                                }))
        );

        dispatcher.register(
                literal("removefollow")
                        .then(argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> suggestMatching(getFollowedPlayers(), builder))
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    if (!toolsEnabled) {
                                        sendError(source, "Tools must be enabled to use this command.");
                                        return 0;
                                    }

                                    String name = StringArgumentType.getString(ctx, "player");
                                    if (remove(name)) {
                                        sendSuccess(source, "Removed " + name + " from followed list.");
                                    } else {
                                        sendError(source, name + " was not in your follow list.");
                                    }
                                    return 1;
                                }))
        );

        dispatcher.register(
                literal("seefollow")
                        .executes(ctx -> {
                            var source = ctx.getSource();
                            if (!toolsEnabled) {
                                sendError(source, "Tools must be enabled to use this command.");
                                return 0;
                            }

                            if (isEmpty()) {
                                sendInfo(source, "No players currently followed.");
                            } else {
                                sendInfo(source, "Following: " + String.join(", ", getFollowedPlayers()));
                            }
                            return 1;
                        })
        );

        // TODO: Remove duplicate code
        dispatcher.register(
                literal("following")
                        .executes(ctx -> {
                            var source = ctx.getSource();
                            if (!toolsEnabled) {
                                sendError(source, "Tools must be enabled to use this command.");
                                return 0;
                            }

                            if (isEmpty()) {
                                sendInfo(source, "No players currently followed.");
                            } else {
                                sendInfo(source, "Following: " + String.join(", ", getFollowedPlayers()));
                            }
                            return 1;
                        })
        );

        dispatcher.register(
                literal("purgefollow")
                        .executes(ctx -> {
                            var source = ctx.getSource();
                            if (!toolsEnabled) {
                                sendError(source, "Tools must be enabled to use this command.");
                                return 0;
                            }
                            clear();
                            sendSuccess(source, "Follow list cleared.");
                            return 1;
                        })
        );
    }

    private static void sendSuccess(FabricClientCommandSource source, String msg) {
        source.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.GREEN)));
    }

    private static void sendError(FabricClientCommandSource source, String msg) {
        source.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.RED)));
    }

    private static void sendInfo(FabricClientCommandSource source, String msg) {
        source.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.WHITE)));
    }
}