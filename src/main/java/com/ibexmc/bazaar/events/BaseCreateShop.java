package com.ibexmc.bazaar.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class BaseCreateShop extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location location;
    private String itemText;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private int cooldown;
    private boolean adminShop;
    private boolean clanShop;
    private boolean serverShop;

    public BaseCreateShop(Player player, Location location, String itemText, int amount, double buyPrice,
                          double sellPrice, int cooldown, boolean adminShop, boolean clanShop, boolean serverShop
    ) {
        this.player = player;
        this.location = location;
        this.itemText = itemText;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.cooldown = cooldown;
        this.adminShop = adminShop;
        this.clanShop = clanShop;
        this.serverShop = serverShop;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public String getItemText() {
        return itemText;
    }

    public int getAmount() {
        return amount;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean isAdminShop() {
        return adminShop;
    }

    public boolean isClanShop() {
        return clanShop;
    }

    public boolean isServerShop() {
        return serverShop;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
