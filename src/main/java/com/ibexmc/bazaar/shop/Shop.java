package com.ibexmc.bazaar.shop;


import com.ibexmc.internal.functions.*;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ibexmc.bazaar.Bazaar;
import java.util.ArrayList;

import java.util.UUID;

public class Shop {

    private Bazaar instance;
    private Location location;
    private String worldName;
    private Location hologramLocation;
    private ArrayList<String> lines;
    private String itemName;
    private String itemText;
    private String itemDescriptionLine;
    private int amount;
    private double buyFromShopPrice;
    private double sellToShopPrice;
    private int cooldown;
    private UUID seller;
    private boolean adminShop;
    private boolean clanShop;
    private String clanTag;
    private boolean serverShop;
    private ItemStack itemStack;
    private boolean isLoaded = false;

    public Shop(Location location, String worldName, String itemName, String itemText, String itemDescriptionLine,
                int amount, double buyPrice, double sellPrice, int cooldown, UUID seller,
                boolean adminShop, boolean clanShop, String clanTag, boolean serverShop) {

        instance = Bazaar.getInstance();
        instance.getDebug().log("Shop", "Constructor", "Initializing shop");
        if (location == null) {
            return;
        }
        if (buyPrice < 1 && sellPrice < 1) {
            return;
        }
        if (amount < 1) {
            return;
        }

        this.location = location;
        this.worldName = worldName; //TODO: generated
        this.itemName = itemName;
        this.itemText = itemText;
        this.itemDescriptionLine = itemDescriptionLine;
        this.amount = amount;
        this.buyFromShopPrice = buyPrice;
        this.sellToShopPrice = sellPrice;
        this.cooldown = cooldown;
        this.seller = seller;
        this.adminShop = adminShop;
        this.clanShop = clanShop;
        this.clanTag = clanTag;
        this.serverShop = serverShop;

        ItemStack itemStack = ItemFunctions.deserialize(itemText);
        if (itemStack != null) {
            itemStack.setAmount(this.amount);

            // Set the ItemStack so we can grab it easily
            this.itemStack = itemStack;
        } else {
            instance.getDebug().log("Shop", "Constructor", "Itemstack is null");
            return;
        }
    }

    /**
     * Gets the serialized location as a name for the shop
     * If the location hasn't been defined yet, returns a random UUID
     * @return Shop Internal Name
     */
    public String getName() {
        if (location != null) {
            return WorldFunctions.serializeSimple(location).replace(":", "").replace("-", "");
        } else {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Gets the ItemStack for the shop item
     * @return Shop Item ItemStack
     */
    public ItemStack getItemStack() {
        ItemStack returnItemStack = itemStack.clone();
        instance.getDebug().log("Shop", "getItemStack", "ItemStack cloned");
        returnItemStack.setAmount(this.amount);
        instance.getDebug().log("Shop", "getItemStack", "ItemStack amount set to " + returnItemStack.getAmount());
        instance.getDebug().log("Shop", "getItemStack", "ItemStack = " + returnItemStack.getAmount() + "x " + returnItemStack.getType().name());
        return returnItemStack;
    }

    /**
     * Gets the shop location
     * @return Shop Location
     */
    public Location getLocation() {
        Location returnLocation = this.location.clone();
        if (returnLocation != null) {
            World world = Bukkit.getWorld(this.worldName);
            if (world != null) {
                if (location.getWorld().getName() == null) {
                    returnLocation = new Location(world, this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
                }
            } else {
                return null;
            }
        }
        return returnLocation;
    }

    /**
     * Sets the shop location
     * @param location Location to set
     */
    public void setLocation(Location location) {
        this.location = location;
        if (location != null) {
            this.worldName = location.getWorld().getName();
        }
    }

    /**
     * Gets the world name of the shop
     * @return World name of the shop
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Sets the world name of the shop
     * @param worldName World Name for the shop
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    /**
     * Gets the hologram location for the shop
     * @return Hologram location
     */
    public Location getHologramLocation() {
        return hologramLocation;
    }

    /**
     * Sets the hologram location for the shop
     * @param hologramLocation
     */
    public void setHologramLocation(Location hologramLocation) {
        this.hologramLocation = hologramLocation;
    }

    /**
     * Gets the text for the hologram
     * @return List of lines for hologram
     */
    public ArrayList<String> getLines() {
        return lines;
    }

    /**
     * Sets the lines for the hologram
     * @param lines List of lines to set
     */
    public void setLines(ArrayList<String> lines) {
        this.lines = lines;
    }

    /**
     * Gets the name of the item being sold in the shop
     * @return Item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Sets the name of the item being sold in the shop
     * @param itemName Item name
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Gets the serialized text of the item being sold
     * @return Serialized item text
     */
    public String getItemText() {
        return itemText;
    }

    /**
     * Sets the serialized text of the item being sold
     * @param itemText Serialized item text to set
     */
    public void setItemText(String itemText) {
        this.itemText = itemText;
    }

    /**
     * Gets the item description line - not currently in use
     * @return Item description line
     */
    public String getItemDescriptionLine() {
        return itemDescriptionLine;
    }

    /**
     * Sets the item description line - not currently in use
     * @param itemDescriptionLine Item description line to set
     */
    public void setItemDescriptionLine(String itemDescriptionLine) {
        this.itemDescriptionLine = itemDescriptionLine;
    }

    /**
     * Gets the amount of the item being sold
     * @return Item amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the item being sold
     * @param amount Item amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the buy price of the item being sold
     * @return Buy price
     */
    public double getBuyFromShopPrice() {
        return buyFromShopPrice;
    }

    /**
     * Sets the buy price of the item being sold
     * @param buyPrice Buy price to set
     */
    public void setBuyFromShopPrice(int buyPrice) {
        this.buyFromShopPrice = buyPrice;
    }

    /**
     * Gets the sell price of the item being sold
     * @return Sell price
     */
    public double getSellToShopPrice() {
        return sellToShopPrice;
    }

    /**
     * Sets the sell price of the item being sold
     * @param sellPrice Sell price to set
     */
    public void setSellToShopPrice(int sellPrice) {
        this.sellToShopPrice = sellPrice;
    }

    /**
     * Gets the number of seconds in the cooldown period for the shop
     * @return
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * Sets the number of seconds in the cooldown period for the shop
     * @param cooldown
     */
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * Gets the unique identifier of the seller
     * @return Sellers unique identifier
     */
    public UUID getSeller() {
        return seller;
    }

    /**
     * Sets the unique identifier of the seller
     * @param seller Seller unique identifier to set
     */
    public void setSeller(UUID seller) {
        this.seller = seller;
    }

    /**
     * Gets the offline player of the seller if it exists, if not, returns null
     * @return
     */
    public OfflinePlayer getSellerOfflinePlayer() {
        if (serverShop) {
            return Bazaar.getInstance().getBazaarConfig().serverAccount();
        } else {
            if (this.seller != null) {
                return Bukkit.getOfflinePlayer(this.seller);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets if it is an admin shop
     * @return If true, this is an admin shop
     */
    public boolean isAdminShop() {
        return adminShop;
    }

    /**
     * Sets if it is an admin shop
     * @param adminShop If true, will set to an admin shop
     */
    public void setAdminShop(boolean adminShop) {
        this.adminShop = adminShop;
    }

    /**
     * Gets if it is a clan shop
     * @return If true, this is a clan shop
     */
    public boolean isClanShop() {
        return clanShop;
    }

    /**
     * Sets if it is a clan shop
     * @param clanShop If true, will set to a clan shop
     */
    public void setClanShop(boolean clanShop) {
        this.clanShop = clanShop;
    }

    /**
     * Gets the clan tag associated with the shop
     * @return Clan tag
     */
    public String getClanTag() {
        return clanTag;
    }

    /**
     * Sets the clan tag associated with the shop
     * @param clanTag Clan tag to set
     */
    public void setClanTag(String clanTag) {
        this.clanTag = clanTag;
    }

    /**
     * Gets if this is a server shop
     * @return If true, this is a server shop
     */
    public boolean isServerShop() { return serverShop; }

    public void setServerShop(boolean serverShop) {
        this.serverShop = serverShop;
    }

    /**
     * Gets if this is a player shop (player or clan)
     * @return If true, this is a player or clan shop
     */
    public boolean playerShop() {
        if (!adminShop && !serverShop) {
            return true;
        }
        return false;
    }

    public boolean hasStock() {
        if (this.location != null) {
            Inventory shopInventory = InventoryFunctions.getInventoryFromLocation(this.location);
            if (shopInventory != null) {
                if (InventoryFunctions.checkAvailableQuantity(shopInventory, this.getItemStack(), this.getAmount())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the inventory associated with this shop
     * @return Shop inventory
     */
    public Inventory inventory() {
        return InventoryFunctions.getInventoryFromLocation(this.getLocation());
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }


}
