package com.ibexmc.bazaar.events;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.util.Logging;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;


public final class SellEvent extends BaseEvents implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private String cancelReason; // The cancellation reason
    private boolean cancelled; // A flag to indicate if the event was cancelled

    /**
     * SellEvent(Location, OfflinePlayer)
     * This event is called when a player tries to sells an item to the shop
     *
     * @param player The player making the purchase
     * @param shop The shop being purchased from
     *
     */
    public SellEvent(Player player, Shop shop) {

        super(player, shop);

        // If the event is cancelled, exit out
        if (this.cancelled) {
            Bazaar.getInstance().getDebug().log("SellEvent", "Sell", "&4Exiting event - Event is cancelled");
        }

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
