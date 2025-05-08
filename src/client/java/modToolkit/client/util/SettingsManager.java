package modToolkit.client.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.*;

public class SettingsManager {
    private static final Path SETTINGS_FILE = Paths.get("config", "modtoolkit", "settings.json");
    private static final Gson GSON = new Gson();

    public static boolean colorsShown = false;
    public static boolean notificationsPlayers = true;
    public static boolean notificationsStaff = true;
    public static Formatting followColor = Formatting.YELLOW;

    public static void load() {
        try {
            if (!Files.exists(SETTINGS_FILE)) {
                save(); // Create default settings file if it doesn't exist
                return;
            }

            String json = Files.readString(SETTINGS_FILE);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);

            // Load all settings with fallback to defaults
            colorsShown = obj.has("colorsShown") && obj.get("colorsShown").getAsBoolean();
            notificationsPlayers = !obj.has("notificationsPlayers") || obj.get("notificationsPlayers").getAsBoolean();
            notificationsStaff = !obj.has("notificationsStaff") || obj.get("notificationsStaff").getAsBoolean();

            if (obj.has("followColor")) {
                String colorName = obj.get("followColor").getAsString().toUpperCase();
                try {
                    followColor = Formatting.valueOf(colorName);
                } catch (IllegalArgumentException ignored) {
                    followColor = Formatting.YELLOW;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());

            JsonObject obj = new JsonObject();
            obj.addProperty("colorsShown", colorsShown);
            obj.addProperty("notificationsPlayers", notificationsPlayers);
            obj.addProperty("notificationsStaff", notificationsStaff);
            obj.addProperty("followColor", followColor.name());

            String json = GSON.toJson(obj);
            Files.writeString(SETTINGS_FILE, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}