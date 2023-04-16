package com.github.elic0de.battleroyale.level;

import lombok.Data;
import org.bukkit.ChatColor;

@Data
public class Level {

    private final int lev;
    private final int neededXP;

    private final int totalXp;

    private final ChatColor levelColor;

    public Level(int lev, int neededXP, int totalXp , ChatColor color) {
        this.lev = lev;
        this.neededXP = neededXP;
        this.totalXp = totalXp;
        this.levelColor = color;
    }

    public int nextLevel() {
        return lev + 1;
    }

    public int previousLevel() {
        return Math.max(lev - 1, 0);
    }

    public int getLevel() {
        return lev;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public int getNeededXP() {
        return neededXP;
    }

    public ChatColor getLevelColor() {
        return levelColor;
    }
}