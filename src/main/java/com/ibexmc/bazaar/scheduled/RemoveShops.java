package com.ibexmc.bazaar.scheduled;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.shop.ShopManager;
import com.ibexmc.internal.functions.WorldFunctions;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveShops {

    Bazaar bazaar;

    public RemoveShops (Bazaar bazaar) {
        this.bazaar = bazaar;
    }

    public void Run() {
        if (!bazaar.getBazaarData().isEnabled()) {
            return;
        }
        bazaar.getDebug().log(
                "RemoveShops",
                "Run",
                "Starting Shop Cleanup"
        );
        ShopManager shopManager = new ShopManager(bazaar);
        if (bazaar.getBazaarConfig().getShopCleanupSeconds() > 0) {
            boolean permanent = bazaar.getBazaarConfig().isShopCleanupDelete();
            List<Location> shopLocations = new ArrayList<>();
            for (Map.Entry<Location, Shop> shopEntry : bazaar.getBazaarData().getShops().entrySet()) {
                shopLocations.add(shopEntry.getKey());
            }
            for (Location location : shopLocations) {
                bazaar.getDebug().log(
                        "RemoveShops",
                        "Run",
                        "Checking Shop at " + WorldFunctions.simpleReadable(location)
                );
                Shop shop = bazaar.getBazaarData().getShopAtLocation(location);
                if (shop != null) {
                    Material shopMaterial = location.getBlock().getType();
                    if (
                            !shopMaterial.equals(Material.CHEST) &&
                                    !shopMaterial.equals(Material.TRAPPED_CHEST) &&
                                    !shopMaterial.equals(Material.BARREL)
                    ) {
                        bazaar.getDebug().log(
                                "RemoveShops",
                                "Run",
                                "Shop location is not a Chest, Trapped Chest, or Barrel. Material Found: " + shopMaterial.name() + " - Removing"
                        );
                        shopManager.cleanup(shop, permanent);
                    }
                }
            }
        }
    }

}
