package com.github.elic0de.battleroyale.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CompassListener implements Listener {

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (!event.getItem().isSimilar(new ItemStack(Material.COMPASS))) return;

        final Player player = event.getPlayer();
        final Player nearestPlayer = getNearest(player, 300.0);
        final Material material = event.getMaterial();

        if (player.getCooldown(material) != 0) return;

        player.setCooldown(material, 60);

        if (nearestPlayer == null) {
            player.sendMessage("見つかりませんでした");
            return;
        }

        final String checkPosition = getPosition(player, nearestPlayer);

        player.setCompassTarget(nearestPlayer.getLocation());
        player.sendMessage("近くのプレイヤーを指しています。" + checkPosition);
    }

    public Player getNearest(Player p, Double range) {
        double distance = Double.POSITIVE_INFINITY; // To make sure the first
        // player checked is closest
        Player target = null;
        for (Entity e : p.getNearbyEntities(range, range, range)) {
            if (!(e instanceof Player)) continue;
            if (e == p) continue; //Added this check so you don't target yourself.
            if (((Player) e).getGameMode() == GameMode.SPECTATOR) continue;
            double distanceto = p.getLocation().distance(e.getLocation());
            if (distanceto > distance)
                continue;
            distance = distanceto;
            target = (Player) e;
        }
        return target;
    }

    public String getPosition(Player player, Player nearestPlayer) {
        final Location playerLoc = player.getLocation();
        final Location nearestPlayerLoc = nearestPlayer.getLocation();

        if (playerLoc.getY() == nearestPlayerLoc.getY()) return "(-)";
        if (playerLoc.getY() <=  nearestPlayerLoc.getY()) return "(↑)";

        return "(↓)";
    }
}
