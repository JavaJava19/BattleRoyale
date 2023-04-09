package com.github.elic0de.battleroyale.modifier.modifiers;

import com.github.elic0de.battleroyale.user.GameUserManager;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.World;

public class NightTime extends GameModifier {

    public NightTime() {
        super("❁", "Night Time", "時刻が常に夜になる。地上にモンスターがスポーンする", ChatColor.DARK_AQUA);
    }

    @Override
    public void modify() {
        GameUserManager.getOnlineUsers().forEach(user -> {
            final World world = user.getPlayer().getWorld();
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(2000);
        });
    }
}
