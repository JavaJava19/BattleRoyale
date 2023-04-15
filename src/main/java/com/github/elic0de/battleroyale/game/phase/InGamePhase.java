package com.github.elic0de.battleroyale.game.phase;

import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.eliccommon.user.OnlineUser;
import com.github.elic0de.battleroyale.game.Game;
import com.github.elic0de.battleroyale.user.GameUser;

public class InGamePhase extends Phase {

    private final Game game;
    public InGamePhase(Game game) {
        super(-0L, -0L);
        this.game = game;
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void end() {

    }

    @Override
    public void join(OnlineUser player) {
        game.rejoin((GameUser) player);
        game.checkPlayerSize();
    }

    @Override
    public void leave(OnlineUser player) {
        game.addRejoinPlayer((GameUser) player);
    }
}
