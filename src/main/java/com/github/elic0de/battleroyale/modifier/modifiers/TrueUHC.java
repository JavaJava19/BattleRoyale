package com.github.elic0de.battleroyale.modifier.modifiers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrueUHC extends GameModifier {

    public TrueUHC() {
        super("❁", "True UHC", "モンスターや落下ダメージなどの自然ダメージが3倍になる。", ChatColor.RED);
    }

    @EventHandler
    private void on(EntityDamageEvent event) {
        if (getPlugin().getGame().getModifierManager().getCurrentMission() != this) return;
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
            event.setDamage(event.getDamage() * 3);
        }
    }
}
