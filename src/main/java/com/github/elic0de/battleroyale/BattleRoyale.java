package com.github.elic0de.battleroyale;

import co.aikar.commands.PaperCommandManager;
import com.github.elic0de.battleroyale.config.Settings;
import com.github.elic0de.eliccommon.plugin.AbstractPlugin;
import com.github.elic0de.battleroyale.command.BattleCommand;
import com.github.elic0de.battleroyale.game.Game;
import com.github.elic0de.battleroyale.listener.EventListener;
import com.github.elic0de.battleroyale.user.GameUserManager;
import lombok.Getter;
import net.william278.annotaml.Annotaml;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

@Getter
public final class BattleRoyale extends AbstractPlugin {

    private static BattleRoyale instance;

    private Game game;

    private Settings settings;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();

        game = new Game();

        registerCommands();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        GameUserManager.getOnlineUsers().forEach(player -> game.join(player));
    }

    private void loadConfig() throws RuntimeException {
        try {
            this.settings = Annotaml.create(new File(getDataFolder(), "settings.yml"), Settings.class).get();
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            getLogger().log(Level.SEVERE, "Failed to load configuration files", e);
            throw new RuntimeException(e);
        }
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

    public static BattleRoyale getInstance() {
        return instance;
    }
}
