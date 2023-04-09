package com.github.elic0de.battleroyale.game;

import com.github.elic0de.battleroyale.user.GameUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.NotNull;

public final class GameBossBar {

    private final @NotNull BossBar bossBar;

    public GameBossBar() {
        this.bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.bossBar.setVisible(false);
        this.bossBar.setProgress(1);
        this.bossBar.setStyle(BarStyle.SEGMENTED_20);
        this.bossBar.setColor(BarColor.YELLOW);
    }

    public void addPlayer(@NotNull GameUser user) {
        bossBar.addPlayer(user.getPlayer());
    }

    public void removePlayer(@NotNull GameUser user) {
        bossBar.removePlayer(user.getPlayer());
    }

    public void reset() {
        bossBar.removeAll();
    }

    public void setBossBar(@NotNull String title) {
        final String coloredTitle = ChatColor.translateAlternateColorCodes('&', title);
        bossBar.setTitle(coloredTitle);
        this.bossBar.setVisible(true);
    }

    public void setProgress(double progress) {
        bossBar.setProgress(progress);
    }

    public void hide() {
        bossBar.setVisible(false);
    }
}