package com.ibexmc.bazaar.scheduled;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Cooldown;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.shop.ShopManager;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.Timestamp;
import java.util.Map;

public class Cleanup {

    Bazaar bazaar;

    public Cleanup (Bazaar bazaar) {
        this.bazaar = bazaar;
    }

    public void Run() {
        if (!bazaar.getBazaarData().isEnabled()) {
            return;
        }
        // Cooldowns
        if (bazaar.getBazaarData().getCooldowns() != null) {
            if (bazaar.getBazaarData().getCooldowns().size() > 0) {
                for (Cooldown cooldownEntry : Bazaar.getInstance().getBazaarData().getCooldowns()) {
                    long milliseconds = new Timestamp(System.currentTimeMillis()).getTime() - cooldownEntry.getTimestamp().getTime();
                    int seconds = (int) milliseconds / 1000;
                    if (seconds >= cooldownEntry.getCooldownLength())
                    {
                        bazaar.getBazaarData().removeCooldown(cooldownEntry.getUuid(), cooldownEntry.getLocation());
                    }
                }
            }
        }

    }
}
