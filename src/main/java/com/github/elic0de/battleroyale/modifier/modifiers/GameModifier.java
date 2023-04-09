package com.github.elic0de.battleroyale.modifier.modifiers;

import com.github.elic0de.battleroyale.BattleRoyale;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

@Getter
public class GameModifier implements Listener {

    private final BattleRoyale plugin = BattleRoyale.getInstance();

    private final String symbol;
    private final String name;
    private final String description;
    private final ChatColor color;

    protected GameModifier(String symbol, String name, String description, ChatColor color) {
        this.symbol = symbol;
        this.name = name;
        this.description = description;
        this.color = color;
        Bukkit.getPluginManager().registerEvents(this, BattleRoyale.getInstance());
    }

    public void modify() {

    }

}
