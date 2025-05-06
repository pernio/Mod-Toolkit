package emcMod.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import emcMod.client.commands.FollowCommand;
import emcMod.client.commands.TaskCommand;
import emcMod.client.util.FollowManager;

import java.util.List;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class EmcModClient implements ClientModInitializer {

    public static boolean toolsEnabled = false;
    public static Formatting followColor = Formatting.YELLOW;

    @Override
    public void onInitializeClient() {
        // Register commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("tools")
                            .executes(ctx -> {
                                toolsEnabled = !toolsEnabled;
                                ctx.getSource().sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                                        .append(toolsEnabled
                                                ? Text.literal("Tools enabled").formatted(Formatting.GREEN)
                                                : Text.literal("Tools disabled.").formatted(Formatting.RED)));
                                return 1;
                            })
                            .then(literal("on").executes(ctx -> {
                                toolsEnabled = true;
                                ctx.getSource().sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                                        .append(Text.literal("Tools enabled").formatted(Formatting.GREEN)));
                                return 1;
                            }))
                            .then(literal("off").executes(ctx -> {
                                toolsEnabled = false;
                                ctx.getSource().sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                                        .append(Text.literal("Tools disabled").formatted(Formatting.RED)));
                                return 1;
                            }))
            );

            // /colorfollow command
            dispatcher.register(
                    literal("colorfollow")
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
                                                ctx.getSource().sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                                                        .append(Text.literal("Invalid color. Choose a predefined color.").formatted(Formatting.RED)));
                                                return 1;
                                            }
                                        }
                                        ctx.getSource().sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                                                .append(Text.literal("Set follow color to " + followColor.getName()).formatted(Formatting.GREEN)));
                                        return 1;
                                    }))
            );

            // Register external command classes
            FollowCommand.register(dispatcher);
            TaskCommand.register(dispatcher);
        });

        // Update tab list every tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) return;

            client.getNetworkHandler().getPlayerList().forEach(entry -> {
                String name = entry.getProfile().getName();
                if (toolsEnabled && FollowManager.isFollowing(name)) {
                    entry.setDisplayName(Text.literal(name).formatted(followColor));
                } else {
                    entry.setDisplayName(null);
                }
            });
        });

        // Optionally clear follow list on disconnect
        // ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FollowManager.clear());
    }
}