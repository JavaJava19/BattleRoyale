package com.github.elic0de.battleroyale.game;

import com.github.elic0de.battleroyale.BattleRoyale;
import de.themoep.minedown.MineDown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;

public class GameBorder {

    private final Game game;

    private final WorldBorder border = Bukkit.getWorlds().get(0).getWorldBorder();

    private BukkitTask borderTask;

    private double MAX_BORDER_SIZE = -0;

    private final AtomicLong borderTicks = new AtomicLong();

    public GameBorder(Game game) {
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
        final long timeInSeconds = 1200;
        borderTicks.set(0);
        if (borderTask != null) borderTask.cancel();
        borderTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (borderTicks.incrementAndGet() >= timeInSeconds) {
                    // １５マスの円柱フィールドを作ってそこに全員TP
                    generateCycleFieldAndTeleport();
                    game.getBossBar().hide();
                    cancel();
                    return;
                }
                game.getBossBar().setBossBar("残りのプレイヤー数: &6" + game.getAlivePlayersSize());
                game.getBossBar().setProgress((double) Math.max(timeInSeconds - borderTicks.get(), 0) / timeInSeconds);
                game.getPlayers().forEach(player -> player.sendActionBar(new MineDown(String.format("ボーダーとの距離: &a%d &rブロック", (int) getDistanceToBorder(player.getPlayer(), border)))));
            }
        }.runTaskTimer(BattleRoyale.getInstance(), 0, PERIOD);
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

    private double getDistanceToBorder(Player player, WorldBorder border) {
        final double borderSize = border.getSize() / 2;
        return Math.abs(Math.min(
                borderSize - Math.abs(player.getLocation().getX() - border.getCenter().getX()),
                borderSize - Math.abs(player.getLocation().getZ() - border.getCenter().getZ())
        ));
    }

    private void generateCycleFieldAndTeleport() {
        game.getPlayers().forEach(player -> player.getPlayer().teleport(new Location(Bukkit.getWorld(""),1,1,1)));
    }
}
