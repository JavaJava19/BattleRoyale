package com.github.elic0de.battleroyale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.game.GameType;
import com.github.elic0de.battleroyale.game.Game;
import org.bukkit.entity.Player;

@CommandAlias("battleroyale|br")
public class BattleCommand extends BaseCommand {

    private final Game game = BattleRoyale.getInstance().getGame();

    @Subcommand("start")
    private void start(Player player, GameType type, @Default("false") boolean modifier) {
        game.startGame(player, type, modifier);
    }

    @Subcommand("end")
    private void end(Player player) {
        game.endGame();
    }

    @Subcommand("team random")
    private void random(Player player) {
        game.randomTeam();
        player.sendMessage("既存のチームにプレイヤーをランダムに振り分けます");
    }

    @Subcommand("team create")
    private void create(Player player, @Default("3") int count) {
        game.createTeams(count);
        player.sendMessage("オンラインプレイヤー数に応じて "+ count + "プレイヤーのチームを作ります");
    }

    @Subcommand("teleport|tp")
    private void teleport(Player player) {
        game.teleportStartLocation(player);
    }
}
