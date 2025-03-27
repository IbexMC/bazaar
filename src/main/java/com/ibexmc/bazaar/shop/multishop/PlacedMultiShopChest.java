package com.ibexmc.bazaar.shop.multishop;

import org.bukkit.Location;

public class PlacedMultiShopChest extends PlacedMultiShop {

    private MultiShopChest multiShop;
    public PlacedMultiShopChest(Location location) {
        super("", location);
    }

    public PlacedMultiShopChest(Location location, MultiShopChest multiShop) {
        super("", location);
        this.multiShop = multiShop;
    }


    public MultiShopChest getMultiShop() {
        return multiShop;
    }

    public void setMultiShop(MultiShopChest multiShop) {
        this.multiShop = multiShop;
    }

}
