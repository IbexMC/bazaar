package com.ibexmc.bazaar.util.functions;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.internal.functions.StringFunctions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BazaarItemFunctions {

    /**
     * Gets the display name from the ItemStack
     * @param itemStack ItemStack to get name from
     * @return Display name from the ItemStack
     */
    public static String itemNameFromItemStack(ItemStack itemStack) {
        String returnValue = "";
        if (itemStack != null) {
            returnValue = itemStack.getType().name();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.getDisplayName() != null) {
                    if (!itemMeta.getDisplayName().equals("")) {
                        returnValue = itemMeta.getDisplayName();
                    }
                }
            }
        }
        returnValue = StringFunctions.toProperCase(returnValue);
        return returnValue;
    }

    /**
     * Gets the display name from the ItemStack, but capitalizes and strips colors from it
     * @param itemStack ItemStack to get display name from
     * @return Cleaned display name from the ItemStack
     */
    public static String itemNameFromItemStackClean(ItemStack itemStack) {
        String returnValue = "";
        if (itemStack != null) {
            returnValue = itemStack.getType().name();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.getDisplayName() != null) {
                    if (!itemMeta.getDisplayName().equals("")) {
                        returnValue = StringFunctions.capitalizeString(ChatColor.stripColor(itemMeta.getDisplayName()));
                    }
                }
            }
        }
        returnValue = StringFunctions.toProperCase(returnValue);
        return returnValue;
    }

}
