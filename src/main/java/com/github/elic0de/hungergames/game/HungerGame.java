package com.github.elic0de.hungergames.game;

import com.github.elic0de.eliccommon.game.AbstractGame;
import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.hungergames.user.GameUser;
import com.github.elic0de.hungergames.user.GameUserManager;
import de.themoep.minedown.MineDown;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class HungerGame extends AbstractGame {

    private final Scoreboard scoreboard;

    private final Set<Team> deadTeams = new HashSet<>();
    private final Set<String> deadPlayers = new HashSet<>();

    public HungerGame() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void onDeath(GameUser user) {
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

        user.getPlayer().setGameMode(GameMode.SPECTATOR);
        user.getPlayer().getWorld().strikeLightningEffect(user.getPlayer().getLocation());
    }

    public void wonGame() {
        scoreboard.getTeams().stream().filter(team -> !deadTeams.contains(team)).findAny().ifPresent(team -> {
            broadcast(new MineDown(String.format("%sのチームが勝利しました", team.getName())));
        });
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
        return new Phase[0];
    }
}
