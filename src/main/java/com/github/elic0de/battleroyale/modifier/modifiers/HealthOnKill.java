package com.github.elic0de.battleroyale.modifier.modifiers;

import com.github.elic0de.battleroyale.event.GamePlayerKillEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

public class HealthOnKill extends GameModifier {

    public HealthOnKill() {
        super("❁", "Health On Kill", "敵を倒すと最大HPが増加する。", ChatColor.YELLOW);
    }

    @EventHandler
    private void on(GamePlayerKillEvent event) {
        if (getPlugin().getGame().getModifierManager().getCurrentMission() != this) return;
        event.getKiller().getPlayer().setHealth(20);
    }
}
