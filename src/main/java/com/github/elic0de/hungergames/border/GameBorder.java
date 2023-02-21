package com.github.elic0de.hungergames.border;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;

public class GameBorder {

    private final WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();

    private final double MAX_BORDER_SIZE = 500;

    public GameBorder() {
        border.setDamageBuffer(1);
        border.setSize(MAX_BORDER_SIZE);
    }

    public void start() {
        reset();
        double MIN_BORDER_SIZE = 16;
        border.setSize(MIN_BORDER_SIZE, 900);
    }

    public void stop() {
        border.setSize(border.getSize());
    }

    public void reset() {
        border.setSize(MAX_BORDER_SIZE);
    }
}
