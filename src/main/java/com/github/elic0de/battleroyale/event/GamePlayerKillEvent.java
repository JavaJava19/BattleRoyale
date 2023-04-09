package com.github.elic0de.battleroyale.event;

import com.github.elic0de.battleroyale.user.GameUser;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class GamePlayerKillEvent extends PlayerEvent {

    private final GameUser killer;
    private final GameUser vitim;
    private static final HandlerList handlers = new HandlerList();

    public GamePlayerKillEvent(@NotNull GameUser killer, @NotNull GameUser vitim) {
        super(killer.getPlayer());
        this.killer = killer;
        this.vitim = vitim;
    }

    public GameUser getKiller() {
        return killer;
    }

    public GameUser getVitim() {
        return vitim;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
}
