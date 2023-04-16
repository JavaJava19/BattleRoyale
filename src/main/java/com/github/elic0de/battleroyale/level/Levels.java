package com.github.elic0de.battleroyale.level;

import com.github.elic0de.battleroyale.user.GameUser;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class Levels {

    private final static HashMap<Integer, Level> LEVELS = new HashMap<>();

    static {
        initialize();
    }

    public static void initialize() {
        // todo 可読性に欠けるためレベリングシステム見直す必要あり　
        Stream.of(
                "1,15,15," + ChatColor.GRAY.name(),
                        "10,30,165," + ChatColor.BLUE.name(),
                        "20,50,480," + ChatColor.DARK_AQUA.name(),
                        "30,75,1000," + ChatColor.DARK_GREEN.name(),
                        "40,125,1775," + ChatColor.GREEN.name(),
                        "50,250,4550," + ChatColor.YELLOW.name(),
                        "60,600,10800," + ChatColor.GOLD.name(),
                        "70,800,16000," + ChatColor.RED.name(),
                        "80,900,28000," + ChatColor.DARK_RED.name(),
                        "90,1000,381000," + ChatColor.AQUA.name()
                ).map(text -> text.split(","))
                .map(data -> new Level(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), ChatColor.valueOf(data[3])))
                .forEach(level -> LEVELS.put(level.getLevel(), level));

        final HashMap<Integer, Level> addedLevels = new HashMap<>();
        for (Level level : LEVELS.values()) {
            final int l = level.getLevel();
            final int neededXp = level.getNeededXP();
            final ChatColor color = level.getLevelColor();
            int totalXp = level.getTotalXp();
            for (int i = 1; i < 10; i++) {
                final int nextLevel = l + i;
                if (LEVELS.containsKey(nextLevel)) continue;
                totalXp += neededXp;
                addedLevels.put(nextLevel, new Level(nextLevel, neededXp, totalXp, color));
            }
        }
        LEVELS.putAll(addedLevels);
    }

    public static int getPlayerLevel(GameUser player) {
        final List<Integer> requirements = LEVELS.values().stream().map(Level::getTotalXp)
                .toList();
        int maxLevel = requirements.size();
        for (int i = 0; i < maxLevel; i++) {
            if (player.getXp() < requirements.get(i)) {
                return i + 1;
            }
        }
        return maxLevel;
    }

    public static int getPlayerNeededXP(int playerLevel, int currentXp) {
        if (LEVELS.containsKey(playerLevel)) {
            final Level level = LEVELS.get(playerLevel);
            final int requirementXp = level.getTotalXp() - currentXp;
            if (requirementXp > 0) {
                return requirementXp;
            }
        }
        return 0;
    }


    public static ChatColor getPlayerLevelColor(int playerLevel) {
        if (LEVELS.containsKey(playerLevel)) {
            final Level level = LEVELS.get(playerLevel);
            return level.getLevelColor();
        }
        return ChatColor.GRAY;
    }
}
