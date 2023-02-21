package com.github.elic0de.hungergames.listener;

import com.github.elic0de.eliccommon.util.ItemBuilder;
import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.game.phase.InGamePhase;
import com.github.elic0de.hungergames.menu.DeathChestMenu;
import com.github.elic0de.hungergames.user.GameUser;
import com.github.elic0de.hungergames.user.GameUserManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
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
            player.getInventory().addItem(ItemBuilder.of(Material.FIREWORK_ROCKET).build());
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
            switch (event.getCause()) {
                case FALL, FLY_INTO_WALL -> event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() == Material.CHEST && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            game.getDeathChest().openDeathChest(event.getPlayer(), block);
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

    @EventHandler
    public void onFight(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player vitim) {
            Player damager = null;
            if (event.getDamager() instanceof Player) damager = (Player) event.getDamager();
            if (event.getDamager() instanceof Arrow arrow) if (arrow.getShooter() instanceof Player) damager = (Player) event.getDamager();
            if (damager != null) damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(String.join("%s %s", vitim.getName(), getHeartLevel(vitim))).create());
        }
    }

    private String getHeartLevel(Player player) {

        int currentHealth = (int) player.getHealth() / 2;
        int maxHealth = (int) player.getMaxHealth() / 2;
        int lostHealth = maxHealth - currentHealth;

        String rHeart = "";
        String lHeart = "";

        for (int i = 0; i < currentHealth; i++) {
            rHeart = rHeart + ChatColor.RESET + "❤";
        }
        for (int i = 0; i < lostHealth; i++) {
            lHeart = lHeart + ChatColor.GRAY + "❤";
        }

        return rHeart + lHeart;
    }
}
