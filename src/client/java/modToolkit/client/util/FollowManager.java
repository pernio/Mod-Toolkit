package modToolkit.client.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FollowManager {
    private static final Set<String> followedPlayers = new HashSet<>();
    private static final Path FOLLOW_FILE = Paths.get("config", "modtoolkit", "follows.json");
    private static final Gson GSON = new Gson();

    public static boolean add(String name) {
        boolean added = followedPlayers.add(name.toLowerCase(Locale.ROOT));
        if (added) save(); // Save only if changed
        return added;
    }

    public static boolean remove(String name) {
        boolean removed = followedPlayers.remove(name.toLowerCase(Locale.ROOT));
        if (removed) save(); // Save only if changed
        return removed;
    }

    public static void clear() {
        followedPlayers.clear();
        save();
    }

    public static Set<String> getFollowedPlayers() {
        return followedPlayers;
    }

    public static boolean isFollowing(String name) {
        return followedPlayers.contains(name.toLowerCase(Locale.ROOT));
    }

    public static boolean isEmpty() {
        return followedPlayers.isEmpty();
    }

    public static void load() {
        try {
            if (!Files.exists(FOLLOW_FILE)) return;

            String json = Files.readString(FOLLOW_FILE);
            Type setType = new TypeToken<Set<String>>() {}.getType();
            Set<String> loaded = GSON.fromJson(json, setType);
            if (loaded != null) {
                followedPlayers.clear();
                followedPlayers.addAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FOLLOW_FILE.getParent());
            String json = GSON.toJson(followedPlayers);
            Files.writeString(FOLLOW_FILE, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}