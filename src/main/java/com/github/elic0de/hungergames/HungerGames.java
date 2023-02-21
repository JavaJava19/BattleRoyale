package com.github.elic0de.hungergames;

import co.aikar.commands.PaperCommandManager;
import com.github.elic0de.eliccommon.plugin.AbstractPlugin;
import com.github.elic0de.hungergames.command.HungerCommand;
import com.github.elic0de.hungergames.game.HungerGame;
import com.github.elic0de.hungergames.listener.EventListener;
import com.github.elic0de.hungergames.user.GameUserManager;
import org.bukkit.Bukkit;

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

        registerCommands();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        GameUserManager.getOnlineUsers().forEach(player -> game.join(player));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        GameUserManager.getOnlineUsers().forEach(player -> game.leave(player));
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.enableUnstableAPI("brigadier");
        commandManager.registerCommand(new HungerCommand());
    }

    public HungerGame getGame() {
        return game;
    }

    public static HungerGames getInstance() {
        return instance;
    }
}
