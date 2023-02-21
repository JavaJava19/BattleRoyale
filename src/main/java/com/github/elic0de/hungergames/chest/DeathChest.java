package com.github.elic0de.hungergames.chest;

import com.github.elic0de.hungergames.menu.DeathChestMenu;
import com.github.elic0de.hungergames.user.GameUser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DeathChest {

    private final Map<Location, ItemStack[]> chestLocations = new HashMap<>();

    public void generateChest(GameUser user) {
        final Location chestLocation = user.getPlayer().getLocation();
        chestLocation.getWorld().setType(chestLocation, Material.CHEST);
        chestLocations.put(chestLocation, user.getPlayer().getInventory().getContents());
    }

    public void openDeathChest(Player player, Location location) {
        if (chestLocations.containsKey(location)) {
            final ItemStack[] contents = chestLocations.get(location);
            new DeathChestMenu(contents).show(player);
        }
    }

    public void reset() {
        chestLocations.clear();
    }
}
