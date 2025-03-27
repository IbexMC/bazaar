package com.ibexmc.bazaar.listeners;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Bazaar instance = Bazaar.getInstance();
        if (!instance.getBazaarData().isEnabled()) {
            return;
        }
        Location location = new Location(event.getBlock().getLocation().getWorld(), event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ());
        if (event.getBlock().getType().equals(Material.CHEST) || event.getBlock().getType().equals(Material.BARREL)) {
            instance.getDebug().log("BlockBreakListener", "onBlockBreak", "Block clicked is a chest or barrel");
            if (Bazaar.getInstance().getBazaarData().containsShopLocation(location)) {
                if (event.getPlayer() != null) {
                    instance.getMessage().send(event.getPlayer(), "block_break_shop", "&cYou cannot break a Bazaar shop.  The shop must be removed first", null, true);
                }
                event.setCancelled(true);
                return;
            }
        }
    }
}