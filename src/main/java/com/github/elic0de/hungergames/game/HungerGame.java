package com.github.elic0de.hungergames.game;

import com.github.elic0de.eliccommon.game.AbstractGame;
import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.eliccommon.util.ItemBuilder;
import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.chest.DeathChest;
import com.github.elic0de.hungergames.dragon.DragonTrait;
import com.github.elic0de.hungergames.game.phase.InGamePhase;
import com.github.elic0de.hungergames.game.phase.WaitingPhase;
import com.github.elic0de.hungergames.user.GameUser;
import com.github.elic0de.hungergames.user.GameUserManager;
import de.themoep.minedown.MineDown;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.EntityControllers;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HungerGame extends AbstractGame {

    private final Scoreboard scoreboard;

    @Getter
    private final DeathChest deathChest;

    @Getter
    private final GameBossBar bossBar = new GameBossBar();

    private final GameBorder border;

    @Getter
    private final Set<GameUser> gameUsers = new HashSet<>();

    private final Set<Team> aliveTeams = new HashSet<>();

    @Getter
    private final Set<String> deadPlayers = new HashSet<>();

    private final Set<UUID> rejoinPlayers = new HashSet<>();

    private DragonTrait dragonTrait;

    private GameRecords records;

    private BukkitTask borderTask;

    public HungerGame() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        border = new GameBorder(this);
        deathChest = new DeathChest();
        records = new GameRecords(this);
    }

    public void join(GameUser user) {
        gameUsers.add(user);
        bossBar.addPlayer(user);
    }

    public void leave(GameUser user) {
        gameUsers.remove(user);
        bossBar.removePlayer(user);
    }

    public void startGame(Player player) {
        if (getPhase() instanceof WaitingPhase) {
            final WorldBorder border = player.getWorld().getWorldBorder();
            final Location start = border.getCenter().clone().add(border.getSize() / 2, 130, border.getSize() / 2);

            getGameUsers().forEach(user -> {
                // プレイヤーが所属しているチームを生存しているチームとして登録
                // チームに所属していなかったら観戦者とする
                getUserTeam(user).ifPresentOrElse(aliveTeams::add, () -> deadPlayers.add(user.getUsername()));

                Bukkit.getScheduler().runTask(HungerGames.getInstance(), () -> {
                    user.getPlayer().getInventory().clear();
                    user.getPlayer().teleport(start);
                    user.getPlayer().setGameMode(GameMode.SPECTATOR);
                });
            });
            nextPhase();
            spawnEnderDragon(player);
        }
    }

    public void startBorder() {
            border.start();
            title("&cエリア縮小開始", "");
            sound(Sound.ENTITY_WITHER_SPAWN);
    }

    public void spawnEnderDragon(Player player) {
        final World world = player.getWorld();
        final WorldBorder border = world.getWorldBorder();
        final Location start = border.getCenter().clone().add(border.getSize() / 2, 130, border.getSize() / 2);
        final Location end = border.getCenter().clone().subtract(border.getSize() / 2, -130, border.getSize() / 2);

        borderTask = Bukkit.getScheduler().runTaskLater(HungerGames.getInstance(), this::startBorder, (long) ((start.distance(end) / 10) * 20));

        dragonTrait = new DragonTrait(border);
        Bukkit.getScheduler().runTaskTimer(HungerGames.getInstance(), task -> {
            CitizensNPC dragon = new CitizensNPC(UUID.randomUUID(), 1, "", EntityControllers.createForType(EntityType.ENDER_DRAGON), CitizensAPI.getNPCRegistry());
            dragon.spawn(player.getLocation());
            dragon.addTrait(dragonTrait);

            if (dragon.isSpawned()) {
                getGameUsers().stream().filter(onlineUser -> !deadPlayers.contains(onlineUser.getUsername())).forEach(onlineUser -> dragon.getEntity().addPassenger(onlineUser.getPlayer()));
                task.cancel();
            }
        }, 0, 20);

    }

    public void dismountWithTeam(GameUser user) {
        Bukkit.getScheduler().runTask(HungerGames.getInstance(), () -> getTeamUsers(user).stream().filter(user1 -> user1 != user).forEach(user1 -> user.getPlayer().addPassenger(user1.getPlayer())));
    }

    public void onDeath(GameUser user) {
        if (getPhase() instanceof InGamePhase) {
            if (isSpectator(user)) return;

            deadPlayers.add(user.getUsername());
            if (getAlivePlayersSize() == 10) {
                broadcast(new MineDown("残りプレイヤー10人"));
            }

            getUserTeam(user).ifPresent(team -> {
                if (checkTeamDead(user)) aliveTeams.remove(team);
            });

            if (user.getPlayer().getKiller() != null) {
                records.addKill(GameUserManager.getGameUser(user.getPlayer().getKiller()));
            }

            Bukkit.getScheduler().runTask(HungerGames.getInstance(), () -> {
                deathChest.generateChest(user);
                user.getPlayer().setGameMode(GameMode.SPECTATOR);
                user.getPlayer().getWorld().strikeLightningEffect(user.getPlayer().getLocation());
            });

            // 生存チーム数が2チーム以下になったら勝利
            if (aliveTeams.size() < 2) {
                wonGame();
            }
        }
    }

    public void wonGame() {
        if (getPhase() instanceof InGamePhase) {
            aliveTeams.stream().findAny().ifPresent(team -> {
                broadcast(new MineDown(String.format("%sのチームが勝利しました", team.getName())));
                title(String.format("%s 勝利しました", team.getName()), "");
            });
            endGame();
        }
    }

    public void endGame() {
        showResult();
        sound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        reset();
    }

    // todo
    private void showResult() {

        final List<Map.Entry<UUID, Integer>> list = new ArrayList<>(records.getRank().entrySet());

        list.stream().limit(10).forEach(entry -> {
            final UUID uuid = entry.getKey();
            final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            final String playerName = player != null ? player.getName() : "No name";

            broadcast(new MineDown(String.format("&c%s&r位 &a%s&r &c%s&r kills", entry.getValue(), playerName, records.personalBest(uuid))));
        });
    }

    @Override
    public void reset() {
        if (getPhase() instanceof InGamePhase) setCurrentPhase(0);
        if (borderTask != null) borderTask.cancel();
        aliveTeams.clear();
        deadPlayers.clear();
        deathChest.reset();
        rejoinPlayers.clear();
        border.reset();
        records.removeAllRecord();
        if (dragonTrait != null) dragonTrait.reset();
    }

    public void sendMessageSpectators(GameUser user, String message) {
        getGameUsers().forEach(onlineUser -> {
            if (isSpectator(onlineUser)) onlineUser.sendMessage(new MineDown(String.format("[観戦者] %s: %s", user.getUsername(), message)));
        });
    }

    public void sendMessageOwnTeam(GameUser user, String message) {
        getUserTeam(user).ifPresent(team -> getTeamUsers(user).forEach(player -> player.sendMessage(new MineDown(String.format("[%s] %s: %s", team.getName(), user.getUsername(), message)))));
    }

    public boolean isSpectator(GameUser user) {
        return deadPlayers.contains(user.getUsername());
    }

    public void rejoin(GameUser user) {
        if (getPhase() instanceof InGamePhase) {
            if (rejoinPlayers.contains(user.getUniqueId())) {
                final Player player = user.getPlayer();
                final ItemStack chestPlate = player.getInventory().getChestplate();
                if (chestPlate == null) return;
                if (chestPlate.getType() == Material.ELYTRA || player.getGameMode() == GameMode.SPECTATOR) {
                    player.getInventory().setChestplate(null);
                    player.getInventory().addItem(ItemBuilder.of(Material.BREAD).amount(20).build());
                    player.getPassengers().forEach(player::removePassenger);
                    player.setGameMode(GameMode.SURVIVAL);
                }
                rejoinPlayers.remove(user.getUniqueId());
                return;
            }
            user.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    public void addRejoinPlayer(GameUser user) {
        // 死んでいる場合は再参加無効に
        if (deadPlayers.contains(user.getUsername())) return;
        rejoinPlayers.add(user.getUniqueId());
    }

    public boolean checkTeamDead(GameUser user) {
        if (getUserTeam(user).isPresent()) {
            final Team team = getUserTeam(user).get();
            for (String name : team.getEntries()) {
                if (Bukkit.getPlayer(name) == null) continue;
                if (deadPlayers.contains(name)) continue;
                return false;
            }
        }
        return true;
    }

    private Optional<Team> getUserTeam(GameUser user) {
        final Team team = scoreboard.getEntryTeam(user.getUsername());
        return Optional.ofNullable(team);
    }

    private Set<GameUser> getTeamUsers(GameUser user) {
        final Team team = scoreboard.getEntryTeam(user.getUsername());
        if (team == null) return new HashSet<>();
        return team.getEntries().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).map(GameUserManager::getGameUser).collect(Collectors.toSet());
    }

    public int getAlivePlayersSize() {
        final AtomicInteger playersSize = new AtomicInteger();
        aliveTeams.forEach(team -> {
            for (String playerName : team.getEntries()) {
                if (deadPlayers.contains(playerName)) continue;
                playersSize.incrementAndGet();
            }
        });

        return playersSize.get();
    }

    @Override
    public @NotNull Phase[] getPhases() {
        return new Phase[]{
                new WaitingPhase(),
                new InGamePhase(this)
        };
    }
}
