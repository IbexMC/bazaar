package com.ibexmc.bazaar.api;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.internal.functions.WorldFunctions;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BazaarAPI {

    private Bazaar instance;

    public BazaarAPI() {

    }

    public BazaarAPI(Bazaar instance) {
        this.instance = instance;
    }

    public void setInstance(Bazaar instance) {
        this.instance = instance;
    }

    /**
     * Gets a set of all shop locations
     * @return Set of shop locations
     */
    public Set<Location> getAllShopLocations() {
        Set<Location> locations = new HashSet<>();
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            locations.add(entry.getKey());
        }
        return locations;
    }

    /**
     * Gets a set of all shop locations owned by the player specified
     * @param uuid Player unique identifier
     * @return Set of shop locations
     */
    public Set<Location> getPlayerShopLocations(UUID uuid) {
        Set<Location> locations = new HashSet<>();
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            if (entry.getValue().getSellerOfflinePlayer().getUniqueId().equals(uuid)) {
                locations.add(entry.getKey());
            }
        }
        return locations;
    }

    /**
     * Gets a set of all admin shop locations
     * @return Set of admin shop locations
     */
    public Set<Location> getAdminShopLocations() {
        Set<Location> locations = new HashSet<>();
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            if (entry.getValue().isAdminShop()) {
                locations.add(entry.getKey());
            }
        }
        return locations;
    }

    /**
     * Gets a set of all server shop locations
     * @return Set of server shop locations
     */
    public Set<Location> getServerShopLocations() {
        Set<Location> locations = new HashSet<>();
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            if (entry.getValue().isServerShop()) {
                locations.add(entry.getKey());
            }
        }
        return locations;
    }

    /**
     * Gets a set of all clan shop locations for the clan tag provided
     * @param tag Clan tag
     * @return Set of clan shop locations
     */
    public Set<Location> getClanShopLocations(String tag) {
        Set<Location> locations = new HashSet<>();
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            if (entry.getValue().isClanShop()) {
                if (entry.getValue().getClanTag().equalsIgnoreCase(tag)) {
                    locations.add(entry.getKey());
                }
            }
        }
        return locations;
    }

    /**
     * Removes all the shop locations owned by the player specified
     * @param uuid Player unique identifier
     */
    public void removeAllPlayerShops(UUID uuid) {
        Set<Location> locations = new HashSet<>();
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            if (entry.getValue().getSellerOfflinePlayer().getUniqueId().equals(uuid)) {
                locations.add(entry.getKey());
            }
        }
        for (Location location : locations) {
            instance.getBazaarData().removeShopLocation(location);
        }
    }

    /**
     * Gets a shop at the specified location
     * @param location Location of shop
     * @return Shop object - null if not found
     */
    public Shop getShop(Location location) {
        location = WorldFunctions.simplify(location);
        return instance.getBazaarData().getShopAtLocation(location);
    }

    /**
     * Replaces the shop at the specified location
     * @param location Location of shop
     * @param shop Shop object
     */
    public void putShop(Location location, Shop shop) {
        instance.getBazaarData().putShops(location, shop);
    }

}
