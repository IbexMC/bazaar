package com.ibexmc.bazaar.events;

import com.ibexmc.bazaar.shop.Shop;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class BaseEvents extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Shop chest;
    private Player player;
    private Location location;
    private String itemText;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private int cooldown;
    private boolean adminShop;
    private boolean clanShop;

    public BaseEvents(Player player, Shop chest) {
        this.player = player;
        this.chest = chest;
    }
    public BaseEvents(Player player, Location location, String itemText, int amount, double buyPrice, double sellPrice, int cooldown, boolean adminShop, boolean clanShop) {
        this.player = player;
        this.location = location;
        this.itemText = itemText;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.cooldown = cooldown;
        this.adminShop = adminShop;
        this.clanShop = clanShop;
    }

    public Shop getChest() {
        return chest;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
