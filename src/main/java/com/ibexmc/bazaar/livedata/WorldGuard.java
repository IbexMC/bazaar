package com.ibexmc.bazaar.livedata;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.functions.WorldFunctions;
import com.ibexmc.internal.messaging.Error;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Objects;

public class WorldGuard implements Listener {

    public static StateFlag CREATE_SHOP;
    public static StateFlag USE_ADMIN_SHOP;
    public static StateFlag USE_PLAYER_SHOP;
    public static StateFlag USE_CLAN_SHOP;

    public enum WorldGuardResult {
        ALLOW,
        DENY,
        IN_REGION,
        PLAYER_IN_BYPASS,
        NOT_IN_BYPASS,
        NOT_IN_REGION,
        ERROR
    }

    /**
     * Adds flags to WorldGuard on load
     */
    public static void registerFlags() {
        Bazaar instance = Bazaar.getInstance();
        try {

            if(instance.getDependencies().hasWorldGuard()) {

                // Get flag name and default values from config
                String createShopFlagName = Bazaar.getInstance().getBazaarConfig().wgCreateShopFlag();
                String useAdminShopFlagName = Bazaar.getInstance().getBazaarConfig().wgUseAdminShopFlag();
                String usePlayerShopFlagName = Bazaar.getInstance().getBazaarConfig().wgUsePlayerShopFlag();
                String useClanShopFlagName = Bazaar.getInstance().getBazaarConfig().wgUseClanShopFlag();
                boolean createShopFlagValue = Bazaar.getInstance().getBazaarConfig().wgCreateShopValue();
                boolean useAdminShopValue = Bazaar.getInstance().getBazaarConfig().wgUseAdminShopValue();
                boolean useShopFlagValue = Bazaar.getInstance().getBazaarConfig().wgUsePlayerShopValue();
                boolean useClanShopValue = Bazaar.getInstance().getBazaarConfig().wgUseClanShopValue();

                com.sk89q.worldguard.WorldGuard worldGuard = Bazaar.getInstance().getDependencies().getWorldGuard();
                FlagRegistry registry = worldGuard.getFlagRegistry();
                // CREATE_SHOP
                try {
                    StateFlag flagCreateShop = new StateFlag(createShopFlagName, createShopFlagValue);
                    registry.register(flagCreateShop);
                    CREATE_SHOP = flagCreateShop;
                } catch (FlagConflictException e) {
                    Flag<?> existingCreateShop = registry.get(createShopFlagName);
                    if (existingCreateShop instanceof StateFlag) {
                        CREATE_SHOP = (StateFlag) existingCreateShop;
                    } else {
                        instance.getError().save(
                                "001",
                                "WorldGuard",
                                "registerFlags",
                                "Unexpected error registering CREATE_SHOP flag",
                                e.getMessage(),
                                Error.Severity.WARN,
                                e.getStackTrace()
                        );
                    }
                }
                // USE_ADMIN_SHOP
                try {
                    StateFlag flagUseAdminShop = new StateFlag(useAdminShopFlagName, useAdminShopValue);
                    registry.register(flagUseAdminShop);
                    USE_ADMIN_SHOP = flagUseAdminShop;
                } catch (FlagConflictException e) {
                    Flag<?> existingUseAdminShop = registry.get(useAdminShopFlagName);
                    if (existingUseAdminShop instanceof StateFlag) {
                        USE_ADMIN_SHOP = (StateFlag) existingUseAdminShop;
                    } else {
                        instance.getError().save(
                                "002",
                                "WorldGuard",
                                "registerFlags",
                                "Unexpected error registering USE_ADMIN_SHOP flag",
                                e.getMessage(),
                                Error.Severity.WARN,
                                e.getStackTrace()
                        );
                    }
                }
                // USE_PLAYER_SHOP
                try {
                    StateFlag flagUsePlayerShop = new StateFlag(usePlayerShopFlagName, useShopFlagValue);
                    registry.register(flagUsePlayerShop);
                    USE_PLAYER_SHOP = flagUsePlayerShop;
                } catch (FlagConflictException e) {
                    Flag<?> existingUsePlayerShop = registry.get(usePlayerShopFlagName);
                    if (existingUsePlayerShop instanceof StateFlag) {
                        USE_PLAYER_SHOP = (StateFlag) existingUsePlayerShop;
                    } else {
                        instance.getError().save(
                                "003",
                                "WorldGuard",
                                "registerFlags",
                                "Unexpected error registering USE_PLAYER_SHOP flag",
                                e.getMessage(),
                                Error.Severity.WARN,
                                e.getStackTrace()
                        );
                    }
                }
                // USE_CLAN_SHOP
                try {
                    StateFlag flagUseClanShop = new StateFlag(useClanShopFlagName, useClanShopValue);
                    registry.register(flagUseClanShop);
                    USE_CLAN_SHOP = flagUseClanShop;
                } catch (FlagConflictException e) {
                    Flag<?> existingUseClanShop = registry.get(useClanShopFlagName);
                    if (existingUseClanShop instanceof StateFlag) {
                        USE_CLAN_SHOP = (StateFlag) existingUseClanShop;
                    } else {
                        instance.getError().save(
                                "004",
                                "WorldGuard",
                                "registerFlags",
                                "Unexpected error registering USE_CLAN_SHOP flag",
                                e.getMessage(),
                                Error.Severity.WARN,
                                e.getStackTrace()
                        );
                    }
                }
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "005",
                    "WorldGuard",
                    "registerFlags",
                    "Unexpected error",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Checks if the player is currently in bypass mode
     * @param player Player to check
     * @return If in bypass, will return WorldGuardResult.PLAYER_IN_BYPASS
     */
    public static WorldGuardResult inBypass(Player player) {
        Bazaar instance = Bazaar.getInstance();
        instance.getDebug().log("WorldGuard", "inBypass", "Started");
        if (player == null) {
            instance.getError().save(
                    "001",
                    "WorldGuard",
                    "inBypass",
                    "Invalid Parameters",
                    "Player is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        com.sk89q.worldguard.WorldGuard worldGuard = Bazaar.getInstance().getDependencies().getWorldGuard();
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(Objects.requireNonNull(player.getLocation().getWorld()));
        instance.getDebug().log("WorldGuard",
                "inBypass",
                "Checking if " + player.getName() + " is in bypass")
        ;
        if (worldGuard.getPlatform().getSessionManager().hasBypass(wgPlayer, wgWorld)) {
            instance.getDebug().log(
                    "WorldGuard",
                    "inBypass",
                    player.getName() + " is in bypass mode"
            );
            return WorldGuardResult.PLAYER_IN_BYPASS;
        } else {
            instance.getDebug().log(
                    "WorldGuard",
                    "inBypass",
                    player.getName() + " is not in bypass mode"
            );
            return WorldGuardResult.NOT_IN_BYPASS;
        }

    }

    /**
     * Checks if the player is allowed to the flag in the location
     * @param player The player to check
     * @param location The location to check
     * @param flag Flag to check
     * @return WorldGuardResult with enum value
     */
    public static WorldGuardResult allowed(Player player, Location location, StateFlag flag) {
        Bazaar instance = Bazaar.getInstance();
        instance.getDebug().log("WorldGuard", "allowed", "Started");
        if (player == null) {
            instance.getError().save(
                    "001",
                    "WorldGuard",
                    "allowed",
                    "Invalid Parameters",
                    "Player is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        if (location == null) {
            instance.getError().save(
                    "002",
                    "WorldGuard",
                    "allowed",
                    "Invalid Parameters",
                    "Location is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        if (flag == null) {
            instance.getError().save(
                    "003",
                    "WorldGuard",
                    "allowed",
                    "Invalid Parameters",
                    "Flag is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }

        instance.getDebug().log(
                "WorldGuard",
                "allowed()",
                "Started - Player: " + player.getName() +
                        " Location: " + WorldFunctions.simpleReadable(location) +
                        " Flag: " + flag.getName()
        );

        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.world.World wgWorld = BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld()));
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
        com.sk89q.worldguard.WorldGuard worldGuard = Bazaar.getInstance().getDependencies().getWorldGuard();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet regionSet = query.getApplicableRegions(loc);

        instance.getDebug().log(
                "WorldGuard",
                "allowed()",
                "Number of regions " + player.getName() + " is in: " + regionSet.size()
        );

        if (regionSet.size() == 0) {
            instance.getDebug().log(
                    "WorldGuard",
                    "allowed()",
                    player.getName() + " is not in a WorldGuard region"
            );
            return WorldGuardResult.NOT_IN_REGION;
        }

        String regions = ""; // Debug purposes only
        for (ProtectedRegion region : regionSet) {
            regions = regions + region.getId() + "; ";
        }
        instance.getDebug().log(
                "WorldGuard",
                "allowed()",
                "Regions " + player.getName() + " is in: " + regions
        );

        instance.getDebug().log("WorldGuard",
                "allowed()",
                "Checking if " + player.getName() + " is in bypass")
        ;
        if (worldGuard.getPlatform().getSessionManager().hasBypass(wgPlayer, wgWorld)) {
            instance.getDebug().log(
                    "WorldGuard",
                    "allowed()",
                    player.getName() + " is in bypass mode"
            );
            return WorldGuardResult.PLAYER_IN_BYPASS;
        } else {
            instance.getDebug().log(
                    "WorldGuard",
                    "allowed()",
                    player.getName() + " is not in bypass mode"
            );
        }
        instance.getDebug().log(
                "WorldGuard",
                "allowed()",
                "Checking status of " + flag.getName() + " for " + player.getName()
        );
        if (query.testState(wgLocation, wgPlayer, flag)) {
            instance.getDebug().log(
                    "WorldGuard",
                    "allowed()",
                    "Flag status of " + flag.getName() + " for " + player.getName() + " is ALLOW"
            );
            return WorldGuardResult.ALLOW;
        } else {
            instance.getDebug().log(
                    "WorldGuard",
                    "allowed()",
                    "Flag status of " + flag.getName() + " for " + player.getName() + " is NOT ALLOW, so is either DENY or not set"
            );
            return WorldGuardResult.DENY;
        }
    }

    /**
     * Checks if a player is in any region
     * @param player Player to check
     * @return if WorldGuardResult is IN_REGION, player is in a region
     */
    public static WorldGuardResult inRegion(Player player) {
        Bazaar instance = Bazaar.getInstance();
        instance.getDebug().log("WorldGuard", "inRegion", "Started");
        if (player == null) {
            instance.getError().save(
                    "001",
                    "WorldGuard",
                    "inRegion",
                    "Invalid Parameters",
                    "Player is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        try {
            com.sk89q.worldguard.WorldGuard worldGuard = Bazaar.getInstance().getDependencies().getWorldGuard();
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
            RegionContainer container = worldGuard.getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regionSet = query.getApplicableRegions(loc);
            if (regionSet.size() > 0) {
                return WorldGuardResult.IN_REGION;
            } else {
                return WorldGuardResult.NOT_IN_REGION;
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "002",
                    "WorldGuard",
                    "inRegion",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
            return WorldGuardResult.ERROR;
        }
    }

    /**
     * Checks if a player is in the region specified
     * @param player Player to check
     * @param region Region id to check
     * @return if WorldGuardResult is IN_REGION, player is in the region
     */
    public static WorldGuardResult inRegion(Player player, String region) {
        Bazaar instance = Bazaar.getInstance();
        instance.getDebug().log("WorldGuard", "inRegion", "Started");
        if (player == null) {
            instance.getError().save(
                    "001",
                    "WorldGuard",
                    "inRegion",
                    "Invalid Parameters",
                    "Player is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        if (region.equalsIgnoreCase("")) {
            instance.getError().save(
                    "001",
                    "WorldGuard",
                    "inRegion",
                    "Invalid Parameters",
                    "Region is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        try {
            com.sk89q.worldguard.WorldGuard worldGuard = Bazaar.getInstance().getDependencies().getWorldGuard();
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
            RegionContainer container = worldGuard.getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regionSet = query.getApplicableRegions(loc);
            if (regionSet.size() > 0) {
                for (ProtectedRegion protectedRegion : regionSet) {
                    if (protectedRegion.getId().equalsIgnoreCase(region)) {
                        return WorldGuardResult.IN_REGION;
                    }
                }

            }
            return WorldGuardResult.NOT_IN_REGION;
        } catch (Exception ex) {
            instance.getError().save(
                    "003",
                    "WorldGuard",
                    "inRegion",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
            return WorldGuardResult.ERROR;
        }
    }

    /**
     * Checks if a player is in the region specified
     * @param player Player to check
     * @param regions List of Region id values to check
     * @return if WorldGuardResult is IN_REGION, player is in at least one of the regions in the list
     */
    public static WorldGuardResult inRegion(Player player, List<String> regions) {
        Bazaar instance = Bazaar.getInstance();
        instance.getDebug().log("WorldGuard", "inRegion", "Started");
        if (player == null) {
            instance.getError().save(
                    "001",
                    "WorldGuard",
                    "inRegion",
                    "Invalid Parameters",
                    "Player is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        }
        if (regions == null) {
            instance.getError().save(
                    "002",
                    "WorldGuard",
                    "inRegion",
                    "Invalid Parameters",
                    "Region List is null",
                    Error.Severity.WARN,
                    null
            );
            return WorldGuardResult.ERROR;
        } else {
            if (regions.size() == 0) {
                instance.getError().save(
                        "003",
                        "WorldGuard",
                        "inRegion",
                        "Invalid Parameters",
                        "Region list is empty",
                        Error.Severity.WARN,
                        null
                );
                return WorldGuardResult.ERROR;
            }
        }
        try {
            com.sk89q.worldguard.WorldGuard worldGuard = Bazaar.getInstance().getDependencies().getWorldGuard();
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
            RegionContainer container = worldGuard.getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regionSet = query.getApplicableRegions(loc);
            if (regionSet.size() > 0) {
                for (ProtectedRegion protectedRegion : regionSet) {
                    for (String region : regions) {
                        if (protectedRegion.getId().equalsIgnoreCase(region)) {
                            return WorldGuardResult.IN_REGION;
                        }
                    }
                }

            }
            return WorldGuardResult.NOT_IN_REGION;
        } catch (Exception ex) {
            instance.getError().save(
                    "004",
                    "WorldGuard",
                    "inRegion",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
            return WorldGuardResult.ERROR;
        }
    }

}
