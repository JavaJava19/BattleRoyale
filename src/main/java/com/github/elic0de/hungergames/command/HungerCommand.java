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
        player.sendMessage("プレイヤーを既存のチームにランダムに振り分けます");
    }
}
