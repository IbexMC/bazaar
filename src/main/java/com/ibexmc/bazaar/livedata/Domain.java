package com.ibexmc.bazaar.livedata;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.messaging.Error;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Domain {
    /**
     * Checks the Domain API to see if the action prohibited by the flag is allowed by this player at this location
     * @param player Player to check
     * @param location Location to check
     * @param flag Flag to check
     * @return If true, player is allowed or flag does not exist at the location.
     */
    public static boolean allowed(Player player, Location location, String flag) {
        try {
            com.ibexmc.domain.Domain domain = Bazaar.getInstance().getDependencies().getDomain();
            if (domain.api().flagAllowed(player, location, flag)) {
                // Flag does not exist at this location, or player is allowed
                return true;
            } else {
                // Flag exists at this location and player is not allowed
                return false;
            }
        } catch (Exception ex) {
            Bazaar.getInstance().getError().save(
                    "001",
                    "Domain",
                    "allowed",
                    "Unexpected Error checking Domain API",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
            return true;
        }
    }
}
