package com.ibexmc.bazaar.shop;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.events.BuyEvent;
import com.ibexmc.bazaar.events.RemoveShopEvent;
import com.ibexmc.bazaar.events.SellEvent;
import com.ibexmc.bazaar.util.functions.BazaarItemFunctions;
import com.ibexmc.bazaar.livedata.*;
import com.ibexmc.bazaar.storage.yaml.FileAccess;
import com.ibexmc.bazaar.util.*;
import com.ibexmc.internal.functions.*;
import com.ibexmc.internal.messaging.Error;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class ShopManager {

    Bazaar instance;

    public ShopManager(Bazaar bazaar) {
        this.instance = bazaar;
    }

    /**
     * Handles the PlayerInteract for a shop
     * @param player Player who clicked
     * @param shop Shop clicked (null if creating new shop)
     * @param playerAction Player action performed (left/right click)
     */
    public void click(Player player, Block block, Shop shop, PlayerAction playerAction) {
        if (player == null) {
            return;
        }

        // Shop is null, check if we should try and create a new one
        if (shop == null) {
            if (isShopBlock(block)) {
                CreatingShop creatingShop = instance.getBazaarData().getCreating(player.getUniqueId());
                if (creatingShop != null) {
                    if (!create(player, creatingShop, block)) {
                        // error
                        return;
                    }
                }
            }
        }

        if (shop == null) {
            return;
        }
        if (playerAction == null) {
            return;
        }

        // Check for current transaction lock
        if (TransactionLock.check(shop.getLocation())) {
            instance.getMessage().send(player, "transaction_in_progress", "&cSorry, another transaction is in progress.  Try again in a few moments", null, true);
            return;
        }

        if (Bazaar.getInstance().getBazaarData().hasDelShop(player.getUniqueId())) {
            // Try to remove the shop
            remove(player, shop);
            return;
        }

        switch (playerAction) {
            case LEFT_CLICK:
                // Apply the transaction lock
                TransactionLock.set(shop, player, true);
                if (instance.getBazaarConfig().leftClick() == ShopAction.BUY_FROM_SHOP) {
                    // Buy from shop
                    if (buyItemFromShop(player, shop)) {
                        // success
                    } else {
                        // error
                    }
                } else {
                    // Sell to shop
                    if (sellItemToShop(player, shop)) {
                        // success
                    } else {
                        // error
                    }
                }
                // Clear the transaction lock
                TransactionLock.set(shop, player, false);
                break;
            case RIGHT_CLICK:
                // If the player right-clicking is the owner or in the owning clan, break out
                if (shop.playerShop()) {
                    if (ownsShop(player, shop)) {
                        player.openInventory(shop.inventory());
                        break;
                    }
                } else if (shop.isServerShop() && !instance.getBazaarConfig().serverAccountInfiniteStock()) {
                    if (Perms.hasPermission(player, Perms.Type.ADMIN)) {
                        player.openInventory(shop.inventory());
                        break;
                    }
                }
                // Apply the transaction lock
                TransactionLock.set(shop, player, true);
                if (instance.getBazaarConfig().rightClick() == ShopAction.BUY_FROM_SHOP) {
                    // Buy from shop
                    if (buyItemFromShop(player, shop)) {
                        // success
                    } else {
                        // error
                    }
                } else {
                    // Sell to shop
                    if (sellItemToShop(player, shop)) {
                        // success
                    } else {
                        // error
                    }
                }
                // Clear the transaction lock
                TransactionLock.set(shop, player, false);
                break;
            case LEFT_CLICK_SNEAK:
                if (instance.getBazaarConfig().leftClick() == ShopAction.BUY_FROM_SHOP) {
                    // Buy Sneak task
                } else {
                    // Sell Sneak task
                }
                break;
            case RIGHT_CLICK_SNEAK:
                if (instance.getBazaarConfig().rightClick() == ShopAction.BUY_FROM_SHOP) {
                    // Buy Sneak task
                } else {
                    // Sell Sneak task
                }
                break;
            default:
                break;
        }
    }

    public boolean buyItemFromShop(Player player, Shop shop) {

        // Check that Player is not null
        if (player == null) {
            // Player cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "buyItemFromShop",
                    "Player cannot be null"
            );
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Player is not null");

        // Check that the Shop is not null
        if (shop == null) {
            // Shop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "buyItemFromShop",
                    "Shop cannot be null"
            );
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Shop is not null");

        // Get the buy from shop price
        double buyFromShopPrice = shop.getBuyFromShopPrice();
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Buy price = " + buyFromShopPrice);

        // Get the buyers OfflinePlayer
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Getting offline player");

        // Check that the player and shop owner are not the same
        if (ownsShop(player, shop)) {
            // Player cannot buy from their own shop
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_fail_own", "&cSorry, you cannot buy from your own shop", null, true);
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "Player owns the shop"
            );
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Player does not own the shop");

        // Check the player has BUY permissions
        if (!Perms.hasPermission(player, Perms.Type.BUY)) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "no_perm", "&cSorry, you do not have permission to do that", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Player has BUY permission");

        // If Domain: Check if player is allowed to sell items here
        if (instance.getDependencies().hasDomain()) {
            if (!Domain.allowed(player, shop.getLocation(),"PREVENT_BAZAAR_SELL_BY_NONALLOWED")) {
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "protection_sell_shop_deny", "&cSorry, you are not allowed to sell to shops here", null, true);
                return false;
            }
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Buying not blocked by Domain");

        // If WorldGuard: Check if player can use this type of shop here
        if (instance.getDependencies().hasWorldGuard()) {
            if (shop.isAdminShop()) {
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_ADMIN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_admin_shop_deny", "&cSorry, you are not allowed to use Admin shops here", null, true);
                    return false;
                }
            }
            if (!shop.isAdminShop() || !shop.isClanShop()) {
                // Player shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_PLAYER_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_player_shop_deny", "&cSorry, you are not allowed to use player shops here", null, true);
                    return false;
                }
            }
            if (shop.isClanShop()) {
                // Clan shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_CLAN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_clan_shop_deny", "&cSorry, you are not allowed to use clan shops here", null, true);
                    return false;
                }
            }
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Buying not blocked by WorldGuard");

        // Check if a cooldown is in effect
        if (Bazaar.getInstance().getBazaarData().hasCooldown(player.getUniqueId(), shop.getLocation())) {
            instance.getMessage().send(player, "cooldown", "&cPlease wait a moment and try again", null, true);
            Particles.denyAccess(shop.getLocation(), player);
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Cooldown is not in effect");

        // Check the shop is selling
        if (buyFromShopPrice <= 0) {
            // Unable to buy here, shop is not selling
            instance.getDebug().log(
                    "ShopManager",
                    "buyItemFromShop",
                    "&4Exiting event - No buy from shop price"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_unavailable", "&cShop is not selling to players at this time", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Shop is selling - buy price > 0");

        // Check if the player can afford it
        if (!Economy.playerHasFunds(offlinePlayer, buyFromShopPrice)) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_item", "&cUnable to purchase item, insufficient funds", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Player has sufficient funds");

        // Check if the shop has sufficient stock
        if (!shopHasSufficientQty(shop)) {
            if (shop.playerShop() || (shop.isServerShop() && !instance.getBazaarConfig().serverAccountInfiniteStock())) {
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "buy_from_shop_out_of_stock", "&cUnable to buy from shop - Shop is out of stock", null, true);
            }
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Shop has sufficient quantity");

        // Check if the player has a free inventory slot
        if (!InventoryFunctions.hasFreeSpace(player, shop.getItemStack())) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_space", "&cUnable to purchase item from shop. You must have a free inventory slot", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Player has a free slot");

        // Call event
        BuyEvent buyEvent = new BuyEvent(player, shop);
        Bukkit.getServer().getPluginManager().callEvent(buyEvent);
        if (buyEvent.isCancelled()) {
            // Buy event was cancelled
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Event was called");


        // Take the money from the player
        if (!withdrawFundsFromPlayer(offlinePlayer, buyFromShopPrice)) {
            instance.getDebug().log(
                    "ShopManager",
                    "buyItemFromShop",
                    "&eFailed due to an error withdrawing funds from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Money taken from player ($" + buyFromShopPrice + ")");

        // Give the money to the shop owner IF NOT Admin shop
        int tax = 0;
        double buyPriceAfterTax = buyFromShopPrice; // Calculate tax
        if (!shop.isAdminShop() && !shop.isServerShop()) {
            // Tax rate applied to shop when selling to players
            tax = instance.getBazaarConfig().salesTax();
            if (tax > 0) {
                buyPriceAfterTax = buyPriceAfterTax - ((buyPriceAfterTax / 100) * tax);
                // If we have a server account set up, the tax should be paid there
                if (instance.getBazaarConfig().serverAccount() != null) {
                    instance.getDebug().log(
                            "ShopManager",
                            "buyItemFromShop",
                            "Paying tax to server account\n" +
                                    "Server Account: " +
                                    Bazaar.getInstance().getBazaarConfig().serverAccount().getUniqueId() +
                                    "\n" +
                                    "Amount: " + (shop.getBuyFromShopPrice() - buyPriceAfterTax)
                    );
                    Economy.depositFunds(
                            instance.getBazaarConfig().serverAccount(),
                            (buyFromShopPrice - buyPriceAfterTax)
                    );
                }
            }
        }
        if (!payShop(shop, buyPriceAfterTax)) {
            // Error paying shop
            instance.getDebug().log(
                    "ShopManager",
                    "buyItemFromShop",
                    "&eFailed due to an error withdrawing funds from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
            // Try to refund player
            if (!payPlayer(offlinePlayer, buyFromShopPrice)) {
                instance.getError().save(
                        "005",
                        "ShopManager",
                        "buyItemFromShop",
                        "Unexpected Error",
                        "Unable to refund player " + player.getName() + " Amount: " + buyFromShopPrice,
                        Error.Severity.WARN,
                        null
                );
                return false;
            }
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Money (after tax) paid to shop owner  ($" + buyPriceAfterTax + ")");

        // Take the item from the shop
        ShopFunctions.InventoryTransactionResult transactionResult = ShopFunctions.takeItemFromShop(shop, false);
        switch (transactionResult.transactionResult()) {
            case INSUFFICIENT_QTY:
                // Out of stock
                instance.getDebug().log(
                        "ShopManager",
                        "buyFromShop",
                        "&eShop is out of stock");
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "buy_from_shop_out_of_stock", "&cUnable to buy from shop - Shop is out of stock", null, true);
                // Try to refund the player
                if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                    instance.getError().save(
                            "001",
                            "ShopManager",
                            "buyFromShop",
                            "Unexpected Error",
                            "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                            Error.Severity.WARN,
                            null
                    );
                }
                return false;
            case ERROR:
                // Unable to take item from shop
                instance.getDebug().log(
                        "ShopManager",
                        "buyFromShop",
                        "&eFailed due to an error taking item from the shop - " + transactionResult.getReturnMessage());
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
                // Try to refund the player
                if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                    instance.getError().save(
                            "002",
                            "ShopManager",
                            "buyFromShop",
                            "Unexpected Error",
                            "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                            Error.Severity.WARN,
                            null
                    );
                }
                return false;
            default:
                break;
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Item taken from shop");

        // Get the item
        ItemStack boughtItem = shop.getItemStack().clone();

        instance.getDebug().log("ShopManager", "buyItemFromShop", "Item given to player");
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Item info: " + shop.getAmount() + "x " + shop.getItemName());
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Actual Item info: " + boughtItem.getAmount() + "x " + boughtItem.getType());

        if (boughtItem.getAmount() != shop.getAmount()) {
            instance.getDebug().log("ShopManager", "buyItemFromShop", "Item amount does not match shop amount. Setting the amount");
            boughtItem.setAmount(shop.getAmount());
        }

        // Give item to player
        if (!giveItemToPlayer(player, boughtItem)) {
            // Unable to give item to player
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "&eFailed due to an error withdrawing funds from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
            // Try to refund player
            if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                instance.getError().save(
                        "003",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                        Error.Severity.WARN,
                        null
                );
            }
            // Try to return the item to the shop
            if (!giveItemToShop(shop, boughtItem, shop.getAmount())) {
                instance.getError().save(
                        "004",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to return item to shop at " + WorldFunctions.simpleReadable(shop.getLocation()) +
                                " Item: " + shop.getItemName() + " Amount: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
        }


        // Message shop owner if online
        if (instance.getBazaarConfig().messageSellerOnPurchase()) {
            if (!shop.isAdminShop() && !shop.isClanShop() && !shop.isServerShop()) {
                Player seller = Bukkit.getPlayer(shop.getSeller());
                if (seller != null) {
                    // Seller is online
                    Map<String, String> placeHolder = new HashMap<>();
                    placeHolder.put("<%amount%>", "" + shop.getAmount());
                    placeHolder.put("<%itemname%>", shop.getItemName());
                    placeHolder.put("<%player%>", player.getName());
                    String buyPriceString = "" + shop.getBuyFromShopPrice();
                    if (shop.getBuyFromShopPrice() > 0) {
                        if ((shop.getBuyFromShopPrice() % 1) == 0) {
                            NumberFormat nf = NumberFormat.getNumberInstance();
                            nf.setMaximumFractionDigits(0);
                            buyPriceString = nf.format(shop.getBuyFromShopPrice());
                        }
                    }
                    placeHolder.put("<%cost%>", Bazaar.getInstance().getBazaarConfig().currencySymbol() + buyPriceString);
                    instance.getMessage().send(seller, "buy_success_seller", "&a<%player%> bought <%amount%>x <%itemname%> from shop for <%cost%>", placeHolder, true);
                }
            }
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Shop owner messaged if online");

        // Adding a cooldown
        Bazaar.getInstance().getBazaarData().putCooldown(shop.getLocation(), offlinePlayer.getUniqueId(), shop.getCooldown());
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Cooldown added");

        // Update the hologram (for Out of Stock check)
        if (!shop.isAdminShop() && !shop.isServerShop()) {
            instance.getHologramManager().removeHologram(shop);
            instance.getHologramManager().createHologram(shop);
        }
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Hologram added");

        // Purchase complete
        instance.getDebug().log("PlayerBuyFromShop", "buy", "&7Sale was completed successfully");
        instance.getDebug().log("ShopManager", "buyItemFromShop", "Purchase complete");

        // Message the player that they bought the item
        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("<%amount%>", "" + shop.getAmount());
        placeHolder.put("<%itemname%>", shop.getItemName());
        instance.getMessage().send(player, "buy_success", "&a<%amount%>x <%itemname%> bought from shop", placeHolder, true);

        // Log the transaction
        Map<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("<%amount%>", "" + shop.getAmount());
        placeHolderMap.put("<%itemname%>", shop.getItemName());
        placeHolderMap.put("<%buyer%>", player.getName());
        placeHolderMap.put("<%price%>", "" + shop.getBuyFromShopPrice());
        placeHolderMap.put("<%location%>", WorldFunctions.simpleReadable(shop.getLocation()));
        if (shop.playerShop()) {
            String owner = "UNKNOWN";
            if (shop.getSellerOfflinePlayer().getName() != null) {
                owner = shop.getSellerOfflinePlayer().getName();
            }
            placeHolderMap.put("<%owner%>", owner);
        }
        placeHolderMap.put("<%clan%>", shop.getClanTag());

        if (instance.getBazaarConfig().isLogSalesToConsole()) {
            if (shop.playerShop()) {
                Logging.transaction(
                        "console_log_player_shop_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the shop at <%location%> belonging to <%owner%>",
                        placeHolderMap
                );
            } else if (shop.isClanShop()) {
                Logging.transaction(
                        "console_log_player_clan_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the clan shop at <%location%> belonging to <%clan%>",
                        placeHolderMap
                );
            } else if (shop.isAdminShop()) {
                Logging.transaction(
                        "console_log_player_admin_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the admin shop at <%location%>",
                        placeHolderMap
                );
            } else if (shop.isServerShop()) {
                Logging.transaction(
                        "console_log_player_server_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the server shop at <%location%>",
                        placeHolderMap
                );
            }
        }

        // Get the players new balance and send it to the player
        OfflinePlayer buyerOfflinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        double newBalance = Dependencies.balancePlayer(buyerOfflinePlayer).getBalance();
        Map<String, String> placeHolder1 = new HashMap<>();
        placeHolder1.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder1.put("<%balance%>", NumberFunctions.doubleTo2DecimalPlaceString(newBalance));
        instance.getMessage().send(player, "sell_success_balance", "&aNew balance: <%currencysymbol%><%balance%>", placeHolder1, true);


        return true;
    }

    public boolean sellItemToShop(Player player, Shop shop) {

        // Check that Player is not null
        if (player == null) {
            // Player cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "Player cannot be null"
            );
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Player is not null");

        // Check that the Shop is not null
        if (shop == null) {
            // Shop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "Shop cannot be null"
            );
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Shop is not null");

        // Get the sell to shop price
        double sellToShopPrice = shop.getSellToShopPrice();
        instance.getDebug().log("ShopManager", "sellItemToShop", "Sell to shop price set: " + sellToShopPrice);

        // Get the sellers OfflinePlayer
        OfflinePlayer offlineSeller = Bukkit.getOfflinePlayer(player.getUniqueId());
        instance.getDebug().log("ShopManager", "sellItemToShop", "Sellers OfflinePlayer gotten: " + offlineSeller.getName());

        // Get the buyers OfflinePlayer or clan tag
        OfflinePlayer offlineBuyer = null;
        String clanBuyer = "";

        String owner = "UNKNOWN";
        if (offlineBuyer != null) {
            if (offlineBuyer.getName() != null) {
                owner = offlineBuyer.getName();
            }
        } else {
            if (shop.isAdminShop()) {
                owner = "ADMIN SHOP";
            }
            if (shop.isServerShop()) {
                owner = "SERVER SHOP";
            }
            if (shop.isClanShop() && !clanBuyer.equals("")) {
                owner = "CLAN " + clanBuyer;
            }
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Shop owner: " + owner);

        if (!shop.isAdminShop() && !shop.isServerShop()) {
            if (!shop.isClanShop()) {
                offlineBuyer = shop.getSellerOfflinePlayer();
            } else {
                clanBuyer = shop.getClanTag();
            }
        }


        // Check that the player and shop owner are not the same
        if (ownsShop(player, shop)) {
            // Player cannot buy from their own shop
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_fail_own", "&cSorry, you cannot sell to your own shop", null, true);
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "Player owns the shop"
            );
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Shop owner and buyer are NOT the same");

        // Check the player has SELL permissions
        if (!Perms.hasPermission(player, Perms.Type.SELL)) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "no_perm", "&cSorry, you do not have permission to do that", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Player has SELL permission");

        // If Domain: Check if player is allowed to sell items here
        if (instance.getDependencies().hasDomain()) {
            if (!Domain.allowed(player, shop.getLocation(),"PREVENT_BAZAAR_BUY_BY_NONALLOWED")) {
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "protection_buy_shop_deny", "&cSorry, you are not allowed to buy from shops here", null, true);
                return false;
            }
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Selling is not prevented by Domain");

        // If WorldGuard: Check if player can use this type of shop here
        if (instance.getDependencies().hasWorldGuard()) {
            if (shop.isAdminShop()) {
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_ADMIN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_admin_shop_deny", "&cSorry, you are not allowed to use Admin shops here", null, true);
                    return false;
                }
            }
            if (!shop.isAdminShop() || !shop.isClanShop()) {
                // Player shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_PLAYER_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_player_shop_deny", "&cSorry, you are not allowed to use player shops here", null, true);
                    return false;
                }
            }
            if (shop.isClanShop()) {
                // Clan shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_CLAN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_clan_shop_deny", "&cSorry, you are not allowed to use clan shops here", null, true);
                    return false;
                }
            }
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Selling is not prevented by WorldGuard");

        // Check if a cooldown is in effect
        if (Bazaar.getInstance().getBazaarData().hasCooldown(player.getUniqueId(), shop.getLocation())) {
            instance.getMessage().send(player, "cooldown", "&cPlease wait a moment and try again", null, true);
            Particles.denyAccess(shop.getLocation(), player);
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "No current cooldown in effect");

        // Check the shop is buying
        if (sellToShopPrice <= 0) {
            // Unable to sell here, shop is not buying
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "&4Exiting event - No sell to shop price"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_unavailable", "&cShop is not buying from players at this time", null, true);
            return false;
        }

        // Check if shop can afford it
        if (!checkShopAfford(shop, sellToShopPrice)) {
            // Unable to sell here, shop cannot afford it
            instance.getDebug().log(
                    "Sell",
                    "sellItemToShop",
                    "&eFailed due to insufficient funds"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_funds", "&cUnable to sell item to shop. Shop has insufficient funds, please try again later", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Shop can afford to buy the item");

        // Get item in hand, compare against shop item
        ItemStack itemInHand = player.getInventory().getItemInMainHand().clone();
        if (!InventoryFunctions.compareItems(itemInHand, shop.getItemStack(), true, false)) {
            // Invalid item in hand
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "&eFailed due to an invalid item in hand"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_invalid_item", "&cUnable to sell item to shop, you must be holding the correct item", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Item being sold is the same as the item being bought");

        // Check if quantity in hand >= sell quantity
        if (itemInHand.getAmount() < shop.getAmount()) {
            // Unable to sell here, you do not have enough in hand
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to player not having enough of the item"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_qty", "&cUnable to sell item to shop.  Insufficient quantity", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Item being sold to shop has sufficient quantity");

        // Check if shop has space
        if (!shopHasSpace(shop, itemInHand, shop.getAmount())) {
            // Unable to sell here, shop does not have space
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "&eFailed due to insufficient space"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_space", "&cUnable to sell item to shop. Shop is currently full, please try again later", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Shop has space available");

        // Call event as all prerequisites are met
        SellEvent sellEvent = new SellEvent(player, shop);
        Bukkit.getServer().getPluginManager().callEvent(sellEvent);
        if (sellEvent.isCancelled()) {
            // Sell event was cancelled
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Sell event called");

        // Take the money from the shop owner (or clan)
        if (!withdrawFundsFromShop(shop, sellToShopPrice)) {
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "&eFailed due to an error withdrawing funds from the shop");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Withdrawn " + sellToShopPrice + " from shop");

        // Give the money to the player
        if (!payPlayer(offlineSeller, sellToShopPrice)) {
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "&eFailed due to an error paying the player that is selling to the shop");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Paid " + sellToShopPrice + " to player");

        // Take the item from the player
        if (!takeItemFromPlayer(player, shop)) {
            instance.getDebug().log(
                    "ShopManager",
                    "sellItemToShop",
                    "&eFailed due to an error taking the item from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            // Refund shop, take money back from player
            payPlayer(offlineBuyer, shop.getSellToShopPrice() * -1);
            payShop(shop, sellToShopPrice);
            return false;
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Item taken from player");

        // Give the item to the store IF NOT Admin shop
        if (!shop.isAdminShop() && !shop.isServerShop()) {
            //addItemToShop(shop, itemInHand, shop.getAmount());
            ItemStack addItemStack = itemInHand.clone();
            addItemStack.setAmount(shop.getAmount());
            addItemsToShopInventory(shop, addItemStack);
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Item given to store");

        // Save transaction history
        if (instance.getBazaarConfig().saveHistory()) {
            TransactionHistory transactionHistory = new TransactionHistory(instance);
            transactionHistory.save(player, shop, ShopAction.SELL_TO_SHOP);
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Saving transaction history");

        // Message the buyer
        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("<%cost%>", "" + sellToShopPrice);
        placeHolder.put("<%itemname%>", shop.getItemName());
        instance.getMessage().send(player, "sell_success", "&a<%cost%>x <%itemname%> sold to shop", placeHolder, true);
        instance.getDebug().log("ShopManager", "sellItemToShop", "Buyer messaged");

        // Message the seller (if online)
        if (!shop.isAdminShop() && !shop.isServerShop() && !shop.isClanShop()) {
            Player sellerPlayer = Bukkit.getPlayer(shop.getSeller());
            if (sellerPlayer != null) {
                placeHolder.put("<%buyer%>", player.getName());
                instance.getMessage().send(sellerPlayer, "sell_success_seller", "&a<%buyer%> has bought <%itemname%> for <%cost%>", placeHolder, true);
            }
        }
        instance.getDebug().log("ShopManager", "sellItemToShop", "Seller messaged (if online)");

        double newBalance = Dependencies.balancePlayer(offlineSeller).getBalance();
        HashMap<String, String> placeHolder1 = new HashMap<>();
        placeHolder1.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder1.put("<%balance%>", NumberFunctions.doubleTo2DecimalPlaceString(newBalance));
        instance.getMessage().send(player, "sell_success_balance", "&aNew balance: <%currencysymbol%><%balance%>", placeHolder1, true);

        // Log the transaction
        Map<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("<%amount%>", "" + shop.getAmount());
        placeHolderMap.put("<%itemname%>", shop.getItemName());
        placeHolderMap.put("<%seller%>", player.getName());
        placeHolderMap.put("<%price%>", "" + shop.getSellToShopPrice());
        placeHolderMap.put("<%location%>", WorldFunctions.simpleReadable(shop.getLocation()));
        placeHolderMap.put("<%owner%>", owner);
        placeHolderMap.put("<%clan%>", shop.getClanTag());

        if (instance.getBazaarConfig().isLogSalesToConsole()) {
            if (shop.playerShop()) {
                Logging.transaction(
                        "console_log_player_shop_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the shop at <%location%> belonging to <%owner%>",
                        placeHolderMap
                );
            } else if (shop.isClanShop()) {
                Logging.transaction(
                        "console_log_player_clan_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the clan shop at <%location%> belonging to <%clan%>",
                        placeHolderMap
                );
            } else if (shop.isAdminShop()) {
                Logging.transaction(
                        "console_log_player_admin_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the admin shop at <%location%>",
                        placeHolderMap
                );
            } else if (shop.isServerShop()) {
                Logging.transaction(
                        "console_log_player_server_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the server shop at <%location%>",
                        placeHolderMap
                );
            }
        }

        return true;

    }

    /**
     * Attempts to buy an item from the shop
     * @param player Player buying the item
     * @param shop Shop being bought from
     * @return If true, item was bought from the shop successfully
     */
    public boolean buyFromShop(Player player, Shop shop) {
        if (player == null) {
            // Player cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "Player cannot be null"
            );
            return false;
        }
        if (shop == null) {
            // Shop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "Shop cannot be null"
            );
            return false;
        }

        if (ownsShop(player, shop)) {
            // Player cannot buy from their own shop
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_fail_own", "&cSorry, you cannot buy from your own shop", null, true);
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "Player owns the shop"
            );
            return false;
        }
        // Check the player has BUY permissions
        if (!Perms.hasPermission(player, Perms.Type.BUY)) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "no_perm", "&cSorry, you do not have permission to do that", null, true);
            return false;
        }

        // If Domain: Check if player is allowed to sell items here
        if (instance.getDependencies().hasDomain()) {
            if (!Domain.allowed(player, shop.getLocation(),"PREVENT_BAZAAR_BUY_BY_NONALLOWED")) {
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "protection_buy_shop_deny", "&cSorry, you are not allowed to buy from shops here", null, true);
                return false;
            }
        }

        // If WorldGuard: Check if player can use this type of shop here
        if (instance.getDependencies().hasWorldGuard()) {
            if (shop.isAdminShop()) {
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_ADMIN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_admin_shop_deny", "&cSorry, you are not allowed to use Admin shops here", null, true);
                    return false;
                }
            }
            if (!shop.isAdminShop() || !shop.isClanShop()) {
                // Player shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_PLAYER_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_player_shop_deny", "&cSorry, you are not allowed to use player shops here", null, true);
                    return false;
                }
            }
            if (shop.isClanShop()) {
                // Clan shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_CLAN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_clan_shop_deny", "&cSorry, you are not allowed to use clan shops here", null, true);
                    return false;
                }
            }
        }

        // Get the buyers OfflinePlayer
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        // Check if a cooldown is in effect
        if (Bazaar.getInstance().getBazaarData().hasCooldown(player.getUniqueId(), shop.getLocation())) {
            instance.getMessage().send(player, "cooldown", "&cPlease wait a moment and try again", null, true);
            Particles.denyAccess(shop.getLocation(), player);
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            return false;
        }

        // Check if the shop is selling items
        if (shop.getBuyFromShopPrice() <= 0) {
            // Unable to buy here, shop is not selling
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "&4Exiting event - No buy price"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_unavailable", "&cShop is not buying from players at this time", null, true);
            return false;
        }

        // Check if the shop has sufficient quantity to sell
        if (!shopHasSufficientQty(shop)) {
            if (shop.playerShop() || (shop.isServerShop() && !instance.getBazaarConfig().serverAccountInfiniteStock())) {
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "buy_from_shop_out_of_stock", "&cUnable to buy from shop - Shop is out of stock", null, true);
            }
            return false;
        }

        // Check if the player has enough space in their inventory
        if (!InventoryFunctions.hasFreeSpace(player, shop.getItemStack())) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_space", "&cUnable to purchase item from shop. You must have a free inventory slot", null, true);
            return false;
        }

        // Check if the player can afford it
        if (!Economy.playerHasFunds(offlinePlayer, shop.getBuyFromShopPrice())) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_item", "&cUnable to purchase item, insufficient funds", null, true);
            return false;
        }

        // Call event
        BuyEvent buyEvent = new BuyEvent(player, shop);
        Bukkit.getServer().getPluginManager().callEvent(buyEvent);
        if (buyEvent.isCancelled()) {
            // Buy event was cancelled
            return false;
        }

        // Withdraw funds from player
        if (!withdrawFundsFromPlayer(offlinePlayer, shop.getSellToShopPrice())) {
            // Unable to take item from player - Nothing to rollback
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "&eFailed due to an error withdrawing funds from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
            return false;
        }

        // Take item from shop
        ShopFunctions.InventoryTransactionResult transactionResult = ShopFunctions.takeItemFromShop(shop, false);
        switch (transactionResult.transactionResult()) {
            case INSUFFICIENT_QTY:
                // Out of stock
                instance.getDebug().log(
                        "ShopManager",
                        "buyFromShop",
                        "&eShop is out of stock");
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "buy_from_shop_out_of_stock", "&cUnable to buy from shop - Shop is out of stock", null, true);
                // Try to refund the player
                if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                    instance.getError().save(
                            "001",
                            "ShopManager",
                            "buyFromShop",
                            "Unexpected Error",
                            "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                            Error.Severity.WARN,
                            null
                    );
                }
                return false;
            case ERROR:
                // Unable to take item from shop
                instance.getDebug().log(
                        "ShopManager",
                        "buyFromShop",
                        "&eFailed due to an error taking item from the shop - " + transactionResult.getReturnMessage());
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
                // Try to refund the player
                if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                    instance.getError().save(
                            "002",
                            "ShopManager",
                            "buyFromShop",
                            "Unexpected Error",
                            "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                            Error.Severity.WARN,
                            null
                    );
                }
                return false;
            default:
                break;
        }


        // Give item to player
        if (!giveItemToPlayer(player, shop.getItemStack())) {
            // Unable to give item to player
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "&eFailed due to an error withdrawing funds from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
            // Try to refund player
            if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                instance.getError().save(
                        "003",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                        Error.Severity.WARN,
                        null
                );
            }
            // Try to return the item to the shop
            if (!giveItemToShop(shop, shop.getItemStack(), shop.getAmount())) {
                instance.getError().save(
                        "004",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to return item to shop at " + WorldFunctions.simpleReadable(shop.getLocation()) +
                                " Item: " + shop.getItemName() + " Amount: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
        }

        // Calculate tax
        int tax = 0;
        double buyPriceAfterTax = shop.getBuyFromShopPrice();
        if (!shop.isAdminShop() && !shop.isServerShop()) {
            // Tax rate applied to shop when selling to players
            tax = instance.getBazaarConfig().salesTax();
            if (tax > 0) {
                buyPriceAfterTax = buyPriceAfterTax - ((buyPriceAfterTax / 100) * tax);
                // If we have a server account set up, the tax should be paid there
                if (instance.getBazaarConfig().serverAccount() != null) {
                    instance.getDebug().log(
                            "ShopManager",
                            "buyFromShop",
                            "Paying tax to server account\n" +
                                    "Server Account: " +
                                    Bazaar.getInstance().getBazaarConfig().serverAccount().getUniqueId() +
                                    "\n" +
                                    "Amount: " + (shop.getBuyFromShopPrice() - buyPriceAfterTax)
                    );
                    Economy.depositFunds(
                            instance.getBazaarConfig().serverAccount(),
                            (shop.getBuyFromShopPrice() - buyPriceAfterTax)
                    );
                }
            }
        }
        if (!payShop(shop, buyPriceAfterTax)) {
            // Error paying shop
            instance.getDebug().log(
                    "ShopManager",
                    "buyFromShop",
                    "&eFailed due to an error withdrawing funds from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "buy_failed_error", "&cAn error occurred.  Unable to purchase item from shop", null, true);
            // Try to refund player
            if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                instance.getError().save(
                        "005",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to refund player " + player.getName() + " Amount: " + shop.getSellToShopPrice(),
                        Error.Severity.WARN,
                        null
                );
            }
            // Try to return item to shop
            if (!giveItemToShop(shop, shop.getItemStack(), shop.getAmount())) {
                instance.getError().save(
                        "006",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to return item to shop at " + WorldFunctions.simpleReadable(shop.getLocation()) +
                                " Item: " + shop.getItemName() + " Amount: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
            // Try to take the item back from the player
            if (!takeItemFromPlayer(player, shop)) {
                instance.getError().save(
                        "007",
                        "ShopManager",
                        "buyFromShop",
                        "Unexpected Error",
                        "Unable to take item back from player " + player.getName() + " during refund process at " +
                                WorldFunctions.simpleReadable(shop.getLocation()) +
                                " Item: " + shop.getItemName() + " Amount: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
        }

        // Message shop owner if online
        if (instance.getBazaarConfig().messageSellerOnPurchase()) {
            if (!shop.isAdminShop() && !shop.isClanShop() && !shop.isServerShop()) {
                Player seller = Bukkit.getPlayer(shop.getSeller());
                if (seller != null) {
                    // Seller is online
                    Map<String, String> placeHolder = new HashMap<>();
                    placeHolder.put("<%amount%>", "" + shop.getAmount());
                    placeHolder.put("<%itemname%>", shop.getItemName());
                    placeHolder.put("<%player%>", player.getName());
                    String buyPriceString = "" + shop.getBuyFromShopPrice();
                    if (shop.getBuyFromShopPrice() > 0) {
                        if ((shop.getBuyFromShopPrice() % 1) == 0) {
                            NumberFormat nf = NumberFormat.getNumberInstance();
                            nf.setMaximumFractionDigits(0);
                            buyPriceString = nf.format(shop.getBuyFromShopPrice());
                        }
                    }
                    placeHolder.put("<%cost%>", Bazaar.getInstance().getBazaarConfig().currencySymbol() + buyPriceString);
                    instance.getMessage().send(seller, "buy_success_seller", "&a<%player%> bought <%amount%>x <%itemname%> from shop for <%cost%>", placeHolder, true);
                }
            }
        }

        // Adding a cooldown
        Bazaar.getInstance().getBazaarData().putCooldown(shop.getLocation(), offlinePlayer.getUniqueId(), shop.getCooldown());

        // Update the hologram (for Out of Stock check)
        if (!shop.isAdminShop() && !shop.isServerShop()) {
            instance.getHologramManager().removeHologram(shop);
            instance.getHologramManager().createHologram(shop);
        }

        // Purchase complete
        instance.getDebug().log("PlayerBuyFromShop", "buy", "&7Sale was completed successfully");

        if (instance.getBazaarConfig().saveHistory()) {
            TransactionHistory transactionHistory = new TransactionHistory(instance);
            transactionHistory.save(player, shop, ShopAction.BUY_FROM_SHOP);
        }

        // Message the player
        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("<%amount%>", "" + shop.getAmount());
        placeHolder.put("<%itemname%>", shop.getItemName());
        instance.getMessage().send(player, "buy_success", "&a<%amount%>x <%itemname%> bought from shop", placeHolder, true);

        Map<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("<%amount%>", "" + shop.getAmount());
        placeHolderMap.put("<%itemname%>", shop.getItemName());
        placeHolderMap.put("<%buyer%>", player.getName());
        placeHolderMap.put("<%price%>", "" + shop.getBuyFromShopPrice());
        placeHolderMap.put("<%location%>", WorldFunctions.simpleReadable(shop.getLocation()));
        if (shop.playerShop()) {
            placeHolderMap.put("<%owner%>", shop.getSellerOfflinePlayer().getName());
        }
        placeHolderMap.put("<%clan%>", shop.getClanTag());

        if (instance.getBazaarConfig().isLogSalesToConsole()) {
            if (shop.playerShop()) {
                Logging.transaction(
                        "console_log_player_shop_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the shop at <%location%> belonging to <%owner%>",
                        placeHolderMap
                        );
            } else if (shop.isClanShop()) {
                Logging.transaction(
                        "console_log_player_clan_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the clan shop at <%location%> belonging to <%clan%>",
                        placeHolderMap
                );
            } else if (shop.isAdminShop()) {
                Logging.transaction(
                        "console_log_player_admin_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the admin shop at <%location%>",
                        placeHolderMap
                );
            } else if (shop.isServerShop()) {
                Logging.transaction(
                        "console_log_player_server_buy",
                        "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the server shop at <%location%>",
                        placeHolderMap
                );
            }
        }

        OfflinePlayer buyerOfflinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        double newBalance = Dependencies.balancePlayer(buyerOfflinePlayer).getBalance();
        Map<String, String> placeHolder1 = new HashMap<>();
        placeHolder1.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder1.put("<%balance%>", "" + NumberFunctions.doubleTo2DecimalPlaceString(newBalance));
        instance.getMessage().send(player, "sell_success_balance", "&aNew balance: <%currencysymbol%><%balance%>", placeHolder1, true);

        return true;
    }

    /**
     * Attempts to sell an item to the shop
     * @param player Player selling the item
     * @param shop Shop being sold to
     * @return If true, item was sold to the shop successfully
     */
    public boolean sellToShop(Player player, Shop shop) {

        if (player == null) {
            // Player cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "Player cannot be null"
            );
            return false;
        }
        if (shop == null) {
            // Shop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "Shop cannot be null"
            );
            return false;
        }

        if (ownsShop(player, shop)) {
            // Player cannot sell to their own shop
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "Player owns the shop"
            );
            return false;
        }

        // Check the player has SELL permissions
        if (!Perms.hasPermission(player, Perms.Type.SELL)) {
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "create_sell_no_perm", "&cSorry, you do not have permission to sell to a Bazaar shop", null, true);
            return false;
        }

        // If Domain: Check if player is allowed to sell items here
        if (instance.getDependencies().hasDomain()) {
            if (!Domain.allowed(player, shop.getLocation(),"PREVENT_BAZAAR_SELL_BY_NONALLOWED")) {
                SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                instance.getMessage().send(player, "protection_sell_shop_deny", "&cSorry, you are not allowed to sell to shops here", null, true);
                return false;
            }
        }

        // If WorldGuard: Check if player can use this type of shop here
        if (instance.getDependencies().hasWorldGuard()) {
            if (shop.isAdminShop()) {
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_ADMIN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_admin_shop_deny", "&cSorry, you are not allowed to use Admin shops here", null, true);
                    return false;
                }
            }
            if (!shop.isAdminShop() || !shop.isClanShop()) {
                // Player shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_PLAYER_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_player_shop_deny", "&cSorry, you are not allowed to use player shops here", null, true);
                    return false;
                }
            }
            if (shop.isClanShop()) {
                // Clan shop
                if (WorldGuard.allowed(player, shop.getLocation(), WorldGuard.USE_CLAN_SHOP) != WorldGuard.WorldGuardResult.ALLOW) {
                    SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
                    instance.getMessage().send(player, "protection_clan_shop_deny", "&cSorry, you are not allowed to use clan shops here", null, true);
                    return false;
                }
            }
        }

        // Get the sellers OfflinePlayer
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        // Check if the shop is buying items
        if (shop.getSellToShopPrice() <= 0) {
            // Unable to sell here, shop is not buying
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&4Exiting event - No buy price"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_unavailable", "&cShop is not selling to players at this time", null, true);
            return false;
        }

        // Get item in hand, compare against shop item
        ItemStack itemInHand = player.getInventory().getItemInMainHand().clone();
        if (!InventoryFunctions.compareItems(itemInHand, shop.getItemStack(), true, false)) {
            // Invalid item in hand
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to an invalid item in hand"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_invalid_item", "&cUnable to sell item to shop, you must be holding the correct item", null, true);
            return false;
        }

        // Check if quantity in hand >= sell quantity
        if (itemInHand.getAmount() < shop.getAmount()) {
            // Unable to sell here, you do not have enough in hand
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to player not having enough of the item"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_qty", "&cUnable to sell item to shop.  Insufficient quantity", null, true);
            return false;
        }

        // Check that the shop has space to receive it
        if (!shopHasSpace(shop, itemInHand, shop.getAmount())) {
            // Unable to sell here, shop does not have space
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to insufficient space"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_space", "&cUnable to sell item to shop. Shop is currently full, please try again later", null, true);
            return false;
        }

        // Check shop can afford it
        if (!checkShopAfford(shop, shop.getBuyFromShopPrice())) {
            // Unable to sell here, shop cannot afford it
            instance.getDebug().log(
                    "Sell",
                    "sellItem",
                    "&eFailed due to insufficient funds"
            );
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_funds", "&cUnable to sell item to shop. Shop has insufficient funds, please try again later", null, true);
            return false;
        }

        // Call event
        SellEvent sellEvent = new SellEvent(player, shop);
        Bukkit.getServer().getPluginManager().callEvent(sellEvent);
        if (sellEvent.isCancelled()) {
            // Sell event was cancelled
            return false;
        }

        // Take item from player
        if (!takeItemFromPlayer(player, shop)) {
            // Unable to take item from player - Nothing to rollback
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to an error taking the item from the player");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            return false;
        }

        // Take cash from shop
        if (!withdrawFundsFromShop(shop, shop.getBuyFromShopPrice())) {
            // Unable to withdraw funds from shop - Give item back to player
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to an error withdrawing funds from the shop");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            // Give the item back to the player
            if (!giveItemToPlayer(player, shop.getItemStack())) {
                instance.getError().save(
                        "001",
                        "ShopManager",
                        "sellToShop",
                        "Unexpected Error",
                        "Unable to return item to player - Player: " + player.getName() + " Item: " + shop.getItemName() + " Qty: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
            return false;
        }

        // Pay player
        if (!payPlayer(offlinePlayer, shop.getSellToShopPrice())) {
            // Unable to pay seller - Give item back to player, refund to shop
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to an error paying the player that is selling to the shop");
            SoundFunctions.playSoundWorld(player.getLocation(), 0.5f, 1.0f, Sound.ENTITY_VILLAGER_NO);
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            // Give the item back to the player
            if (!giveItemToPlayer(player, shop.getItemStack())) {
                instance.getError().save(
                        "002",
                        "ShopManager",
                        "sellToShop",
                        "Unexpected Error",
                        "Unable to return item to player - Player: " + player.getName() + " Item: " + shop.getItemName() + " Qty: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
            if (!payShop(shop, shop.getSellToShopPrice())) {
                instance.getError().save(
                        "003",
                        "ShopManager",
                        "sellToShop",
                        "Unexpected Error",
                        "Unable to refund shop - Location: " +
                                WorldFunctions.simpleReadable(shop.getLocation()) +
                                " Amount: " + shop.getBuyFromShopPrice(),
                        Error.Severity.WARN,
                        null
                );
            }
            return false;
        }

        // Give item to shop
        if (!giveItemToShop(shop, itemInHand, shop.getAmount())) {
            // Unable to give item to shop - Give item back to player, refund to shop, remove funds from player
            instance.getDebug().log(
                    "ShopManager",
                    "sellToShop",
                    "&eFailed due to an error giving the item to the shop");
            instance.getMessage().send(player, "sell_failed_error", "&cAn error occurred.  Unable to sell item to shop", null, true);
            // Give the item back to the player
            if (!giveItemToPlayer(player, shop.getItemStack())) {
                instance.getError().save(
                        "004",
                        "ShopManager",
                        "sellToShop",
                        "Unexpected Error",
                        "Unable to return item to player - Player: " + player.getName() + " Item: " + shop.getItemName() + " Qty: " + shop.getAmount(),
                        Error.Severity.WARN,
                        null
                );
            }
            if (!payShop(shop, shop.getSellToShopPrice())) {
                instance.getError().save(
                        "005",
                        "ShopManager",
                        "sellToShop",
                        "Unexpected Error",
                        "Unable to refund shop - Location: " +
                                WorldFunctions.simpleReadable(shop.getLocation()) +
                                " Amount: " + shop.getBuyFromShopPrice(),
                        Error.Severity.WARN,
                        null
                );
            }
            if (!withdrawFundsFromPlayer(offlinePlayer, shop.getSellToShopPrice())) {
                instance.getError().save(
                        "006",
                        "ShopManager",
                        "sellToShop",
                        "Unexpected Error",
                        "Unable to withdraw funds from player - Player: " + player.getName() + " Amount: " + shop.getBuyFromShopPrice(),
                        Error.Severity.WARN,
                        null
                );
            }
            return false;
        }

        // Sale complete

        if (instance.getBazaarConfig().saveHistory()) {
            TransactionHistory transactionHistory = new TransactionHistory(instance);
            transactionHistory.save(player, shop, ShopAction.SELL_TO_SHOP);
        }

        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("<%cost%>", "" + shop.getAmount());
        placeHolder.put("<%itemname%>", shop.getItemName());
        instance.getMessage().send(player, "sell_success", "&a<%cost%>x <%itemname%> sold to shop", placeHolder, true);
        double newBalance = Dependencies.balancePlayer(offlinePlayer).getBalance();
        HashMap<String, String> placeHolder1 = new HashMap<>();
        placeHolder1.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder1.put("<%balance%>", "" + NumberFunctions.doubleTo2DecimalPlaceString(newBalance));
        instance.getMessage().send(player, "sell_success_balance", "&aNew balance: <%currencysymbol%><%balance%>", placeHolder1, true);

        Map<String, String> placeHolderMap = new HashMap<>();
        placeHolderMap.put("<%amount%>", "" + shop.getAmount());
        placeHolderMap.put("<%itemname%>", shop.getItemName());
        placeHolderMap.put("<%seller%>", player.getName());
        placeHolderMap.put("<%price%>", "" + shop.getSellToShopPrice());
        placeHolderMap.put("<%location%>", WorldFunctions.simpleReadable(shop.getLocation()));
        placeHolderMap.put("<%owner%>", shop.getSellerOfflinePlayer().getName());
        placeHolderMap.put("<%clan%>", shop.getClanTag());

        if (instance.getBazaarConfig().isLogSalesToConsole()) {
            if (shop.playerShop()) {
                Logging.transaction(
                        "console_log_player_shop_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the shop at <%location%> belonging to <%owner%>",
                        placeHolderMap
                );
            } else if (shop.isClanShop()) {
                Logging.transaction(
                        "console_log_player_clan_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the clan shop at <%location%> belonging to <%clan%>",
                        placeHolderMap
                );
            } else if (shop.isAdminShop()) {
                Logging.transaction(
                        "console_log_player_admin_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the admin shop at <%location%>",
                        placeHolderMap
                );
            } else if (shop.isServerShop()) {
                Logging.transaction(
                        "console_log_player_server_sell",
                        "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the server shop at <%location%>",
                        placeHolderMap
                );
            }
        }

        return true;
    }

    /**
     * Attempts to create a new shop
     * @param player Player creating the shop
     * @param creatingShop Shop details
     * @return If true, shop was created successfully
     */
    public boolean create(Player player, CreatingShop creatingShop, Block block) {

        if (player == null) {
            // Player cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "create",
                    "Player cannot be null"
            );
            return false;
        }
        if (creatingShop == null) {
            // CreatingShop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "create",
                    "CreatingShop cannot be null"
            );
            return false;
        }

        // Check Permissions
        if (creatingShop.isAdminShop()) {
            if (!Perms.hasPermission(player, Perms.Type.CREATE_ADMIN)) {
                instance.getMessage().send(player, "create_admin_shop_no_perm", "&cSorry, you do not have permission to create a Bazaar admin shop", null, true);
                return false;
            }
        }
        if (creatingShop.isServerShop()) {
            if (!Perms.hasPermission(player, Perms.Type.CREATE_SERVER)) {
                instance.getMessage().send(player, "create_server_shop_no_perm", "&cSorry, you do not have permission to create a Bazaar server shop", null, true);
                return false;
            }
        }
        if (creatingShop.isClanShop()) {
            if (
                    !Perms.hasPermission(player, Perms.Type.CREATE_CLAN) ||
                    !ClanTasks.getClanTagByUUID(player.getUniqueId()).equalsIgnoreCase(creatingShop.getClanTag())
            ) {
                if (!Perms.hasPermission(player, Perms.Type.CREATE_SERVER)) {
                    instance.getMessage().send(player, "create_clan_shop_no_perm", "&cSorry, you do not have permission to create a Bazaar clan shop", null, true);
                    return false;
                }
            }
        }
        if (creatingShop.isPlayerShop()) {
            if (!Perms.hasPermission(player, Perms.Type.CREATE)) {
                instance.getMessage().send(player, "create_shop_no_perm", "&cSorry, you do not have permission to create a Bazaar shop", null, true);
                return false;
            }
        }

        // Check if there is already a shop here
        Shop existingShop = Bazaar.getInstance().getBazaarData().getShopAtLocation(block.getLocation());
        if (existingShop != null) {
            // Error - shop already exists here
            instance.getDebug().log(
              "ShopManager",
              "create",
              "Shop already exists at " +
                      WorldFunctions.simpleReadable(block.getLocation())
            );
            instance.getMessage().send(player, "shop_create_exists_fail", "&cERROR &f - A shop is already assigned to this container", null, true);
        }

        // Check for Invalid World
        if (instance.getBazaarConfig().invalidWorlds() != null) {
            for (World world : instance.getBazaarConfig().invalidWorlds()) {
                if (world != null) {
                    if (world.getUID().equals(block.getLocation().getWorld().getUID())) {
                        instance.getMessage().send(player, "shop_create_invalid_world", "&cERROR &f - Sorry, you cannot create shops in this World", null, true);
                        instance.getDebug().log("ShopManager", "create", "Cancelled: " + "Shops are not allowed in " + block.getLocation().getWorld().getName());
                        return false;
                    }
                }
            }
        }

        // Check for Invalid Materials
        if (instance.getBazaarConfig().invalidMaterials() != null) {
            for (Material material : instance.getBazaarConfig().invalidMaterials()) {
                if (material != null) {
                    if (material.equals(creatingShop.getFinalItem().clone().getType())) {
                        instance.getMessage().send(player, "shop_create_invalid_material", "&cERROR &f - Sorry, you cannot sell that type of item", null, true);
                        instance.getDebug().log("ShopManager", "create", "Cancelled: " + "Unable to sell " + material.name());
                        return false;
                    }
                }
            }
        }

        // Check if Domain prevents creating shops here
        if (instance.getDependencies().hasDomain()) {
            if (!Domain.allowed(player, block.getLocation(),"PREVENT_BAZAAR_CREATE_BY_NONALLOWED")) {
                instance.getMessage().send(player, "protection_create_shop_deny", "&cSorry, you are not allowed to create shops here", null, true);
                return false;
            }
        }

        if (instance.getBazaarConfig().isBuySellSafeguard()) {
            if (creatingShop.getBuyPrice() > 0 && creatingShop.getSellPrice() > 0) {
                if (creatingShop.getSellPrice() > creatingShop.getBuyPrice()) {
                    instance.getMessage().send(player, "buy_sell_safeguard", "&cSorry, you can't create a shop that sells for more than it buys", null, true);
                    return false;
                }
            }
        }

        // Check WorldGuard
        if (instance.getDependencies().hasWorldGuard()) {
            WorldGuard.WorldGuardResult worldGuardOnlyResult = WorldGuard.allowed(
                    player,
                    block.getLocation(),
                    WorldGuard.CREATE_SHOP
            );
            switch (worldGuardOnlyResult) {
                case ERROR:
                case DENY:
                    instance.getMessage().send(player, "worldguard_create_shop_deny", "&cSorry, you are not allowed to create shops here", null, true);
                    return false;
                case NOT_IN_REGION:
                    if (instance.getBazaarConfig().wgOnlyShops()) {
                        WorldGuard.WorldGuardResult inBypass = WorldGuard.inBypass(player);
                        if (inBypass != WorldGuard.WorldGuardResult.PLAYER_IN_BYPASS) {
                            instance.getMessage().send(player, "worldguard_create_shop_deny", "&cSorry, you are not allowed to create shops here", null, true);
                            return false;
                        }
                    }
                default:
                    break;
            }
        }

        // Check if player is at their maximum number of shops
        if (atMaxShops(player)) {
            instance.getMessage().send(player, "create_shop_limit", "&cSorry, you have reached your limit for Bazaar shops", null, true);
            instance.getDebug().log("ShopManager", "create", "Player has reached their limit of shops");
            return false;
        }

        // Get costs
        int cost = 0;
        int costClan = 0;
        if (instance.getBazaarConfig().costCreate() > 0 && creatingShop.isPlayerShop()) {
            // If not an admin shop or clan shop, get the cost (if > 0)
            cost = Bazaar.getInstance().getBazaarConfig().costCreate();
            instance.getDebug().log("ShopManager", "create", "Cost: " + cost);
        }
        if (instance.getDependencies().hasMyriadClans()) {
            if (Bazaar.getInstance().getBazaarConfig().costCreateClan() > 0 && creatingShop.isClanShop()) {
                // If a clan shop, get the cost to charge the clan (if > 0)
                costClan = Bazaar.getInstance().getBazaarConfig().costCreateClan();
                instance.getDebug().log("ShopManager", "create", "Clan Cost: " + costClan + " Clan Tag: " + creatingShop.getClanTag());
            }
        }

        ItemStack itemStack = creatingShop.getItemStack().clone();
        ItemStack finalItem = creatingShop.getFinalItem();

        String itemName = StringFunctions.toProperCase(com.ibexmc.bazaar.util.functions.BazaarItemFunctions.itemNameFromItemStackClean(creatingShop.getItemStack()));

        if (!creatingShop.getCustomName().isEmpty()) {
            itemName = creatingShop.getCustomName();
        }

        // Create Shop
        Shop shop = new Shop(
                block.getLocation(),
                block.getLocation().getWorld().getName(),
                itemName,
                ItemFunctions.serialize(finalItem),
                "",
                creatingShop.getAmount(),
                creatingShop.getBuyPrice(),
                creatingShop.getSellPrice(),
                creatingShop.getCooldown(),
                player.getUniqueId(),
                creatingShop.isAdminShop(),
                creatingShop.isClanShop(),
                creatingShop.getClanTag(),
                creatingShop.isServerShop()
        );

        // Withdraw funds from Clan
        if (costClan > 0) {
            Double dblCostClan = (double) costClan;
            ClanTasks.ClanTransactionResult clanTransactionResult = ClanTasks.withdrawFundsFromClan(shop.getClanTag(), dblCostClan);
            if (!clanTransactionResult.isSuccess()) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("<%error%>", clanTransactionResult.getReturnText());
                instance.getMessage().send(player, "shop_create_clan_fail", "&cERROR &f - <%error%>", placeHolder, true);
                instance.getHologramManager().removeHologram(shop);
                return false;
            }
        }

        // Withdraw funds from Player
        if (cost > 0) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            Double dblCost = (double) cost;
            VaultTasks.VaultTaskResult vaultTaskResult = VaultTasks.chargeForNewShop(offlinePlayer, dblCost);
            if (!vaultTaskResult.isSuccess()) {
                instance.getHologramManager().removeHologram(shop);
                return false;
            }
        }

        // Add shop to list
        instance.getBazaarData().putShops(shop.getLocation(), shop);

        // Remove shop from creating list
        instance.getBazaarData().removeCreating(player.getUniqueId());

        // Save the shop to file
        new BukkitRunnable() {
            @Override
            public void run() {
                shop.setLoaded(true);

                if (Bazaar.getInstance().getBazaarConfig().useMySQL()) {
                    com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
                    mySQL.createShop(shop);
                } else {
                    FileAccess fileAccess = new FileAccess(Bazaar.getInstance());
                    fileAccess.savePlayerShop(shop);
                }

                instance.getHologramManager().removeHologram(shop);
                instance.getHologramManager().createHologram(shop);

            }

        }.runTaskAsynchronously(Bazaar.getInstance());

        // Send a message to the player
        instance.getMessage().send(player, "shop_create_success", "&aSUCCESS &f- Your shop has been created", null, true);
        Map<String, String> placeHolder = new HashMap<>();
        if (creatingShop.isPlayerShop()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            if (offlinePlayer != null) {
                Dependencies.BalanceResponse balanceResponse = Dependencies.balancePlayer(offlinePlayer);
                if (balanceResponse.isSuccess()) {
                    HashMap<String, String> balPlaceHolder = new HashMap<>();
                    balPlaceHolder.put("<%balance%>", "" + Bazaar.getInstance().getBazaarConfig().currencySymbol() + NumberFunctions.doubleTo2DecimalPlaceString(balanceResponse.getBalance()));
                    balPlaceHolder.put("<%currencysymbol%>", Bazaar.getInstance().getBazaarConfig().currencySymbol());
                    placeHolder.put("<%newbalance%>", instance.getLocale().localeText("new_balance", "New balance: <%balance%>", balPlaceHolder));
                } else {
                    placeHolder.put("<%newbalance%>", "");
                }
            } else {
                placeHolder.put("<%newbalance%>", "");
            }
            placeHolder.put("<%cost%>", Bazaar.getInstance().getBazaarConfig().currencySymbol() + cost);
            instance.getMessage().send(player, "shop_create_success_player_paid", "&eYou were charged <%cost%>.  <%newbalance%>", placeHolder, true);
        }
        if (creatingShop.isClanShop()) {
            double clanBal = ClanTasks.balanceClan(creatingShop.getClanTag());
            if (clanBal >= 0) {
                placeHolder.put("<%cost%>", "" + costClan);
                placeHolder.put("<%newbalance%>", "" + NumberFunctions.doubleTo2DecimalPlaceString(clanBal));
                instance.getMessage().send(player, "shop_create_success_clan_paid", "&eYour clan was charged <%cost%>.  New balance: <%newbalance%>", placeHolder, true);
            }
        }

        return true;
    }

    public boolean cleanup(Shop shop, boolean permanent) {
        if (shop == null) {
            // Shop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "cleanup",
                    "Shop cannot be null"
            );
            return false;
        }
        try {
            RemoveShopEvent removeShopEvent = new RemoveShopEvent(null, shop);
            Bukkit.getServer().getPluginManager().callEvent(removeShopEvent); // Call the RemoveShopEvent event
            if (!removeShopEvent.isCancelled()) {
                instance.getDebug().log("ShopManager", "cleanup", "&7Cleanup - Removing shop");
                instance.getHologramManager().removeHologram(shop);
                instance.getBazaarData().removeDelShop(shop.getSeller());
                Location removeLocation = new Location(shop.getLocation().getWorld(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ());
                instance.getBazaarData().removeShopLocation(removeLocation);
                if (permanent) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (Bazaar.getInstance().getBazaarConfig().useMySQL()) {
                                com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
                                mySQL.deleteShop(shop);
                            } else {
                                FileAccess fileAccess = new FileAccess(Bazaar.getInstance());
                                fileAccess.removePlayerShop(shop);
                            }
                        }
                    }.runTaskAsynchronously(Bazaar.getInstance());
                }
            } else {
                instance.getDebug().log("ShopManager", "cleanup", "&7Cleanup - Unable to remove shop as event was cancelled");
                return false;
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "001",
                    "ShopManager",
                    "cleanup",
                    "Unable to remove shop",
                    "Shop Location: " + WorldFunctions.simpleReadable(shop.getLocation()),
                    Error.Severity.URGENT,
                    ex.getStackTrace()
            );
            return false;
        }
        return true;
    }

    public boolean remove(Player player, Shop shop) {
        return remove(player, shop, true);
    }

    /**
     * Attempts to remove a shop
     * @param player Player removing shop
     * @param shop Shop to remove
     * @param permanent If true, removes the entry permanently
     * @return If true, shop was removed successfully
     */
    public boolean remove(Player player, Shop shop, boolean permanent) {
        if (player == null) {
            // Player cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "remove",
                    "Player cannot be null"
            );
            return false;
        }
        if (shop == null) {
            // Shop cannot be null
            instance.getDebug().log(
                    "ShopManager",
                    "remove",
                    "Shop cannot be null"
            );
            return false;
        }

        boolean noPerm = false;
        if (shop.isAdminShop()) {
            if (!Perms.hasPermission(player, Perms.Type.REMOVE_ADMIN)) {
                noPerm = true;
            }
        }
        if (shop.isServerShop()) {
            if (!Perms.hasPermission(player, Perms.Type.REMOVE_SERVER)) {
                noPerm = true;
            }
        }
        if (!shop.isAdminShop() && !shop.isServerShop()) {
            if (!Perms.hasPermission(player, Perms.Type.REMOVE)) {
                noPerm = true;
            } else {
                if (shop.isClanShop()) {
                    if (!ClanTasks.getClanTagByUUID(player.getUniqueId()).equalsIgnoreCase(shop.getClanTag())) {
                        noPerm = true;
                    }
                } else {
                    if (!shop.getSellerOfflinePlayer().getUniqueId().equals(player.getUniqueId())) {
                        noPerm = true;
                    }
                }
            }
        }
        if (noPerm && Perms.hasPermission(player, Perms.Type.ADMIN)) {
            // Catch-all for admins
            noPerm = true;
        }
        if (noPerm) {
            instance.getMessage().send(player, "create_remove_no_perm", "&cSorry, you do not have permission to remove a Bazaar shop", null, true);
            return false;
        }

        // Check if Domain prevents removing shops here
        if (instance.getDependencies().hasDomain()) {
            if (!Domain.allowed(player, shop.getLocation(),"PREVENT_BAZAAR_REMOVE_BY_NONALLOWED")) {
                instance.getMessage().send(player, "protection_remove_shop_deny", "&cSorry, you are not allowed to remove shops here", null, true);
                return false;
            }
        }

        RemoveShopEvent removeShopEvent = new RemoveShopEvent(player, shop);
        Bukkit.getServer().getPluginManager().callEvent(removeShopEvent); // Call the RemoveShopEvent event
        if (!removeShopEvent.isCancelled()) {
            instance.getDebug().log("ShopManager", "remove", "&7Removing shop");

            instance.getHologramManager().removeHologram(shop);

            Location removeLocation = new Location(shop.getLocation().getWorld(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ());

            instance.getMessage().send(player, "shop_remove_success", "&aSUCCESS &f- Shop has been removed", null, true);
            instance.getBazaarData().removeDelShop(player.getUniqueId());
            instance.getBazaarData().removeShopLocation(removeLocation);

            // Remove shop from list
            final String locationString = WorldFunctions.serializeSimple(removeLocation);

            // Remove the file/db entry
            if (permanent) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Bazaar.getInstance().getBazaarConfig().useMySQL()) {
                            com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
                            mySQL.deleteShop(shop);
                        } else {
                            FileAccess fileAccess = new FileAccess(Bazaar.getInstance());
                            fileAccess.removePlayerShop(shop);
                        }
                    }
                }.runTaskAsynchronously(Bazaar.getInstance());
            }

            return true;
        } else {
            instance.getDebug().log("ShopManager", "remove", "&7Remove Event was cancelled");
            return false;
        }
    }

    /**
     * Checks if the shop has sufficient space to accept the incoming ItemStack amount
     * @param shop Shop to check
     * @param itemStack ItemStack to add
     * @param amount Amount of item
     * @return If true, shop has sufficient space
     */
    private boolean shopHasSpace(Shop shop, ItemStack itemStack, int amount) {
        Block block = shop.getLocation().clone().getBlock();
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.BARREL)) {
            Inventory inventory = getInventory(block);
            // Exit out if the inventory is null
            if (inventory == null) {
                instance.getLog().log("InventoryFunctions", "sellItemToShopCheck", StringFunctions.color("&cUnexpected error - Inventory returned null"));
                return false;
            }
            return InventoryFunctions.hasFreeSpace(inventory, itemStack, amount);
        } else {
            return false;
        }
    }


    public static boolean addItemsToShopInventory(Shop shop, ItemStack itemStack) {
        Block block = shop.getLocation().clone().getBlock();
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.BARREL)) {
            Inventory inventory = getInventory(block);
            // Exit out if the inventory is null
            if (inventory == null) {
                return false;
            }
            if (itemStack == null) {
                return false;
            }
            int amount = itemStack.getAmount(); // Amount to add
            // Loop through the inventory - only looking at slots with items in them
            for (ItemStack inventoryItem : inventory.getStorageContents()) {
                if (inventoryItem != null) {
                    // Not sure if we can have AIR, but if so, it can be considered a free slot
                    if (inventoryItem.getType() == Material.AIR) {
                        break;
                    }
                    if (inventoryItem.getMaxStackSize() > 1) {
                        if (inventoryItem.getType() == itemStack.getType()) {
                            // First check if the base types are the same
                            // Next check that the slot isn't completely full
                            if (inventoryItem.getAmount() < inventoryItem.getMaxStackSize()) {
                                // Clone the items and set their amounts to 1 - they'll then be
                                // serialized and comapared
                                ItemStack invItem1 = inventoryItem.clone();
                                invItem1.setAmount(1);
                                ItemStack itemStack1 = itemStack.clone();
                                itemStack1.setAmount(1);
                                // Serialize
                                if (InventoryFunctions.itemToStringBlob(invItem1).equals(InventoryFunctions.itemToStringBlob(itemStack1))) {
                                    // Item is exactly the same (not including amount)
                                    // Get the free space in this slot
                                    int freeSpace = inventoryItem.getMaxStackSize() - inventoryItem.getAmount();
                                    if (freeSpace >= amount) {
                                        // Free space is more or equal to what we need
                                        inventoryItem.setAmount(inventoryItem.getAmount() + amount);
                                        amount = 0;
                                        break;
                                    } else {
                                        // Free space is not enough to hold the entire item stack
                                        inventoryItem.setAmount(inventoryItem.getMaxStackSize()); // Max out the slot
                                        amount = amount - freeSpace; // Lower the needed amount
                                    }
                                } else {
                                    // Items do not match
                                }
                                invItem1 = null; // cleanup
                                itemStack1 = null;
                            }
                        } else {
                            // Material types do not match
                        }
                    } else {
                        // Max stack size is 1 and there is already an item here
                    }
                }
            } // End of for loop

            // Check if amount is still more than 0
            if (amount > 0) {
                int freeSlot = inventory.firstEmpty(); // Get the first completely empty slot
                itemStack.setAmount(amount); // Set the itemstack to the amount still needed
                if (freeSlot >= 0) {
                    // There is a free slot, set that slot to the remaining amount
                    inventory.setItem(freeSlot, itemStack);
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean addItemToShop(Shop shop, ItemStack itemStack, int amount) {
        Block block = shop.getLocation().clone().getBlock();
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.BARREL)) {
            Inventory inventory = getInventory(block);
            // Exit out if the inventory is null
            if (inventory == null) {
                instance.getLog().log("InventoryFunctions", "sellItemToShopCheck", StringFunctions.color("&cUnexpected error - Inventory returned null"));
                return false;
            }
            ItemStack addItem = itemStack.clone();
            addItem.setAmount(amount);

            // Get a backup of the contents in case we need to rollback
            ItemStack[] rollback = inventory.getContents().clone();

            ItemStack[] changingInventory = inventory.getStorageContents().clone();

            ItemStack[] newInventory = new ItemStack[inventory.getSize()];

            // Remaining is how much we have left while distributing the item
            int remaining = addItem.getAmount();

            // Loop through filled slots, and if there is free space, try to apply the item into it
            for (ItemStack inventoryItem : changingInventory) {
                if (inventoryItem != null) {
                    if (remaining > 0) {
                        ItemStack invItem1 = inventoryItem.clone();
                        invItem1.setAmount(1);
                        ItemStack itemStack1 = addItem.clone();
                        itemStack1.setAmount(1);

                        // Check if the item matches, if so, try and apply some of the item to the slot
                        if (ItemFunctions.serialize(invItem1).equals(ItemFunctions.serialize(itemStack1))) {

                            int freeSpace = inventoryItem.getMaxStackSize() - inventoryItem.getAmount(); // Free space left in slot
                            if (freeSpace > 0) { // If there is no space left in the slot, move on
                                if (remaining > freeSpace) {
                                    // Remaining amount is more than free space - so fill the slot
                                    // and decrement our remaining
                                    inventoryItem.setAmount(inventoryItem.getMaxStackSize());
                                    remaining = remaining - freeSpace;
                                } else {
                                    // Remaining amount is not more than free space, so add the remaining
                                    // to the item, and set remaining amount to 0
                                    inventoryItem.setAmount(inventoryItem.getAmount() + remaining);
                                    remaining = 0;
                                }
                            }
                        }
                    }
                }
            }
            // If we couldn't distribute the entire amount across existing items, apply to any empty slots
            if (remaining > 0) {
                for (ItemStack inventoryItem : changingInventory) {
                    if (remaining > 0) {
                        if (inventoryItem == null) { // A null item has no item in it
                            inventoryItem = itemStack;
                            inventoryItem.setAmount(remaining);
                            remaining = 0;
                        } else { // If we have an item in it, but the item is air, treat it as empty
                            if (inventoryItem.getType() == Material.AIR) {
                                inventoryItem = itemStack;
                                inventoryItem.setAmount(remaining);
                                remaining = 0;
                            }
                        }
                    }
                }
            }

            if (remaining == 0) {
                inventory.setContents(changingInventory);
                return true;
            } else {
                // Rollback the changes
                inventory.setContents(rollback);
                return false;
            }





        } else {
            return false;
        }
    }

    private static @Nullable Inventory getInventory(Block block) {
        BlockState shopState = block.getState();
        Inventory inventory = null;
        // If the shop is a chest, get the inventory from it
        if (shopState instanceof Chest) {
            Chest chestState = (Chest) shopState;
            inventory = chestState.getInventory();
            // If the shop is a double chest, get the full inventory from it
            if (inventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                if (doubleChest != null) {
                    inventory = doubleChest.getInventory();
                }
            }
        }
        // If the shop is a barrel, get the inventory from it
        if (shopState instanceof Barrel) {
            Barrel barrelState = (Barrel) shopState;
            inventory = barrelState.getInventory();
        }
        return inventory;
    }

    /**
     * Checks if the shop has sufficient quantity for a transaction
     * @param shop Shop to check
     * @return If true, shop has sufficient quantity
     */
    private boolean shopHasSufficientQty(Shop shop) {
        if (shop.isAdminShop() || (shop.isServerShop() && Bazaar.getInstance().getBazaarConfig().serverAccountInfiniteStock())) {
            // Admin shops or server shops with infinite stock always have enough
            return true;
        }
        if (InventoryFunctions.checkAvailableQuantity(
                InventoryFunctions.getInventoryFromLocation(
                        shop.getLocation()
                ),
                shop.getItemStack(),
                shop.getAmount()
        )) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the shop can afford to pay the amount
     * @param shop Shop to check
     * @param amount Amount that would need to be paid
     * @return If true, shop can afford it
     */
    private boolean checkShopAfford(Shop shop, double amount) {
        if (shop.isAdminShop()) {
            return true;
        }
        if (shop.isClanShop()) {
            if (ClanTasks.sufficientFundsAvailable(shop.getClanTag(), amount)) {
                return true;
            } else {
                instance.getDebug().log("ShopManager", "checkShopAfford", "&eFailed due to insufficient funds");
                return false;
            }
        }
        // check player or server account
        if (shop.getSellerOfflinePlayer() != null) {
            Dependencies.BalanceResponse balCheck = Dependencies.balancePlayer(shop.getSellerOfflinePlayer());
            if (balCheck.isSuccess()) {
                if (balCheck.getBalance() >= amount) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Takes the item from the player
     * @param player Player to take item from
     * @param shop Shop the sale is for
     * @return If true, item was taken successfully
     */
    private boolean takeItemFromPlayer(Player player, Shop shop) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        try {
            itemInHand.setAmount(itemInHand.getAmount() - shop.getAmount());
            return true;
        } catch (Exception ex) {
            instance.getError().save(
                    "001",
                    "ShopManager",
                    "takeItemFromPlayer",
                    "Unexpected error taking item from player " + player.getName() +
                            " for shop at " + WorldFunctions.simpleReadable(shop.getLocation()),
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
            return false;
        }
    }

    /**
     * Gives the ItemStack to the player
     * @param player Player to give ItemStack to
     * @param itemStack ItemStack to give
     * @return If true, ItemStack was given successfully
     */
    private boolean giveItemToPlayer(Player player, ItemStack itemStack) {
        try {
            InventoryFunctions.addItemsToPlayerInventory(player, itemStack);
            return true;
        } catch (Exception ex) {
            instance.getError().save(
                    "001",
                    "ShopManager",
                    "giveItemToPlayer",
                    "Unexpected error giving item to player",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
            return false;
        }
    }

    /**
     * Charges the owner of the shop the amount specified
     * @param shop Shop to withdraw funds from
     * @param amount Amount to withdraw
     * @return If true, transaction was successful
     */
    private boolean withdrawFundsFromShop(Shop shop, double amount) {
        if (shop.isAdminShop()) {
            // Admin shops don't get charged
            return true;
        }
        // Clan Shops
        if (shop.isClanShop()) {
            ClanTasks.ClanTransactionResult chargeClanTransactionResult = ClanTasks.withdrawFundsFromClan(shop.getClanTag(), shop.getSellToShopPrice());
            if (chargeClanTransactionResult.isSuccess()) {
                instance.getDebug().log("ShopManager", "debitShop", "&7Clan was successfully charged");
                return true;
            } else {
                // error charging clan
                instance.getDebug().log("ShopManager", "debitShop", "&eError charging clan - " + chargeClanTransactionResult.getReturnText());
                return false;
            }
        }
        if (shop.getSellerOfflinePlayer() != null) {
            Dependencies.TransactionResponse chargeTransactionResponse = Dependencies.chargePlayer(shop.getSellerOfflinePlayer(), amount);
            if (chargeTransactionResponse.isSuccess()) {
                instance.getDebug().log("ShopManager", "debitShop", "&7Shop player " + shop.getSellerOfflinePlayer().getName() + " was successfully charged " + amount);
                return true;
            } else {
                instance.getDebug().log("ShopManager", "debitShop", "&eError charging player - " + chargeTransactionResponse.getErrorMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Pays the amount to the shop owner
     * @param shop Shop to pay
     * @param amount Amount to pay
     * @return If true, transaction was successful
     */
    private boolean payShop(Shop shop, double amount) {
        if (shop.isAdminShop()) {
            // Admin shops don't maintain a balance
            return true;
        }
        if (shop.isClanShop()) {
            ClanTasks.ClanTransactionResult chargeClanTransactionResult = ClanTasks.addFundsToClan(shop.getClanTag(), amount);
            if (chargeClanTransactionResult.isSuccess()) {
                instance.getDebug().log("ShopManager", "payShop", "&7Clan was successfully paid");
                return true;
            } else {
                instance.getDebug().log("ShopManager", "payShop", "&eError charging clan - " + chargeClanTransactionResult.getReturnText());
                return false;
            }
        }
        if (shop.getSellerOfflinePlayer() != null) {
            Dependencies.TransactionResponse chargeTransactionResponse = Dependencies.payPlayer(shop.getSellerOfflinePlayer(), amount);
            if (chargeTransactionResponse.isSuccess()) {
                instance.getDebug().log("ShopManager", "payShop", "&7Shop owner was successfully paid");
                return true;
            } else {
                // error charging private seller
                instance.getDebug().log("ShopManager", "payShop", "&eError paying shop owner - " + chargeTransactionResponse.getErrorMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Withdraws funds from a player
     * @param offlinePlayer OfflinePlayer to withdraw funds from
     * @param amount Amount to withdraw
     * @return If true, transaction was successful
     */
    private boolean withdrawFundsFromPlayer(OfflinePlayer offlinePlayer, double amount) {
        Dependencies.TransactionResponse chargeTransactionResponse = Dependencies.chargePlayer(offlinePlayer, amount);
        if (chargeTransactionResponse.isSuccess()) {
            instance.getDebug().log("ShopManager", "withdrawFundsFromPlayer", "&7Seller was successfully charged");
            return true;
        } else {
            instance.getDebug().log("ShopManager", "withdrawFundsFromPlayer", "&eError charging seller - " + chargeTransactionResponse.getErrorMessage());
            return false;
        }
    }

    /**
     * Pays the OfflinePlayer the amount specified
     * @param offlinePlayer OfflinePlayer to pay
     * @param amount Amount to pay
     * @return If true, player was paid successfully
     */
    private boolean payPlayer(OfflinePlayer offlinePlayer, double amount) {
        Dependencies.TransactionResponse chargeTransactionResponse = Dependencies.payPlayer(offlinePlayer, amount);
        if (chargeTransactionResponse.isSuccess()) {
            instance.getDebug().log("ShopManager", "payPlayer", "&7Seller player was successfully paid");
            return true;
        } else {
            // error charging private seller
            instance.getDebug().log("ShopManager", "payPlayer", "&eError paying player - " + chargeTransactionResponse.getErrorMessage());
            return false;
        }
    }

    /**
     * Gives the specified item to the shop
     * @param shop Shop to give item to
     * @param itemStack ItemStack to give
     * @param amount Amount of item to give
     * @return If true, item was given to the shop successfully
     */
    private boolean giveItemToShop(Shop shop, ItemStack itemStack, int amount) {
        if (shop.isAdminShop() || (shop.isServerShop() && Bazaar.getInstance().getBazaarConfig().serverAccountInfiniteStock())) {
            // Admin shops have infinite stock.  Server shops with infinite stock flags have infinite stock, no need to run this code
            return true;
        }
        ShopFunctions.SellResult sellResult = ShopFunctions.sellItemToShop(shop.getLocation(), itemStack, amount);
        if (sellResult.isSuccess()) {
            return true;
        } else {
            instance.getDebug().log("Sell", "sellItem", "&eError adding item to shop inventory - " + sellResult.getReturnMessage());
            return false;
        }
    }

    /**
     * Takes the ItemStack from the shop specified
     * @param shop Shop to take from
     * @param itemStack ItemStack to take
     * @param amount Amount to take
     * @return If true, item was taken successfully
     */
    private boolean takeItemFromShop(Shop shop, ItemStack itemStack, int amount) {
        if (
                shop.isAdminShop() || (
                        shop.isServerShop() && instance.getBazaarConfig().serverAccountInfiniteStock()
                )
        ) {
            return true;
        }
        ShopFunctions.InventoryTransactionResult buyResult = ShopFunctions.takeItemFromShop(shop, false);
        if (buyResult.transactionResult() == ShopTransactionResult.SUCCESS) {
            return true;
        } else {
            instance.getDebug().log(
                    "ShopManager",
                    "takeItemFromShop",
                    "&eError taking item from shop inventory - " +
                            buyResult.getReturnMessage());
            return false;
        }
    }

    /**
     * Checks if the player specified is at the maximum number of shops
     * @param player Player to check
     * @return If true, player is at or beyond maximum shops already
     */
    private boolean atMaxShops(Player player) {
        boolean returnValue = false;
        if (player.isOp()) {
            return false;
        }
        for (PermissionAttachmentInfo perms : player.getEffectivePermissions()) {
            if (perms.getPermission().startsWith("bazaar.limit.")) {
                instance.getDebug().log("Check", "atMaxShops", "Perm found, getting limit");
                int permNum = NumberFunctions.stringToInt((perms.getPermission().split("bazaar.limit.")[1]));
                if (permNum >= 0) {
                    int shopCount = 0;
                    if (instance.getBazaarData().getShops() != null) {
                        for (Map.Entry<Location, Shop> entry : instance.getBazaarData().getShops().entrySet()) {
                            if (!entry.getValue().isAdminShop() && !entry.getValue().isClanShop() && !entry.getValue().isServerShop()) {
                                if (entry.getValue().getSeller().equals(player.getUniqueId())) {
                                    shopCount++;
                                }
                            }
                        }
                    }
                    if (shopCount >= permNum) {
                        instance.getDebug().log("Check", "atMaxShops", "Shop Count = " + shopCount + " Perm Limit = " + permNum);
                        returnValue = true;
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     * Checks if the player owns the shop, or is in the owning clan
     * @param player Player to check
     * @param shop Shop to check
     * @return If true, player is an owner and should be able to open the chest
     */
    private boolean ownsShop(Player player, Shop shop) {
        if (shop.isAdminShop() || shop.isServerShop()) {
            return false;
        }
        if (shop.getSellerOfflinePlayer() != null) {
            if (shop.getSellerOfflinePlayer().getUniqueId().equals(player.getUniqueId())) {
                return true;
            }
            if (shop.isClanShop() && instance.getDependencies().hasMyriadClans()) {
                if (ClanTasks.getClanTagByUUID(player.getUniqueId()).equalsIgnoreCase(shop.getClanTag())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the block is a valid shop block (chest, trapped chest, barrel)
     * @param block Block to check
     * @return If true, block is a valid shop block
     */
    public static boolean isShopBlock(Block block) {
        boolean returnValue = false;
        switch (block.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
            case BARREL:
                returnValue = true;
                break;
            default:
                returnValue = false;
                break;
        }
        return returnValue;
    }

   // public static String getItemNameLine(Shop shop) {
   //             return "" + shop.getAmount() + "x " + shop.getItemName();
   // }

    public static String getItemNameLine(Shop shop) {
        ItemStack itemStack = shop.getItemStack().clone();
        String itemNameClean = ItemFunctions.itemNameFromItemStackClean(itemStack);
        if (Bazaar.getInstance().getBazaarConfig().isUseLocalTranslate())
            itemNameClean = StringFunctions.toProperCase(LocalTranslate.getItemName(itemStack));
        return "" + shop.getAmount() + "x " + itemNameClean;
    }


    public static String getPotionTypeLine(Shop shop) {

        Bazaar instance = Bazaar.getInstance();

        ItemStack itemStack = shop.getItemStack().clone();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            if (itemMeta instanceof PotionMeta) {
                PotionMeta potionMeta = ((PotionMeta) itemMeta).clone();
                String potionTypeName = potionMeta.getAsString().toUpperCase()
                        .replace("{", "")
                        .replace("}", "")
                        .replace("\"", "")
                        .replace("MINECRAFT:", "")
                        .replace("POTION_CONTENTS:", "")
                        .replace("POTION:", "");
                switch (potionTypeName) {
                    case "WATER":
                        return instance.getLocale().localeText("potiontype_WATER", "Water", null);
                    case "MUNDANE":
                        return instance.getLocale().localeText("potiontype_MUNDANE", "Mundane", null);
                    case "THICK":
                        return instance.getLocale().localeText("potiontype_AWKWARD", "Awkward", null);
                    case "AWKWARD":
                        return instance.getLocale().localeText("potiontype_THICK", "Thick", null);
                    case "NIGHT_VISION":
                        return instance.getLocale().localeText("potiontype_NIGHT_VISION", "Night Vision", null);
                    case "LONG_NIGHT_VISION":
                        return instance.getLocale().localeText("potiontype_LONG_NIGHT_VISION", "Night Vision &4[long]&r", null);
                    case "INVISIBILITY":
                        return instance.getLocale().localeText("potiontype_INVISIBILITY", "Invisibility", null);
                    case "LONG_INVISIBILITY":
                        return instance.getLocale().localeText("potiontype_LONG_INVISIBILITY", "Invisibility &4[long]&r", null);
                    case "LEAPING":
                        return instance.getLocale().localeText("potiontype_LEAPING", "Leaping", null);
                    case "LONG_LEAPING":
                        return instance.getLocale().localeText("potiontype_LONG_LEAPING", "Leaping &4[long]&r", null);
                    case "STRONG_LEAPING":
                        return instance.getLocale().localeText("potiontype_STRONG_LEAPING", "Leaping (Jump Boost II)", null);
                    case "FIRE_RESISTANCE":
                        return instance.getLocale().localeText("potiontype_FIRE_RESISTANCE", "Fire Resistance", null);
                    case "LONG_FIRE_RESISTANCE":
                        return instance.getLocale().localeText("potiontype_LONG_FIRE_RESISTANCE", "Fire Resistance &4[long]&r", null);
                    case "SWIFTNESS":
                        return instance.getLocale().localeText("potiontype_SWIFTNESS", "Swiftness (Speed)", null);
                    case "LONG_SWIFTNESS":
                        return instance.getLocale().localeText("potiontype_LONG_SWIFTNESS", "Swiftness (Speed) &4[long]&r", null);
                    case "STRONG_SWIFTNESS":
                        return instance.getLocale().localeText("potiontype_STRONG_SWIFTNESS", "Swiftness (Speed II)", null);
                    case "SLOWNESS":
                        return instance.getLocale().localeText("potiontype_SLOWNESS", "Slowness", null);
                    case "LONG_SLOWNESS":
                        return instance.getLocale().localeText("potiontype_LONG_SLOWNESS", "Slowness &4[long]&r", null);
                    case "STRONG_SLOWNESS":
                        return instance.getLocale().localeText("potiontype_STRONG_SLOWNESS", "Slow (Slowness II)", null);
                    case "WATER_BREATHING":
                        return instance.getLocale().localeText("potiontype_WATER_BREATHING", "Water Breathing", null);
                    case "LONG_WATER_BREATHING":
                        return instance.getLocale().localeText("potiontype_LONG_WATER_BREATHING", "Water Breathing &4[long]&r", null);
                    case "HEALING":
                        return instance.getLocale().localeText("potiontype_HEALING", "Healing (Instant Health)", null);
                    case "STRONG_HEALING":
                        return instance.getLocale().localeText("potiontype_STRONG_HEALING", "Healing (Instant Health II)", null);
                    case "HARMING":
                        return instance.getLocale().localeText("potiontype_HARMING", "Harming (Instant Damage)", null);
                    case "STRONG_HARMING":
                        return instance.getLocale().localeText("potiontype_STRONG_HARMING", "Harming (Instant Damage II)", null);
                    case "POISON":
                        return instance.getLocale().localeText("potiontype_POISON", "Poison", null);
                    case "LONG_POISON":
                        return instance.getLocale().localeText("potiontype_LONG_POISON", "Poison &4[long]&r", null);
                    case "STRONG_POISON":
                        return instance.getLocale().localeText("potiontype_STRONG_POISON", "Poison (Poison II)", null);
                    case "REGENERATION":
                        return instance.getLocale().localeText("potiontype_REGENERATION", "Regeneration", null);
                    case "LONG_REGENERATION":
                        return instance.getLocale().localeText("potiontype_LONG_REGENERATION", "Regeneration &4[long]&r", null);
                    case "STRONG_REGENERATION":
                        return instance.getLocale().localeText("potiontype_STRONG_REGENERATION", "Regeneration (Regeneration II)", null);
                    case "STRENGTH":
                        return instance.getLocale().localeText("potiontype_STRENGTH", "Strength", null);
                    case "LONG_STRENGTH":
                        return instance.getLocale().localeText("potiontype_LONG_STRENGTH", "Strength &4[long]&r", null);
                    case "STRONG_STRENGTH":
                        return instance.getLocale().localeText("potiontype_STRONG_STRENGTH", "Strength (Strength II)", null);
                    case "WEAKNESS":
                        return instance.getLocale().localeText("potiontype_WEAKNESS", "Weakness", null);
                    case "LONG_WEAKNESS":
                        return instance.getLocale().localeText("potiontype_LONG_WEAKNESS", "Weakness &4[long]&r", null);
                    case "LUCK":
                        return instance.getLocale().localeText("potiontype_LUCK", "Luck", null);
                    case "TURTLE_MASTER":
                        return instance.getLocale().localeText("potiontype_TURTLE_MASTER", "Turtle Master (Slowness IV Resistance III)", null);
                    case "LONG_TURTLE_MASTER":
                        return instance.getLocale().localeText("potiontype_LONG_TURTLE_MASTER", "Turtle Master (Slowness IV Resistance III &4[long]&r)", null);
                    case "STRONG_TURTLE_MASTER":
                        return instance.getLocale().localeText("potiontype_STRONG_TURTLE_MASTER", "Turtle Master (Slowness VI Resistance IV)", null);
                    case "SLOW_FALLING":
                        return instance.getLocale().localeText("potiontype_SLOW_FALLING", "Slow Falling", null);
                    case "LONG_SLOW_FALLING":
                        return instance.getLocale().localeText("potiontype_LONG_SLOW_FALLING", "Slow Falling &4[long]&r", null);
                    case "WIND_CHARGED":
                        return instance.getLocale().localeText("potiontype_WIND_CHARGED", "Wind Charged", null);
                    case "WEAVING":
                        return instance.getLocale().localeText("potiontype_WEAVING", "Weaving", null);
                    case "OOZING":
                        return instance.getLocale().localeText("potiontype_OOZING", "Oozing", null);
                    case "INFESTED":
                        return instance.getLocale().localeText("potiontype_INFESTED", "Infested", null);
                    default:
                        return "";
                }
            }
        }
        return "";
    }
    public static String getEnchantmentLine(Shop shop) {
        Bazaar instance = Bazaar.getInstance();
        ItemStack itemStack = shop.getItemStack().clone();
        String enchantLine = "";
        if (itemStack.getEnchantments() != null) {
            if (itemStack.getEnchantments().size() > 0) {
                Map<String, String> placeHolder = new HashMap<>();
                placeHolder.put("<%enchants%>", "" + itemStack.getEnchantments().size());
                if (itemStack.getEnchantments().size() > 1) {
                    enchantLine = instance.getLocale().localeText("enchant_count", "&e<%enchants%> Enchantments", placeHolder);
                } else {
                    for (Map.Entry<Enchantment, Integer> enchantment  : itemStack.getEnchantments().entrySet()) {
                        String enchantmentName = StringFunctions.capitalizeString(enchantment.getKey().getKey().toString().replace("_", " ").replace("minecraft:", ""));

                        if (Bazaar.getInstance().getBazaarConfig().isUseLocalTranslate()) {
                            enchantmentName = StringFunctions.toProperCase(LocalTranslate.getEnchantmentName(enchantment.getKey()));
                        }

                        int enchantmentLevel = enchantment.getValue();
                        enchantLine = StringFunctions.color("&e" + enchantmentName + " " + NumberFunctions.numberToRoman(enchantmentLevel));
                        placeHolder.put("<%enchantment_name%>", enchantLine);
                        break;
                    }
                    enchantLine = instance.getLocale().localeText("enchant_single", "&e<%enchantment_name%>", placeHolder);
                }
            }
        }
        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
            if (meta.hasStoredEnchants()) {
                if (meta.getStoredEnchants().size() == 1) {
                    for (Map.Entry<Enchantment, Integer> enchantment : meta.getStoredEnchants().entrySet()) {
                        String enchantmentName = StringFunctions.capitalizeString(enchantment.getKey().getKey().toString().replace("_", " ").replace("minecraft:", ""));
                        if (Bazaar.getInstance().getBazaarConfig().isUseLocalTranslate()) {
                            enchantmentName = StringFunctions.toProperCase(LocalTranslate.getEnchantmentName(enchantment.getKey()));
                        }
                        int enchantmentLevel = enchantment.getValue();
                        enchantLine = StringFunctions.color("&e" + enchantmentName + " " + NumberFunctions.numberToRoman(enchantmentLevel));
                        break;
                    }
                } else {
                    Map<String, String> placeHolder = new HashMap<>();
                    placeHolder.put("<%enchants%>", "" + meta.getStoredEnchants().size());
                    enchantLine = instance.getLocale().localeText("enchant_count", "&e<%enchants%> Enchantments", placeHolder);
                }
            }
        }
        return enchantLine;
    }
    public static String getPriceLine(Shop shop) {
        Bazaar instance = Bazaar.getInstance();
        String buySellLine = "";
        String buyFromShopPrice = "" + shop.getBuyFromShopPrice();
        String sellToShopPrice = "" + shop.getSellToShopPrice();
        Map<String, String> placeHolder = new HashMap<>();
        placeHolder.put("%currency_symbol%", Bazaar.getInstance().getBazaarConfig().currencySymbol());
        placeHolder.put("%from_shop_price%", buyFromShopPrice.replace(".0", ""));
        placeHolder.put("%to_shop_price%", sellToShopPrice.replace(".0", ""));
        if (shop.getBuyFromShopPrice() > 0) {
            buySellLine = buySellLine + instance.getLocale().localeText("buy_from_shop", "&cⒷ %currency_symbol%%from_shop_price%", placeHolder);
        }
        if (shop.getBuyFromShopPrice() > 0 && shop.getSellToShopPrice() > 0) {
            buySellLine = buySellLine + " | ";
        }
        if (shop.getSellToShopPrice() > 0) {
            buySellLine = buySellLine + instance.getLocale().localeText("sell_to_shop", "&aⓈ %currency_symbol%%to_shop_price%", placeHolder);
        }
        return buySellLine;
    }
}
