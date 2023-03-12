package com.github.elic0de.hungergames.modifier.modifiers;

import com.github.elic0de.hungergames.HungerGames;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

@Getter
public class GameModifier implements Listener {

    private final char symbol;
    private final String name;
    private final String description;

    private final ChatColor color;

    protected GameModifier(char symbol, String name, String description, ChatColor color) {
        this.symbol = symbol;
        this.name = name;
        this.description = description;
        this.color = color;
        Bukkit.getPluginManager().registerEvents(this, HungerGames.getInstance());
    }

}
