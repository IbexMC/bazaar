package com.ibexmc.bazaar.scheduled;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.internal.functions.WorldFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class ShopTarget {

    Bazaar instance;

    public ShopTarget(Bazaar instance) {
        this.instance = instance;
    }

    public void Run() {

        if (!instance.getBazaarData().isEnabled()) {
            return;
        }

        try {
            switch (instance.getBazaarConfig().getHologramType()) {
                case DECENT_HOLOGRAMS:
                    if (!instance.getBazaarData().hasDecentHolograms()) {
                        return;
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        boolean change = false;
                        Block targetBlock = player.getTargetBlock((Set<Material>) null, 5);
                        for (Map.Entry<Location, Shop> shopEntry : instance.getBazaarData().getShops().entrySet()) {
                            eu.decentsoftware.holograms.api.holograms.Hologram hologram = instance.getBazaarData().getDecentHologram(shopEntry.getKey());
                            if (WorldFunctions.match(targetBlock.getLocation(), shopEntry.getValue().getLocation(), true)) {
                                if (hologram.isHideState(player)) {
                                    hologram.removeHidePlayer(player);
                                    hologram.setShowPlayer(player);
                                    change = true;
                                }
                            } else {
                                if (!hologram.isHideState(player)) {
                                    hologram.removeShowPlayer(player);
                                    hologram.setHidePlayer(player);
                                    change = true;
                                }
                            }
                            if (change) {
                                hologram.update(player);
                                instance.getBazaarData().putDecentHologram(shopEntry.getKey(), hologram);
                            }
                        }
                    }
                    break;
            }
        }
        catch (IllegalStateException illegalEx) {
            instance.getDebug().log("ShopTarget", "Run()", "Unexpected Illegal State Error: " + illegalEx.getMessage());
        }
        catch (Exception ex) {
            instance.getDebug().log("ShopTarget", "Run()", "Unexpected Error: " + ex.getMessage());
        }

    }
}
