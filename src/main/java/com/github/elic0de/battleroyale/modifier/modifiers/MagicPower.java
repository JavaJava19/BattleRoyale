package com.github.elic0de.battleroyale.modifier.modifiers;

import com.github.elic0de.battleroyale.event.GamePlayerKillEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MagicPower extends GameModifier {

    public MagicPower() {
        super("☣", "Magic Power", "敵を倒した時、またはアシストでランダムな良いポーション効果が付与される。", ChatColor.GREEN);
    }

    @EventHandler
    private void on(GamePlayerKillEvent event) {
        if (getPlugin().getGame().getModifierManager().getCurrentMission() != this) return;
        event.getKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20, 2));
    }
}