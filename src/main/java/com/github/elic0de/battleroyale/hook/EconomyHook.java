package com.github.elic0de.battleroyale.hook;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.user.GameUser;

import java.math.BigDecimal;

public abstract class EconomyHook extends Hook {

    protected EconomyHook(BattleRoyale plugin, String name) {
        super(plugin, name);
    }

    public abstract BigDecimal getBalance(GameUser player);

    public abstract boolean hasMoney(GameUser player, BigDecimal amount);

    public abstract void takeMoney(GameUser player, BigDecimal amount);

    public abstract void giveMoney(GameUser player, BigDecimal amount);

    public abstract String formatMoney(BigDecimal amount);

}
