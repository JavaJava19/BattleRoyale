package com.github.elic0de.hungergames.menu;

import com.github.elic0de.hungergames.HungerGames;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathChestMenu {

    private final InventoryGui menu;

    private final Player player;

    private static final String[] MENU_LAYOUT = {
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "ppppppppp"
    };
    public DeathChestMenu(ItemStack[] contents, Player player) {
        this.player = player;
        this.menu = new InventoryGui(HungerGames.getInstance(), "DeathChest", MENU_LAYOUT);
        Inventory inv = Bukkit.createInventory(null, 54);
        inv.setContents(contents);
        menu.addElement(new GuiStorageElement('p', inv));
        menu.setCloseAction(close -> false);

    }

    public void show() {
        menu.show(player);
    }
}
