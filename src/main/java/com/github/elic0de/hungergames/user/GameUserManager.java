package com.github.elic0de.hungergames.user;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameUserManager {

    private static final Map<String, GameUser> onlineUsers = new HashMap<>();

    public static GameUser getGameUser(final @NotNull Player player) {
        final String uuid = player.getUniqueId().toString();
        if (onlineUsers.containsKey(uuid)) return onlineUsers.get(uuid);
        final GameUser gameUser = new GameUser(player);
        onlineUsers.put(uuid, gameUser);
        return gameUser;
    }

    public static Collection<GameUser> getOnlineUsers() {
        return onlineUsers.values();
    }

    public static void removeGameUser(final @NotNull Player player) {
        final String uuid = player.getUniqueId().toString();
        onlineUsers.remove(uuid);
    }
}
