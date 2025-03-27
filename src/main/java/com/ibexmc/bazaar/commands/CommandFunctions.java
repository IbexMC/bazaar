package com.ibexmc.bazaar.commands;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.livedata.ClanTasks;
import com.ibexmc.bazaar.shop.CreatingShop;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.storage.yaml.FileAccess;
import com.ibexmc.bazaar.util.*;
import com.ibexmc.internal.functions.ItemFunctions;
import com.ibexmc.internal.functions.NumberFunctions;
import com.ibexmc.internal.messaging.Error;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandFunctions {

    Bazaar instance;

    public CommandFunctions(Bazaar instance) {
        this.instance = instance;
    }

    /**
     * Cancels any existing create or remove actions
     * @param sender CommandSender issuing the command
     */
    public void cancel(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            instance.getMessage().send(
                    sender,
                    "cancel_existing_actions",
                    "&aCancelling any existing create/remove actions",
                    null,
                    true
            );
            instance.getBazaarData().removeCreating(player.getUniqueId());
            instance.getBazaarData().removeDelShop(player.getUniqueId());
        } else {
            instance.getMessage().send(sender,"shop_no_console","&cERROR &f- Command must be run by an in-game player",null,true);
        }
    }

    /**
     * Clears all shops from the currently selected data source (file/mysql)
     * @param sender CommandSender issuing the command
     */
    public void clearData(CommandSender sender) {
        if (sender instanceof Player) {
            instance.getMessage().send(sender,"console_only","&cERROR &f- Command can only be run in console",null,true);
            return;
        }
        Bazaar.getInstance().getHologramManager().removeHolograms();
        instance.getBazaarData().clearShops();
        if (instance.getBazaarConfig().useMySQL()) {
            // clear database
            com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
            mySQL.clearBazaarData();
        } else {
            // clear files
            FileAccess fileAccess = new FileAccess(Bazaar.getInstance());
            fileAccess.clearAllFiles();
        }
    }

    /**
     * Allows the user to create a new shop
     * @param sender CommandSender issuing the command
     * @param arguments Map of arguments being applied
     */
    public void create(CommandSender sender, Map<Integer, String> arguments) {
        // shop create <amount> <buy> <sell> -c:<cooldown> -admin -clan -server

        String shopCreateAdmin = instance.getBazaarConfig().getCommandShopCreateAdmin().toLowerCase();
        String shopCreateServer = instance.getBazaarConfig().getCommandShopCreateServer().toLowerCase();
        String shopCreateClan = instance.getBazaarConfig().getCommandShopCreateClan().toLowerCase();
        String shopCreateCooldown = instance.getBazaarConfig().getCommandShopCreateCooldown().toLowerCase();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            int amount = 0;
            double buy = 0;
            double sell = 0;
            int cooldown = instance.getBazaarConfig().getDefaultCooldown();
            boolean admin = false;
            boolean server = false;
            boolean clan = false;
            String clanTag = "";

            // Check if the player is already creating a shop
            if (instance.getBazaarData().isCreating(player.getUniqueId())) {
                instance.getMessage().send(sender,"shop_create_duplicate","&cERROR &f- You are already creating a shop.  Complete that or wait for it to timeout",null,true);
                return;
            }

            // Check if the player is currently deleting a shop
            if (Bazaar.getInstance().getBazaarData().hasDelShop(player.getUniqueId())) {
                // shop create is already active
                instance.getMessage().send(sender,"shop_create_remove","&cERROR &f- Unable to create a shop while removing a shop.  Complete the removal or wait for it to timeout and then try again",null,true);
                return;
            }

            // Get Item
            ItemStack shopItem;
            if (player.getInventory().getItemInMainHand() != null) {
                instance.getDebug().log("ShopCommand", "Create", "Item in hand: " + player.getInventory().getItemInMainHand().getType().name());
                if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                    shopItem = player.getInventory().getItemInMainHand().clone();
                    shopItem.setAmount(1);
                } else {
                    // no item in main hand
                    instance.getMessage().send(player,"shop_create_no_item","&cERROR &f- You must have an item in hand to create the shop",null,true);
                    return;
                }

                // Get amount
                if (arguments.containsKey(1)) {
                    amount = NumberFunctions.stringToInt(arguments.get(1));
                    if (amount < 1) {
                        instance.getMessage().send(player,"shop_create_min_amt","&cERROR &f- You must allow at least 1 item",null,true);
                        return;
                    }
                    // Do not allow amount exceed maximum stack size
                    if (amount > shopItem.getMaxStackSize()) {
                        Map<String, String> placeHolder = new HashMap<>();
                        placeHolder.put("<%maxamount%>", "" + shopItem.getMaxStackSize());
                        instance.getMessage().send(player,"shop_create_invalid_amt","&cERROR &f- Maximum amount allowed for this item is <%maxamount%> (maximum stack size)",placeHolder,true);
                        return;
                    }
                } else {
                    // missing amount - cancel
                    instance.getMessage().send(player,"shop_create_invalid","&cERROR &f- Invalid command parameters",null,true);
                    instance.getMessage().send(player,"shop_create_usage","&lUsage: &r&5/shop create &d<amount> <buy FROM shop price> <sell TO shop price> &6-c:<cooldown> (optional) -admin (optional) -clan (optional) -server (optional)",null,true);
                    instance.getMessage().send(player,"shop_create_example","&lExample: &r&5/shop create &d1 100 50 -c:3 &r- Would create a store that players can buy 1 item for 100 or sell 1 item for 50 with a 3 second cooldown",null,true);


                    return;
                }

                // Get buy price
                if (arguments.containsKey(2)) {
                    buy = NumberFunctions.stringToDouble(arguments.get(2));
                    if (buy < 0) {
                        instance.getMessage().send(player,"shop_create_min_buy","&cERROR &f- Buy price cannot be negative",null,true);
                        return;
                    }
                } else {
                    // missing amount - cancel
                    instance.getMessage().send(player,"shop_create_invalid","&cERROR &f- Invalid command parameters",null,true);
                    instance.getMessage().send(player,"shop_create_usage","&lUsage: &r&5/shop create &d<amount> <buy FROM shop price> <sell TO shop price> &6-c:<cooldown> (optional) -admin (optional) -clan (optional) -server (optional)",null,true);
                    instance.getMessage().send(player,"shop_create_example","&lExample: &r&5/shop create &d1 100 50 -c:3 &r- Would create a store that players can buy 1 item for 100 or sell 1 item for 50 with a 3 second cooldown",null,true);

                    return;
                }

                // Get sell price
                if (arguments.containsKey(3)) {
                    sell = NumberFunctions.stringToDouble(arguments.get(3));
                    if (sell < 0) {
                        instance.getMessage().send(player,"shop_create_min_sell","&cERROR &f- Sell price cannot be negative",null,true);
                        return;
                    }
                } else {
                    // missing amount - cancel
                    instance.getMessage().send(player,"shop_create_invalid","&cERROR &f- Invalid command parameters",null,true);
                    instance.getMessage().send(player,"shop_create_usage","&lUsage: &r&5/shop create &d<amount> <buy FROM shop price> <sell TO shop price> &6-c:<cooldown> (optional) -admin (optional) -clan (optional) -server (optional)",null,true);
                    instance.getMessage().send(player,"shop_create_example","&lExample: &r&5/shop create &d1 100 50 -c:3 &r- Would create a store that players can buy 1 item for 100 or sell 1 item for 50 with a 3 second cooldown",null,true);

                    return;
                }

                StringBuilder customName = new StringBuilder();
                for (Map.Entry<Integer, String> entry : arguments.entrySet()) {
                    if (entry.getValue().toLowerCase().contains(shopCreateAdmin)) {
                        admin = true;
                    } else if (entry.getValue().toLowerCase().contains(shopCreateServer)) {
                        server = true;
                    } else if (entry.getValue().toLowerCase().contains(shopCreateClan)) {
                        if (instance.getDependencies().hasMyriadClans()) {
                            clanTag = ClanTasks.getClanTagByUUID(player.getUniqueId());
                            clan = true;
                        }
                    } else if (entry.getValue().equalsIgnoreCase(shopCreateCooldown)) {
                        String coolDownString = entry.getValue().replace(shopCreateCooldown, "");
                        cooldown = NumberFunctions.stringToInt(coolDownString);
                    } else {
                        if (entry.getKey() >= 4) {
                            customName.append(entry.getValue()).append(" ");
                        }
                    }
                }
                if (customName.length() > 0) {
                    customName = new StringBuilder((customName + "[end]").replace(" [end]", "").replace("[end]", ""));
                }

                // Check if admin/server and clan flag are set
                if ((admin || server) && clan) {
                    instance.getMessage().send(player,"shop_create_admin_clan","&cERROR &f- A shop cannot be both an Admin/Server shop and a Clan shop",null,true);
                    return;
                }
                // Check if both admin and server flags are set
                if (admin && server) {
                    instance.getMessage().send(player,"shop_create_admin_server","&cERROR &f- A shop cannot be both an Admin & Server shop",null,true);
                    return;
                }

                // Create CreatingShop object
                CreatingShop creatingShop = new CreatingShop(
                        shopItem,
                        amount,
                        buy,
                        sell,
                        admin,
                        server,
                        clanTag,
                        cooldown,
                        customName.toString()
                );

                // Add the CreatingShop object to data
                instance.getBazaarData().addCreating(player.getUniqueId(), creatingShop);

                // Start a timed task to remove the item from newChests in Data after x seconds
                final Player timedPlayer = player;
                Bukkit.getScheduler().runTaskLater(Bazaar.getInstance(), new Runnable(){
                    public void run(){
                        if (Bazaar.getInstance().getBazaarData().isCreating(timedPlayer.getUniqueId())) {
                            Bazaar.getInstance().getBazaarData().removeCreating(timedPlayer.getUniqueId());
                            instance.getMessage().send(timedPlayer,"shop_create_timeout","&cShop create timed out &f- Please run &d/shop create &fto try again",null,true);
                        }
                    }
                }, Bazaar.getInstance().getBazaarConfig().shopEditTimeout() * 20L);

                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("<%timeout%>", "" + Bazaar.getInstance().getBazaarConfig().shopEditTimeout());
                instance.getMessage().send(player,"shop_create_instructions","&aRight click a chest/barrel within the next <%timeout%> seconds to create the shop",placeHolder,true);
            }

        } else {
            instance.getMessage().send(sender,"shop_no_console","&cERROR &f- Command must be run by an in-game player",null,true);
        }
    }

    /**
     * Toggles the debug logging
     * @param sender CommandSender issuing the command
     */
    public void debug(CommandSender sender) {
        boolean hasPerm = true;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Perms.hasPermission(player, Perms.Type.ADMIN)) {
                hasPerm = false;
            }
        }
        if (hasPerm) {
            instance.toggleDebug();
            instance.getBazaarConfig().toggleDebugLog();
            if (instance.getBazaarConfig().debugLog()) {
                instance.getMessage().send(sender,"debug_on","Debug mode is &aON&r",null,true);
            } else {
                instance.getMessage().send(sender,"debug_off","Debug mode is &cOFF&r",null,true);
            }
        } else {
            instance.getMessage().send(sender,"shop_no_perm","&cERROR &f- You do not have permission to run that command",null,true);
        }
    }

    /**
     * Displays help messages
     * @param sender CommandSender issuing the command
     */
    public void help(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Perms.hasPermission(player, Perms.Type.CREATE)) {
                instance.getMessage().send(sender,"shop_help_create","&lCreate a shop: &r&5/shop create &d<amount> <buy> <sell> &6-c:<cooldown> (optional) -admin (optional) -clan (optional)",null,true);
            }
            if (Perms.hasPermission(player, Perms.Type.REMOVE)) {
                instance.getMessage().send(sender,"shop_help_remove","&lRemove a shop: &r&5/shop remove",null,true);
            }
            if (Perms.hasPermission(player, Perms.Type.ADMIN)) {
                instance.getMessage().send(sender,"shop_help_debug","&lToggle debug mode: &r/shop debug",null,true);
                instance.getMessage().send(sender,"shop_help_reload","&lReload the shops: &r&5/shop reload",null,true);
                instance.getMessage().send(sender,"shop_help_version","&lGet the version: &r&5/shop version",null,true);
            }
        } else {
            instance.getMessage().send(sender,"shop_help_create","&lCreate a shop: &r&5/shop create &d<amount> <buy> <sell> &6-c:<cooldown> (optional) -admin (optional) -clan (optional)",null,true);
            instance.getMessage().send(sender,"shop_help_remove","&lRemove a shop: &r&5/shop remove",null,true);
            instance.getMessage().send(sender,"shop_help_debug","&lToggle debug mode: &r/shop debug",null,true);
            instance.getMessage().send(sender,"shop_help_reload","&lReload the shops: &r&5/shop reload",null,true);
            instance.getMessage().send(sender,"shop_help_version","&lGet the version: &r&5/shop version",null,true);
        }
    }

    /**
     * Rekeys the entire ibex_plugin table in MySQL
     * @param sender CommandSender issuing the command
     */
    public void rekeyDatabase(CommandSender sender) {
        if (sender instanceof Player) {
            instance.getMessage().send(sender,"console_only","&cERROR &f- Command can only be run in console",null,true);
            return;
        }
        if (instance.getBazaarConfig().useMySQL()) {
            com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
            mySQL.rekeyIbexPluginTable();
        } else {
            // error
            instance.getMessage().send(sender,"mysql_only","&cERROR &f- This action can only be run if using MySQL",null,true);
        }
        reload(sender);
    }

    /**
     * Reloads the config and shops
     * @param sender CommandSender issuing the command
     */
    public void reload(CommandSender sender) {
        boolean hasPerm = true;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Perms.hasPermission(player, Perms.Type.ADMIN)) {
                hasPerm = false;
            }
        }
        if (hasPerm) {
            instance.getLog().log("CommandFunctions", "Reload", "Reloading..");
            instance.getLog().log("CommandFunctions", "Reload", "Clearing holograms");

            Bazaar.getInstance().getHologramManager().removeHolograms();
            instance.getBazaarData().clearShops();

            instance.getLog().log("CommandFunctions", "Reload", "Reloading config file");
            Bazaar.getInstance().reloadConfig();
            Bazaar.getInstance().setBazaarConfig(new BazaarConfig(instance)); // reload the config
            if (Bazaar.getInstance().getBazaarConfig().useMySQL()) {
                instance.getLog().log("CommandFunctions", "Reload", "Reloading shop data from MySQL");

                if (instance.getBazaarConfig().useMySQL()) {
                    instance.setMySQL( instance.getIbexInternal().getMySQL(
                            instance.getBazaarConfig().getMySQLHost(),
                            "" + instance.getBazaarConfig().getMySQLPort(),
                            instance.getBazaarConfig().getMySQLDatabase(),
                            instance.getBazaarConfig().getMySQLUsername(),
                            instance.getBazaarConfig().getMySQLPassword(),
                            instance.getBazaarConfig().getMySQLUseSSL()
                    ));
                }

                com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
                mySQL.checkTable();
                mySQL.loadPlayerShops(false);
            } else {
                instance.getLog().log("CommandFunctions", "Reload", "Reloading shop data from file");
                FileAccess fileAccess = new FileAccess(instance);
                fileAccess.loadPlayerShops();
            }
            instance.getMessage().send(sender,"reload_complete","&aReload complete",null,true);
        } else {
            instance.getMessage().send(sender,"shop_no_perm","&cERROR &f- You do not have permission to run that command",null,true);
        }
    }

    /**
     * Allows a user to remove a shop
     * @param sender CommandSender issuing the command
     */
    public void remove(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Perms.hasPermission(player, Perms.Type.REMOVE)) {
                instance.getDebug().log("ShopCommand", "remove", "Player has REMOVE permission");
                if (instance.getBazaarData().hasDelShop(player.getUniqueId())) {
                    // shop remove is already active
                    instance.getMessage().send(player,"shop_remove_duplicate","&cERROR &f- You are already removing a shop.  Complete that or wait for it to timeout",null,true);
                    return;
                }
                if (instance.getBazaarData().isCreating(player.getUniqueId())) {
                    instance.getMessage().send(player,"shop_remove_create","&cERROR &f- Unable to remove a shop while creating a shop.  Finish creating the shop or wait for it to timeout and then try again",null,true);
                    return;
                }

                instance.getDebug().log("ShopCommand", "remove", "Adding DelChest object");

                // Add a DelChest object so the next right click on a chest will be used to remove it
                Bazaar.getInstance().getBazaarData().putDelShop(player.getUniqueId());

                instance.getDebug().log("ShopCommand", "remove", "Starting timed task to remove DelChest object");

                // Start a timed task to remove the item from delChests in Data after x seconds
                final Player timedPlayer = player;
                Bukkit.getScheduler().runTaskLater(Bazaar.getInstance(), new Runnable() {
                    public void run() {
                        if (instance.getBazaarData().hasDelShop(timedPlayer.getUniqueId())) {
                            instance.getDebug().log("ShopCommand", "remove", "Removing DelChest object");
                            instance.getBazaarData().removeDelShop(timedPlayer.getUniqueId());
                            instance.getMessage().send(timedPlayer,"shop_remove_timeout","&cShop remove timed out &f- Please run &d/shop remove &fto try again",null,true);
                        }
                    }
                }, instance.getBazaarConfig().shopEditTimeout() * 20L);

                instance.getDebug().log("ShopCommand", "remove", "Sending Remove instructions");
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("<%timeout%>", "" + Bazaar.getInstance().getBazaarConfig().shopEditTimeout());
                instance.getMessage().send(player,"shop_remove_instructions","&aRight click a Bazaar shop within the next <%timeout%> seconds to remove it.  &cWarning: This action cannot be undone.",placeHolder,true);

            }
        } else {
            instance.getMessage().send(sender,"shop_no_console","&cERROR &f- Command must be run by an in-game player",null,true);
        }
    }

    /**
     * Saves all shops
     * @param sender
     */
    public void saveAll(CommandSender sender) {
        if (sender instanceof Player) {
            instance.getMessage().send(sender,"console_only","&cERROR &f- Command can only be run in console",null,true);
            return;
        }
        if (instance.getBazaarConfig().useMySQL()) {
            for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
                com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
                instance.getBazaarData().clearShops();
                mySQL.createShop(entry.getValue());
            }
        } else {
            for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
                FileAccess fileAccess = new FileAccess(instance);
                fileAccess.clearAllFiles();
                fileAccess.savePlayerShop(entry.getValue());
            }
        }
    }

    /**
     * Transfers shops from file to database
     * Bazaar must be using MySQL storage
     * Replaces database data - This will clear ALL Bazaar data from the database before loading
     * @param sender
     */
    public void transferToDatabase(CommandSender sender) {
        if (sender instanceof Player) {
            instance.getMessage().send(sender,"console_only","&cERROR &f- Command can only be run in console",null,true);
            return;
        }
        if (!instance.getBazaarConfig().useMySQL()) {
            instance.getMessage().send(sender,"mysql_only","&cERROR &f- This action can only be run if using MySQL",null,true);
            return;
        }
        try {
            Bazaar.getInstance().getHologramManager().removeHolograms();
            instance.getBazaarData().clearShops();
            com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
            mySQL.clearBazaarData();
            FileAccess fileAccess = new FileAccess(instance);
            fileAccess.loadPlayerShops();
            for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
                mySQL.createShop(entry.getValue());
            }
            instance.getBazaarData().clearShops();
            mySQL.loadPlayerShops(false);
            instance.getMessage().send(sender,"transfer_success","&aSUCCESS &f- Transfer complete",null,true);
        } catch (Exception ex) {
            instance.getMessage().send(sender,"transfer_error","&cERROR &f- Transfer failed",null,true);
            instance.getError().save(
                    "001",
                    "CommandFunctions",
                    "transferToDatabase",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Transfers shops from database to file
     * Bazaar must be in File storage
     * Replaces file data - This will delete ALL Bazaar data files before loading
     * @param sender
     */
    public void transferToFile(CommandSender sender) {
        if (sender instanceof Player) {
            instance.getMessage().send(sender,"console_only","&cERROR &f- Command can only be run in console",null,true);
            return;
        }
        if (!instance.getBazaarConfig().useMySQL()) {
            instance.getMessage().send(sender,"mysql_only","&cERROR &f- This action can only be run if using MySQL",null,true);
            return;
        }
        try {
            FileAccess fileAccess = new FileAccess(instance);
            fileAccess.clearAllFiles();
            for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
                fileAccess.savePlayerShop(entry.getValue());
            }
            instance.getMessage().send(sender,"transfer_success","&aSUCCESS &f- Transfer complete",null,true);
        } catch (Exception ex) {
            instance.getMessage().send(sender,"transfer_error","&cERROR &f- Transfer failed",null,true);
            instance.getError().save(
                    "001",
                    "CommandFunctions",
                    "transferToFile",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Displays the current version
     * @param sender CommandSender issuing the command
     */
    public void version(CommandSender sender) {
        boolean hasPerm = true;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Perms.hasPermission(player, Perms.Type.ADMIN)) {
                hasPerm = false;
            }
        }
        if (hasPerm) {
            Map<String, String> versionParam = new HashMap<>();
            versionParam.put("<%name%>", instance.getDisplayName());
            versionParam.put("<%ver%>", instance.getDescription().getVersion());
            instance.getMessage().send(sender,"shop_version","<%name%> - Version <%ver%>",versionParam,true);
        } else {
            instance.getMessage().send(sender,"shop_no_perm","&cERROR &f- You do not have permission to run that command",null,true);
        }
    }

    public void serialize(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
            String itemText = ItemFunctions.serialize(itemStack);
            /*
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) itemMeta;
                //List<PotionEffect> potionEffects = potionMeta.getBasePotionType().getPotionEffects();
                Color color = null;
                for(PotionEffect effect : potionEffects) {
                    if(color == null)
                        color = effect.getType().getColor();
                    else
                        color = color.mixColors(effect.getType().getColor());
                }
                String a = "";

            }
                */
        }

    }
}
