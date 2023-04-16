package com.github.elic0de.battleroyale.game.phase;

import com.github.elic0de.battleroyale.game.Game;
import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.eliccommon.user.OnlineUser;

public class WaitingPhase extends Phase {

    private final Game game;
    public WaitingPhase(Game game) {
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
        if (game.checkPlayerSize()) game.startCountDown();
    }

    @Override
    public void leave(OnlineUser player) {

    }
}
