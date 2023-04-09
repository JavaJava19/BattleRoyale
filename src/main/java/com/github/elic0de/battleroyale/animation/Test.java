package com.github.elic0de.battleroyale.animation;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.animation.text.ZoomInAnimation;
import com.github.elic0de.battleroyale.animation.text.ZoomOutAnimation;
import com.github.elic0de.battleroyale.modifier.modifiers.GameModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;

public class Test {

    // todo
    public void animation(Collection<GameModifier> gameModifiers) {
        Bukkit.getOnlinePlayers().forEach(player -> new BukkitRunnable() {
            int frame = 0;
            float pitch = 1.0f;

            @Override
            public void run() {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', new ZoomOutAnimation().animate("ゲームモディファイアー", frame)), "ピックされました！", 0, 70, 0);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, Math.min(2.0f, pitch));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, Math.min(2.0f, pitch));

                frame++;
                pitch += .1;
                if (frame == 15) {
                    cancel();
                    startSlotAnimationTitle(player, gameModifiers);
                }
            }
        }.runTaskTimer(BattleRoyale.getInstance(), 0L, 2));
    }

    // todo
    public void startSlotAnimationTitle(Player player, Collection<GameModifier> modifiers) {
        List<String> symbols = modifiers.stream().map(modifier ->
                String.format(modifier.getColor() + "%s,%s,%s", modifier.getColor(), modifier.getSymbol(), modifier.getColor() + modifier.getName())).toList();

        new BukkitRunnable() {
            int frameIn = 0;
            int frameIndex = 0;
            int cycles = 10;

            @Override
            public void run() {
                final String[] frame = symbols.get(frameIndex).split(",");
                final String color = frame[0];
                final String title = frame[1];
                final String subtitle = frame[2];
                final GameModifier pickedModifier = BattleRoyale.getInstance().getGame().getModifierManager().getCurrentMission();

                if (cycles <= 5) {

                    player.sendTitle(String.format(pickedModifier.getColor() + "[%s]", pickedModifier.getSymbol()), pickedModifier.getColor() + pickedModifier.getName(), 0, 20, 0);


                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, .5f);
                } else {
                    player.sendTitle(color + new ZoomInAnimation().animate(title, frameIn), subtitle, 0, 20, 0);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 2f);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, .5f);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1);
                }
                frameIn++;
                frameIndex++;
                if (frameIndex >= symbols.size()) {
                    frameIndex = 0;
                    cycles--;

                    if (cycles <= 0) {
                        cancel();
                        pickedModifier.modify();
                    }
                }
            }
        }.runTaskTimer(BattleRoyale.getInstance(), 0L, 2L);
    }
}
