package com.github.elic0de.battleroyale;

import co.aikar.commands.PaperCommandManager;
import com.github.elic0de.battleroyale.command.BattleCommand;
import com.github.elic0de.battleroyale.config.Settings;
import com.github.elic0de.battleroyale.game.Game;
import com.github.elic0de.battleroyale.hook.EconomyHook;
import com.github.elic0de.battleroyale.hook.Hook;
import com.github.elic0de.battleroyale.hook.VaultEconomyHook;
import com.github.elic0de.battleroyale.listener.CompassListener;
import com.github.elic0de.battleroyale.listener.EventListener;
import com.github.elic0de.battleroyale.user.GameUserData;
import com.github.elic0de.battleroyale.user.GameUserManager;
import com.github.elic0de.battleroyale.utils.annoData.AnnoData;
import lombok.Getter;
import net.william278.annotaml.Annotaml;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Getter
public final class BattleRoyale extends JavaPlugin {

    private static BattleRoyale instance;

    private Game game;

    private Settings settings;

    private List<Hook> hooks = new ArrayList<>();

    private AnnoData database;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();

        hooks = new ArrayList<>();
        game = new Game();
        database = new AnnoData();
        database.createTable(GameUserData.class);

        registerCommands();
        registerEconomyHook();
        loadHooks();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(new CompassListener(), this);

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
        database.close();
        //Bukkit.getScheduler().cancelTasks(this);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.enableUnstableAPI("brigadier");
        commandManager.registerCommand(new BattleCommand());
    }

    private void registerEconomyHook() {
        final PluginManager plugins = Bukkit.getPluginManager();
        if (plugins.getPlugin("Vault") != null) {
            this.registerHook(new VaultEconomyHook(this));
        }
    }

    private void registerHook(Hook hook) {
        getHooks().add(hook);
    }

    private void loadHooks() {
        getHooks().stream().filter(Hook::isDisabled).forEach(Hook::enable);
        getLogger().log(Level.INFO, "Successfully loaded " + getHooks().size() + " hooks");
    }

    private <T extends Hook> Optional<T> getHook(Class<T> hookClass) {
        return hooks.stream()
                .filter(hook -> hookClass.isAssignableFrom(hook.getClass()))
                .map(hookClass::cast)
                .findFirst();
    }

    public Optional<EconomyHook> getEconomyHook() {
        return getHook(EconomyHook.class);
    }

    public static BattleRoyale getInstance() {
        return instance;
    }
}
