package com.github.elic0de.battleroyale.hook;

import com.github.elic0de.battleroyale.BattleRoyale;
import org.jetbrains.annotations.NotNull;

public abstract class Hook {

    protected final BattleRoyale plugin;
    private final String name;
    private boolean enabled = false;

    protected Hook(@NotNull BattleRoyale plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    protected abstract void onEnable();

    public final void enable() {
        this.onEnable();
        this.enabled = true;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    /**
     * Get the name of the hook
     *
     * @return the name of the hook
     */
    @NotNull
    public String getName() {
        return name;
    }

}
