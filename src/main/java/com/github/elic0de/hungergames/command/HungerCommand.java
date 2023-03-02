package com.github.elic0de.hungergames.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.game.HungerGame;
import org.bukkit.entity.Player;

@CommandAlias("hungergames|hg")
public class HungerCommand extends BaseCommand {

    private final HungerGame game = HungerGames.getInstance().getGame();

    @Subcommand("start")
    private void start(Player player) {
        game.startGame(player);
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
    private void create(Player player, int count) {
        game.createTeams(count);
        player.sendMessage("オンラインプレイヤー数に応じて "+ count + "プレイヤーのチームを作ります");
    }
}
