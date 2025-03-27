package com.ibexmc.bazaar.livedata;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class VaultTasks {

    public static class VaultTaskResult {
        private boolean success;
        private String returnText;

        public VaultTaskResult(boolean success, String returnText) {
            this.success = success;
            this.returnText = returnText;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReturnText() {
            return returnText;
        }
    }

    /**
     * Charges the OfflinePlayer the cost of the new shop
     * @param offlinePlayer OfflinePlayer to charge
     * @param cost Shop cost
     * @return If VaultTaskResult.isSuccess = true, player was charged successfully
     */
    public static VaultTaskResult chargeForNewShop(OfflinePlayer offlinePlayer, Double cost) {

        Bazaar instance = Bazaar.getInstance();
        Map<String, String> costPlaceHolder = new HashMap<>();

        if (offlinePlayer == null) {
            costPlaceHolder.put("<%error%>", instance.getLocale().localeText("invalid_player", "Invalid Player", null));
            instance.getLog().log("VaultTasks", "Create Shop", "Error creating shop - Invalid Player: " + offlinePlayer.getUniqueId());
            return new VaultTaskResult(false, "Cancelled due to invalid player trying to create shop");
        }
        Player player = (Player) offlinePlayer;
        if (player == null) {
            costPlaceHolder.put("<%error%>", instance.getLocale().localeText("invalid_player", "Invalid Player", null));

            instance.getMessage().send(player, "shop_create_error_fail", "&cERROR &f- Unable to create shop. <%error%>", costPlaceHolder, true);


            instance.getLog().log("VaultTasks", "Create Shop", "Error creating shop - Invalid Player: " + player.getUniqueId());
            return new VaultTaskResult(false, "Cancelled due to invalid player trying to create shop");
        }

        Dependencies.BalanceResponse balanceResponse = Dependencies.balancePlayer(offlinePlayer);

        if (balanceResponse.isSuccess()) {
            if (balanceResponse.getBalance() >= cost) {
                // do stuff
                Dependencies.TransactionResponse chargeResponse = Dependencies.chargePlayer(offlinePlayer, cost);
                if (chargeResponse.isSuccess()) {
                    return new VaultTaskResult(true, "");
                } else {
                    costPlaceHolder.put("<%error%>", instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", costPlaceHolder));
                    instance.getMessage().send(player, "shop_create_error_fail", "&cERROR &f- Unable to create shop. <%error%>", costPlaceHolder, true);
                    instance.getLog().log("VaultTasks", "Create Shop", "Error creating shop - Error: " + chargeResponse.getErrorMessage() + " " + chargeResponse.getStackTrace());
                    return new VaultTaskResult(false, "Cancelled due to unexpected error");
                }
            } else {
                costPlaceHolder.put("<%balance%>", "" + balanceResponse.getBalance());
                costPlaceHolder.put("<%cost%>", "" + cost);
                costPlaceHolder.put("<%error%>", instance.getLocale().localeText("insufficient_funds_cost_bal", "Insufficient Funds.  Current balance: <%balance%> | Cost: <%cost%>", costPlaceHolder));
                instance.getMessage().send(player, "shop_create_error_fail", "&cERROR &f- Unable to create shop. <%error%>", costPlaceHolder, true);
                return new VaultTaskResult(false, "Cancelled due to insufficient funds");
            }
        } else {
            costPlaceHolder.put("<%error%>", balanceResponse.getErrorMessage());
            instance.getMessage().send(player, "shop_create_error_fail", "&cERROR &f- Unable to create shop. <%error%>", costPlaceHolder, true);
            instance.getLog().log("VaultTasks", "Create Shop", "Error creating shop - Error: " + balanceResponse.getErrorMessage() + " " + balanceResponse.getStackTrace());
            return new VaultTaskResult(false, "Cancelled due to error charging player for shop");
        }



    }

}
