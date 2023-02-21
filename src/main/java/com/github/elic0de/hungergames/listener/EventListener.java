package com.github.elic0de.hungergames.listener;

import com.github.elic0de.eliccommon.util.ItemBuilder;
import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.game.phase.InGamePhase;
import com.github.elic0de.hungergames.user.GameUser;
import com.github.elic0de.hungergames.user.GameUserManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityDismountEvent;

public class EventListener implements Listener {

    private final HungerGame game = HungerGames.getInstance().getGame();

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        game.join(GameUserManager.getGameUser(event.getPlayer()));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        game.leave(GameUserManager.getGameUser(player));
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        final GameUser user = GameUserManager.getGameUser(event.getEntity());
        game.onDeath(user);
    }

    @EventHandler
    private void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.setGameMode(GameMode.SURVIVAL);
            if (event.getDismounted() instanceof EnderDragon) {
                game.dismountWithTeam(GameUserManager.getGameUser(player));
            }
            player.getInventory().addItem(ItemBuilder.of(Material.FIREWORK_STAR).build());
            player.getInventory().setChestplate(ItemBuilder.of(Material.ELYTRA).build());
            player.setGliding(true);
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (event.getFrom().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            final ItemStack chestPlate = player.getInventory().getChestplate();
            if (chestPlate == null) return;
            if (chestPlate.getType() == Material.ELYTRA) {
                player.getInventory().setChestplate(null);
                player.getInventory().addItem(ItemBuilder.of(Material.BREAD).amount(20).build());
            }
        }
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onTeamChat(AsyncPlayerChatEvent event) {
        if (game.getPhase() instanceof InGamePhase) {
            final GameUser sender = GameUserManager.getGameUser(event.getPlayer());
            if (game.isSpectator(sender)) {
                game.sendMessageSpectators(sender, event.getMessage());
                return;
            }
            game.sendMessageOwnTeam(sender, event.getMessage());
        }
    }
}
