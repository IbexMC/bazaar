package com.ibexmc.bazaar.util;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.hologram.HologramType;
import com.ibexmc.bazaar.shop.ShopAction;
import com.ibexmc.bazaar.util.functions.FileFunctions;
import com.ibexmc.internal.functions.GameFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import com.ibexmc.internal.functions.StringFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BazaarConfig {
    //region Objects
    Bazaar instance;
    YmlParser ymlParser;
    //endregion
    //region Config Objects
    private boolean useMySQL = false;
    private String mysqlHost = "";
    private int mysqlPort = 0;
    private String mysqlDatabase = "";
    private String mysqlUsername = "";
    private String mysqlPassword = "";
    boolean mysqlUseSSL = false;
    private boolean debugLog = false;
    private boolean debugLogToFile = false;
    private boolean disableShopCommand = false;
    private String messagePrefix = "&6[&fBazaar&6]&r";
    private String currencySymbol = "$";
    private boolean saveHistory = false;
    private boolean logSalesToConsole = false;
    private int shopEditTimeout = 15;
    private int defaultCooldown = 1;
    private int costCreate = 0;
    private int costCreateClan = 0;
    private int salesTax = 0;
    private List<String> bannedWords = new ArrayList<>();
    private List<World> invalidWorlds = new ArrayList<>();
    private List<Material> invalidMaterials = new ArrayList<>();
    private boolean displayOutOfStock = true;
    private boolean messageSellerOnPurchase;
    private OfflinePlayer offlinePlayerAccount;
    private boolean serverAccountInfiniteStock = false;
    private boolean domainProtection = false;
    private boolean worldguardProtection = false;
    private String wgCreateShopFlag = "create-shop";
    private boolean wgCreateShopValue = false;
    private String wgUseAdminShopFlag = "use-admin-shop";
    private boolean wgUseAdminShopValue = false;
    private String wgUsePlayerShopFlag = "use-shop";
    private boolean wgUsePlayerShopValue = false;
    private String wgUseClanShopFlag = "use-clan-shop";
    private boolean wgUseClanShopValue = false;
    private boolean wgOnlyShops = false;
    private int shopCleanupSeconds = 0;
    private boolean shopCleanupDelete = false;
    private ShopAction leftClick = ShopAction.BUY_FROM_SHOP;
    private ShopAction rightClick = ShopAction.SELL_TO_SHOP;
    private HologramType hologramType = HologramType.NONE;
    private boolean showOutOfStock = true;
    private boolean showOutOfStockPrice = true;
    private boolean showEnchantments = true;
    private boolean showPotionType = true;
    private boolean showOnlyIfTargeted = false;
    private boolean showIconAlwaysTextIfTargeted = false;
    private boolean useLocalTranslate = false;
    private int shopTargetTicks = 20;
    private String commandShopAlias = "";
    private String commandShopCreate = "";
    private String commandShopCreateAdmin = "";
    private String commandShopCreateServer = "";
    private String commandShopCreateClan = "";
    private String commandShopCreateCooldown = "";
    private String commandShopRemove = "";
    private String commandShopCancel = "";
    private String commandShopDebug = "";
    private String commandShopHelp = "";
    private String commandShopReload = "";
    private String commandShopVersion = "";
    private String commandShopTransferToDB = "";
    private String commandShopTransferToFile = "";
    private boolean buySellSafeguard = true;

    //endregion
    //region Constructor
    public BazaarConfig(Bazaar instance) {

        this.instance = instance;
        File configFile = FileFunctions.getConfigYml();
        ymlParser = new YmlParser(instance, configFile);

        load();

    }
    //endregion
    //region Getters

    public boolean useMySQL() {
        return useMySQL;
    }

    public void setUseMySQL(boolean value) {
        this.useMySQL = value;
    }

    public String getMySQLHost() {
        return mysqlHost;
    }

    public int getMySQLPort() {
        return mysqlPort;
    }

    public String getMySQLDatabase() {
        return mysqlDatabase;
    }

    public String getMySQLUsername() {
        return mysqlUsername;
    }

    public String getMySQLPassword() {
        return mysqlPassword;
    }

    public boolean getMySQLUseSSL() {
        return mysqlUseSSL;
    }

    /**
     * Gets the Debug log flag
     * @return If true, debug logging is on
     */
    public boolean debugLog() {
        return debugLog;
    }

    /**
     * Gets if the debug log should write to a file
     * @return If true, debug logging should be written to a file
     */
    public boolean debugLogToFile() {
        return debugLogToFile;
    }

    /**
     * Gets the left-click shop action
     * @return Left-click Shop Action
     */
    public ShopAction leftClick() {
        return this.leftClick;
    }

    /**
     * Gets the right-click shop action
     * @return Right-click Shop Action
     */
    public ShopAction rightClick() {
        return this.rightClick;
    }

    /**
     * Checks of the /shop command should be disabled
     * @return If true, the /shop command should be disabled
     */
    public boolean disableShopCommand() { return disableShopCommand; }

    /**
     * Gets the message prefix for in-game messages
     * @return Message Prefix
     */
    public String messagePrefix() {
        return this.messagePrefix;
    }

    /**
     * Gets the currency symbol, used in in-game messages
     * @return Currency Symbol
     */
    public String currencySymbol() { return this.currencySymbol; }

    /**
     * Gets if an out-of-stock message should be displayed in the hologram
     * @return If true, show the out-of-stock message
     */
    public boolean displayOutOfStock() { return this.displayOutOfStock;}

    /**
     * Gets if the transaction history should be saved - TODO: NOT CURRENTLY IN USE
     * @return If true, record the transaction history
     */
    public boolean saveHistory() { return this.saveHistory;}

    /**
     * Checks if sales should be logged directly to console
     * @return If true, log sales to console
     */
    public boolean isLogSalesToConsole() { return this.logSalesToConsole; }

    /**
     * Gets if the seller should receive a message when someone buys from their shop
     * @return If true, message the seller when someone buys something
     */
    public boolean messageSellerOnPurchase() {
        return messageSellerOnPurchase;
    }

    /**
     * Gets the number of seconds timeout that exists when editing a shop
     * @return Number of Seconds timeout
     */
    public int shopEditTimeout() {
        return shopEditTimeout;
    }

    /**
     * Gets the number of seconds cooldown to be used by default
     * @return Cooldown seconds
     */
    public int getDefaultCooldown() { return defaultCooldown; }

    /**
     * Gets the cost to create a player shop
     * @return Shop creation cost
     */
    public int costCreate() {
        return costCreate;
    }

    /**
     * Gets the cost to create a clan shop
     * @return Clan Shop creation cost
     */
    public int costCreateClan() {
        return costCreateClan;
    }

    /**
     * Sales tax to applied to shop sales
     * @return Sales tax percentage
     */
    public int salesTax() {
        return salesTax;
    }

    /**
     * Gets a list of words that are banned from shop/item descriptions
     * @return List of banned words
     */
    public List<String> bannedWords() {
        return bannedWords;
    }

    /**
     * Gets a list of worlds that players cannot place shops in
     * @return List of Invalid Worlds
     */
    public List<World> invalidWorlds() {
        if (invalidWorlds == null) {
            return new ArrayList<>();
        }
        return invalidWorlds;
    }

    /**
     * Gets a list of materials that players cannot place shops for
     * @return List of Invalid Materials
     */
    public List<Material> invalidMaterials() {
        if (invalidMaterials == null) {
            return new ArrayList<>();
        }
        return invalidMaterials;
    }

    /**
     * Gets the Server account used in economy plugin - Used for purchases and sales in a closed economy
     * @return Server account OfflinePlayer
     */
    public OfflinePlayer serverAccount() { return offlinePlayerAccount; }

    /**
     * Gets if server shops should be allowed to have infinite stock
     * @return If true, server shops can have infinite stock
     */
    public boolean serverAccountInfiniteStock() { return this.serverAccountInfiniteStock;}

    /**
     * Gets if Domain Protection should be enabled
     * @return If true, Domain protection should be enabled
     */
    public boolean domainProtection() { return domainProtection; }

    /**
     * Gets if Worldguard Protection should be enabled
     * @return If true, Worldguard protection should be enabled
     */
    public boolean worldguardProtection() { return worldguardProtection; }

    /**
     * Gets the WorldGuard flag name for creating shops
     * @return WorldGuard flag name
     */
    public String wgCreateShopFlag() { return wgCreateShopFlag; }

    /**
     * Gets the default value to use for WorldGuard flag for creating shops
     * @return If true, set to ALLOW, if false, set to DENY
     */
    public boolean wgCreateShopValue() { return wgCreateShopValue; }

    /**
     * Gets the WorldGuard flag name for using Admin shops
     * @return WorldGuard flag name
     */
    public String wgUseAdminShopFlag() { return wgUseAdminShopFlag; }

    /**
     * Gets the default value to use for WorldGuard flag for using Admin shops
     * @return If true, set to ALLOW, if false, set to DENY
     */
    public boolean wgUseAdminShopValue() { return wgUseAdminShopValue; }

    /**
     * Gets the WorldGuard flag name for using Player shops
     * @return WorldGuard flag name
     */
    public String wgUsePlayerShopFlag() { return wgUsePlayerShopFlag; }

    /**
     * Gets the default value to use for WorldGuard flag for using Player Shops
     * @return If true, set to ALLOW, if false, set to DENY
     */
    public boolean wgUsePlayerShopValue() { return wgUsePlayerShopValue; }

    /**
     * Gets the WorldGuard flag name for using Clan shops
     * @return WorldGuard flag name
     */
    public String wgUseClanShopFlag() { return wgUseClanShopFlag; }

    /**
     * Gets the default value to use for WorldGuard flag for using Clan Shops
     * @return If true, set to ALLOW, if false, set to DENY
     */
    public boolean wgUseClanShopValue() { return wgUseClanShopValue; }

    /**
     * Gets if shops should ONLY be allowed in WorldGuard regions (NOT including __global__)
     * @return If true, shops can only be created in WorldGuard regions
     */
    public boolean wgOnlyShops() { return wgOnlyShops; }

    public int getShopCleanupSeconds() {
        return shopCleanupSeconds;
    }

    public boolean isShopCleanupDelete() {
        return shopCleanupDelete;
    }

    /**
     * Gets the hologram plugin to use
     * @return Hologram Type
     */
    public HologramType getHologramType() {
        return hologramType;
    }

    public boolean isShowOutOfStock() {
        return showOutOfStock;
    }

    public boolean isShowOutOfStockPrice() {
        return showOutOfStockPrice;
    }


    public boolean isShowEnchantments() {
        return showEnchantments;
    }

    public boolean isShowPotionType() { return showPotionType; }

    public boolean isShowOnlyIfTargeted() {
        return showOnlyIfTargeted;
    }

    public boolean isShowIconAlwaysTextIfTargeted() { return showIconAlwaysTextIfTargeted; }

    public boolean isUseLocalTranslate() { return useLocalTranslate; }

    public int getShopTargetTicks() { return shopTargetTicks; }

    public String getCommandShopAlias() {
        if (StringFunctions.isNullOrEmpty(commandShopAlias)) {
            return "";
        }
        return commandShopAlias;
    }

    public String getCommandShopCreateAdmin() {
        if (StringFunctions.isNullOrEmpty(commandShopCreateAdmin)) {
            return "admin";
        } else {
            return commandShopCreateAdmin;
        }
    }

    public String getCommandShopCreateServer() {
        if (StringFunctions.isNullOrEmpty(commandShopCreateServer)) {
            return "server";
        } else {
            return commandShopCreateServer;
        }
    }

    public String getCommandShopCreateClan() {
        if (StringFunctions.isNullOrEmpty(commandShopCreateClan)) {
            return "clan";
        } else {
            return commandShopCreateClan;
        }
    }

    public String getCommandShopCreateCooldown() {
        if (StringFunctions.isNullOrEmpty(commandShopCreateCooldown)) {
            return "c";
        } else {
            return commandShopCreateCooldown;
        }
    }

    public String getCommandShopCreate() {
        if (StringFunctions.isNullOrEmpty(commandShopCreate)) {
            return "create";
        } else {
            return commandShopCreate;
        }
    }

    public String getCommandShopRemove() {
        if (StringFunctions.isNullOrEmpty(commandShopRemove)) {
            return "remove";
        } else {
            return commandShopRemove;
        }
    }

    public String getCommandShopCancel() {
        if (StringFunctions.isNullOrEmpty(commandShopCancel)) {
            return "cancel";
        } else {
            return commandShopCancel;
        }
    }

    public String getCommandShopDebug() {
        if (StringFunctions.isNullOrEmpty(commandShopDebug)) {
            return "debug";
        } else {
            return commandShopDebug;
        }
    }

    public String getCommandShopHelp() {
        if (StringFunctions.isNullOrEmpty(commandShopHelp)) {
            return "help";
        } else {
            return commandShopHelp;
        }
    }

    public String getCommandShopReload() {
        if (StringFunctions.isNullOrEmpty(commandShopReload)) {
            return "reload";
        } else {
            return commandShopReload;
        }
    }

    public String getCommandShopVersion() {
        if (StringFunctions.isNullOrEmpty(commandShopVersion)) {
            return "version";
        } else {
            return commandShopVersion;
        }
    }

    public String getCommandShopTransferToDB() {
        if (StringFunctions.isNullOrEmpty(commandShopTransferToDB)) {
            return "transfer_to_database";
        } else {
            return commandShopTransferToDB;
        }
    }

    public String getCommandShopTransferToFile() {
        if (StringFunctions.isNullOrEmpty(commandShopTransferToFile)) {
            return "transfer_to_file";
        } else {
            return commandShopTransferToFile;
        }
    }

    public boolean isBuySellSafeguard() {
        return buySellSafeguard;
    }

    //endregion

    //region Methods
    /**
     * Loads the config from config.yml using the YmlParser class
     */
    public void load() {
        //region Debug Logging
        YmlParser.ConfigReturn crDebugFlag = ymlParser.getBooleanValue(
                "debug_logging",
                false,
                false
        );
        if (crDebugFlag.success()) {
            this.debugLog = crDebugFlag.getBoolean();
        }
        //endregion
        //region Debug Logging to File
        YmlParser.ConfigReturn crDebugToFile = ymlParser.getBooleanValue(
                "debug_to_file",
                false,
                false
        );
        if (crDebugToFile.success()) {
            this.debugLogToFile = crDebugToFile.getBoolean();
        }
        //endregion
        //region Save History
        YmlParser.ConfigReturn crSaveHistory = ymlParser.getBooleanValue(
                "save_history",
                false,
                false
        );
        if (crSaveHistory.success()) {
            this.saveHistory = crSaveHistory.getBoolean();
        }
        //endregion
        //region Shop Create Timeout
        YmlParser.ConfigReturn crShopEditTimeout = ymlParser.getIntValue(
                "shop_edit_timeout",
                15,
                false
        );
        if (crShopEditTimeout.success()) {
            this.shopEditTimeout = crShopEditTimeout.getInt();
        }
        //endregion
        YmlParser.ConfigReturn crDefaultCooldown = ymlParser.getIntValue(
                "default_cooldown",
                1,
                false
        );
        if (crDefaultCooldown.success()) {
            this.defaultCooldown = crDefaultCooldown.getInt();
        }
        //region Left/Right Click Action
        YmlParser.ConfigReturn crLeftClick = ymlParser.getStringValue(
                "click.left",
                "BUY",
                false
        );
        if (crLeftClick.success()) {
            if (crLeftClick.getString().equalsIgnoreCase("buy")) {
                this.leftClick = ShopAction.BUY_FROM_SHOP;
            } else if (crLeftClick.getString().equalsIgnoreCase("sell")) {
                this.leftClick = ShopAction.SELL_TO_SHOP;
            }
        }
        YmlParser.ConfigReturn crRightClick = ymlParser.getStringValue(
                "click.right",
                "SELL",
                false
        );
        if (crRightClick.success()) {
            if (crRightClick.getString().equalsIgnoreCase("buy")) {
                this.rightClick = ShopAction.BUY_FROM_SHOP;
            } else if (crRightClick.getString().equalsIgnoreCase("sell")) {
                this.rightClick = ShopAction.SELL_TO_SHOP;
            }
        }
        // If the user has set them both the same, override to defaults
        if (leftClick == rightClick) {
            instance.getLog().log("BazaarConfig", "Left/Right Click", "Left click and right click cannot be the same");
            leftClick = ShopAction.BUY_FROM_SHOP;
            rightClick = ShopAction.SELL_TO_SHOP;
        }
        //endregion
        //region Message Prefix
        YmlParser.ConfigReturn crMessagePrefix = ymlParser.getStringValue(
                "message_prefix",
                "&6[&fBazaar&6]&r",
                false
        );
        if (crMessagePrefix.success()) {
            this.messagePrefix = crMessagePrefix.getString();
        }
        //endregion
        //region Currency Symbol
        YmlParser.ConfigReturn crCurrencySymbol = ymlParser.getStringValue(
                "currency_symbol",
                "$",
                false
        );
        if (crCurrencySymbol.success()) {
            this.currencySymbol = crCurrencySymbol.getString();
        }
        //endregion
        //region Shop Create Cost
        YmlParser.ConfigReturn crCreateCost = ymlParser.getIntValue(
                "create_cost",
                5,
                false
        );
        if (crCreateCost.success()) {
            this.costCreate = crCreateCost.getInt();
        }
        //endregion
        //region Clan Shop Create Cost
        YmlParser.ConfigReturn crCreateClanCost = ymlParser.getIntValue(
                "create_cost_clan",
                5,
                false
        );
        if (crCreateClanCost.success()) {
            this.costCreateClan = crCreateClanCost.getInt();
        }
        //endregion
        //region Sales Tax
        YmlParser.ConfigReturn crSalesTax = ymlParser.getIntValue(
                "sales_tax",
                25,
                false
        );
        if (crSalesTax.success()) {
            this.salesTax = crSalesTax.getInt();
        }
        //endregion
        //region Banned Words
        YmlParser.ConfigReturn crBannedWords = ymlParser.getStringList(
                "banned_words",
                false
        );
        if (crBannedWords.success()) {
            this.bannedWords = crBannedWords.getStringList();
        }
        //endregion
        //region Invalid Worlds
        YmlParser.ConfigReturn crInvalidWorlds = ymlParser.getStringList(
                "invalid_worlds",
                false
        );
        if (crInvalidWorlds.success()) {
            List<String> invalidWorldNames = crInvalidWorlds.getStringList();
            if (invalidWorldNames != null) {
                this.invalidWorlds = new ArrayList<>();
                for (String worldName : invalidWorldNames) {
                    World invalidWorld = Bukkit.getWorld(worldName);
                    if (invalidWorld != null) {
                        invalidWorlds.add(invalidWorld);
                    }
                }
            }
        }
        //endregion
        //region Invalid Materials
        YmlParser.ConfigReturn crInvalidMaterials = ymlParser.getStringList(
                "invalid_items",
                false
        );
        if (crInvalidMaterials.success()) {
            List<String> invalidMaterialNames = crInvalidMaterials.getStringList();
            if (invalidMaterialNames != null) {
                this.invalidMaterials = new ArrayList<>();
                for (String materialName : invalidMaterialNames) {
                    Material material = Material.getMaterial(materialName);
                    if (material != null) {
                        invalidMaterials.add(material);
                    }
                }
            }
        }
        //endregion
        //region Economy Server Account
        String serverAccount;
        UUID serverAccountUUID;
        YmlParser.ConfigReturn crServerAccount = ymlParser.getStringValue(
                "server_account.account",
                "",
                false
        );
        if (crServerAccount.success()) {
            serverAccount = crServerAccount.getString();
            serverAccountUUID = StringFunctions.uuidFromString(serverAccount);
            if (serverAccountUUID != null) {
                offlinePlayerAccount = Bukkit.getOfflinePlayer(serverAccountUUID);
            } else {
                // Deprecated use
                offlinePlayerAccount = Bukkit.getOfflinePlayer(serverAccount);
            }
        }
        YmlParser.ConfigReturn crServerAcctInfiniteStock = ymlParser.getBooleanValue(
                "server_account.infinite_stock",
                false,
                false
        );
        if (crServerAcctInfiniteStock.success()) {
            this.serverAccountInfiniteStock = crServerAcctInfiniteStock.getBoolean();
        }
        //endregion
        //region Display Out of Stock
        YmlParser.ConfigReturn crDisplayOutOfStock = ymlParser.getBooleanValue(
                "display_out_of_stock",
                false,
                false
        );
        if (crDisplayOutOfStock.success()) {
            this.displayOutOfStock = crDisplayOutOfStock.getBoolean();
        }
        //endregion
        //region Message Seller on Purchase
        YmlParser.ConfigReturn crMessageSeller = ymlParser.getBooleanValue(
                "message_seller_on_purchase",
                false,
                false
        );
        if (crMessageSeller.success()) {
            this.messageSellerOnPurchase = crMessageSeller.getBoolean();
        }
        //endregion
        YmlParser.ConfigReturn crLogToConsole = ymlParser.getBooleanValue(
                "log_sales_to_console",
                false,
                false
        );
        if (crLogToConsole.success()) {
            this.logSalesToConsole = crLogToConsole.getBoolean();
        }
        //region Domain Protection
        YmlParser.ConfigReturn crDomainProtection = ymlParser.getBooleanValue(
                "domain_protection",
                false,
                false
        );
        if (crDomainProtection.success()) {
            this.domainProtection = crDomainProtection.getBoolean();
        }
        //endregion
        //region WorldGuard
        //region WorldGuard Protection
        YmlParser.ConfigReturn crWGProtection = ymlParser.getBooleanValue(
                "worldguard_protection",
                false,
                false
        );
        if (crWGProtection.success()) {
            this.worldguardProtection = crWGProtection.getBoolean();
        }
        //endregion
        //region Create Shop
        YmlParser.ConfigReturn crWGCreateShopFlag = ymlParser.getStringValue(
                "worldguard.create_shop.flag",
                "create-shop",
                false
        );
        if (crWGCreateShopFlag.success()) {
            this.wgCreateShopFlag = crWGCreateShopFlag.getString();
        }
        YmlParser.ConfigReturn crWGCreateShopDefault = ymlParser.getBooleanValue(
                "worldguard.create_shop.default",
                false,
                false
        );
        if (crWGCreateShopDefault.success()) {
            this.wgCreateShopValue = crWGCreateShopDefault.getBoolean();
        }
        //endregion
        //region Use Admin Shop
        YmlParser.ConfigReturn crWGUseAdminFlag = ymlParser.getStringValue(
                "worldguard.use_admin_shop.flag",
                "use-admin-shop",
                false
        );
        if (crWGUseAdminFlag.success()) {
            this.wgUseAdminShopFlag = crWGUseAdminFlag.getString();
        }
        YmlParser.ConfigReturn crWGUseAdminDefault = ymlParser.getBooleanValue(
                "worldguard.use_admin_shop.default",
                false,
                false
        );
        if (crWGUseAdminDefault.success()) {
            this.wgUseAdminShopValue = crWGUseAdminDefault.getBoolean();
        }
        //endregion
        //region Use Clan Shop
        YmlParser.ConfigReturn crWGUseClanFlag = ymlParser.getStringValue(
                "worldguard.use_clan_shop.flag",
                "use-clan-shop",
                false
        );
        if (crWGUseClanFlag.success()) {
            this.wgUseClanShopFlag = crWGUseClanFlag.getString();
        }
        YmlParser.ConfigReturn crWGUseClanDefault = ymlParser.getBooleanValue(
                "worldguard.use_clan_shop.default",
                false,
                false
        );
        if (crWGUseClanDefault.success()) {
            this.wgUseClanShopValue = crWGUseClanDefault.getBoolean();
        }
        //endregion
        //region Use Player Shop
        YmlParser.ConfigReturn crWGUsePlayerFlag = ymlParser.getStringValue(
                "worldguard.use_player_shop.flag",
                "use-shop",
                false
        );
        if (crWGUsePlayerFlag.success()) {
            this.wgUsePlayerShopFlag = crWGUsePlayerFlag.getString();
        }
        YmlParser.ConfigReturn crWGUsePlayerDefault = ymlParser.getBooleanValue(
                "worldguard.use_player_shop.default",
                false,
                false
        );
        if (crWGUsePlayerDefault.success()) {
            this.wgUsePlayerShopValue = crWGUsePlayerDefault.getBoolean();
        }
        //endregion
        //region WorldGuard Region Shops Only
        YmlParser.ConfigReturn crWGRegionShopsOnly = ymlParser.getBooleanValue(
                "worldguard_only_shops",
                false,
                false
        );
        if (crWGRegionShopsOnly.success()) {
            this.wgOnlyShops = crWGRegionShopsOnly.getBoolean();
        }
        //endregion

        YmlParser.ConfigReturn crShopCleanupSeconds = ymlParser.getIntValue(
                "shop_cleanup.seconds",
                0,
                false
        );
        if (crShopCleanupSeconds.success()) {
            this.shopCleanupSeconds = crShopCleanupSeconds.getInt();
        }
        YmlParser.ConfigReturn crShopCleanupDelete = ymlParser.getBooleanValue(
                "shop_cleanup.delete",
                false,
                false
        );
        if (crShopCleanupDelete.success()) {
            this.shopCleanupDelete = crShopCleanupDelete.getBoolean();
        }


        //endregion
        //region Disable Shop Command
        YmlParser.ConfigReturn crDisableShopCommand = ymlParser.getBooleanValue(
                "disable_shop_command",
                false,
                false
        );
        if (crDisableShopCommand.success()) {
            this.disableShopCommand = crDisableShopCommand.getBoolean();
        }
        //endregion
        //region Hologram
        // 0 = None, 1 = DecentHolograms, 2 = HolographicDisplays, 3 = CMI
        YmlParser.ConfigReturn crHologramType = ymlParser.getIntValue(
                "hologram.plugin",
                0,
                false
        );
        if (crHologramType.success()) {
            switch (crHologramType.getInt()) {
                case 1:
                    this.hologramType = HologramType.DECENT_HOLOGRAMS;
                    break;
                default:
                    this.hologramType = HologramType.NONE;
                    break;
            }
        }

        YmlParser.ConfigReturn crHologramShowOOS = ymlParser.getBooleanValue(
                "hologram.show_out_of_stock",
                false,
                false
        );
        if (crHologramShowOOS.success()) {
            this.showOutOfStock = crHologramShowOOS.getBoolean();
        }

        YmlParser.ConfigReturn crHologramShowOOSPrice = ymlParser.getBooleanValue(
                "hologram.show_price_out_of_stock",
                false,
                false
        );
        if (crHologramShowOOSPrice.success()) {
            this.showOutOfStockPrice = crHologramShowOOSPrice.getBoolean();
        }

        YmlParser.ConfigReturn crHologramShowTargeted = ymlParser.getBooleanValue(
                "hologram.show_only_if_targeted",
                false,
                false
        );
        if (crHologramShowTargeted.success()) {
            this.showOnlyIfTargeted = crHologramShowTargeted.getBoolean();
        }

        YmlParser.ConfigReturn crHologramShowIconAlwaysTargeted = ymlParser.getBooleanValue(
                "hologram.show_icon_always_text_if_targeted",
                false,
                false
        );
        if (crHologramShowIconAlwaysTargeted.success()) {
            this.showIconAlwaysTextIfTargeted = crHologramShowIconAlwaysTargeted.getBoolean();
        }

        YmlParser.ConfigReturn crHologramShowEnchants = ymlParser.getBooleanValue(
                "hologram.show_enchantments",
                false,
                false
        );
        if (crHologramShowEnchants.success()) {
            this.showEnchantments = crHologramShowEnchants.getBoolean();
        }

        YmlParser.ConfigReturn crHologramShowPotionType = ymlParser.getBooleanValue(
                "hologram.show_potion_type",
                false,
                false
        );
        if (crHologramShowPotionType.success()) {
            this.showPotionType = crHologramShowPotionType.getBoolean();
        }

        //endregion

        YmlParser.ConfigReturn crMySQLUse = ymlParser.getBooleanValue(
                "mysql.use",
                false,
                false
        );
        if (crMySQLUse.success()) {
            this.useMySQL = crMySQLUse.getBoolean();
            if (useMySQL) {
                YmlParser.ConfigReturn crMySQLHost = ymlParser.getStringValue(
                        "mysql.host",
                        "",
                        false
                );
                if (crMySQLHost.success()) {
                    this.mysqlHost = crMySQLHost.getString();
                }
                YmlParser.ConfigReturn crMySQLPort = ymlParser.getIntValue(
                        "mysql.port",
                        3306,
                        false
                );
                if (crMySQLPort.success()) {
                    this.mysqlPort = crMySQLPort.getInt();
                }
                YmlParser.ConfigReturn crMySQLDatabase = ymlParser.getStringValue(
                        "mysql.database",
                        "",
                        false
                );
                if (crMySQLDatabase.success()) {
                    this.mysqlDatabase = crMySQLDatabase.getString();
                }
                YmlParser.ConfigReturn crMySQLUsername = ymlParser.getStringValue(
                        "mysql.username",
                        "",
                        false
                );
                if (crMySQLUsername.success()) {
                    this.mysqlUsername = crMySQLUsername.getString();
                }
                YmlParser.ConfigReturn crMySQLPassword = ymlParser.getStringValue(
                        "mysql.password",
                        "",
                        false
                );
                if (crMySQLPassword.success()) {
                    this.mysqlPassword = crMySQLPassword.getString();
                }
                YmlParser.ConfigReturn crMySQLUseSSL = ymlParser.getBooleanValue(
                        "mysql.use_ssl",
                        false,
                        false
                );
                if (crMySQLUseSSL.success()) {
                    this.mysqlUseSSL = crMySQLUseSSL.getBoolean();
                }
                if (
                        StringFunctions.isNullOrEmpty(mysqlHost) ||
                                StringFunctions.isNullOrEmpty(mysqlDatabase) ||
                                StringFunctions.isNullOrEmpty(mysqlUsername) ||
                                StringFunctions.isNullOrEmpty(mysqlPassword)
                ) {
                    // Unable to use MySQL - invalid config
                    this.useMySQL = false;
                }
            }
        }

        YmlParser.ConfigReturn crUseLocalTranslate = ymlParser.getBooleanValue(
                "use_local_translate",
                false,
                false
        );
        if (crUseLocalTranslate.success()) {
            if (GameFunctions.isVersionSafe(1.17)) {
                this.useLocalTranslate = crUseLocalTranslate.getBoolean();
            }
        }

        YmlParser.ConfigReturn crShopTargetTicks = ymlParser.getIntValue(
                "shop_target_ticks",
                20,
                false
        );
        if (crShopTargetTicks.success()) {
            this.shopTargetTicks = crShopTargetTicks.getInt();
            if (shopTargetTicks < 5) {
                shopTargetTicks = 5;
            }
        }

        YmlParser.ConfigReturn crCmdShopAlias = ymlParser.getStringValue(
                "commands.shop.alias",
                "",
                false
        );
        if (crCmdShopAlias.success()) {
            this.commandShopAlias  = crCmdShopAlias.getString();
        }
        YmlParser.ConfigReturn crCmdShopCreate = ymlParser.getStringValue(
                "commands.shop.create",
                "",
                false
        );
        if (crCmdShopCreate.success()) {
            this.commandShopCreate   = crCmdShopCreate.getString();
        }
        YmlParser.ConfigReturn crCmdShopCreateAdmin = ymlParser.getStringValue(
                "commands.shop.create_admin",
                "",
                false
        );
        if (crCmdShopCreateAdmin.success()) {
            this.commandShopCreateAdmin   = crCmdShopCreateAdmin.getString();
        }
        YmlParser.ConfigReturn crCmdShopCreateServer = ymlParser.getStringValue(
                "commands.shop.create_server",
                "",
                false
        );
        if (crCmdShopCreateServer.success()) {
            this.commandShopCreateServer   = crCmdShopCreateServer.getString();
        }
        YmlParser.ConfigReturn crCmdShopCreateClan = ymlParser.getStringValue(
                "commands.shop.create_clan",
                "",
                false
        );
        if (crCmdShopCreateClan.success()) {
            this.commandShopCreateClan   = crCmdShopCreateClan.getString();
        }
        YmlParser.ConfigReturn crCmdShopCreateCooldown = ymlParser.getStringValue(
                "commands.shop.create_cooldown",
                "",
                false
        );
        if (crCmdShopCreateCooldown.success()) {
            this.commandShopCreateCooldown   = crCmdShopCreateCooldown.getString();
        }
        YmlParser.ConfigReturn crCmdShopRemove = ymlParser.getStringValue(
                "commands.shop.remove",
                "",
                false
        );
        if (crCmdShopRemove.success()) {
            this.commandShopRemove  = crCmdShopRemove.getString();
        }
        YmlParser.ConfigReturn crCmdShopCancel = ymlParser.getStringValue(
                "commands.shop.cancel",
                "",
                false
        );
        if (crCmdShopCancel.success()) {
            this.commandShopCancel  = crCmdShopCancel.getString();
        }
        YmlParser.ConfigReturn commandShopDebug = ymlParser.getStringValue(
                "commands.shop.debug",
                "",
                false
        );
        if (commandShopDebug .success()) {
            this.commandShopDebug  = commandShopDebug .getString();
        }
        YmlParser.ConfigReturn crCmdShopHelp = ymlParser.getStringValue(
                "commands.shop.help",
                "",
                false
        );
        if (crCmdShopHelp.success()) {
            this.commandShopHelp  = crCmdShopHelp.getString();
        }
        YmlParser.ConfigReturn crCmdShopReload = ymlParser.getStringValue(
                "commands.shop.reload",
                "",
                false
        );
        if (crCmdShopReload.success()) {
            this.commandShopReload  = crCmdShopReload.getString();
        }
        YmlParser.ConfigReturn crCmdShopVersion = ymlParser.getStringValue(
                "commands.shop.version",
                "",
                false
        );
        if (crCmdShopVersion.success()) {
            this.commandShopVersion  = crCmdShopVersion.getString();
        }
        YmlParser.ConfigReturn crCmdShopTransferToDb = ymlParser.getStringValue(
                "commands.shop.transfer_to_database",
                "",
                false
        );
        if (crCmdShopTransferToDb.success()) {
            this.commandShopTransferToDB = crCmdShopTransferToDb.getString();
        }
        YmlParser.ConfigReturn crCmdShopTransferToFile = ymlParser.getStringValue(
                "commands.shop.transfer_to_file",
                "",
                false
        );
        if (crCmdShopTransferToFile.success()) {
            this.commandShopTransferToFile  = crCmdShopTransferToFile.getString();
        }

        YmlParser.ConfigReturn crBuySellSafeguard = ymlParser.getBooleanValue(
                "buy_sell_safeguard",
                true,
                false
        );
        if (crBuySellSafeguard.success()) {
            this.buySellSafeguard = crBuySellSafeguard.getBoolean();
        }
    }

    /**
     * Toggles the Debug log flag
     */
    public void toggleDebugLog() {
        this.debugLog = !debugLog;
    }
    //endregion
}
