package com.github.elic0de.hungergames.menu;

import com.github.elic0de.hungergames.HungerGames;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathChestMenu {

    private final InventoryGui menu;

    private static final String[] MENU_LAYOUT = {
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp"
    };
    public DeathChestMenu(ItemStack[] contents) {
        this.menu = new InventoryGui(HungerGames.getInstance(), "DeathChest", MENU_LAYOUT);
        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST);
        inv.setContents(contents);
        menu.addElement(new GuiStorageElement('p', inv));
        menu.setCloseAction(close -> false);
    }

    public void show(Player player) {
        menu.show(player);
    }
}
