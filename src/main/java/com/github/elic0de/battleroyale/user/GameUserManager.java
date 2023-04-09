package com.github.elic0de.battleroyale.user;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GameUserManager {

    private static final Map<String, GameUser> onlineUsers = new HashMap<>();

    public static GameUser getGameUser(final @NotNull Player player) {
        final String uuid = player.getUniqueId().toString();
        if (onlineUsers.containsKey(uuid)) {
            return onlineUsers.get(uuid);
        }
        final GameUser gameUser = new GameUser(player);
        onlineUsers.put(uuid, gameUser);
        return gameUser;
    }

    public static void unRegisterUser(Player player) {
        onlineUsers.remove(player.getUniqueId().toString());
    }

    public static Collection<GameUser> getOnlineUsers() {
        return  Bukkit.getOnlinePlayers().stream().map(GameUserManager::getGameUser).filter(user -> user.getPlayer().isOnline()).collect(Collectors.toList());
    }
}
