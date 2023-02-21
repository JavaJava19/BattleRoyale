package com.github.elic0de.hungergames.game;

import com.github.elic0de.eliccommon.game.AbstractGame;
import com.github.elic0de.eliccommon.game.phase.Phase;
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
        if (getPhase() instanceof WaitingPhase) nextPhase();
        spawnEnderDragon(player.getWorld());
    }

    public void startBorder() {
            border.start();
            title("&cエリア縮小開始", "");
            sound(Sound.ENTITY_WITHER_SPAWN);
    }

    public void spawnEnderDragon(World world) {
        final WorldBorder border = world.getWorldBorder();
        final Location start = border.getCenter().clone().add(border.getSize() / 2, 130, border.getSize() / 2);
        getPlayers().forEach(onlineUser -> {
            onlineUser.getPlayer().setGameMode(GameMode.SPECTATOR);
            onlineUser.getPlayer().teleport(start);
        });

        CitizensNPC dragon = new CitizensNPC(UUID.randomUUID(), 1, "", EntityControllers.createForType(EntityType.ENDER_DRAGON), CitizensAPI.getNPCRegistry());
        dragon.spawn(start);
        dragon.addTrait(new DragonTrait(border));
        getPlayers().forEach(onlineUser -> dragon.getEntity().addPassenger(onlineUser.getPlayer()));
    }

    public void dismountWithTeam(GameUser user) {
        getTeamUsers(user).stream().filter(user1 -> user1 != user).forEach(user1 -> user.getPlayer().addPassenger(user.getPlayer()));
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
            scoreboard.getTeams().stream().filter(team -> !deadTeams.contains(team)).findAny().ifPresent(team -> broadcast(new MineDown(String.format("%sのチームが勝利しました", team.getName()))));

        }
    }

    @Override
    public void reset() {
        border.reset();
    }

    public void sendMessageSpectators(GameUser user, String message) {
        getPlayers(GameUser.class).forEach(onlineUser -> {
            if (isSpectator(onlineUser)) onlineUser.sendMessage(new MineDown(String.format("%s: %s", user.getUsername(), message)));
        });
    }

    public void sendMessageOwnTeam(GameUser user, String message) {
        getTeamUsers(user).forEach(player -> player.sendMessage(new MineDown(String.format("%s: %s", user.getUsername(), message))));
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
