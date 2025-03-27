package com.ibexmc.bazaar.shop;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.functions.ItemFunctions;
import com.ibexmc.internal.functions.InventoryFunctions;
import com.ibexmc.internal.functions.StringFunctions;
import com.ibexmc.internal.functions.WorldFunctions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopFunctions {

    /**
     * Checks if the ItemStack can be sold to the shop at the location specified
     * @param shopLocation Shop location
     * @param sellingItemStack ItemStack being sold to the shop
     * @param sellingAmount Amount that would be sold to the shop
     * @return If amount can be sold to the shop, returns SellResult.isSuccess = true
     */
    public static SellResult sellItemToShopCheck(Location shopLocation, ItemStack sellingItemStack, int sellingAmount) {
        Bazaar instance = Bazaar.getInstance();
        if (sellingItemStack == null) {
            return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
        }

        String sellingItemStackString = ItemFunctions.serialize(sellingItemStack);

        Block block = shopLocation.getBlock();

        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.BARREL)) {
            BlockState shopState = block.getState();
            Inventory inventory = null;
            // If the shop is a chest, get the inventory from it
            if (shopState instanceof Chest) {
                Chest chestState = (Chest) shopState;
                inventory = chestState.getInventory();
                // If the shop is a double chest, get the full inventory from it
                if (inventory instanceof DoubleChestInventory) {
                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                    inventory = doubleChest.getInventory();
                }
            }
            // If the shop is a barrel, get the inventory from it
            if (shopState instanceof Barrel) {
                Barrel barrelState = (Barrel) shopState;
                inventory = barrelState.getInventory();
            }
            // Exit out if the inventory is null
            if (inventory == null) {
                instance.getLog().log("InventoryFunctions", "sellItemToShopCheck", StringFunctions.color("&cUnexpected error - Inventory returned null"));
                return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
            }
            // Create a new array list for the items to go into
            List<ItemStack> newItems = new ArrayList<>();
            // Convert the inventory into an array
            ItemStack[] items = inventory.getContents();
            // Check our array isn't null
            if (items != null) {
                // Loop through each ItemStack in the array
                int itemsArrayPosition = 0;
                for (ItemStack itemStack : items) {

                    // We'll be using cloned items so we don't overwrite any of our originals
                    ItemStack clonedItem = null;
                    int clonedAmt = 1;

                    // Check if the itemstack is null (if so, its a blank slot)
                    if (itemStack == null) {
                        // A blank slot can be used for our item.  Update it, but set amount = 0
                        itemStack = sellingItemStack.clone();
                        itemStack.setAmount(sellingAmount);
                        sellingAmount = 0;
                    }

                    // Set the cloned item = our itemStack with amount = 1 (so when we convert to string blob, it matches)
                    clonedItem = itemStack.clone();
                    clonedItem.setAmount(1);
                    String clonedString = ItemFunctions.serialize(clonedItem);
                    clonedAmt = itemStack.getAmount();

                    // Check to make sure we still have some items to add to the shop
                    if (sellingAmount > 0) {
                        // Check our string blob against the item we're selling so we know we're not combining invalid items
                        if (sellingItemStackString.equals(clonedString)) {
                            // Check that we're not trying to add to an already full stack
                            if (clonedAmt != clonedItem.getMaxStackSize()) {
                                // Get the amount of free space left in this slot
                                int freeSpaceInSlot = clonedItem.getMaxStackSize() - clonedAmt;
                                // Check if the free space is in the slot is more or equal to the amount we have left to sell to the shop
                                if (freeSpaceInSlot >= sellingAmount) {
                                    // We have enough space to take the rest of the selling item.  Set the amount on the itemStack and clear our selling amount
                                    clonedAmt = clonedAmt + sellingAmount;
                                    sellingAmount = 0;
                                } else {
                                    // There isn't enough space for all of our selling item, so add as much as the slot will take and modify our selling amount
                                    clonedAmt = clonedAmt + freeSpaceInSlot;
                                    sellingAmount = sellingAmount - freeSpaceInSlot;
                                }
                            }
                        }
                        itemsArrayPosition++;
                    }
                    // Set the itemStack amount
                    clonedItem.setAmount(clonedAmt);

                    if (clonedAmt == 0) {
                        // If the amount is 0, it means it was and still is a blank slot, change back to air
                        clonedItem = new ItemStack(Material.AIR);
                    }
                    // Add the cloned item to the arraylist
                    newItems.add(clonedItem);

                }
                // Check if we have any items left to sell to the shop, if so, there was not enough space
                // if it is 0, then we had space for all of our items
                if (sellingAmount < 1) {
                    return new SellResult(true, "");
                } else {
                    return new SellResult(false, instance.getLocale().localeText("sell_to_shop_insufficient_space", "&cUnable to sell to shop - Insufficient space left in shop", null));
                }
            } else {
                instance.getLog().log("InventoryFunctions", "sellItemToShop", StringFunctions.color("&cUnexpected error - Items array returned null"));
                return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
            }
        } else {
            return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
        }
    }

    /**
     * Transfers the ItemStack that is being sold to the shop at the location specified
     * @param shopLocation Shop location
     * @param sellingItemStack ItemStack being sold to the shop
     * @param sellingAmount Amount that is being sold to the shop
     * @return If amount is sold to the shop successfully, returns SellResult.isSuccess = true
     */
    public static SellResult sellItemToShop(Location shopLocation, ItemStack sellingItemStack, int sellingAmount) {
        Bazaar instance = Bazaar.getInstance();
        if (sellingItemStack == null) {
            return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
        }
        if (sellingItemStack == null) {
            return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
        }
        String sellingItemStackString = ItemFunctions.serialize(sellingItemStack);
        Block block = shopLocation.getBlock();
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.BARREL)) {
            BlockState shopState = block.getState();
            Inventory inventory = null;
            // If the shop is a chest, get the inventory from it
            if (shopState instanceof Chest) {
                Chest chestState = (Chest) shopState;
                inventory = chestState.getInventory();
                // If the shop is a double chest, get the full inventory from it
                if (inventory instanceof DoubleChestInventory) {
                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                    inventory = doubleChest.getInventory();
                }
            }
            // If the shop is a barrel, get the inventory from it
            if (shopState instanceof Barrel) {
                Barrel barrelState = (Barrel) shopState;
                inventory = barrelState.getInventory();
            }
            // Exit out if the inventory is null
            if (inventory == null) {
                instance.getLog().log("InventoryFunctions", "sellItemToShop", StringFunctions.color("&cUnexpected error - Inventory returned null"));
                return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
            }
            // Create a new array list for the items to go into
            List<ItemStack> newItems = new ArrayList<>();
            // Convert the inventory into an array
            ItemStack[] items = inventory.getContents();
            // Check our array isn't null
            if (items != null) {
                // Loop through each ItemStack in the array
                int itemsArrayPosition = 0;
                for (ItemStack itemStack : items) {

                    // We'll be using cloned items so we don't overwrite any of our originals
                    ItemStack clonedItem = null;
                    int clonedAmt = 1;

                    // Check if the itemstack is null (if so, its a blank slot)
                    if (itemStack == null) {
                        // A blank slot can be used for our item.  Update it, but set amount = 0
                        itemStack = sellingItemStack.clone();
                        itemStack.setAmount(sellingAmount);
                        sellingAmount = 0;
                    }

                    // Set the cloned item = our itemStack with amount = 1 (so when we convert to string blob, it matches)
                    clonedItem = itemStack.clone();
                    clonedItem.setAmount(1);
                    String clonedString = ItemFunctions.serialize(clonedItem);
                    clonedAmt = itemStack.getAmount();

                    // Check to make sure we still have some items to add to the shop
                    if (sellingAmount > 0) {
                        // Check our string blob against the item we're selling so we know we're not combining invalid items
                        if (sellingItemStackString.equals(clonedString)) {
                            // Check that we're not trying to add to an already full stack
                            if (clonedAmt != clonedItem.getMaxStackSize()) {
                                // Get the amount of free space left in this slot
                                int freeSpaceInSlot = clonedItem.getMaxStackSize() - clonedAmt;
                                // Check if the free space is in the slot is more or equal to the amount we have left to sell to the shop
                                if (freeSpaceInSlot >= sellingAmount) {
                                    // We have enough space to take the rest of the selling item.  Set the amount on the itemStack and clear our selling amount
                                    clonedAmt = clonedAmt + sellingAmount;
                                    sellingAmount = 0;
                                } else {
                                    // There isn't enough space for all of our selling item, so add as much as the slot will take and modify our selling amount
                                    clonedAmt = clonedAmt + freeSpaceInSlot;
                                    sellingAmount = sellingAmount - freeSpaceInSlot;
                                }
                            }
                        }
                        itemsArrayPosition++;
                    }

                    // Set the itemStack amount
                    clonedItem.setAmount(clonedAmt);

                    if (clonedAmt == 0) {
                        // If the amount is 0, it means it was and still is a blank slot, change back to air
                        clonedItem = new ItemStack(Material.AIR);
                    }
                    // Add the cloned item to the arraylist
                    newItems.add(clonedItem);

                }
                // Check if we have any items left to sell to the shop, if so, there was not enough space
                // if it is 0, then we had space for all of our items
                if (sellingAmount < 1) {
                    // Convert the arraylist back into an array
                    ItemStack[] newItemsArray = newItems.toArray(new ItemStack[newItems.size()]);

                    // Clear the inventory
                    inventory.clear();

                    if (inventory instanceof Chest) {
                        Chest chestState  = (Chest) shopState;
                        chestState.getInventory().clear();
                    }
                    if (inventory instanceof DoubleChestInventory) {

                    }
                    if (shopState instanceof Barrel) {
                        Barrel barrelState = (Barrel) shopState;
                        inventory = barrelState.getInventory();
                    }
                    int itemArrayPosition = 0;
                    for (ItemStack itemStackNew : newItemsArray) {
                        if (itemStackNew != null) {
                            inventory.setItem(itemArrayPosition, itemStackNew);
                        }
                        itemArrayPosition++;
                    }
                    return new SellResult(true, "");

                } else {
                    return new SellResult(false, instance.getLocale().localeText("sell_to_shop_insufficient_space", "&cUnable to sell to shop - Insuffficient space left in shop", null));
                }
            } else {
                instance.getLog().log("InventoryFunctions", "sellItemToShop", StringFunctions.color("&cUnexpected error - Items array returned null"));
                return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
            }
        } else {
            return new SellResult(false, instance.getLocale().localeText("unexpected_error", "&cUnexpected error.  Please contact an admin for help", null));
        }
    }

    /**
     * Attempts to take one transaction worth of item from the shop
     * If checkOnly = true, then it will return only if it CAN take the
     * item.  If checkOnly = false, it will decrement the shop inventory
     * @param shop Shop to take inventory from
     * @param checkOnly If true, only checks if taking items is possible (sufficient quantity)
     * @return If InventoryTransactionResult.SUCCESS, item was taken successfully, or, a sufficient quantity exists
     */
    public static InventoryTransactionResult takeItemFromShop(Shop shop, boolean checkOnly) {

        try {
            if (shop == null) {
                return new InventoryTransactionResult(ShopTransactionResult.ERROR, "Shop is null");
            }

            if (shop.isAdminShop() || (shop.isServerShop() && Bazaar.getInstance().getBazaarConfig().serverAccountInfiniteStock())) {
                return new InventoryTransactionResult(ShopTransactionResult.SUCCESS, "Sufficient stock available");
            }

            Inventory inventory = shop.inventory();

            if (inventory == null) {
                return new InventoryTransactionResult(ShopTransactionResult.ERROR, "Unable to get shop inventory");
            }

            // Create a new array list for the items to go into
            List<InventoryItems> newItems = new ArrayList<>();

            // Convert the inventory into an array
            ItemStack[] items = inventory.getContents();

            // Add all the items that match what is being sold to a list
            int currentSlot = 0;
            if (items != null) {
                for (ItemStack itemStack : items) {
                    if (itemStack == null) {
                        // If null, do not add
                        currentSlot++;
                    } else {
                        if (InventoryFunctions.compareItems(itemStack, shop.getItemStack(), true, false)) {
                            newItems.add(new InventoryItems(currentSlot, itemStack.clone()));

                        }
                        currentSlot++;
                    }
                }
            }

            // Loop through the list and add up total amount
            int runningCount = 0;
            for (InventoryItems inventoryItem : newItems) {
                runningCount = runningCount + inventoryItem.item().getAmount();
            }
            int remaining = shop.getAmount();
            if (runningCount >= shop.getAmount()) {
                // Sufficient stock found
                for (InventoryItems inventoryItem : newItems) {
                    if (remaining > 0) {
                        if (remaining > inventoryItem.item.getAmount()) {
                            // We have more remaining than this item would give us.  Decrement the remaining, set the item in inventory to air
                            if (!checkOnly) {
                                inventory.setItem(inventoryItem.slot(), new ItemStack(Material.AIR, 1));
                            }
                            remaining = remaining - inventoryItem.item.getAmount();
                        } else {
                            if (remaining == inventoryItem.item.getAmount()) {
                                // The amount remaining is exactly what this would give us. Set remaining to 0 and remove the item
                                if (!checkOnly) {
                                    inventory.setItem(inventoryItem.slot(), new ItemStack(Material.AIR, 1));
                                }
                                remaining = 0;
                            } else {
                                if (remaining < inventoryItem.item.getAmount()) {
                                    // The amount remaining is less than what this would give us.  Set the remaining to 0 and decrement the item by remaining
                                    ItemStack newAmountItem = inventoryItem.item().clone();
                                    newAmountItem.setAmount(newAmountItem.getAmount() - remaining);
                                    if (!checkOnly) {
                                        inventory.setItem(inventoryItem.slot(), newAmountItem);
                                    }
                                    remaining = 0;
                                }
                            }
                        }
                    }
                }
                Block block = shop.getLocation().getBlock();
                block.getState().update();
            } else {
                // Insufficient stock found
                return new InventoryTransactionResult(ShopTransactionResult.INSUFFICIENT_QTY, "Insufficient stock");
            }
        } catch (Exception ex) {
            return new InventoryTransactionResult(ShopTransactionResult.ERROR, "Unexpected Error - " + ex.getMessage());
        }
        if (checkOnly) {
            return new InventoryTransactionResult(ShopTransactionResult.SUCCESS, "Sufficient stock available");
        } else {
            return new InventoryTransactionResult(ShopTransactionResult.SUCCESS, "Stock taken from shop");
        }
    }

    private static class InventoryItems {
        private int slot;
        private ItemStack item;
        public InventoryItems(int slot, ItemStack item) {
            this.slot = slot;
            this.item = item;
        }
        public int slot() { return this.slot; }
        public ItemStack item() { return this.item; }
        public void item(ItemStack item) {
            this.item = item;
        }
    }
    public static class SellResult {
        private boolean success;
        private String returnMessage;

        public SellResult(boolean success, String returnMessage) {
            this.success = success;
            this.returnMessage = returnMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReturnMessage() {
            return returnMessage;
        }
    }
    public static class InventoryTransactionResult {
        private ShopTransactionResult transactionResult;
        private String returnMessage;

        public InventoryTransactionResult(ShopTransactionResult transactionResult, String returnMessage) {
            this.transactionResult = transactionResult;
            this.returnMessage = returnMessage;
        }

        public ShopTransactionResult transactionResult() { return this.transactionResult; }

        public String getReturnMessage() {
            return returnMessage;
        }
    }
}
