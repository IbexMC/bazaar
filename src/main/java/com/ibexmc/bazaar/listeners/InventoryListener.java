package com.ibexmc.bazaar.listeners;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.internal.functions.WorldFunctions;
import com.ibexmc.internal.gui.IInventoryGUI;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof IInventoryGUI) {
            IInventoryGUI clickedGUI = (IInventoryGUI) event.getInventory().getHolder();
            clickedGUI.onClick(event);
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (
                event.getInventory().getType().equals(InventoryType.CHEST) ||
                        event.getInventory().getType().equals(InventoryType.BARREL)
        )
        {
            if (event.getInventory() != null) {
                if (event.getInventory().getLocation() != null) {
                    Block block = event.getInventory().getLocation().getBlock();
                    if (block != null) {
                        Shop shop = Bazaar.getInstance().getBazaarData().getShopAtLocation(
                                WorldFunctions.simplify(block.getLocation())
                        );
                        if (shop != null) {
                            if (!shop.isAdminShop() && !shop.isServerShop()) {
                                Bazaar.getInstance().getHologramManager().removeHologram(shop);
                                Bazaar.getInstance().getHologramManager().createHologram(shop);
                            }
                        }
                    }
                }
            }
        }
    }
}
