package com.ibexmc.bazaar.shop.multishop;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.Location;

public class PlacedMultiShop implements IPlacedMultiShop {

    private final String shopKey;
    private final Location location;

    public PlacedMultiShop(String shopKey, Location location) {
        this.shopKey = shopKey;
        this.location = location;
    }

    public String getShopKey() {
        return shopKey;
    }

    public Location getLocation() {
        return location;
    }

}
