package com.github.elic0de.battleroyale.game;

import com.github.elic0de.eliccommon.game.AbstractGame;
import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.eliccommon.user.OnlineUser;
import com.github.elic0de.eliccommon.util.ItemBuilder;
import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.chest.DeathChest;
import com.github.elic0de.battleroyale.dragon.DragonTrait;
import com.github.elic0de.battleroyale.event.GamePlayerKillEvent;
import com.github.elic0de.battleroyale.game.phase.InGamePhase;
import com.github.elic0de.battleroyale.game.phase.WaitingPhase;
import com.github.elic0de.battleroyale.modifier.ModifierManager;
import com.github.elic0de.battleroyale.user.GameUser;
import com.github.elic0de.battleroyale.user.GameUserManager;
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

public class Game extends AbstractGame {

    private final Scoreboard scoreboard;

    @Getter
    private final DeathChest deathChest;

    @Getter
    private final ModifierManager modifierManager;

    @Getter
    private final GameBossBar bossBar = new GameBossBar();

    private final GameBorder border;


    private final Set<Team> aliveTeams = new HashSet<>();

    @Getter
    private final Set<String> deadPlayers = new HashSet<>();

    private final Set<String> rejoinPlayers = new HashSet<>();

    private DragonTrait dragonTrait;

    private GameRecords records;
    private BukkitTask borderTask;

    public Game() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        border = new GameBorder(this);
        deathChest = new DeathChest();
        records = new GameRecords(this);
        modifierManager = new ModifierManager(this);
    }

    public void join(GameUser user) {
        super.join(user);
        bossBar.addPlayer(user);
    }

    public void leave(GameUser user) {
        super.leave(user);
        bossBar.removePlayer(user);
    }

    public boolean checkPlayerSize() {
        final boolean canStart = getPlayers().size() <= 20;
        if (canStart) {
            startCountDown();
        }
        return canStart;
    }


    public void createTeams(int count) {
        int teamSize = Math.max(Math.round(getPlayers().size() / count), 1);

        // 既存のチームを削除
        scoreboard.getTeams().forEach(Team::unregister);
        // 人数に応じてチームを作る
        for (int i = 0; i < teamSize; i++) {
            final String teamName = scoreboard.getTeam(String.valueOf(i)) != null ? UUID.randomUUID().toString().substring(0, 6) : String.valueOf(i);
            final Team team = scoreboard.registerNewTeam(teamName);

            team.setPrefix(String.format("[%s] ", i));
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        }
    }

    public void randomTeam() {
        final List<OnlineUser> users = new ArrayList<>(getPlayers());
        Collections.shuffle(users);
        users.forEach(user -> scoreboard.getTeams().stream().min(Comparator.comparing(Team::getSize)).ifPresent(team -> team.addEntry(user.getUsername())));
    }


    public void teleportStartLocation(Player player) {
        final WorldBorder border = player.getWorld().getWorldBorder();
        final Location start = border.getCenter().clone().add((border.getSize() / 2) - 2, 130, (border.getSize() / 2) - 2);
        player.teleport(start);
    }

    public void startCountDown() {
        
    }

    public void startGame() {
        if (getPhase() instanceof WaitingPhase) {
            final WorldBorder border = Bukkit.getWorld("").getWorldBorder();
            final Location start = border.getCenter().clone().add((border.getSize() / 2) - 2, 130, (border.getSize() / 2) - 2);

            Bukkit.getScheduler().runTask(BattleRoyale.getInstance(), () -> {
                getPlayers(GameUser.class).forEach(user -> {
                    // プレイヤーが所属しているチームを生存しているチームとして登録
                    // チームに所属していなかったら観戦者とする
                    getUserTeam(user).ifPresentOrElse(aliveTeams::add, () -> deadPlayers.add(user.getUsername()));
                    user.clearEffectAndHeal();

                    user.getPlayer().getInventory().clear();
                    user.getPlayer().teleport(start);
                    user.getPlayer().setGameMode(GameMode.SPECTATOR);

                    // 10秒のクールダウン
                    user.getPlayer().setCooldown(Material.COMMAND_BLOCK, 10 * 20);
                });
                nextPhase();
                spawnEnderDragon(border.getWorld());
            });
        }
    }

    public void startGame(Player player, GameType type, boolean modifier) {
        if (getPhase() instanceof WaitingPhase) {
            final WorldBorder border = player.getWorld().getWorldBorder();
            final Location start = border.getCenter().clone().add((border.getSize() / 2) - 2, 130, (border.getSize() / 2) - 2);

            Bukkit.getScheduler().runTask(BattleRoyale.getInstance(), () -> {
                if (type == GameType.SOLO) {
                    createTeams(1);
                    randomTeam();
                }
                // modifierが有効の場合、ランダムにmodifierを加える
                if (modifier) modifierManager.modify();

                getPlayers(GameUser.class).forEach(user -> {
                    // プレイヤーが所属しているチームを生存しているチームとして登録
                    // チームに所属していなかったら観戦者とする
                    getUserTeam(user).ifPresentOrElse(aliveTeams::add, () -> deadPlayers.add(user.getUsername()));
                    user.clearEffectAndHeal();

                    user.getPlayer().getInventory().clear();
                    user.getPlayer().teleport(start);
                    user.getPlayer().setGameMode(GameMode.SPECTATOR);

                    // 10秒のクールダウン
                    user.getPlayer().setCooldown(Material.COMMAND_BLOCK, 10 * 20);
                });
                nextPhase();
                spawnEnderDragon(player.getWorld());
            });
        }
    }

    public void startBorder() {
        border.start();
        title("&cエリア縮小開始", "");
        sound(Sound.ENTITY_WITHER_SPAWN);
    }

    public void spawnEnderDragon(World world) {
        final WorldBorder border = world.getWorldBorder();
        final Location start = border.getCenter().clone().add(border.getSize() / 2, 130, border.getSize() / 2);
        final Location end = border.getCenter().clone().subtract(border.getSize() / 2, -130, border.getSize() / 2);

        borderTask = Bukkit.getScheduler().runTaskLater(BattleRoyale.getInstance(), this::startBorder, (long) ((start.distance(end) / 10) * 20));

        dragonTrait = new DragonTrait(border);
        Bukkit.getScheduler().runTaskTimer(BattleRoyale.getInstance(), task -> {
            CitizensNPC dragon = new CitizensNPC(UUID.randomUUID(), 1, "", EntityControllers.createForType(EntityType.ENDER_DRAGON), CitizensAPI.getNPCRegistry());
            dragon.spawn(start);
            dragon.addTrait(dragonTrait);
            if (dragon.isSpawned()) {
                getPlayers().stream().filter(onlineUser -> !deadPlayers.contains(onlineUser.getUsername())).forEach(onlineUser -> dragon.getEntity().addPassenger(onlineUser.getPlayer()));
                task.cancel();
            }
        }, 0, 20);

    }

    public void dismountWithTeam(GameUser user) {
        Bukkit.getScheduler().runTask(BattleRoyale.getInstance(), () -> getTeamUsers(user).stream().filter(user1 -> user1 != user).forEach(user1 -> user.getPlayer().addPassenger(user1.getPlayer())));
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
                final GameUser killer = GameUserManager.getGameUser(user.getPlayer().getKiller());
                Bukkit.getPluginManager().callEvent(new GamePlayerKillEvent(killer, user));
                records.addKill(killer);
            }

            deathChest.generateChest(user);
            user.getPlayer().setGameMode(GameMode.SPECTATOR);
            user.getPlayer().getWorld().strikeLightningEffect(user.getPlayer().getLocation());

            // 生存チーム数が2チーム以下になったら勝利
            if (aliveTeams.size() < 2) {
                wonGame();
            }
        }
    }

    public void wonGame() {
        if (getPhase() instanceof InGamePhase) {
            aliveTeams.stream().findAny().ifPresent(team -> {
                broadcast(new MineDown(String.format("%sのチームが勝利しました", team.getDisplayName())));
                team.getEntries().forEach(s -> broadcast(new MineDown("&6" + team.getDisplayName())));
                title(String.format("%sの勝利", team.getDisplayName()), "");


                // todo: ここにfireworkの処理を実装させる
            });
            endGame();
        }
    }

    public void endGame() {
        showResult();
        sound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        // 20秒後にリセット
        Bukkit.getScheduler().runTaskLater(BattleRoyale.getInstance(), () -> reset(), 20 * 20);
    }

    // todo
    private void showResult() {
        final List<Map.Entry<UUID, Integer>> list = new ArrayList<>(records.getRank().entrySet());

        // 最大で上位10件の記録をブロードキャスト
        list.stream().sorted(Map.Entry.comparingByValue()).limit(10).forEach(entry -> {
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
        modifierManager.reset();
        if (dragonTrait != null) dragonTrait.reset();
    }

    public void sendMessageSpectators(GameUser user, String message) {
        getPlayers(GameUser.class).forEach(onlineUser -> {
            if (isSpectator(onlineUser))
                onlineUser.sendMessage(new MineDown(String.format("&b[観戦者] %s&r: &7%s", user.getUsername(), message)));
        });
    }

    public void sendMessageOwnTeam(GameUser user, String message) {
        getUserTeam(user).ifPresent(team -> getTeamUsers(user).forEach(player -> player.sendMessage(new MineDown(String.format("%s[%s] %s&r: &7%s", team.getColor(), team.getName(), user.getUsername(), message)))));
    }

    public void shout(GameUser user, String message) {
        getUserTeam(user).ifPresent(team -> broadcast(new MineDown(String.format("%s[%s] %s&r: &7%s", team.getColor(), team.getName(), user.getUsername(), message))));
    }

    public boolean isSpectator(GameUser user) {
        return deadPlayers.contains(user.getUsername());
    }

    public void rejoin(GameUser user) {
        if (getPhase() instanceof InGamePhase) {
            if (rejoinPlayers.contains(user.getUsername())) {
                final Player player = user.getPlayer();
                final ItemStack chestPlate = player.getInventory().getChestplate();
                if (chestPlate == null) return;
                if (chestPlate.getType() == Material.ELYTRA || player.getGameMode() == GameMode.SPECTATOR) {
                    player.getInventory().setChestplate(null);
                    player.getInventory().addItem(ItemBuilder.of(Material.BREAD).amount(20).build());
                    player.getPassengers().forEach(player::removePassenger);
                    player.setGameMode(GameMode.SURVIVAL);
                }
                rejoinPlayers.remove(user.getUsername());
                return;
            }
            user.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    public void addRejoinPlayer(GameUser user) {
        // 死んでいる場合は再参加無効に
        if (deadPlayers.contains(user.getUsername())) return;
        rejoinPlayers.add(user.getUsername());
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

    public Set<GameUser> getTeamUsers(GameUser user) {
        final Team team = scoreboard.getEntryTeam(user.getUsername());
        if (team == null) return new HashSet<>();
        return team.getEntries().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).map(GameUserManager::getGameUser).collect(Collectors.toSet());
    }

    public int getAlivePlayersSize() {
        final AtomicInteger playersSize = new AtomicInteger();
        aliveTeams.forEach(team -> {
            for (String playerName : team.getEntries()) {
                if (deadPlayers.contains(playerName)) continue;
                if (rejoinPlayers.contains(playerName)) continue;
                if (Bukkit.getPlayer(playerName) == null) continue;
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
