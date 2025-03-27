package com.ibexmc.bazaar.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class CreateShopEvent extends BaseCreateShop implements Cancellable {

    private String cancelReason; // The cancellation reason
    private boolean cancelled; // A flag to indicate if the event was cancelled

    /**
     * CreateShopEvent(Player, Location, String, int, int, int, boolean)
     * This event is called when a shop is created
     *
     * @param player The player creating the shop
     * @param location The location to create the shop at
     * @param itemText The serialized ItemStack
     * @param amount The number of items to sell in each transaction
     * @param buyPrice The cost to buy the item
     * @param sellPrice The cost to sell the item for
     * @param cooldown The number of seconds to cooldown before buying/selling again
     * @param adminShop Indicates if it is an admin shop
     * @param clanShop Indicates if it is a clan shop
     * @param serverShop Indicates if it is a server shop

     */
    public CreateShopEvent(Player player, Location location, String itemText, int amount, double buyPrice,
                           double sellPrice, int cooldown, boolean adminShop, boolean clanShop, boolean serverShop
    ) {
        super(player, location, itemText, amount, buyPrice, sellPrice, cooldown, adminShop, clanShop, serverShop);
    }


    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
