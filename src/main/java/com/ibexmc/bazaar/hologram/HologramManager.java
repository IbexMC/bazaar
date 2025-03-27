package com.ibexmc.bazaar.hologram;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.messaging.Debug;
import com.ibexmc.bazaar.util.log.Error;
import org.bukkit.Location;

import java.util.Map;

public class HologramManager {

    private final Bazaar instance;
    private final Error error;
    private final Debug debug;

    public HologramManager(Bazaar instance) {
        this.instance = instance;
        this.error = new Error(instance);
        this.debug = new Debug(instance, "Bazaar", instance.getInternalData());
    }

    /**
     * Creates the hologram for a shop
     * @param shop Shop to create hologram for
     */
    public void createHologram(Shop shop) {
        if (shop != null) {
            if (shop.isLoaded()) {
                IHologram hologram;
                HologramType hologramType = instance.getBazaarConfig().getHologramType();
                debug.log(
                        "HologramManager",
                        "createHologram",
                        "Hologram Type: " + hologramType
                );
                switch (hologramType) {
                    case DECENT_HOLOGRAMS:
                        hologram = new DecentHolograms(instance);
                        break;
                    default:
                        hologram = null;
                        break;
                }
                if (hologram != null) {
                    hologram.createHologram(shop);
                }
            } else {
                debug.log(
                        "HologramManager",
                        "createHologram",
                        "Not showing hologram for " + shop.getName() + " as it is not a valid shop"
                );
            }
        } else {
            debug.log(
                    "HologramManager",
                    "createHologram",
                    "Not showing hologram for shop - Shop returned null"
            );
        }
    }

    /**
     * Removes the hologram for a shop
     * @param shop Shop to remove hologram for
     */
    public void removeHologram(Shop shop) {
        instance.getDebug().log("HologramManager", "remove", "&7Removing hologram");
        if (shop != null) {
            IHologram hologram;
            HologramType hologramType = instance.getBazaarConfig().getHologramType();
            debug.log(
                    "HologramManager",
                    "removeHologram",
                    "Hologram Type: " + hologramType
            );
            switch (hologramType) {
                case DECENT_HOLOGRAMS:
                    instance.getDebug().log("HologramManager", "remove", "&7Removing Decent Holograms hologram");
                    hologram = new DecentHolograms(instance);
                    break;
                default:
                    instance.getDebug().log("HologramManager", "remove", "&7Not removing any hologram");
                    hologram = null;
                    break;
            }
            if (hologram != null) {
                instance.getDebug().log("HologramManager", "remove", "&7Hologram is not null - destroying");
                hologram.destroyHologram(shop);
            }
        } else {
            debug.log(
                    "HologramManager",
                    "removeHologram",
                    "Unable to remove hologram - Shop returned null"
            );
        }
    }

    /**
     * Removes and creates all shop holograms
     */
    public void reloadHolograms() {
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            removeHologram(entry.getValue());
            createHologram(entry.getValue());
        }
    }

    /**
     * Removes all shop holograms
     */
    public void removeHolograms() {
        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
            removeHologram(entry.getValue());
        }
    }
}
