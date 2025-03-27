package com.ibexmc.bazaar.shop;

import com.ibexmc.bazaar.util.Logging;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.ibexmc.bazaar.Bazaar;

public class TransactionLock {
    /**
     * This code checks if there is a transaction locked chest at the location
     * @param location The location to check
     */
    public static boolean check(Location location) {
        return Bazaar.getInstance().getBazaarData().isLockedShop(location.clone());
    }

    /**
     * This code sets or releases the transaction lock on the chest
     * @param shop The shop being purchased from
     * @param player The player making the purchase
     * @param lockStatus The transaction lock status to set
     */
    public static void set(Shop shop, Player player, boolean lockStatus) {
        Bazaar instance = Bazaar.getInstance();
        if (lockStatus) {
            instance.getDebug().log(
                    "TransactionLock",
                    "set(Chest, Player, boolean)",
                    "Applying transaction lock"
            );
            Bazaar.getInstance().getBazaarData().putLockedShop(shop.getLocation(), player.getUniqueId());
        } else {
            instance.getDebug().log(
                    "TransactionLock",
                    "set(Chest, Player, boolean)",
                    "Removing transaction lock"
            );
            Bazaar.getInstance().getBazaarData().removeLockedShop(shop.getLocation());
        }
    }
}
