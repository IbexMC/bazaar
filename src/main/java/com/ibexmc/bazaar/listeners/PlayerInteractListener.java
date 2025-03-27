package com.ibexmc.bazaar.listeners;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.PlayerAction;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.shop.ShopManager;
import com.ibexmc.bazaar.util.Logging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;


public class PlayerInteractListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractLow(org.bukkit.event.player.PlayerInteractEvent event) {
        // This is a messy workaround to avoid WorldGuard issues.  If we don't cancel the event
        // immediately, WorldGuard may return an error saying you can't open the chest if it is
        // a right click.  By cancelling, we prevent the WorldGuard check and do the region
        // check manually elsewhere
        if (!Bazaar.getInstance().getBazaarData().isEnabled()) {
            return;
        }
        if (Bazaar.getInstance().getDependencies().hasWorldGuard()) {
            if (event.getClickedBlock() != null) {
                if (ShopManager.isShopBlock(event.getClickedBlock())) {
                    Shop chest = Bazaar.getInstance().getBazaarData().getShopAtLocation(event.getClickedBlock().getLocation());
                    if (chest != null) {
                        //event.setCancelled(true); // Prevents WorldGuard checking
                        String a = "A";
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (!Bazaar.getInstance().getBazaarData().isEnabled()) {
            return;
        }
        if (event.getClickedBlock() != null) {
            Bazaar.getInstance().getDebug().log(
                    "PlayerInteractListener",
                    "onInteract",
                    "On Click\n" +
                            "&c==================================================================&r\n" +
                            "&lPlayer: &r" + event.getPlayer().getDisplayName() + "\n" +
                            "&lAction: &r" + event.getAction().name() + "\n" +
                            "&lBlock Type: &r" + event.getClickedBlock().getType().name() + "\n" +
                            "&c=================================================================="
            );

            if (ShopManager.isShopBlock(event.getClickedBlock())) {



                // Get the shop
                Shop shop = Bazaar.getInstance().getBazaarData().getShopAtLocation(event.getClickedBlock().getLocation());

                if (shop != null) {
                    event.setCancelled(true);
                }

                ShopManager shopManager = new ShopManager(Bazaar.getInstance());
                if (event.getHand() == (EquipmentSlot.HAND)) {
                    switch (event.getAction()) {
                        case LEFT_CLICK_BLOCK:
                            if (event.getPlayer().isSneaking()) {
                                shopManager.click(
                                        event.getPlayer(),
                                        event.getClickedBlock(),
                                        shop,
                                        PlayerAction.LEFT_CLICK_SNEAK
                                );
                            } else {
                                shopManager.click(
                                        event.getPlayer(),
                                        event.getClickedBlock(),
                                        shop,
                                        PlayerAction.LEFT_CLICK
                                );
                            }
                            break;
                        case RIGHT_CLICK_BLOCK:
                            if (event.getPlayer().isSneaking()) {
                                shopManager.click(
                                        event.getPlayer(),
                                        event.getClickedBlock(),
                                        shop,
                                        PlayerAction.RIGHT_CLICK_SNEAK
                                );
                            } else {
                                shopManager.click(
                                        event.getPlayer(),
                                        event.getClickedBlock(),
                                        shop,
                                        PlayerAction.RIGHT_CLICK
                                );
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

}
