package com.github.elic0de.battleroyale.user;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.eliccommon.user.OnlineUser;
import com.github.elic0de.eliccommon.util.ItemBuilder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class GameUser extends OnlineUser {

    private final Player player;

    private BigDecimal coins;

    public GameUser(Player player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    public void addItems() {
        final PlayerInventory inventory = player.getInventory();
        inventory.addItem(ItemBuilder.of(Material.BREAD).amount(20).build());
        inventory.addItem(ItemBuilder.of(Material.COMPASS).build());
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

    public void increaseKills() {
        // ここにキル数を増加させる処理を
        giveCoins(BigDecimal.valueOf(2));
    }

    public void giveCoins(BigDecimal coins) {
        coins.add(coins);
    }

    public void giveRewards(BigDecimal reward) {
        BattleRoyale.getInstance().getEconomyHook().ifPresent(hook -> hook.giveMoney(this, reward));
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
