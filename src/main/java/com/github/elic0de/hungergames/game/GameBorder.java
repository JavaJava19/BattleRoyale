package com.github.elic0de.hungergames.game;

import com.github.elic0de.hungergames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;

public class GameBorder {

    private final HungerGame game;

    private final GameBossBar bossBar;
    private final WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();
    private final double MAX_BORDER_SIZE = 500;

    private BukkitTask borderTask;

    private final AtomicLong borderTicks = new AtomicLong();

    public GameBorder(HungerGame game) {
        this.game = game;
        this.bossBar = game.getBossBar();
        border.setDamageBuffer(2);
        border.setDamageAmount(0.5D);
        border.setSize(MAX_BORDER_SIZE);
    }

    public void start() {
        reset();
        double MIN_BORDER_SIZE = 16;
        border.setSize(MIN_BORDER_SIZE, 900);

        final long PERIOD = 20;
        final long timeInSeconds = 900;
        borderTicks.set(0);
        if (borderTask != null) borderTask.cancel();
        borderTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (borderTicks.incrementAndGet() >= timeInSeconds) {
                    bossBar.hide();
                    cancel();
                    return;
                }
                bossBar.setBossBar("残りのプレイヤー数表示 &6" + (game.getPlayers().size() - game.getDeadPlayers().size()));
                bossBar.setProgress((double) Math.max(timeInSeconds - borderTicks.get(), 0)/timeInSeconds);
            }
        }.runTaskTimer(HungerGames.getInstance(), 0, PERIOD);
    }

    public void stop() {
        border.setSize(border.getSize());
    }

    public void reset() {
        border.setSize(MAX_BORDER_SIZE);
        if (borderTask != null) borderTask.cancel();
        borderTicks.set(0);
        bossBar.hide();
    }
}
