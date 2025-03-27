package com.ibexmc.bazaar.listeners;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;


public class EntityEventListener implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!Bazaar.getInstance().getBazaarData().isEnabled()) {
            return;
        }
        List<Block> blockListCopy = new ArrayList<>();
        blockListCopy.addAll(event.blockList());
        for (Block block : blockListCopy) {
            if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.BARREL)) {
                Location location = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
                if (Bazaar.getInstance().getBazaarData().containsShopLocation(location)) {
                    event.blockList().remove(block);
                    return;
                }
            }
        }
    }
}
