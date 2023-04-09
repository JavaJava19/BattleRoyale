package com.github.elic0de.battleroyale.dragon;

import com.github.elic0de.battleroyale.BattleRoyale;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DragonTrait extends Trait {

    private final Map<Integer, Location> locations = new HashMap<>();
    private final AtomicInteger index = new AtomicInteger();

    private BukkitTask task;

    public DragonTrait(WorldBorder worldBorder) {
        super("dragonTrait");
        final Location origin =  worldBorder.getCenter();
        final double gap = worldBorder.getSize()/2;
        final Location start = new Location(origin.getWorld(), origin.getX() + gap, 130, origin.getZ() + gap);
        for(int i = 0; i <= worldBorder.getSize(); i+=45){
            locations.put(index.incrementAndGet(), start.clone().subtract(i, 0, i));
        }
        index.set(1);
    }

    @Override
    public void run() {
        if (task != null) task.cancel();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (locations.get(index.get()) == null) {
                    npc.destroy();
                    cancel();
                    return;
                }
                if (npc.getNavigator().isNavigating()) return;
                if (npc.isSpawned()) npc.getNavigator().setTarget(locations.get(index.incrementAndGet()));
            }
        }.runTaskTimer(BattleRoyale.getInstance(), 0, 20);
    }

    public void reset() {
        npc.destroy();
        if (task != null) task.cancel();
    }
}
