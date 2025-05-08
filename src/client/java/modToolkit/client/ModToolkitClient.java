package modToolkit.client;

import modToolkit.client.util.TaskManager;
import modToolkit.client.util.SettingsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.text.Text;

import modToolkit.client.commands.FollowCommand;
import modToolkit.client.commands.TaskCommand;
import modToolkit.client.util.FollowManager;


public class ModToolkitClient implements ClientModInitializer {

    private static final Set<String> previousPlayers = new HashSet<>();

    @Override
    public void onInitializeClient() {
        SettingsManager.load();
        FollowManager.init(); // This loads the staff data
        FollowManager.load(); // This loads followed players
        TaskManager.load();

        // Register commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Register external command classes
            FollowCommand.register(dispatcher);
            TaskCommand.register(dispatcher);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) return;

            Set<String> currentPlayers = new HashSet<>();

            // Update tab list every tick
            client.getNetworkHandler().getPlayerList().forEach(entry -> {
                String name = entry.getProfile().getName();
                UUID uuid = entry.getProfile().getId();
                currentPlayers.add(name);

                // THIS IS THE CRUCIAL LINE WE NEED TO ADD:
                FollowManager.updatePlayerEntry(uuid, name);

                if (SettingsManager.colorsShown && FollowManager.isFollowing(name)) {
                    Text original = entry.getDisplayName();

                    if (original != null) {
                        MutableText newDisplay = Text.empty().copy();
                        boolean nameStyled = false;

                        for (Text sibling : original.getSiblings()) {
                            if (!nameStyled && sibling.getString().equals(name)) {
                                newDisplay.append(Text.literal(name).styled(style -> style.withColor(SettingsManager.followColor)));
                                nameStyled = true;
                            } else {
                                newDisplay.append(sibling.copy());
                            }
                        }

                        if (!nameStyled) {
                            newDisplay.append(Text.literal(name).styled(style -> style.withColor(SettingsManager.followColor)));
                        }

                        entry.setDisplayName(newDisplay);
                    } else {
                        // No original display name — just color the name
                        entry.setDisplayName(Text.literal(name).styled(style -> style.withColor(SettingsManager.followColor)));
                    }
                } else {
                    entry.setDisplayName(null); // Let server display default
                }
            });

            // Detect joins
            for (String name : currentPlayers) {
                if (!previousPlayers.contains(name)) {
                    boolean shouldNotify = false;
                    String suffix = "";

                    String role = FollowManager.getStaffRole(name);
                    if (role != null && SettingsManager.notificationsStaff) {
                        shouldNotify = true;
                        suffix = " (" + role + ")";
                    } else if (SettingsManager.notificationsPlayers && FollowManager.isFollowing(name)) {
                        shouldNotify = true;
                    }

                    if (shouldNotify) {
                        client.inGameHud.getChatHud().addMessage(
                                Text.literal("[Mod Toolkit] ")
                                        .formatted(Formatting.LIGHT_PURPLE)
                                        .append(Text.literal(name + suffix + " joined the server.")
                                                .formatted(Formatting.GREEN)
                                        ));
                    }
                }
            }

            // Detect leaves
            for (String name : previousPlayers) {
                if (!currentPlayers.contains(name)) {
                    boolean shouldNotify = false;
                    String suffix = "";

                    String role = FollowManager.getStaffRole(name);
                    if (role != null && SettingsManager.notificationsStaff) {
                        shouldNotify = true;
                        suffix = " (" + role + ")";
                    } else if (SettingsManager.notificationsPlayers && FollowManager.isFollowing(name)) {
                        shouldNotify = true;
                    }

                    if (shouldNotify) {
                        client.inGameHud.getChatHud().addMessage(
                                Text.literal("[Mod Toolkit] ")
                                        .formatted(Formatting.LIGHT_PURPLE)
                                        .append(Text.literal(name + suffix + " left the server.")
                                                .formatted(Formatting.RED)
                                        ));
                    }
                }
            }

            previousPlayers.clear();
            previousPlayers.addAll(currentPlayers);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> previousPlayers.clear());
    }

    public static void sendSuccess(FabricClientCommandSource source, String msg) {
        source.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.GREEN)));
    }

    public static void sendError(FabricClientCommandSource source, String msg) {
        source.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.RED)));
    }

    public static void sendInfo(FabricClientCommandSource source, String msg) {
        source.sendFeedback(Text.literal("[Mod Toolkit] ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(msg).formatted(Formatting.WHITE)));
    }
}