package com.ibexmc.bazaar.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CreatingShop {

    private ItemStack itemStack = new ItemStack(Material.AIR, 1);
    private int amount = 0;
    private double buyPrice = 0;
    private double sellPrice = 0;
    private boolean adminShop = false;
    private boolean serverShop = false;
    private boolean clanShop = false;
    private String clanTag = "";
    private int cooldown = 0;
    private String customName = "";

    public CreatingShop(ItemStack itemStack, int amount, double buyPrice, double sellPrice, boolean adminShop, boolean serverShop, String clanTag, int cooldown, String customName) {
        this.itemStack = itemStack;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.adminShop = adminShop;
        this.serverShop = serverShop;
        if (!clanTag.equalsIgnoreCase("")) {
            this.clanShop = true;
            this.clanTag = clanTag;
        }
        this.cooldown = cooldown;
        this.customName = customName;
    }

    /**
     * Gets the ItemStack associated with this shop
     * @return
     */
    public ItemStack getItemStack() { return this.itemStack; }

    /**
     * Gets the Amount for the ItemStack
     * @return ItemStack amount
     */
    public int getAmount() { return this.amount; }

    /**
     * Gets the Buy Price
     * @return Buy Price
     */
    public double getBuyPrice() { return this.buyPrice; }

    /**
     * Gets the Sell Price
     * @return Sell Price
     */
    public double getSellPrice() { return this.sellPrice; }

    /**
     * Gets if this is an admin shop
     * @return If true, this is an admin shop
     */
    public boolean isAdminShop() { return this.adminShop; }

    /**
     * Gets if this is a server shop
     * @return If true, this is a server shop
     */
    public boolean isServerShop() { return this.serverShop; }

    /**
     * Gets if this is a Clan shop
     * @return If true, this is a clan shop
     */
    public boolean isClanShop() { return this.clanShop; }

    /**
     * Gets if this is a Player shop (not an admin, server, or clan shop)
     * @return If true, this is a player shop
     */
    public boolean isPlayerShop() {
        return (!this.adminShop && !this.serverShop && this.clanShop);
    }

    /**
     * Gets the Clan Tag
     * @return Clan tag
     */
    public String getClanTag() { return this.clanTag; }

    /**
     * Gets the cooldown time in seconds
     * @return Cooldown time
     */
    public int getCooldown() { return this.cooldown; }

    /**
     * Gets the ItemStack associated with this shop, with the correct amount set
     * @return Shop ItemStack
     */
    public ItemStack getFinalItem() {
        ItemStack finalItem = this.itemStack.clone();
        finalItem.setAmount(1);
        return finalItem;
    }

    public String getCustomName() {
        return customName;
    }
}
