package com.ibexmc.bazaar.events;

import com.ibexmc.bazaar.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public final class RemoveShopEvent extends BaseEvents implements Cancellable {

    private String cancelReason; // The cancellation reason
    private boolean cancelled; // A flag to indicate if the event was cancelled

    /**
     * RemoveShopEvent(Location)
     * This event is called when a shop is removed
     *
     * @param player The player removing the shop
     * @param chest The chest of the shop to remove
     *
     */
    public RemoveShopEvent(Player player, Shop chest) {
        super(player, chest);
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
