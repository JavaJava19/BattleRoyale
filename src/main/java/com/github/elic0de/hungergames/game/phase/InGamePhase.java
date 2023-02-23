package com.github.elic0de.hungergames.game.phase;

import com.github.elic0de.eliccommon.game.phase.Phase;
import com.github.elic0de.eliccommon.user.OnlineUser;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.user.GameUser;

public class InGamePhase extends Phase {

    private final HungerGame game;
    public InGamePhase(HungerGame game) {
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
    }

    @Override
    public void leave(OnlineUser player) {
        game.addRejoinPlayer((GameUser) player);
    }
}
