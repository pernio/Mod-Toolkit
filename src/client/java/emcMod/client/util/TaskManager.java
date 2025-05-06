package emcMod.client.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskManager {
    private static final Map<String, String> tasks = new HashMap<>();

    public static void addTask(String name, String description) {
        tasks.put(name.toLowerCase(Locale.ROOT), description);
    }

    public static String getTask(String name) {
        return tasks.get(name.toLowerCase(Locale.ROOT));
    }

    public static boolean removeTask(String name) {
        return tasks.remove(name.toLowerCase(Locale.ROOT)) != null;
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
}