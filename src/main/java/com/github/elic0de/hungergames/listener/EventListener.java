package com.github.elic0de.hungergames.listener;

import com.github.elic0de.eliccommon.util.ItemBuilder;
import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.game.phase.InGamePhase;
import com.github.elic0de.hungergames.game.phase.WaitingPhase;
import com.github.elic0de.hungergames.user.GameUser;
import com.github.elic0de.hungergames.user.GameUserManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
        game.leave(GameUserManager.getGameUser(event.getPlayer()));
        GameUserManager.unRegisterUser(player);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        final GameUser user = GameUserManager.getGameUser(event.getEntity());
        game.onDeath(user);
        event.getDrops().clear();
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(event.getPlayer().getLocation());
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (game.getPhase() instanceof WaitingPhase) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onDismount(EntityDismountEvent event) {
        if (game.getPhase() instanceof InGamePhase) {
            if (event.getEntity() instanceof Player player) {
                if (game.getDeadPlayers().contains(player.getName())) return;
                if (event.getDismounted() instanceof EnderDragon) {
                    game.dismountWithTeam(GameUserManager.getGameUser(player));
                    player.getInventory().addItem(ItemBuilder.of(Material.FIREWORK_ROCKET).build());
                    player.getInventory().setChestplate(ItemBuilder.of(Material.ELYTRA).build());
                    player.setGliding(true);
                }
                if (event.getDismounted() instanceof Player) {
                    player.getInventory().addItem(ItemBuilder.of(Material.FIREWORK_ROCKET).build());
                    player.getInventory().setChestplate(ItemBuilder.of(Material.ELYTRA).build());
                    player.setGliding(true);
                }
                player.setGameMode(GameMode.SURVIVAL);
            }
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
                player.getPassengers().forEach(player::removePassenger);
            }
        }
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (game.getPhase() instanceof WaitingPhase) {
                event.setCancelled(true);
                return;
            }

            switch (event.getCause()) {
                case FALL, FLY_INTO_WALL -> event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onTest(EntityDamageByEntityEvent event) {
        if (game.getPhase() instanceof InGamePhase) {
            if (event.getEntity() instanceof Player player) {
                if (player.isInsideVehicle()) {
                    if (player.getVehicle() instanceof EnderDragon) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() == Material.CHEST && event.getAction() == Action.RIGHT_CLICK_BLOCK && game.getDeathChest().containsDeathChest(block)) {
            event.setCancelled(true);
            game.getDeathChest().openDeathChest(event.getPlayer(), block);
        }
    }

    @EventHandler
    private void blockBreak(BlockBreakEvent event) {
        if (game.getPhase() instanceof InGamePhase) {
            final Block block = event.getBlock();
            if (block.getType() == Material.CHEST && game.getDeathChest().containsDeathChest(block)) {
                game.getDeathChest().breakDeathChest(block);
            }
        }
    }

    @EventHandler
    private void onTeamChat(AsyncPlayerChatEvent event) {
        if (game.getPhase() instanceof InGamePhase) {
            event.setCancelled(true);
            final GameUser sender = GameUserManager.getGameUser(event.getPlayer());
            final String message = event.getMessage();
            if (game.isSpectator(sender)) {
                game.sendMessageSpectators(sender, message);
                return;
            }
            if (event.getMessage().startsWith("@")) {
                game.shout(sender, message.substring(1));
                return;
            }
            game.sendMessageOwnTeam(sender, message);
        }
    }

    @EventHandler
    public void onFight(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player vitim) {
            Player damager = null;
            if (event.getDamager() instanceof Player) damager = (Player) event.getDamager();
            if (event.getDamager() instanceof Arrow arrow) if (arrow.getShooter() instanceof Player) damager = (Player) event.getDamager();
            if (damager != null) damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(vitim.getName() + " " + getHeartLevel(vitim)).create());
        }
    }

    private String getHeartLevel(Player player) {

        int currentHealth = (int) player.getHealth() / 2;
        int maxHealth = (int) player.getMaxHealth() / 2;
        int lostHealth = maxHealth - currentHealth;

        StringBuilder rHeart = new StringBuilder();
        StringBuilder lHeart = new StringBuilder();

        for (int i = 0; i < currentHealth; i++) {
            rHeart.append(ChatColor.RED).append("❤");
        }
        for (int i = 0; i < lostHealth; i++) {
            lHeart.append(ChatColor.GRAY).append("❤");
        }

        return rHeart + lHeart.toString();
    }

    @EventHandler
    private void onCompassInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (!event.getItem().isSimilar(new ItemStack(Material.COMPASS))) return;

        final Player player = event.getPlayer();
        final Player nearestPlayer = getNearest(player, 300.0);
        final Material material = event.getMaterial();

        if (player.getCooldown(material) != 0) return;

        final GameUser user = GameUserManager.getGameUser(player);

        player.setCooldown(material, 60);

        if (nearestPlayer == null) {
            user.sendActionBar("&c範囲300ブロック以内のプレイヤーを見つけられませんでした");
            return;
        }

        final String checkPosition = getPosition(player, nearestPlayer);

        player.setCompassTarget(nearestPlayer.getLocation());
        user.sendActionBar("近くのプレイヤーを指しています。" + checkPosition);
    }

    public Player getNearest(Player p, Double range) {
        final GameUser user = GameUserManager.getGameUser(p);
        double distance = Double.POSITIVE_INFINITY; // To make sure the first
        // player checked is closest
        for (Entity entity : p.getNearbyEntities(range, range, range)) {
            if (entity instanceof Player player) {
                if (player == p) continue; //Added this check so you don't target yourself.
                if (player.getGameMode() == GameMode.SPECTATOR) continue;
                if (game.getTeamUsers(user).contains(GameUserManager.getGameUser(player))) continue;
                double distanceto = p.getLocation().distance(player.getLocation());
                if (distanceto > distance)
                    continue;
                return player;
            }
        }
        return null;
    }

    public String getPosition(Player player, Player nearestPlayer) {
        final Location playerLoc = player.getLocation();
        final Location nearestPlayerLoc = nearestPlayer.getLocation();

        if (playerLoc.getY() == nearestPlayerLoc.getY()) return "-";
        if (playerLoc.getY() <=  nearestPlayerLoc.getY()) return "↑";

        return "↓";
    }
}
