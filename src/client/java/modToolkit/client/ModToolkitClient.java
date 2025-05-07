package modToolkit.client;

import modToolkit.client.util.TaskManager;
import modToolkit.client.util.SettingsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import modToolkit.client.commands.FollowCommand;
import modToolkit.client.commands.TaskCommand;
import modToolkit.client.util.FollowManager;

public class ModToolkitClient implements ClientModInitializer {

    public static boolean colorsShown = false;
    public static Formatting followColor = Formatting.YELLOW;

    @Override
    public void onInitializeClient() {
        SettingsManager.load();
        FollowManager.load();
        TaskManager.load();

        // Register commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Register external command classes
            FollowCommand.register(dispatcher);
            TaskCommand.register(dispatcher);
        });

        // Update tab list every tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) return;

            client.getNetworkHandler().getPlayerList().forEach(entry -> {
                String name = entry.getProfile().getName();
                if (colorsShown && FollowManager.isFollowing(name)) {
                    entry.setDisplayName(Text.literal(name).formatted(followColor));
                } else {
                    entry.setDisplayName(null);
                }
            });
        });
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