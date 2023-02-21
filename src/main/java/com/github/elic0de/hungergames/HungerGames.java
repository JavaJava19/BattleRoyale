package com.github.elic0de.hungergames;

import com.github.elic0de.eliccommon.plugin.AbstractPlugin;
import com.github.elic0de.hungergames.game.HungerGame;

public final class HungerGames extends AbstractPlugin {

    private static HungerGames instance;

    private HungerGame game;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        game = new HungerGame();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public HungerGame getGame() {
        return game;
    }

    public static HungerGames getInstance() {
        return instance;
    }
}
