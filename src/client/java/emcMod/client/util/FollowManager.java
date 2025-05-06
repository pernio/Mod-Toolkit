package emcMod.client.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FollowManager {
    private static final Set<String> followedPlayers = new HashSet<>();

    public static boolean add(String name) {
        return followedPlayers.add(name.toLowerCase(Locale.ROOT));
    }

    public static boolean remove(String name) {
        return followedPlayers.remove(name.toLowerCase(Locale.ROOT));
    }

    public static void clear() {
        followedPlayers.clear();
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
}