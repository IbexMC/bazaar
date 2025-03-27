package com.ibexmc.bazaar.storage.db;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.storage.yaml.FileAccess;
import com.ibexmc.bazaar.util.Logging;
import com.ibexmc.internal.functions.ItemFunctions;
import com.ibexmc.internal.functions.NumberFunctions;
import com.ibexmc.internal.functions.StringFunctions;
import com.ibexmc.internal.functions.WorldFunctions;
import com.ibexmc.internal.messaging.Error;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MySQL {

    private Bazaar instance;
    private final String createTableSQL = "CREATE TABLE IF NOT EXISTS `ibex_plugin` (`ibex_id` int NOT NULL AUTO_INCREMENT,`plugin_name` varchar(45) NOT NULL,`key_type` varchar(50) NOT NULL,`ibex_key` varchar(255) NOT NULL,`ibex_value` varchar(255) DEFAULT NULL, PRIMARY KEY (`ibex_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;";
    private final String clearTableSQL = "DELETE FROM ibex_plugin WHERE plugin_name = 'bazaar';";
    private final String shopLocationsSQL = "SELECT DISTINCT '' AS plugin_name, key_type, '' AS ibex_key, '' AS ibex_value FROM ibex_plugin WHERE plugin_name = 'bazaar';";
    private final String shopDetailsSQL = "SELECT ibex_key, ibex_value FROM ibex_plugin WHERE plugin_name = 'bazaar' AND key_type = '%location%';";
    private final String deleteShopSQL = "DELETE FROM ibex_plugin WHERE plugin_name = 'bazaar' AND key_type = '%location%';";
    private final String insertShopSQL = "INSERT INTO ibex_plugin (plugin_name, key_type, ibex_key, ibex_value) VALUES ('bazaar','%location%','item_name', '%itemName%'),('bazaar','%location%','item_blob', '%itemBlob%'),('bazaar','%location%','item_desc', '%itemDesc%'),('bazaar','%location%','amount', '%amount%'),('bazaar','%location%','buy_price', '%buyPrice%'),('bazaar','%location%','sell_price', '%sellPrice%'),('bazaar','%location%','cooldown', '%cooldown%'),('bazaar','%location%','owner', '%owner%');";
    private final String rekeyIbexPluginSQL = "CREATE TABLE IF NOT EXISTS `temp_ibex_plugin` (`ibex_id` int NOT NULL AUTO_INCREMENT,`plugin_name` varchar(45) NOT NULL,`key_type` varchar(50) NOT NULL,`ibex_key` varchar(255) NOT NULL,`ibex_value` varchar(255) DEFAULT NULL, PRIMARY KEY (`ibex_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4; " +
            "INSERT INTO temp_ibex_plugin (plugin_name, key_type, ibex_key, ibex_value) SELECT plugin_name, key_type, ibex_key, ibex_value FROM ibex_plugin; " +
            "TRUNCATE TABLE ibex_plugin; " +
            "INSERT INTO ibex_plugin (plugin_name, key_type, ibex_key, ibex_value) SELECT plugin_name, key_type, ibex_key, ibex_value FROM temp_ibex_plugin; " +
            "DROP TABLE temp_ibex_plugin;";

    public MySQL(Bazaar instance) {
        this.instance = instance;
    }


    /**
     * Checks if the ibex_plugin table exists, if not, creates it
     */
    public void checkTable() {
        instance.getDebug().log(
                "MySQL",
                "checkDbStatus",
                "Creating ibex_plugin if it does not exist.  SQL: " + createTableSQL
        );
        try {
            CompletableFuture<Integer> createResult = instance.getMySQL().updateAsync(createTableSQL);
            createResult.thenAcceptAsync(intResult -> {
                int result = intResult;
                instance.getDebug().log(
                        "MySQL",
                        "checkDbStatus",
                        "Creating ibex_plugin if it does not exist.  Result: " + result
                );
            });
        } catch (Exception ex) {
            instance.getError().save(
                    "002",
                    "MySQL",
                    "checkDbStatus",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Loads a distinct list of shops in the ibex_plugin table
     * @param saveToFile If true, will also save to file
     */
    public void loadPlayerShops(boolean saveToFile) {
        instance.getDebug().log(
                "MySQL",
                "loadPlayerShops",
                "Getting list of shops.  SQL: " + shopLocationsSQL
        );
        try {
            CompletableFuture<com.ibexmc.internal.sql.MySQL.ListIbexTable> listResultFuture = instance.getMySQL().getListAsync(shopLocationsSQL);

            listResultFuture.thenAcceptAsync(listResult -> {
                List<com.ibexmc.internal.sql.MySQL.IbexTable> results = listResult.getResults();
                if (!results.isEmpty()) {
                    for (com.ibexmc.internal.sql.MySQL.IbexTable result : results) {
                        String stringLocation = result.getKeyType();
                        instance.getDebug().log(
                                "MySQL",
                                "loadPlayerShops",
                                "Shop found at location: " + stringLocation
                        );
                        if (!StringFunctions.isNullOrEmpty(stringLocation)) {
                            Location location = WorldFunctions.deserialize(stringLocation);
                            if (location != null) {
                                instance.getDebug().log(
                                        "MySQL",
                                        "loadPlayerShops",
                                        "Loading shop at location: " + stringLocation
                                );
                                loadShopDetails(stringLocation, saveToFile);
                            }
                        }
                    }
                } else {
                    instance.getDebug().log(
                            "MySQL",
                            "loadPlayerShops",
                            "No shops found"
                    );
                }
            });

            try {
                listResultFuture.get();
            } catch (InterruptedException | ExecutionException ex) {
                instance.getError().save(
                        "001",
                        "MySQL",
                        "loadPlayerShops",
                        "Unexpected Error",
                        ex.getMessage(),
                        Error.Severity.CRITICAL,
                        ex.getStackTrace()
                );
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "002",
                    "MySQL",
                    "loadPlayerShops",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }

    }

    /**
     * Loads the shop details from ibex_plugin at the location specified
     * @param location Serialized location
     * @param saveToFile If true, will also save to file
     */
    public void loadShopDetails(String location, boolean saveToFile) {
        try {
            String sqlShop = shopDetailsSQL.replace("%location%", location);
            instance.getDebug().log(
                    "MySQL",
                    "loadShopDetails",
                    "Getting shop detail.  SQL: " + sqlShop
            );
            CompletableFuture<com.ibexmc.internal.sql.MySQL.ListIbexTableKeyValue> shopFuture = instance.getMySQL().getListAsyncKeyValue(sqlShop);
            shopFuture.thenAcceptAsync(listResultDetail -> {
                List<com.ibexmc.internal.sql.MySQL.IbexTableKeyValue> shopDetails = listResultDetail.getResults();
                if (!shopDetails.isEmpty()) {
                    ItemStack shopItem = null;
                    String itemName = "";
                    String itemBlob = "";
                    String itemDesc = "";
                    int amount = 0;
                    double buyPrice = 0d;
                    double sellPrice = 0d;
                    int cooldown = 0;
                    String owner = "";
                    UUID ownerUUID = null;
                    boolean isAdmin = false;
                    boolean isServer = false;
                    String clanTag = "";
                    instance.getDebug().log(
                            "MySQL",
                            "loadShopDetails",
                            "Looping through shop details at location: " + location
                    );
                    for (com.ibexmc.internal.sql.MySQL.IbexTableKeyValue result : shopDetails) {
                        instance.getDebug().log(
                                "MySQL",
                                "loadShopDetails",
                                "Key: " + result.getKey() + " Value: " + result.getValue()
                        );
                        if (result.getKey().equalsIgnoreCase("item_blob")) {
                            shopItem = ItemFunctions.deserialize(result.getValue());
                            itemBlob = result.getValue();
                        }
                        if (result.getKey().equalsIgnoreCase("item_name")) {
                            itemName = result.getValue();
                        }
                        if (result.getKey().equalsIgnoreCase("item_desc")) {
                            itemDesc = result.getValue();
                        }
                        if (result.getKey().equalsIgnoreCase("amount")) {
                            amount = NumberFunctions.stringToInt(result.getValue(), -1);
                        }
                        if (result.getKey().equalsIgnoreCase("buy_price")) {
                            buyPrice = NumberFunctions.stringToDouble(result.getValue(), 0d);
                        }
                        if (result.getKey().equalsIgnoreCase("sell_price")) {
                            sellPrice = NumberFunctions.stringToDouble(result.getValue(), -1d);
                        }
                        if (result.getKey().equalsIgnoreCase("cooldown")) {
                            amount = NumberFunctions.stringToInt(result.getValue(), 0);
                        }
                        if (result.getKey().equalsIgnoreCase("owner")) {
                            owner = result.getValue();
                            if (owner.equalsIgnoreCase("ADMIN")) {
                                isAdmin = true;
                            } else if (owner.equalsIgnoreCase("SERVER")) {
                                isServer = true;
                            } else if (owner.toUpperCase().contains("CLAN:")) {
                                clanTag = owner.toUpperCase().replace("CLAN:", "");
                            } else {
                                // uuid
                                UUID uuid = StringFunctions.uuidFromString(owner);
                                if (uuid != null) {
                                    ownerUUID = uuid;
                                }
                            }
                        }

                    }
                    instance.getDebug().log(
                            "MySQL",
                            "loadShopDetails",
                            "Validating Shop details"
                    );
                    if (
                            shopItem == null ||
                                    StringFunctions.isNullOrEmpty(itemName) ||
                                    amount < 1 ||
                                    (buyPrice == 0 && sellPrice == 0) ||
                                    (!isAdmin && !isServer && ownerUUID == null && StringFunctions.isNullOrEmpty(clanTag))
                    ) {
                        // error
                        instance.getLog().log("MySQL", "loadShopDetails", "Some item didn't load");
                        instance.getError().save(
                                "001",
                                "MySQL",
                                "loadShopDetails",
                                "Validation Error",
                                "Shop details invalid",
                                Error.Severity.CRITICAL,
                                null
                        );
                    } else {
                        instance.getDebug().log(
                                "MySQL",
                                "loadShopDetails",
                                "Shop validated successfully - Creating shop in-game"
                        );
                        Location shopLocation = WorldFunctions.deserialize(location);
                        Shop shop = new Shop(
                                shopLocation,
                                shopLocation.getWorld().getName(),
                                itemName,
                                itemBlob,
                                itemDesc,
                                amount,
                                buyPrice,
                                sellPrice,
                                cooldown,
                                ownerUUID,
                                isAdmin,
                                !StringFunctions.isNullOrEmpty(clanTag),
                                clanTag,
                                isServer
                        );

                        instance.getDebug().log(
                                "MySQL",
                                "loadShopDetails",
                                "Shop created in-game"
                        );
                        shop.setLoaded(true);

                        instance.getHologramManager().removeHologram(shop);
                        instance.getHologramManager().createHologram(shop);

                        // Add the chest to the map
                        instance.getBazaarData().putShops(shopLocation, shop);

                        if (saveToFile) {
                            FileAccess fileAccess = new FileAccess(instance);
                            fileAccess.savePlayerShop(shop);
                        }
                    }
                }
            });

            try {
                shopFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                instance.getError().save(
                        "002",
                        "MySQL",
                        "loadShopDetails",
                        "Unexpected Error",
                        e.getMessage(),
                        Error.Severity.CRITICAL,
                        e.getStackTrace()
                );
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "003",
                    "MySQL",
                    "loadShopDetails",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Deletes the specified shop from the ibex_plugin table
     * @param shop Shop to delete
     */
    public void deleteShop(Shop shop) {
        try {
            String sqlShop = deleteShopSQL.replace("%location%", WorldFunctions.serializeSimple(shop.getLocation()));
            instance.getDebug().log(
                    "MySQL",
                    "deleteShop",
                    "Deleting shop.  SQL: " + sqlShop
            );
            CompletableFuture<Integer> shopFuture = instance.getMySQL().updateAsync(sqlShop);
            shopFuture.thenAcceptAsync(deleteResultInt -> {
                int deleteResult = deleteResultInt.intValue();
                instance.getDebug().log(
                        "MySQL",
                        "deleteShop",
                        "Result from deleting shop: " + deleteResult
                );
                if (deleteResult < 1) {
                    instance.getError().save(
                            "001",
                            "MySQL",
                            "deleteShop",
                            "No rows deleted",
                            "No rows deleted",
                            Error.Severity.CRITICAL,
                            null
                    );
                } else {
                    instance.getDebug().log(
                            "MySQL",
                            "deleteShop",
                            "Shop deleted"
                    );
                }
            });
            try {
                shopFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                instance.getError().save(
                        "002",
                        "MySQL",
                        "deleteShop",
                        "Unexpected Error",
                        e.getMessage(),
                        Error.Severity.CRITICAL,
                        e.getStackTrace()
                );
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "003",
                    "MySQL",
                    "deleteShop",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Creates the specified shop in the ibex_plugin table
     * @param shop
     */
    public void createShop(Shop shop) {
        try {
            String sqlShop = insertShopSQL.replace("%location%", WorldFunctions.serializeSimple(shop.getLocation()));
            sqlShop = sqlShop.replace("%itemName%", shop.getItemName());
            sqlShop = sqlShop.replace("%itemBlob%", ItemFunctions.serialize(shop.getItemStack()));
            sqlShop = sqlShop.replace("%itemDesc%", shop.getItemDescriptionLine());
            sqlShop = sqlShop.replace("%amount%", "" + shop.getAmount());
            sqlShop = sqlShop.replace("%buyPrice%", "" + shop.getBuyFromShopPrice());
            sqlShop = sqlShop.replace("%sellPrice%", "" + shop.getSellToShopPrice());
            sqlShop = sqlShop.replace("%cooldown%", "" + shop.getCooldown());
            if (shop.isAdminShop()) {
                sqlShop = sqlShop.replace("%owner%", "ADMIN");
            } else if (shop.isServerShop()) {
                sqlShop = sqlShop.replace("%owner%", "SERVER");
            } else if (shop.isClanShop()) {
                sqlShop = sqlShop.replace("%owner%", "CLAN:" + shop.getClanTag().toUpperCase());
            } else {
                sqlShop = sqlShop.replace("%owner%", "" + shop.getSellerOfflinePlayer().getUniqueId());
            }

            instance.getDebug().log(
                    "MySQL",
                    "createShop",
                    "Creating shop.  SQL: " + sqlShop
            );

            CompletableFuture<Integer> shopFuture = instance.getMySQL().updateAsync(sqlShop);
            shopFuture.thenAcceptAsync(createResultInt -> {
                int createResult = createResultInt.intValue();
                if (createResult < 1) {
                    instance.getError().save(
                            "001",
                            "MySQL",
                            "createShop",
                            "No rows created",
                            "No rows created",
                            Error.Severity.CRITICAL,
                            null
                    );
                } else {
                    instance.getDebug().log(
                            "MySQL",
                            "createShop",
                            "Shop created"
                    );
                }
            });

            try {
                shopFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                instance.getError().save(
                        "002",
                        "MySQL",
                        "createShop",
                        "Unexpected Error",
                        e.getMessage(),
                        Error.Severity.CRITICAL,
                        e.getStackTrace()
                );
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "003",
                    "MySQL",
                    "createShop",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Clears all Bazaar data from the ibex_plugin table
     */
    public void clearBazaarData() {
        try {
            instance.getDebug().log(
                    "MySQL",
                    "clearBazaarData",
                    "Clearing Bazaar data.  SQL: " + clearTableSQL
            );
            CompletableFuture<Integer> shopFuture = instance.getMySQL().updateAsync(clearTableSQL);
            shopFuture.thenAcceptAsync(deleteResultInt -> {
                int deleteResult = deleteResultInt.intValue();
                instance.getDebug().log(
                        "MySQL",
                        "clearBazaarData",
                        "Result from clearing Bazaar data: " + deleteResult
                );
                if (deleteResult < 1) {
                    instance.getError().save(
                            "001",
                            "MySQL",
                            "clearBazaarData",
                            "No rows deleted",
                            "No rows deleted",
                            Error.Severity.CRITICAL,
                            null
                    );
                } else {
                    instance.getDebug().log(
                            "MySQL",
                            "clearBazaarData",
                            "Bazaar data cleared"
                    );
                }
            });
            try {
                shopFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                instance.getError().save(
                        "002",
                        "MySQL",
                        "clearBazaarData",
                        "Unexpected Error",
                        e.getMessage(),
                        Error.Severity.CRITICAL,
                        e.getStackTrace()
                );
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "003",
                    "MySQL",
                    "clearBazaarData",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Duplicates all the data in ibex_plugin into a temporary table, truncates ibex_plugin
     * then restores from the temp table, and drops the temp table.
     * This should only be used if your id ibex_id values are approaching max int value
     * This will apply to data from ALL ibex plugins that use this table
     */
    public void rekeyIbexPluginTable() {
        try {
            instance.getDebug().log(
                    "MySQL",
                    "rekeyIbexPluginTable",
                    "Resetting the id values for ibex_plugin.  SQL: " + rekeyIbexPluginSQL
            );
            CompletableFuture<Integer> shopFuture = instance.getMySQL().updateAsync(rekeyIbexPluginSQL);
            shopFuture.thenAcceptAsync(deleteResultInt -> {
                int deleteResult = deleteResultInt.intValue();
                instance.getDebug().log(
                        "MySQL",
                        "rekeyIbexPluginTable",
                        "Result from resetting the id values for ibex_plugin: " + deleteResult
                );
                if (deleteResult < 1) {
                    instance.getError().save(
                            "001",
                            "MySQL",
                            "rekeyIbexPluginTable",
                            "No rows modified",
                            "No rows modified",
                            Error.Severity.CRITICAL,
                            null
                    );
                } else {
                    instance.getDebug().log(
                            "MySQL",
                            "rekeyIbexPluginTable",
                            "ibex_plugin table has been reloaded"
                    );
                }
            });
            try {
                shopFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                instance.getError().save(
                        "002",
                        "MySQL",
                        "rekeyIbexPluginTable",
                        "Unexpected Error",
                        e.getMessage(),
                        Error.Severity.CRITICAL,
                        e.getStackTrace()
                );
            }
        } catch (Exception ex) {
            instance.getError().save(
                    "003",
                    "MySQL",
                    "rekeyIbexPluginTable",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.CRITICAL,
                    ex.getStackTrace()
            );
        }
    }
}
