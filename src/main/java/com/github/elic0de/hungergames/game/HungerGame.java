package com.github.elic0de.hungergames.game;

import com.github.elic0de.eliccommon.game.AbstractGame;
import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.eliccommon.user.OnlineUser;
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


    private final Set<Team> aliveTeams = new HashSet<>();

    @Getter
    private final Set<String> deadPlayers = new HashSet<>();

    private final Set<String> rejoinPlayers = new HashSet<>();

    private DragonTrait dragonTrait;

    private GameRecords records;

    private BukkitTask borderTask;

    private final List<ChatColor> colors = List.of(
            ChatColor.RED,
            ChatColor.BLUE,
            ChatColor.AQUA,
            ChatColor.YELLOW,
            ChatColor.GOLD,
            ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE,
            ChatColor.WHITE,
            ChatColor.GRAY
    );

    public HungerGame() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        border = new GameBorder(this);
        deathChest = new DeathChest();
        records = new GameRecords(this);
    }

    public void join(GameUser user) {
        super.join(user);
        bossBar.addPlayer(user);
    }

    public void leave(GameUser user) {
        super.leave(user);
        bossBar.removePlayer(user);
    }


    public void createTeams(int count) {
        final AtomicInteger colorIndex = new AtomicInteger();
        int teamSize = Math.round(getPlayers().size()/count);

        // 既存のチームを削除
        scoreboard.getTeams().forEach(Team::unregister);
        // 人数に応じてチームを作る
        for(int i = 0; i < teamSize; i++) {
            if (colorIndex.get() < colors.size()) {
                final ChatColor color = colors.get(colorIndex.get());
                final String teamName = scoreboard.getTeam(color.name()) != null ? UUID.randomUUID().toString().substring(0, 6) : color.name();
                final Team team = scoreboard.registerNewTeam(teamName);

                team.setColor(color);
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
                colorIndex.incrementAndGet();
                continue;
            }
            colorIndex.set(0);
        }
    }

    public void randomTeam() {
        final List<OnlineUser> users = new ArrayList<>(getPlayers());
        Collections.shuffle(users);
        users.forEach(user -> scoreboard.getTeams().stream().min(Comparator.comparing(Team::getSize)).ifPresent(team -> team.addEntry(user.getUsername())));
    }

    public void startGame(Player player) {
        if (getPhase() instanceof WaitingPhase) {
            final WorldBorder border = player.getWorld().getWorldBorder();
            final Location start = border.getCenter().clone().add((border.getSize() / 2) - 2, 130, (border.getSize() / 2) - 2);

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
                getPlayers().stream().filter(onlineUser -> !deadPlayers.contains(onlineUser.getUsername())).forEach(onlineUser -> dragon.getEntity().addPassenger(onlineUser.getPlayer()));
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
                broadcast(new MineDown(String.format("%sのチームが勝利しました", team.getName())));
                title(String.format("%sの勝利", team.getName()), "");
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
        if (dragonTrait != null) dragonTrait.reset();
    }

    public void sendMessageSpectators(GameUser user, String message) {
        getPlayers(GameUser.class).forEach(onlineUser -> {
            if (isSpectator(onlineUser)) onlineUser.sendMessage(new MineDown(String.format("&b[観戦者] %s&r: &7%s", user.getUsername(), message)));
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
