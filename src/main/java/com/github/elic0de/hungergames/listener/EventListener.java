package com.github.elic0de.hungergames.listener;

import com.github.elic0de.hungergames.HungerGames;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.game.phase.InGamePhase;
import com.github.elic0de.hungergames.user.GameUser;
import com.github.elic0de.hungergames.user.GameUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EventListener implements Listener {

    private final HungerGame game = HungerGames.getInstance().getGame();

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        final GameUser user = GameUserManager.getGameUser(event.getEntity());
        game.onDeath(user);
    }

    @EventHandler
    private void onTeamChat(AsyncPlayerChatEvent event) {
        if (game.getPhase() instanceof InGamePhase) {
            final GameUser sender = GameUserManager.getGameUser(event.getPlayer());
            if (game.isSpectator(sender)) {
                game.sendMessageSpectators(sender, event.getMessage());
                return;
            }
            game.sendMessageOwnTeam(sender, event.getMessage());
        }
    }
}
