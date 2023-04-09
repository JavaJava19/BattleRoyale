package com.github.elic0de.battleroyale.modifier.modifiers;

import com.github.elic0de.eliccommon.util.ItemBuilder;
import com.github.elic0de.battleroyale.event.GamePlayerKillEvent;
import com.github.elic0de.battleroyale.user.GameUserManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;

public class Pearls extends GameModifier {

    public Pearls() {
        super("❁", "Pearls", "最初にエンダーパールが3つ貰える。敵を倒した際にもエンダーパールをドロップする。", ChatColor.LIGHT_PURPLE);
    }

    @EventHandler
    private void on(GamePlayerKillEvent event) {
        if (getPlugin().getGame().getModifierManager().getCurrentMission() != this) return;
        event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), ItemBuilder.of(Material.ENDER_PEARL).build());
    }

    @Override
    public void modify() {
        GameUserManager.getOnlineUsers().forEach(user -> user.getPlayer().getInventory().addItem(ItemBuilder.of(Material.ENDER_PEARL).amount(3).build()));
    }
}
