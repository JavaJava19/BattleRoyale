package com.github.elic0de.hungergames.game;

import com.github.elic0de.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;

public class GameBorder {

    private final HungerGame game;

    private final WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();

    private BukkitTask borderTask;

    private double MAX_BORDER_SIZE = -0;

    private final AtomicLong borderTicks = new AtomicLong();

    public GameBorder(HungerGame game) {
        this.game = game;
        border.setDamageBuffer(2);
        border.setDamageAmount(0.5D);
    }

    public void start() {
        reset();
        double MIN_BORDER_SIZE = 16;
        MAX_BORDER_SIZE = border.getSize();
        border.setSize(MIN_BORDER_SIZE, 900);

        final long PERIOD = 20;
        final long timeInSeconds = 900;
        borderTicks.set(0);
        if (borderTask != null) borderTask.cancel();
        borderTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (borderTicks.incrementAndGet() >= timeInSeconds) {
                    game.getBossBar().hide();
                    cancel();
                    return;
                }
                game.getBossBar().setBossBar("残りのプレイヤー数: &6" + game.getAlivePlayersSize());
                game.getBossBar().setProgress((double) Math.max(timeInSeconds - borderTicks.get(), 0)/timeInSeconds);
            }
        }.runTaskTimer(HungerGames.getInstance(), 0, PERIOD);
    }

    public void stop() {
        border.setSize(border.getSize());
    }

    public void reset() {
        if (MAX_BORDER_SIZE != -0) border.setSize(MAX_BORDER_SIZE);
        if (borderTask != null) borderTask.cancel();
        borderTicks.set(0);
        game.getBossBar().hide();
    }
}
