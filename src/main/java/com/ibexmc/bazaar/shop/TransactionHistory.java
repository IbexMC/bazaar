package com.ibexmc.bazaar.shop;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.functions.StringFunctions;
import com.ibexmc.internal.functions.WorldFunctions;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionHistory {

    Bazaar instance;

    public TransactionHistory(Bazaar instance) {
        this.instance = instance;
    }

    /**
     * Saves the transaction history to a file
     * @param player Player performing the transaction
     * @param shop Shop transaction is happening at
     * @param shopAction Shop action
     */
    public void save(Player player, Shop shop, ShopAction shopAction) {
        try {
            File dataFolder = instance.getDataFolder();
            if(!dataFolder.exists())
            {
                boolean res = dataFolder.mkdir();
            }

            File saveTo = new File(instance.getDataFolder(), instance.getInternalName() + "_transactions.log");
            if (!saveTo.exists())
            {
                if (!saveTo.createNewFile()) {
                    instance.getDebug().log(
                            "TransactionHistory",
                            "save",
                            "Unexpected Error saving file: " + saveTo.getName()
                    );
                }
            }

            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String action = "";
            double cost = 0;
            String shopType = "";
            if (shopAction == ShopAction.BUY_FROM_SHOP) {
                action = "bought";
                cost = shop.getBuyFromShopPrice();
            } else if (shopAction == ShopAction.SELL_TO_SHOP) {
                action = "sold";
                cost = shop.getSellToShopPrice();
            }
            if (shop.playerShop() && !shop.isClanShop()) {
                if (shop.getSellerOfflinePlayer().getName() != null) {
                    shopType = "player shop belonging to " + shop.getSellerOfflinePlayer().getName() + " (" + shop.getSellerOfflinePlayer().getUniqueId() + ")";
                } else {
                    shopType = "player shop";
                }
            }
            if (shop.isClanShop()) {
                if (shop.getClanTag() != null) {
                    shopType = "clan shop belonging to " + shop.getClanTag();
                } else {
                    shopType = "clan shop";
                }
            }
            if (shop.isAdminShop()) {
                shopType = "admin shop";
            }
            if (shop.isServerShop()) {
                shopType = "server shop";
            }
            pw.println(
                    timeStamp + " - " +
                            StringFunctions.stripColor(player.getDisplayName()) + " (" + player.getUniqueId() + ") " +
                            action + " " + shop.getAmount() + "x " + shop.getItemName() + " for " +
                            cost + " at " + shopType + " placed at " + WorldFunctions.simpleReadable(shop.getLocation())
            );
            pw.flush();
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
