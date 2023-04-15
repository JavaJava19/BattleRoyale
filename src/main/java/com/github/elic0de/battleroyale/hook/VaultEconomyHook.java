package com.github.elic0de.battleroyale.hook;

import com.github.elic0de.battleroyale.BattleRoyale;
import com.github.elic0de.battleroyale.user.GameUser;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.logging.Level;

public class VaultEconomyHook extends EconomyHook {

    protected Economy economy;

    public VaultEconomyHook(BattleRoyale plugin) {
        super(plugin, "Vault");
    }

    @Override
    public void onEnable() throws IllegalStateException {
        final RegisteredServiceProvider<Economy> economyProvider = plugin.getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            throw new IllegalStateException("Could not resolve Vault economy provider");
        }
        this.economy = economyProvider.getProvider();
        plugin.getLogger().log(Level.INFO, "Enabled Vault economy hook");
    }

    @Override
    public BigDecimal getBalance(GameUser player) {
        return BigDecimal.valueOf(economy.getBalance(player.getPlayer()));
    }

    @Override
    public boolean hasMoney(GameUser player, BigDecimal amount) {
        return getBalance(player).compareTo(amount) >= 0;
    }

    @Override
    public void takeMoney(GameUser player, BigDecimal amount) {
        economy.withdrawPlayer(player.getPlayer(), amount.doubleValue());
        //player.getBoard().updateCoins();
    }

    @Override
    public void giveMoney(GameUser player, BigDecimal amount) {
        economy.depositPlayer(player.getPlayer(), amount.doubleValue());
        //player.getBoard().updateCoins();
    }

    @Override
    public String formatMoney(BigDecimal amount) {
        return economy.format(amount.doubleValue());
    }

}
