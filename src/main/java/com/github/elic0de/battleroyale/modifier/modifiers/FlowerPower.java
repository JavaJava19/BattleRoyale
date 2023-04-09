package com.github.elic0de.battleroyale.modifier.modifiers;

import com.github.elic0de.eliccommon.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.Random;

public class FlowerPower extends GameModifier {

    public FlowerPower() {
        super("❁", "Flower Power", "花を壊した時にランダムなアイテムがドロップする。", ChatColor.YELLOW);
    }

    @EventHandler
    private void on(BlockBreakEvent event) {
        if (getPlugin().getGame().getModifierManager().getCurrentMission() != this) return;
        if (event.getBlock().getType() != Material.POPPY) return;

        event.setDropItems(false);
        Arrays.stream(Material.values()).skip(new Random().nextInt(Material.values().length)).findAny().ifPresent(material ->
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ItemBuilder.of(material).build()));
    }
}
