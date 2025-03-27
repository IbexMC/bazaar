package com.ibexmc.bazaar.livedata;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.CreatingShop;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.shop.Cooldown;
import com.ibexmc.bazaar.shop.multishop.*;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.functions.WorldFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

public class BazaarData {

    private static Bazaar instance = Bazaar.getInstance();

    // Plugin Status
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // Reload flag
    private boolean reloadFlag = false;
    public boolean getReloadFlag() {
        return reloadFlag;
    }
    public void setReloadFlag(boolean reloadFlag) {
        this.reloadFlag = reloadFlag;
    }

    // Language
    private HashMap<String, String> locale = new HashMap<>();
    public void loadLanguage() {
        instance = Bazaar.getInstance();
        File localeYML = new File(instance.getDataFolder()+"/lang.yml");
        if (!localeYML.exists()) {
            instance.getDebug().log("Bazaar", "loadLocale()", "Locale file not found - saving default");
            instance.saveResource("lang.yml", false);
        }

        FileConfiguration localeConfig = YamlConfiguration.loadConfiguration(localeYML);
        for (String localeCode : localeConfig.getConfigurationSection("").getKeys(false))
        {
            String localeValue = localeConfig.getString(localeCode);
            if (localeValue != null) {
                if (!localeValue.equalsIgnoreCase("")) {
                    instance.getBazaarData().putLocale(localeCode, localeValue);
                }
            }
        }

    }
    public void putLocale(String code, String text) {
        this.locale.put(code, text);
    }
    public boolean checkLocaleContainsCode(String code) {
        return this.locale.containsKey(code);
    }
    public String getLocaleTextByCode(String code) {
        return this.locale.get(code);
    }
    public void removeLocaleByCode(String code) {
        this.locale.remove(code);
    }
    public void clearLocale() {
        this.locale.clear();
    }

    // Creating New Shop
    private Map<UUID, CreatingShop> creating = new HashMap<>();
    public void addCreating(UUID uuid, CreatingShop creatingShop) {
        creating.remove(uuid);
        creating.put(uuid, creatingShop);
    }
    public void removeCreating(UUID uuid) {
        creating.remove(uuid);
    }
    public CreatingShop getCreating(UUID uuid) {
        return creating.get(uuid);
    }
    public boolean isCreating(UUID uuid) {
        return creating.containsKey(uuid);
    }

    private Set<UUID> delShops = new HashSet<>();
    public void putDelShop(UUID uuid) {
        this.delShops.add(uuid);
    }
    public boolean hasDelShop(UUID uuid) {
        return this.delShops.contains(uuid);
    }
    public void removeDelShop(UUID uuid) {
        if (this.delShops.contains(uuid)) {
            this.delShops.remove(uuid);
        }
    }

    // Shops
    private HashMap<Location, Shop> shops = new HashMap<>();
    public Map<Location, Shop> getShops() {
        return this.shops;
    }
    public void putShops(Location location, Shop shop) {
        Location putLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.shops.put(putLocation, shop);
    }
    public void removeShopLocation(Location location) {
        Location remLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (this.shops.containsKey(remLocation)) {
            instance.getDebug().log("Data", "removeShopLocation", "Shop exists - removing");
            this.shops.remove(remLocation);
            if (this.shops.containsKey(remLocation)) {
                instance.getDebug().log("Data", "removeShopLocation", "Shop still exists");
            }
        } else {
            instance.getDebug().log("Data", "removeShopLocation", "Shop does not exist - removing");
        }
    }
    public boolean containsShopLocation(Location location) {
        instance.getDebug().log("Data", "containsShopLocation", "Checking if shop exists at " + WorldFunctions.simpleReadable(location));
        if (shops.containsKey(location)) {
            instance.getDebug().log("Data", "containsShopLocation", "shops contains a shop at this location");
            return true;
        } else {
            instance.getDebug().log("Data", "containsShopLocation", "shops does not contain a shop at this location");
            return false;
        }
    }
    public void clearShops() {
        this.shops.clear();
    }
    public Shop getShopAtLocation(Location location) {
        Location searchLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (this.shops.containsKey(searchLocation)) {
            return this.shops.get(searchLocation);
        } else {
            return null;
        }
    }

    // Locked Shops (transaction in progress)
    private HashMap<Location, UUID> lockedShops = new HashMap<>();
    public Map<Location, UUID> getLockedShops() {
        return this.lockedShops;
    }
    public boolean isLockedShop(Location location) {
        if (lockedShops.containsKey(location)) {
            return true;
        } else {
            return false;
        }
    }
    public void putLockedShop(Location location, UUID uuid) {
        this.lockedShops.put(location, uuid);

        final Location timedLocation = location;
        Bukkit.getScheduler().runTaskLater(Bazaar.getInstance(), new Runnable(){
            public void run(){
                Bazaar.getInstance().getBazaarData().removeLockedShop(location);
            }
        }, 3 * 20);

    }
    public void removeLockedShop(Location location) {
        if (lockedShops.containsKey(location)) {
            this.lockedShops.remove(location);
        }
    }

    // Cooldown
    private HashSet<Cooldown> cooldowns = new HashSet<>();
    public void putCooldown(Location location, UUID uuid, int seconds) {
        Cooldown cooldown = new Cooldown(uuid, location, new Timestamp(System.currentTimeMillis()), seconds);
        if (cooldowns == null) {
            cooldowns = new HashSet<>();
        }
        this.cooldowns.add(cooldown);
        new BukkitRunnable() {
            @Override
            public void run() {
                Bazaar.getInstance().getBazaarData().removeCooldown(uuid, location);
            }

        }.runTaskLaterAsynchronously(Bazaar.getInstance(), seconds * 20);
    }
    public void removeCooldown(UUID uuid, Location location) {
        for (Cooldown cooldown : this.cooldowns) {
            if (
                    cooldown.getUuid().equals(uuid) &&
                    cooldown.getLocation().getWorld().equals(location.getWorld()) &&
                    cooldown.getLocation().getBlockX() == location.getBlockX() &&
                    cooldown.getLocation().getBlockY() == location.getBlockY() &&
                    cooldown.getLocation().getBlockZ() == location.getBlockZ()
            ) {
                this.cooldowns.remove(cooldown);
                break;
            }
        }
    }
    public boolean hasCooldown(UUID uuid, Location location) {
        boolean returnValue = false;
        if (cooldowns != null) {
            for (Cooldown cooldown : this.cooldowns) {
                if (
                        cooldown.getUuid().equals(uuid) &&
                                cooldown.getLocation().getWorld().equals(location.getWorld()) &&
                                cooldown.getLocation().getBlockX() == location.getBlockX() &&
                                cooldown.getLocation().getBlockY() == location.getBlockY() &&
                                cooldown.getLocation().getBlockZ() == location.getBlockZ()
                ) {
                    returnValue = true;
                    long milliseconds = new Timestamp(System.currentTimeMillis()).getTime() - cooldown.getTimestamp().getTime();
                    int seconds = (int) milliseconds / 1000;
                    if (seconds >= cooldown.getCooldownLength())
                    {
                        this.cooldowns.remove(cooldown);
                        returnValue = true;
                    }
                    break;
                }
            }
        }
        return returnValue;
    }
    public HashSet<Cooldown> getCooldowns() {
        return this.cooldowns;
    }

    // ItemStack Holograms
    private boolean decentHologramsExists = false;
    public void setHasDecentHolograms(boolean exists) {
        this.decentHologramsExists = exists;
    }
    public boolean hasDecentHolograms() {
        return this.decentHologramsExists;
    }
    private Map<Location, eu.decentsoftware.holograms.api.holograms.Hologram> decentHolograms = new HashMap<>();
    public void putDecentHologram(Location location, eu.decentsoftware.holograms.api.holograms.Hologram hologram) {
        this.decentHolograms.put(location, hologram);
    }
    public boolean hasDecentHologram(Location location) {
        return this.decentHolograms.containsKey(location);
    }
    public eu.decentsoftware.holograms.api.holograms.Hologram getDecentHologram(Location location) {
        return this.decentHolograms.get(location);
    }
    public void removeDecentHologram(Location location) {
        this.decentHolograms.remove(location);
    }


    // MultiShops
    private HashMap<String, IMultiShop> multiShops = new HashMap<>();

    public HashMap<String, IMultiShop> getMultiShops() {
        return this.multiShops;
    }

    private HashMap<Location, IPlacedMultiShop> placedMultiShops = new HashMap<>();


}
