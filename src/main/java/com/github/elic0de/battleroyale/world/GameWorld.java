package com.github.elic0de.battleroyale.world;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.user.GameUser;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.util.FileUtil;

import java.io.File;

@Getter
public class GameWorld {

    private World world;
    private String worldName;

    public GameWorld(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            world = Bukkit.getWorld(worldName);
        } else {
            world = Bukkit.createWorld(WorldCreator.name(worldName));
        }
        worldName = world.getName();

        final WorldBorder border = world.getWorldBorder();
        border.setSize(900);
        border.setCenter(world.getSpawnLocation());
    }

    public void teleportSpawnLocation(GameUser user) {
        user.getPlayer().teleport(world.getSpawnLocation());
    }

    public void resetWorld() {
        if (!Bukkit.unloadWorld(world, false)) return;

        File worldFolder = new File(BattleRoyale.getInstance().getDataFolder().getParentFile().getParentFile(), world.getName()); // World folder
        worldFolder.delete();

        world = Bukkit.createWorld(WorldCreator.name(worldName));

    }
}
