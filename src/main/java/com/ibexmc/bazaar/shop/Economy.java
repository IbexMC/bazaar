package com.ibexmc.bazaar.shop;

import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.functions.NumberFunctions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.livedata.ClanTasks;
import com.ibexmc.bazaar.livedata.Dependencies;

import java.util.HashMap;
import java.util.Map;

public class Economy {

    /**
     * Checks if the OfflinePlayer has at least the amount of funds specified available
     * @param offlinePlayer OfflinePlayer to check
     * @param amount Amount to check for
     * @return If true, funds are available
     */
    public static boolean playerHasFunds(OfflinePlayer offlinePlayer, double amount) {
        Dependencies.BalanceResponse balCheck = Dependencies.balancePlayer(offlinePlayer);
        if (balCheck.isSuccess()) {
            return balCheck.getBalance() >= amount;
        } else {
            return false;
        }
    }

    /**
     * Withdraws funds from the OfflinePlayer
     * @param offlinePlayer OfflinePlayer to withdraw from
     * @param amount Amount to withdraw
     * @return If true, funds were withdrawn successfully
     */
    public static boolean withdrawFunds(OfflinePlayer offlinePlayer, double amount) {
        Bazaar instance = Bazaar.getInstance();
        Dependencies.TransactionResponse chargeTransactionResponse = Dependencies.chargePlayer(offlinePlayer, amount);
        if (chargeTransactionResponse.isSuccess()) {
            instance.getDebug().log(
                    "Economy",
                    "withdrawFunds",
                    "Funds withdrawn successfully\n" +
                            "OfflinePlayer UUID: " + offlinePlayer.getUniqueId() + "\n" +
                            "Amount: " + amount
            );
            return true;
        } else {
            instance.getDebug().log(
                    "Economy",
                    "withdrawFunds",
                    "&eUnexpected error withdrawing funds:\n" +
                            "OfflinePlayer UUID: " + offlinePlayer.getUniqueId() + "\n" +
                            "Amount: " + amount + "\n" +
                            "Error: " + chargeTransactionResponse.getErrorMessage()
            );
            return false;
        }
    }

    /**
     * Deposits funds to an OfflinePlayer
     * @param offlinePlayer OfflinePlayer to deposit funds to
     * @param amount Amount to deposit
     * @return If true, funds were deposited successfully
     */
    public static boolean depositFunds(OfflinePlayer offlinePlayer, double amount) {
        Bazaar instance = Bazaar.getInstance();
        Dependencies.TransactionResponse chargeTransactionResponse = Dependencies.payPlayer(offlinePlayer, amount);
        if (chargeTransactionResponse.isSuccess()) {
            instance.getDebug().log(
                    "Economy",
                    "depositFunds",
                    "Funds deposited successfully\n" +
                            "OfflinePlayer UUID: " + offlinePlayer.getUniqueId() + "\n" +
                            "Amount: " + amount
            );
            return true;
        } else {
            instance.getDebug().log(
                    "Economy",
                    "depositFunds",
                    "&eUnexpected error depositing funds:\n" +
                            "OfflinePlayer UUID: " + offlinePlayer.getUniqueId() + "\n" +
                            "Amount: " + amount + "\n" +
                            "Error: " + chargeTransactionResponse.getErrorMessage()
            );
            return false;
        }
    }

    /**
     * Gets the balance for an OfflinePlayer
     * @param offlinePlayer OfflinePlayer to get balance for
     * @return Current balance
     */
    public static double getBalance(OfflinePlayer offlinePlayer) {
        Dependencies.BalanceResponse balCheck = Dependencies.balancePlayer(offlinePlayer);
        if (balCheck.isSuccess()) {
            return balCheck.getBalance();
        } else {
            return 0d;
        }
    }

    /**
     * Reports the balance of an OfflinePlayer and sends it in a message to another Player
     * @param player Player to report to
     * @param offlinePlayer OfflinePlayer to check the balance for
     */
    public static void reportBalance(Player player, OfflinePlayer offlinePlayer) {
        Bazaar instance = Bazaar.getInstance();
        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder.put("<%balance%>", "" + NumberFunctions.doubleTo2DecimalPlaceString(Economy.getBalance(offlinePlayer)));
        instance.getMessage().send(player, "balance", "&aBalance: <%currencysymbol%><%balance%>", placeHolder, true);
    }

    /**
     * Checks if the Clan has the specified amount of funds available
     * @param clanTag Clan Tag
     * @param amount Amount to check for
     * @return If true, funds are available
     */
    public static boolean clanHasFunds(String clanTag, double amount) {
        return ClanTasks.sufficientFundsAvailable(clanTag, amount);
    }

    /**
     * Withdraws funds from a Clan
     * @param clanTag Clan Tag
     * @param amount Amount to withdraw
     * @return If true, funds were withdrawn successfully
     */
    public static boolean withdrawClanFunds(String clanTag, double amount) {
        Bazaar instance = Bazaar.getInstance();
        ClanTasks.ClanTransactionResult chargeClanTransactionResult = ClanTasks.withdrawFundsFromClan(clanTag, amount);
        if (chargeClanTransactionResult.isSuccess()) {
            instance.getDebug().log(
                    "Economy",
                    "withdrawClanFunds",
                    "Clan funds withdrawn successfully\n" +
                            "Clan tag: " + clanTag + "\n" +
                            "Amount: " + amount
            );
            return true;
        } else {
            instance.getDebug().log(
                    "Economy",
                    "withdrawClanFunds",
                    "&eUnexpected error withdrawing clan funds:\n" +
                            "Clan tag: " + clanTag + "\n" +
                            "Amount: " + amount + "\n" +
                            "Error: " + chargeClanTransactionResult.getReturnText()
            );
            return false;
        }
    }

    /**
     * Deposits funds to a Clan
     * @param clanTag Clan Tag
     * @param amount Amount to deposit
     * @return If true, funds were deposited successfully
     */
    public static boolean depositClanFunds(String clanTag, double amount) {
        Bazaar instance = Bazaar.getInstance();
        ClanTasks.ClanTransactionResult payClanTransactionResult = ClanTasks.addFundsToClan(clanTag, amount);
        if (payClanTransactionResult.isSuccess()) {
            instance.getDebug().log(
                    "Economy",
                    "depositClanFunds",
                    "Clan funds deposited successfully\n" +
                            "Clan tag: " + clanTag + "\n" +
                            "Amount: " + amount
            );
            return true;
        } else {
            instance.getDebug().log(
                    "Economy",
                    "depositClanFunds",
                    "&eUnexpected error depositing clan funds:\n" +
                            "Clan tag: " + clanTag + "\n" +
                            "Amount: " + amount + "\n" +
                            "Error: " + payClanTransactionResult.getReturnText()
            );
            return false;
        }
    }

    /**
     * Gets the current balance for the Clan
     * @param clanTag Clan Tag
     * @return Current balance
     */
    public static double getClanBalance(String clanTag) {
        return ClanTasks.balanceClan(clanTag);
    }

    /**
     * Reports the Clan balance to a specified player
     * @param player Player to report to
     * @param clanTag Clan Tag
     */
    public static void reportClanBalance(Player player, String clanTag) {
        Bazaar instance = Bazaar.getInstance();
        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder.put("<%balance%>", "" + NumberFunctions.doubleTo2DecimalPlaceString(ClanTasks.balanceClan(clanTag)));
        instance.getMessage().send(player, "clan_balance", "&aClan Balance: <%currencysymbol%><%balance%>", placeHolder, true);
    }
}
