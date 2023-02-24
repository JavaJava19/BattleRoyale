package com.github.elic0de.hungergames.chest;

import com.github.elic0de.hungergames.menu.DeathChestMenu;
import com.github.elic0de.hungergames.user.GameUser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DeathChest {

    private final Map<Block, ItemStack[]> chestLocations = new HashMap<>();

    public void generateChest(GameUser user) {
        final Location chestLocation = user.getPlayer().getLocation();
        chestLocation.getWorld().setType(chestLocation, Material.CHEST);
        chestLocations.put(chestLocation.getBlock(), user.getPlayer().getInventory().getContents());
    }

    public void openDeathChest(Player player, Block block) {
        if (chestLocations.containsKey(block)) {
            final ItemStack[] contents = chestLocations.get(block);
            new DeathChestMenu(contents, player).show();
        }
    }

    public void reset() {
        restoreChest();
        chestLocations.clear();
    }

    private void restoreChest() {
        for (Block block : chestLocations.keySet())  {
            block.setType(Material.AIR);
        }
    }

    public boolean containsDeathChest(Block block) {
        return  chestLocations.containsKey(block);
    }
}
