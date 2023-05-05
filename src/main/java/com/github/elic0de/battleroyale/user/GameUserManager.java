package com.github.elic0de.battleroyale.user;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.utils.annoData.AnnoData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameUserManager {

    private static final Map<String, GameUser> onlineUsers = new HashMap<>();

    public static Optional<GameUser> getGameUser(final @NotNull String playerName) {
        final Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return Optional.of(getGameUser(player));
        }
        return Optional.empty();
    }

    public static GameUser getGameUser(final @NotNull Player player) {
        final String uuid = player.getUniqueId().toString();
        if (onlineUsers.containsKey(uuid)) {
            return onlineUsers.get(uuid);
        }
        final GameUserData userData = BattleRoyale.getInstance().getDatabase().find(GameUserData.class, player.getUniqueId().toString());
        if (userData.getUniqueId() == null) {
            userData.setUniqueId(player.getUniqueId().toString());
            BattleRoyale.getInstance().getDatabase().insertData(userData);
        }

        final GameUser gameUser = new GameUser(player, userData);
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
