package com.ibexmc.bazaar.livedata;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.ibexmc.domain.Domain;
import com.ibexmc.myriad.Myriad;

public class Dependencies {

    // DecentHolograms
    private boolean decentHologramsExists = false;
    public boolean hasDecentHolograms() {
        Plugin plugin = Bazaar.getInstance().getServer().getPluginManager().getPlugin("DecentHolograms");
        return (plugin != null);
    }

    // Economy
    private Economy econ = null;
    private boolean vaultExists = false;
    public boolean hasVault() {
        return vaultExists;
    }
    public void setVault(boolean vault) {
        vaultExists = true;
    }
    public boolean setupEconomy() {
        Bazaar instance = Bazaar.getInstance();
        try {
            if (instance.getServer().getPluginManager().getPlugin("Vault") == null) {
                return false;
            }
            RegisteredServiceProvider<Economy> rsp = Bazaar.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            econ = rsp.getProvider();
            return econ != null;
        } catch (Exception ex) {
            instance.getDebug().log("Bazaar", "setupEconomy", "Unexpected Error - " + ex.getMessage());
            return false;
        }
    }
    public Economy getEcononomy() {
        return econ;
    }
    public static class BalanceResponse {
        private double balance;
        private boolean success;
        private String errorMessage;
        private String stackTrace;

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }
    }
    public static class TransactionResponse {
        private boolean success;
        private String errorMessage;
        private String stackTrace;
        private double newBalance;

        public TransactionResponse(boolean success, String errorMessage, String stackTrace, double newBalance) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.stackTrace = stackTrace;
            this.newBalance = newBalance;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public double getNewBalance() {
            return newBalance;
        }
    }
    public static BalanceResponse balancePlayer(OfflinePlayer offlinePlayer) {
        Bazaar instance = Bazaar.getInstance();
        BalanceResponse balanceResponse = new BalanceResponse();
        try {
            if (offlinePlayer != null) {
                Economy economy = Bazaar.getInstance().getDependencies().econ;
                balanceResponse.setSuccess(true);
                balanceResponse.setBalance(economy.getBalance(offlinePlayer));
            } else {
                balanceResponse.setBalance(0);
                balanceResponse.setSuccess(false);
                balanceResponse.setErrorMessage(instance.getLocale().localeText("invalid_player", "Invalid Player", null));
                balanceResponse.setStackTrace("");
            }
        } catch (Exception ex) {
            balanceResponse.setBalance(0);
            balanceResponse.setSuccess(false);
            balanceResponse.setErrorMessage(instance.getLocale().localeText("unexpected_error", "Unexpected Error", null));
            balanceResponse.setStackTrace(ex.getStackTrace().toString());
        }
        return balanceResponse;
    }
    public static TransactionResponse payPlayer(OfflinePlayer offlinePlayer, double amount) {
        Bazaar instance = Bazaar.getInstance();
        try {
            if (offlinePlayer != null) {
                Economy economy = Bazaar.getInstance().getDependencies().econ;


                EconomyResponse r1 = economy.depositPlayer(offlinePlayer, amount);
                if (r1.transactionSuccess()) {
                    return new TransactionResponse(true, "", "", economy.getBalance(offlinePlayer));
                } else {
                    return new TransactionResponse(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null), r1.errorMessage, economy.getBalance(offlinePlayer));
                }

            } else {
                return new TransactionResponse(false, instance.getLocale().localeText("invalid_player", "Invalid Player", null), "", 0);
            }
        } catch (Exception ex) {
            return new TransactionResponse(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null), ex.getMessage(), 0);
        }
    }
    public static TransactionResponse chargePlayer(OfflinePlayer offlinePlayer, double amount) {
        Bazaar instance = Bazaar.getInstance();
        BalanceResponse balanceResponse = new BalanceResponse();
        try {
            if (offlinePlayer != null) {
                Economy economy = Bazaar.getInstance().getDependencies().econ;
                if (economy.getBalance(offlinePlayer) >= amount) {
                    EconomyResponse r1 = economy.withdrawPlayer(offlinePlayer, amount);
                    if (r1.transactionSuccess()) {
                        return new TransactionResponse(true, "", "", economy.getBalance(offlinePlayer));
                    } else {
                        return new TransactionResponse(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null), r1.errorMessage, economy.getBalance(offlinePlayer));
                    }
                } else {
                    return new TransactionResponse(false, instance.getLocale().localeText("insufficient_funds", "Insufficient funds", null), "", economy.getBalance(offlinePlayer));
                }
            } else {
                return new TransactionResponse(false, instance.getLocale().localeText("invalid_player", "Invalid Player", null), "", 0);
            }
        } catch (Exception ex) {
            return new TransactionResponse(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null), ex.getMessage(), 0);
        }
    }

    // Myriad Clans
    private boolean hasMyriadClans = false;
    private Myriad mc;
    public boolean hookMyriadClans() {
        Plugin plug = Bazaar.getInstance().getServer().getPluginManager().getPlugin("Myriad");

        if (plug != null)
        {
            mc = ((Myriad) plug);
            hasMyriadClans = true;
            return true;
        } else {
            return false;
        }
    }
    public Myriad getMyriadClans()
    {
        return mc;
    }
    public boolean hasMyriadClans()
    {
        return hasMyriadClans;
    }

    // Domain
    private boolean hasDomain = false;
    private Domain domain;
    public boolean hookDomain() {
        Plugin plug = Bazaar.getInstance().getServer().getPluginManager().getPlugin("Domain");

        if (plug != null)
        {
            domain = ((Domain) plug);
            hasDomain = true;
            return true;
        } else {
            return false;
        }
    }
    public Domain getDomain()
    {
        return domain;
    }
    public boolean hasDomain()
    {
        if (Bazaar.getInstance().getBazaarConfig().domainProtection()) {
            return hasDomain;
        } else {
            return false;
        }
    }


    // WorldGuard
    private boolean hasWorldGuard = false;
    private WorldGuardPlugin worldGuard;
    public boolean hookWorldGuard() {
        Plugin plug = Bazaar.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plug != null) {
            worldGuard = ((WorldGuardPlugin) plug);
            hasWorldGuard = true;
            return true;
        }
        return false;
    }
    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuard;
    }
    public WorldGuard getWorldGuard() {
        return WorldGuard.getInstance();
    }
    public boolean hasWorldGuard() {
        if (Bazaar.getInstance().getBazaarConfig().worldguardProtection()) {
            return hasWorldGuard;
        } else {
            return false;
        }
    }
    public boolean hasWorldGuardIgnoreConfig() {
        return hasWorldGuard;
    }
}
