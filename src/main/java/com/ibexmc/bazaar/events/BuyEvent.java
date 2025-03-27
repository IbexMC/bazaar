package com.ibexmc.bazaar.events;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.util.Logging;

public final class BuyEvent extends BaseEvents implements Cancellable {

    private String cancelReason; // The cancellation reason
    private boolean cancelled; // A flag to indicate if the event was cancelled
    /**
     * BuyEvent()
     * This event is called when a player tries to sells an item to the shop
     *
     * @param player The player making the purchase
     * @param chest The shop being purchased from
     *
     */
    public BuyEvent(Player player, Shop chest) {

        super(player, chest);

        Bazaar.getInstance().getDebug().log("BuyEvent", "Buy", "BuyEvent called");

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
