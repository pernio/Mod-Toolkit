package modToolkit.client.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskManager {
    private static final Map<String, String> tasks = new HashMap<>();
    private static final Path FOLLOW_FILE = Paths.get("config", "modtoolkit", "tasks.json");
    private static final Gson GSON = new Gson();

    public static void addTask(String name, String description) {
        tasks.put(name.toLowerCase(Locale.ROOT), description);
        save();
    }

    public static String getTask(String name) {
        return tasks.get(name.toLowerCase(Locale.ROOT));
    }

    public static boolean removeTask(String name) {
        boolean removed = tasks.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) save();
        return removed;
    }

    public static void clearTasks() {
        tasks.clear();
    }

    public static boolean hasTasks() {
        return !tasks.isEmpty();
    }

    public static Map<String, String> getAllTasks() {
        return tasks;
    }

    public static boolean updateTaskDescription(String name, String newDescription) {
        if (!tasks.containsKey(name)) return false;
        tasks.put(name, newDescription);
        save();
        return true;
    }

    public static boolean renameTask(String oldName, String newName) {
        if (!tasks.containsKey(oldName) || tasks.containsKey(newName)) return false;
        String desc = tasks.remove(oldName);
        tasks.put(newName.toLowerCase(Locale.ROOT), desc);
        save();
        return true;
    }

    public static void load() {
        try {
            if (!Files.exists(FOLLOW_FILE)) return;

            String json = Files.readString(FOLLOW_FILE);
            Type mapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = GSON.fromJson(json, mapType);
            if (loaded != null) {
                tasks.clear();
                tasks.putAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FOLLOW_FILE.getParent());
            String json = GSON.toJson(tasks);
            Files.writeString(FOLLOW_FILE, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}