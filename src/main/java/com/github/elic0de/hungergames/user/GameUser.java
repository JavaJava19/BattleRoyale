package com.github.elic0de.hungergames.user;

import com.github.elic0de.eliccommon.user.OnlineUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameUser extends OnlineUser {

    private final Player player;

    public GameUser(Player player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
