package com.github.elic0de.battleroyale.user;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.level.Levels;
import com.github.elic0de.eliccommon.user.OnlineUser;
import com.github.elic0de.eliccommon.util.ItemBuilder;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;


@Getter
public class GameUser extends OnlineUser {

    private final Player player;
    private final GameUserData data;

    public GameUser(Player player, GameUserData data) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
        this.data = data;
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
        increaseXP();
        data.setKills(data.getKills() + 1);
    }

    public void increaseXP() {
        data.setXp(data.getXp());
        // レベルアップ
        if (Levels.getPlayerNeededXP(data.getLevel(), (int) data.getXp()) == 0) levelUp();
        //getBoard().updateNeededXp();
    }

    public void giveCoins(BigDecimal coins) {
        coins.add(coins);
    }

    public void giveRewards(BigDecimal reward) {
        BattleRoyale.getInstance().getEconomyHook().ifPresent(hook -> hook.giveMoney(this, reward));
    }

    public void levelUp() {
        final int level = data.getLevel();
        final int nextLevel = level + 1;
        final int previousLevel = level;
        //player.sendTitle("§b§lLEVEL UP!", previousLevel + " → " + nextLevel, 20,40, 20);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        updateDisplayName();
        data.setLevel(nextLevel);
        //getBoard().updateLevel();
    }

    public void updateDisplayName() {
        final int level = data.getLevel();

        final ChatColor color = Levels.getPlayerLevelColor(level);
        player.setDisplayName("[" + color + level + ChatColor.RESET + "]" + " " + getUsername());
        player.setPlayerListName("[" + color + level + ChatColor.RESET + "]" + " " + getUsername());
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
