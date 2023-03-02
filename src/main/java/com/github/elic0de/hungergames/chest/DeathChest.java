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
import java.util.Optional;

public class DeathChest {

    private final Map<Block, ItemStack[]> chestLocations = new HashMap<>();

    public void generateChest(GameUser user) {
        final Location chestLocation = user.getPlayer().getLocation();
        chestLocation.getWorld().setType(chestLocation, Material.CHEST);
        chestLocations.put(chestLocation.getBlock(), user.getPlayer().getInventory().getContents());
    }

    public void openDeathChest(Player player, Block block) {
        if (getChestContents(block).isPresent()) new DeathChestMenu(getChestContents(block).get(), itemStacks -> updateChestContents(block, itemStacks), player).show();
    }

    public void breakDeathChest(Block block) {
        final Location location = block.getLocation();
        getChestContents(block).ifPresent(itemStacks -> {
            for (ItemStack item : itemStacks) {
                if (item == null) continue;
                location.getWorld().dropItemNaturally(location, item);
            }
            chestLocations.remove(block);
        });
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

    public void updateChestContents(Block block, ItemStack[] contents) {
        if (chestLocations.containsKey(block)) chestLocations.put(block, contents);
    }

    public Optional<ItemStack[]> getChestContents(Block block) {
        if (chestLocations.containsKey(block)) {
            return Optional.of(chestLocations.get(block));
        }
        return Optional.empty();
    }

    public boolean containsDeathChest(Block block) {
        return  chestLocations.containsKey(block);
    }
}
