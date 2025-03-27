package com.ibexmc.bazaar.storage.yaml;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.util.functions.FileFunctions;
import com.ibexmc.internal.functions.ItemFunctions;
import com.ibexmc.internal.functions.WorldFunctions;
import com.ibexmc.internal.messaging.Error;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class FileAccess {

    Bazaar instance;

    public FileAccess(Bazaar instance) {
        this.instance = instance;
    }

    /**
     * Gets the path for the player shop files
     * @return Player Shop file path
     */
    private String getPlayerShopPath() {
        return instance.getDataFolder() +
                File.separator +
                "Shops" +
                File.separator;
    }

    /**
     * Gets the path for the multi-shop files
     * @return Multi Shop file path
     */
    private String getMultiShopPath() {
        return instance.getDataFolder() +
                File.separator +
                "MultiShops" +
                File.separator;
    }

    /**
     * Gets the filename for the player shop
     * @param shop Shop to get filename for
     * @return File for the shop
     */
    private File getPlayerShopFile(Shop shop) {
        return new File(
                getPlayerShopPath() +
                        FileFunctions.getFilenameFromLocation(shop.getLocation())
        );
    }

    /**
     * Saves the shop to file
     * @param shop Shop to save
     */
    public void savePlayerShop(Shop shop) {
        if (shop == null) {
            return;
        }

        File dataFile = getPlayerShopFile(shop);
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        boolean active = true;
        config.set("location", WorldFunctions.serializeSimple(shop.getLocation()));
        config.set("item_name", shop.getItemName());
        config.set("item_blob", shop.getItemText());
        config.set("item_desc", shop.getItemDescriptionLine());
        config.set("amount", shop.getAmount());
        config.set("buy_price", shop.getBuyFromShopPrice());
        config.set("sell_price", shop.getSellToShopPrice());
        config.set("cooldown", shop.getCooldown());

        if (shop.isAdminShop()) {
            config.set("owner", null);
            config.set("admin_shop", true);
            config.set("clan_shop", null);
            config.set("clan_tag", null);
            config.set("server_shop", null);
        } else if (shop.isClanShop()) {
            config.set("admin_shop", null);
            config.set("server_shop", null);
            config.set("clan_shop", true);
            if (shop.getClanTag() != null) {
                if (shop.getClanTag().length() > 0) {
                    config.set("clan_tag", shop.getClanTag());
                } else {
                    config.set("clan_tag", "");
                    active = false;
                }
            } else {
                config.set("clan_tag", "");
                active = false;
            }
        } else if (shop.isServerShop()) {
            config.set("owner", null);
            config.set("admin_shop", null);
            config.set("clan_shop", null);
            config.set("clan_tag", null);
            config.set("server_shop", true);
        }

        if (!shop.isAdminShop() && !shop.isServerShop()){
            // If not an admin or server shop, must be a player shop or clan shop
            // either one needs the owner
            if (shop.getSellerOfflinePlayer() != null) {
                config.set("owner", shop.getSellerOfflinePlayer().getUniqueId().toString());
            } else {
                config.set("owner", "");
                active = false;
            }
        }
        config.set("active", active);

        try {
            config.save(dataFile);
            if (dataFile.length() == 0) {
                instance.getDebug().log(
                        "FileAccess",
                        "savePlayerShop(Chest)",
                        dataFile.getName() + " saved"
                );
            }
        } catch (IOException e) {
            instance.getError().save(
                    "001",
                    "FileAccess",
                    "savePlayerShop",
                    "Unexpected error saving shop",
                    e.getMessage(),
                    Error.Severity.WARN,
                    e.getStackTrace()
            );
        }

    }

    /**
     * Removes a player shop
     * @param shop Shop to remove
     */
    public void removePlayerShop(Shop shop) {
        if (shop == null) {
            return;
        }

        File dataFile = getPlayerShopFile(shop);

        if (dataFile.exists()) {
            try {
                dataFile.delete();
            } catch (Exception ex) {

            }
        }
    }

    /**
     * Loads player shops
     */
    public void loadPlayerShops() {
        // Set reload flag so nobody can access shops
        instance.getBazaarData().setReloadFlag(true);

        // Clear chests so we can reload them cleanly
        instance.getBazaarData().clearShops();

        // load chests
        File datadir = new File(getPlayerShopPath());
        if (!datadir.exists()) {
            return;
        }
        for (File dataFile : new File(getPlayerShopPath()).listFiles()) {
            boolean stop = false;
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

            Location chestLocation = null;
            String chestItemName = "";
            String chestItemBlob = "";
            String chestItemDescriptionLine = "";
            int chestAmount = 0;
            double chestBuyPrice = 0d;
            double chestSellPrice = 0d;
            int chestCooldown = 1;
            UUID chestSeller = null;
            boolean chestAdminShop = false;
            boolean chestClanShop = false;
            String chestClanTag = "";
            boolean chestServerShop = false;

            //SECTION: Location
            if (config.contains("location")) {
                if (config.isString("location")) {
                    String serializedLocation = config.getString("location");
                    Location location = WorldFunctions.deserialize(serializedLocation);

                    if (location != null) {
                        chestLocation = location;
                    } else {
                        instance.getError().save(
                                "001",
                                "FileAccess",
                                "loadPlayerShops",
                                "Unexpected error",
                                "Location is malformed.  Location provided: " + serializedLocation + "| File: " + dataFile.getName(),
                                Error.Severity.WARN,
                                null
                        );
                        stop = true;
                    }
                } else {
                    instance.getError().save(
                            "002",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Location is not a string. File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }
            } else {
                instance.getError().save(
                        "003",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Location is missing.  File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
                stop = true;
            }

            //SECTION: Item Name
            if (config.contains("item_name")) {
                if (config.isString("item_name")) {
                    chestItemName = config.getString("item_name");
                } else {
                    instance.getError().save(
                            "004",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Item name is not a string. File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }
            } else {
                instance.getError().save(
                        "005",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Item name is missing. File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
                stop = true;
            }

            //SECTION: Item Blob
            if (config.contains("item_blob")) {
                if (config.isString("item_blob")) {
                    chestItemBlob = config.getString("item_blob");
                    ItemStack itemStack = ItemFunctions.deserialize(chestItemBlob);
                    if (itemStack == null) {
                        instance.getError().save(
                                "006",
                                "FileAccess",
                                "loadPlayerShops",
                                "Unexpected error",
                                "Malformed Item Blob.  File: " + dataFile.getName() + "\nItem Blob:\n" + chestItemBlob,
                                Error.Severity.WARN,
                                null
                        );
                        stop = true;
                    }
                } else {
                    instance.getError().save(
                            "007",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Item Blob is not a string.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }
            } else {
                instance.getError().save(
                        "008",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Item Blob is missing.  File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
                stop = true;
            }

            //SECTION: Item Description
            if (config.contains("item_desc")) {
                if (config.isString("item_desc")) {
                    chestItemDescriptionLine = config.getString("item_desc");
                } else {
                    instance.getError().save(
                            "009",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Item Description is not a string, skipping value.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                }
            }

            //SECTION: Amount
            if (config.contains("amount")) {
                if (config.isInt("amount")) {
                    chestAmount = config.getInt("amount");
                } else {
                    instance.getError().save(
                            "010",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Amount is not an integer.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }
            } else {
                instance.getError().save(
                        "011",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Amount is missing.  File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
                stop = true;
            }

            //SECTION: Buy Price
            if (config.contains("buy_price")) {
                if (config.isDouble("buy_price")) {
                    chestBuyPrice = config.getDouble("buy_price");
                } else {
                    instance.getError().save(
                            "012",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Buy Price is not a double.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }
            } else {
                instance.getError().save(
                        "013",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Buy Price is missing.  File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
                stop = true;
            }

            //SECTION: Sell Price
            if (config.contains("sell_price")) {
                if (config.isDouble("sell_price")) {
                    chestSellPrice = config.getDouble("sell_price");
                } else {
                    instance.getError().save(
                            "014",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Sell Price is not a double.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }
            } else {
                instance.getError().save(
                        "015",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Sell Price is missing.  File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
                stop = true;
            }

            //SECTION: Cooldown
            if (config.contains("cooldown")) {
                if (config.isInt("cooldown")) {
                    chestCooldown = config.getInt("cooldown");
                } else {
                    instance.getError().save(
                            "016",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Cooldown is not an integer, defaulting to 1.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                }
            }

            //SECTION: Admin Shop
            if (config.contains("admin_shop")) {
                if (config.isBoolean("admin_shop")) {
                    chestAdminShop = config.getBoolean("admin_shop");
                } else {
                    instance.getError().save(
                            "017",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Admin shop flag is not a boolean value, defaulting to false.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                }
            }

            //SECTION: Clan Shop
            // Only get Clan Shop if not an admin shop
            if (!chestAdminShop) {
                if (config.contains("clan_shop")) {
                    if (config.isBoolean("clan_shop")) {
                        chestClanShop = config.getBoolean("clan_shop");
                        //SECTION Clan Shop Tag
                        if (config.contains("clan_tag")) {
                            if (config.isString("clan_tag")) {
                                chestClanTag = config.getString("clan_tag");
                            } else {
                                instance.getError().save(
                                        "018",
                                        "FileAccess",
                                        "loadPlayerShops",
                                        "Unexpected error",
                                        "Clan Tag is not a string. Removing Clan Shop Flag  File: " + dataFile.getName(),
                                        Error.Severity.WARN,
                                        null
                                );
                                chestClanShop = false;
                            }
                        } else {
                            instance.getError().save(
                                    "019",
                                    "FileAccess",
                                    "loadPlayerShops",
                                    "Unexpected error",
                                    "Clan Tag is missing. Removing Clan Shop Flag  File: " + dataFile.getName(),
                                    Error.Severity.WARN,
                                    null
                            );
                            chestClanShop = false;
                        }
                    } else {
                        instance.getError().save(
                                "020",
                                "FileAccess",
                                "loadPlayerShops",
                                "Unexpected error",
                                "Clan shop flag is not a boolean value, defaulting to false.  File: " + dataFile.getName(),
                                Error.Severity.WARN,
                                null
                        );
                    }
                }
            }

            //SECTION: Server Shop
            if (config.contains("server_shop")) {
                if (config.isBoolean("server_shop")) {
                    chestServerShop = config.getBoolean("server_shop");
                } else {
                    instance.getError().save(
                            "021",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Server shop flag is not a boolean value, defaulting to false.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                }
            }


            //SECTION: Owner
            // Get owner last as we only want to get that if not admin/clan/server
            if (!chestAdminShop && !chestServerShop) {
                if (config.contains("owner")) {
                    if (config.isString("owner")) {
                        String owner = config.getString("owner");
                        UUID ownerUUID = UUID.fromString(owner);
                        if (ownerUUID != null) {
                            OfflinePlayer ownerOfflinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
                            if (ownerOfflinePlayer != null) {
                                chestSeller = ownerOfflinePlayer.getUniqueId();
                            } else {
                                // error - invalid player
                                instance.getError().save(
                                        "022",
                                        "FileAccess",
                                        "loadPlayerShops",
                                        "Unexpected error",
                                        "Invalid Player.  Owner with UUID: " + owner +
                                                " does not exist on this server. File: " + dataFile.getName(),
                                        Error.Severity.WARN,
                                        null
                                );
                                stop = true;
                            }
                        } else {
                            // error - malformed uuid
                            instance.getError().save(
                                    "023",
                                    "FileAccess",
                                    "loadPlayerShops",
                                    "Unexpected error",
                                    "Malformed Owner value.  Owner provided: " + owner +
                                            ". File: " + dataFile.getName(),
                                    Error.Severity.WARN,
                                    null
                            );
                            stop = true;
                        }
                    } else {
                        instance.getError().save(
                                "024",
                                "FileAccess",
                                "loadPlayerShops",
                                "Unexpected error",
                                "Owner is not a string.  File: " + dataFile.getName(),
                                Error.Severity.WARN,
                                null
                        );
                        stop = true;
                    }
                } else {
                    instance.getError().save(
                            "025",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Owner is missing.  File: " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                    stop = true;
                }

            }

            if (!chestAdminShop && !chestClanShop && !chestServerShop && chestSeller == null) {
                // If admin/clan shop have been reset and there is no owner
                // stop the load
                stop = true;
            }

            if (!stop) {
                if (chestLocation.getWorld() == null) {
                    instance.getError().save(
                            "026",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected error",
                            "Unable to add player shop.  File: " + dataFile.getName() + " - Invalid world",
                            Error.Severity.WARN,
                            null
                    );
                }
                try {
                    // Create the player shop
                    Shop shop = new Shop(
                            chestLocation,
                            chestLocation.getWorld().getName(),
                            chestItemName,
                            chestItemBlob,
                            chestItemDescriptionLine,
                            chestAmount,
                            chestBuyPrice,
                            chestSellPrice,
                            chestCooldown,
                            chestSeller,
                            chestAdminShop,
                            chestClanShop,
                            chestClanTag,
                            chestServerShop
                    );

                    shop.setLoaded(true);

                    instance.getHologramManager().removeHologram(shop);
                    instance.getHologramManager().createHologram(shop);

                    // Add the chest to the map
                    instance.getBazaarData().putShops(chestLocation, shop);
                }
                catch (Exception ex) {
                    instance.getError().save(
                            "027",
                            "FileAccess",
                            "loadPlayerShops",
                            "Unexpected errorFile: " + dataFile.getName(),
                            ex.getMessage(),
                            Error.Severity.WARN,
                            ex.getStackTrace()
                    );
                }
            } else {
                instance.getError().save(
                        "028",
                        "FileAccess",
                        "loadPlayerShops",
                        "Unexpected error",
                        "Unable to add player shop.  File: " + dataFile.getName(),
                        Error.Severity.WARN,
                        null
                );
            }

        }

        // Clear reload flag so shops can be accessed again
        instance.getBazaarData().setReloadFlag(false);
    }

    public void loadMultiShops() {

    }

    /**
     * Clears all shop files
     */
    public void clearAllFiles() {
        File datadir = new File(getPlayerShopPath());
        if (!datadir.exists()) {
            return;
        }
        for (File dataFile : Objects.requireNonNull(new File(getPlayerShopPath()).listFiles())) {
            try {
                boolean delete = dataFile.delete();
                if (!delete) {
                    instance.getError().save(
                            "001",
                            "FileAccess",
                            "clearAllFiles",
                            "Unexpected Error deleting player shops",
                            "File not deleted - " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                }
            } catch (Exception ex) {
                instance.getError().save(
                        "002",
                        "FileAccess",
                        "clearAllFiles",
                        "Unexpected Error deleting player shops",
                        ex.getMessage(),
                        Error.Severity.WARN,
                        ex.getStackTrace()
                );
            }
        }
        File multiShopDir = new File(getPlayerShopPath());
        if (!multiShopDir.exists()) {
            return;
        }
        for (File dataFile : Objects.requireNonNull(new File(getMultiShopPath()).listFiles())) {
            try {
                boolean delete = dataFile.delete();
                if (!delete) {
                    instance.getError().save(
                            "003",
                            "FileAccess",
                            "clearAllFiles",
                            "Unexpected Error deleting player shops",
                            "File not deleted - " + dataFile.getName(),
                            Error.Severity.WARN,
                            null
                    );
                }
            } catch (Exception ex) {
                instance.getError().save(
                        "004",
                        "FileAccess",
                        "clearAllFiles",
                        "Unexpected Error deleting multi-shops",
                        ex.getMessage(),
                        Error.Severity.WARN,
                        ex.getStackTrace()
                );
            }
        }
    }

}
