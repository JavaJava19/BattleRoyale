package com.github.elic0de.battleroyale.config;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public class KitConfig {

    private final String name;
    private final String description;
    private final Material icon;
    private final ItemStack[] contents;

    public KitConfig(String name, String description, Material icon, ItemStack[] contents) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.contents = contents;
    }
}
