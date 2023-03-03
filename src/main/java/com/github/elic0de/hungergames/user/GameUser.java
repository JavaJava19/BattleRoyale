package com.github.elic0de.hungergames.user;

import com.github.elic0de.eliccommon.user.OnlineUser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameUser extends OnlineUser {

    private final Player player;

    public GameUser(Player player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    public void sendActionBar(String message) {
        final String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(coloredMessage).create());
    }

    public void clearEffectAndHeal() {
        player.getActivePotionEffects().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
