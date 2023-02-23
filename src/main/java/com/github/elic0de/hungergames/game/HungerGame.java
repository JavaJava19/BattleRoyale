package com.github.elic0de.hungergames.game;

import com.github.elic0de.eliccommon.game.AbstractGame;
import com.github.elic0de.eliccommon.game.phase.Phase;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class HungerGame extends AbstractGame {

    private final Scoreboard scoreboard;

    @Getter
    private final DeathChest deathChest;

    @Getter
    private final GameBossBar bossBar = new GameBossBar();

    private final GameBorder border;

    private final Set<Team> deadTeams = new HashSet<>();

    @Getter
    private final Set<String> deadPlayers = new HashSet<>();

    private DragonTrait dragonTrait;

    public HungerGame() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        border = new GameBorder(this);
        deathChest = new DeathChest();
    }

    public void join(GameUser user) {
        super.join(user);
        bossBar.addPlayer(user);
    }

    public void leave(GameUser user) {
        super.leave(user);
        bossBar.removePlayer(user);
    }

    public void startGame(Player player) {
        if (getPhase() instanceof WaitingPhase) {
            final WorldBorder border = player.getWorld().getWorldBorder();
            final Location start = border.getCenter().clone().add(border.getSize() / 2, 130, border.getSize() / 2);

            getPlayers(GameUser.class).forEach(user -> {
                // プレイヤーが所属しているチームを生存しているチームとして登録
                getUserTeam(user).ifPresent(aliveTeams::add);
                user.getPlayer().getInventory().clear();
                user.getPlayer().teleport(start);
                user.getPlayer().setGameMode(GameMode.SPECTATOR);
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
        getPlayers().forEach(onlineUser -> {
            onlineUser.getPlayer().teleport(start);
            onlineUser.getPlayer().setGameMode(GameMode.SPECTATOR);
        });

        dragonTrait = new DragonTrait(border);
        Bukkit.getScheduler().runTaskTimer(HungerGames.getInstance(), task -> {
            CitizensNPC dragon = new CitizensNPC(UUID.randomUUID(), 1, "", EntityControllers.createForType(EntityType.ENDER_DRAGON), CitizensAPI.getNPCRegistry());
            dragon.spawn(player.getLocation());
            dragon.addTrait(dragonTrait);

            if (dragon.isSpawned()) {
                dragon.teleport(start, PlayerTeleportEvent.TeleportCause.PLUGIN);
                getPlayers().forEach(onlineUser -> dragon.getEntity().addPassenger(onlineUser.getPlayer()));
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
            if (deadPlayers.size() == 10) {
                broadcast(new MineDown("残りプレイヤー10人"));
            }

            getUserTeam(user).ifPresent(team -> {
                if (checkTeamDead(user)) deadTeams.add(team);
            });

            if (scoreboard.getTeams().size() == deadTeams.size() + 1) {
                wonGame();
            }

            deathChest.generateChest(user);
            user.getPlayer().setGameMode(GameMode.SPECTATOR);
            user.getPlayer().getWorld().strikeLightningEffect(user.getPlayer().getLocation());
        }
    }

    public void wonGame() {
        if (getPhase() instanceof InGamePhase) {
            aliveTeams.stream().findAny().ifPresent(team -> {
                broadcast(new MineDown(String.format("%sのチームが勝利しました", team.getName())));
                title(String.format("%s 勝利しました", team.getName()), "");
                sound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
            });
            endGame();
        }
    }

    public void endGame() {
        reset();
    }

    @Override
    public void reset() {
        setCurrentPhase(0);
        aliveTeams.clear();
        deadPlayers.clear();
        border.reset();
        if (dragonTrait != null) dragonTrait.reset();
    }

    public void sendMessageSpectators(GameUser user, String message) {
        getPlayers(GameUser.class).forEach(onlineUser -> {
            if (isSpectator(onlineUser)) onlineUser.sendMessage(new MineDown(String.format("[観戦者] %s: %s", user.getUsername(), message)));
        });
    }

    public void sendMessageOwnTeam(GameUser user, String message) {
        getUserTeam(user).ifPresent(team -> {
            getTeamUsers(user).forEach(player -> player.sendMessage(new MineDown(String.format("[%s] %s: %s", team.getName(), user.getUsername(), message))));
        });
    }

    public boolean isSpectator(GameUser user) {
        return deadPlayers.contains(user.getUsername());
    }

    public boolean checkTeamDead(GameUser user) {
        if (getUserTeam(user).isPresent()) {
            final Team team = getUserTeam(user).get();
            for (String name : team.getEntries()) {
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

    @Override
    public @NotNull Phase[] getPhases() {
        return new Phase[]{
                new WaitingPhase(),
                new InGamePhase(this)
        };
    }
}
