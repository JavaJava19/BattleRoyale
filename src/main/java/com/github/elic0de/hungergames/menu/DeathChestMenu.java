package com.github.elic0de.hungergames.menu;

import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.chest.DeathChest;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class DeathChestMenu {

    private final InventoryGui menu;

    private final Player player;

    private final DeathChest deathChest = HungerGames.getInstance().getGame().getDeathChest();

    private static final String[] MENU_LAYOUT = {
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp"
    };

    public DeathChestMenu(ItemStack[] itemStacks, Consumer<ItemStack[]> items, Player player) {
        this.player = player;
        this.menu = new InventoryGui(HungerGames.getInstance(), "DeathChest", MENU_LAYOUT);
        Inventory inv = Bukkit.createInventory(null, 54);
        inv.setContents(itemStacks);
        menu.addElement(new GuiStorageElement('p', inv));
        menu.setCloseAction(close -> {
            items.accept(inv.getContents());
            return false;
        });
    }

    public void show() {
        menu.show(player);
    }
}
