package com.github.elic0de.hungergames.modifier.modifiers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrueUHC extends GameModifier {

    public TrueUHC() {
        super('❁', "True UHC", "モンスターや落下ダメージなどの自然ダメージが3倍になる。", ChatColor.RED);
    }

    @EventHandler
    private void on(EntityDamageEvent event) {
        /*if (event.getEntity() instanceof Player player) {
            event.setDamage(event.getDamage() * 3);
        }*/
    }

}
