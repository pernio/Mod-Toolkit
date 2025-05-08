package modToolkit.client.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FollowManager {
    private static final Set<String> followedPlayers = new HashSet<>();
    private static final Map<UUID, String> staffRoles = new HashMap<>(); // UUID to role mapping
    private static final Map<UUID, String> uuidToNameCache = new HashMap<>();
    private static final Map<String, String> nameToRoleCache = new HashMap<>(); // New cache for name->role mapping
    private static final Path FOLLOW_FILE = Paths.get("config", "modtoolkit", "follows.json");
    private static final Gson GSON = new Gson();

    public static void init() {
        loadStaffData();
    }

    private static void loadStaffData() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/jwkerr/staff/master/staff.json");
                try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
                    JsonObject staffData = GSON.fromJson(reader, JsonObject.class);

                    synchronized (staffRoles) {
                        staffRoles.clear();
                        uuidToNameCache.clear();
                        nameToRoleCache.clear();

                        // Process each role category
                        for (String role : staffData.keySet()) {
                            staffData.getAsJsonArray(role).forEach(element -> {
                                try {
                                    UUID uuid = UUID.fromString(element.getAsString());
                                    staffRoles.put(uuid, role.toLowerCase());

                                    // Try to resolve name immediately if player is online
                                    resolveAndCachePlayerName(uuid, role);
                                } catch (IllegalArgumentException e) {
                                    System.err.println("Invalid UUID format: " + element.getAsString());
                                }
                            });
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to load staff data: " + e.getMessage());
            }
        });
    }

    private static void resolveAndCachePlayerName(UUID uuid, String role) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            PlayerListEntry entry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(uuid);
            if (entry != null) {
                String name = entry.getProfile().getName();
                synchronized (uuidToNameCache) {
                    uuidToNameCache.put(uuid, name);
                    nameToRoleCache.put(name.toLowerCase(Locale.ROOT), role);
                }
            }
        }
    }

    public static boolean add(String name) {
        boolean added = followedPlayers.add(name.toLowerCase(Locale.ROOT));
        if (added) save();
        return added;
    }

    public static boolean remove(String name) {
        boolean removed = followedPlayers.remove(name.toLowerCase(Locale.ROOT));
        if (removed) save();
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

    public static boolean isStaff(String name) {
        synchronized (nameToRoleCache) {
            return nameToRoleCache.containsKey(name.toLowerCase(Locale.ROOT));
        }
    }

    public static String getStaffRole(String name) {
        synchronized (nameToRoleCache) {
            return nameToRoleCache.get(name.toLowerCase(Locale.ROOT));
        }
    }

    public static void updatePlayerEntry(UUID uuid, String name) {
        synchronized (staffRoles) {
            if (staffRoles.containsKey(uuid)) {
                synchronized (uuidToNameCache) {
                    uuidToNameCache.put(uuid, name);
                    nameToRoleCache.put(name.toLowerCase(Locale.ROOT), staffRoles.get(uuid));
                }
            }
        }
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