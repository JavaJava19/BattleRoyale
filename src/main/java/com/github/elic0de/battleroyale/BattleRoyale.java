package com.github.elic0de.battleroyale;

import co.aikar.commands.PaperCommandManager;
import com.github.elic0de.eliccommon.plugin.AbstractPlugin;
import com.github.elic0de.battleroyale.command.BattleCommand;
import com.github.elic0de.battleroyale.game.Game;
import com.github.elic0de.battleroyale.listener.EventListener;
import com.github.elic0de.battleroyale.user.GameUserManager;
import org.bukkit.Bukkit;

public final class BattleRoyale extends AbstractPlugin {

    private static BattleRoyale instance;

    private Game game;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        game = new Game();

        registerCommands();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        GameUserManager.getOnlineUsers().forEach(player -> game.join(player));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        GameUserManager.getOnlineUsers().forEach(player -> game.leave(player));
        game.reset();
        //Bukkit.getScheduler().cancelTasks(this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.enableUnstableAPI("brigadier");
        commandManager.registerCommand(new BattleCommand());
    }

    public Game getGame() {
        return game;
    }

    public static BattleRoyale getInstance() {
        return instance;
    }
}
