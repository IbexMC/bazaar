package com.ibexmc.bazaar;

import com.ibexmc.bazaar.api.BazaarAPI;
import com.ibexmc.bazaar.gui.InfoGUI;
import com.ibexmc.bazaar.hologram.HologramManager;
import com.ibexmc.bazaar.listeners.*;
import com.ibexmc.bazaar.scheduled.RemoveShops;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.util.functions.FileFunctions;
import com.ibexmc.internal.Internal;
import com.ibexmc.internal.data.Data;
import com.ibexmc.internal.functions.StringFunctions;
import com.ibexmc.internal.messaging.*;
import com.ibexmc.internal.messaging.Error;
import com.ibexmc.internal.sql.MySQL;
import com.ibexmc.internal.utility.CommandAdder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import com.ibexmc.bazaar.commands.ShopCommand;
import com.ibexmc.bazaar.livedata.BazaarData;
import com.ibexmc.bazaar.livedata.Dependencies;
import com.ibexmc.bazaar.livedata.WorldGuard;
import com.ibexmc.bazaar.scheduled.Cleanup;
import com.ibexmc.bazaar.scheduled.ShopTarget;
import com.ibexmc.bazaar.storage.yaml.FileAccess;
import com.ibexmc.bazaar.util.BazaarConfig;

import java.io.File;
import java.util.Map;

public class Bazaar extends JavaPlugin {

    private static Bazaar instance;
    private String displayName = "Bazaar";
    private String internalName = "bazaar";
    private Locale locale = null;
    private BazaarConfig config;
    private Message message;
    private final Dependencies dependencies = new Dependencies();
    private BazaarAPI api = new BazaarAPI();
    private HologramManager hologramManager;
    private Data internalData;
    private MySQL mysql = null;
    private Internal ibexInternal = new Internal(this, "Bazaar");
    private Debug debug;
    private Log log= new Log(this, "Bazaar");
    private Error error = new Error(this, "Bazaar");

    BazaarData bazaarData = new BazaarData();

    public String getDisplayName() {
        return displayName;
    }
    public String getInternalName() {
        return internalName;
    }
    public Locale getLocale() { return locale; }
    public Message getMessage() { return this.message; }
    public BazaarAPI getApi() {
        return this.api;
    }
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    public Data getInternalData() {
        return this.internalData;
    }
    public MySQL getMySQL() { return mysql; }
    public void setMySQL(MySQL mySQL) { this.mysql = mySQL; }
    public Internal getIbexInternal() { return this.ibexInternal; }
    public Debug getDebug() { return this.debug; }
    public void toggleDebug() {
        internalData.setDebugMode(!internalData.getDebugMode());
        debug = new Debug(this, "Bazaar", internalData);
    }
    public Log getLog() { return this.log; }
    public Error getError() { return this.error; }

    /**
     * Gets the current instance
     * @return Bazaar instance
     */
    public static Bazaar getInstance() {
        return instance;
    }

    /**
     * Gets the Bazaar config from config.yml
     * @return Bazaar Config
     */
    public BazaarConfig getBazaarConfig() {

        return this.config;
    }

    /**
     * Sets the Bazaar config
     * @param config Bazaar Config
     */
    public void setBazaarConfig(BazaarConfig config) {
        this.config = config;
    }

    /**
     * Gets the Live data
     * @return Live Data
     */
    public BazaarData getBazaarData() { return this.bazaarData; }

    /**
     * Gets the dependencies
     * @return Bazaar Dependencies
     */
    public Dependencies getDependencies() {
        return dependencies;
    }

    /**
     * Registers the /bazaar and /shop commands
     */
    private void loadCommands() {
        this.getDebug().log(
                "Bazaar",
                "loadCommands",
                "Loading Commands"
        );
        try {
            if (!this.config.disableShopCommand()) {
                // Allow /shop to be disabled in case it hits against another plugin
                getCommand("shop").setExecutor(new ShopCommand(this));
            }
            getCommand("bazaar").setExecutor(new ShopCommand(this));
        } catch (Exception ex) {
            this.getError().save(
                    "002",
                    "Bazaar",
                    "loadCommands",
                    "Unexpected Error",
                    ex.getMessage(),
                    Error.Severity.WARN,
                    ex.getStackTrace()
            );
        }
    }

    /**
     * Registers the listeners
     */
    private void loadEventListeners() {
        this.getDebug().log("Bazaar", "loadEventListeners()", "Starting Event Listeners");
        try {
            Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
            Bukkit.getPluginManager().registerEvents(new EntityEventListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
            Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        } catch (Exception ex) {
            this.getLog().log("Load Event Listeners - Unexpected Error - " + ex.getMessage());
        }
    }

    /**
     * Starts the cleanup scheduled task
     */
    private void startCleanup() {
        // Cooldowns should automatically clean themselves up - but if not, every 30 seconds
        // run this cleanup to double check
        try {
            BukkitTask task = new BukkitRunnable() {
                public void run() {
                    Cleanup cleanup = new Cleanup(Bazaar.getInstance());
                    cleanup.Run();
                }
            }.runTaskTimer(this, 0, 30 * 20);
            this.getDebug().log("Bazaar", "startCleanup", "startCleanup running with id " + task.getTaskId());
        } catch (Exception ex) {
            this.getDebug().log("Bazaar", "startCleanup", "Unexpected Error - " + ex.getMessage());
        }
    }

    /**
     * Starts the player targeting shops scheduled task
     */
    private void startShopTarget(int ticks) {
        this.getLog().log("Running getShopTargetTicks - Every " + ticks + " ticks");
        try {
            BukkitTask task = new BukkitRunnable() {
                public void run() {
                    ShopTarget shopTarget = new ShopTarget(Bazaar.getInstance());
                    shopTarget.Run();
                }
            }.runTaskTimer(this, 0, ticks);
            this.getDebug().log("Bazaar", "startShopTarget", "startShopTarget running with id " + task.getTaskId());
        } catch (Exception ex) {
            this.getDebug().log("Bazaar", "startShopTarget", "Unexpected Error - " + ex.getMessage());
        }
    }

    /**
     * Starts the cleanup shop scheduled task
     */
    private void startShopCleanup(int ticks) {
        this.getLog().log("Running startShopCleanup - Every " + ticks + " ticks");
        try {
            BukkitTask task = new BukkitRunnable() {
                public void run() {
                    RemoveShops removeShops = new RemoveShops(Bazaar.getInstance());
                    removeShops.Run();
                }
            }.runTaskTimer(this, 0, ticks);
            this.getDebug().log("Bazaar", "startShopCleanup", "startShopCleanup running with id " + task.getTaskId());
        } catch (Exception ex) {
            this.getDebug().log("Bazaar", "startShopCleanup", "Unexpected Error - " + ex.getMessage());
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        internalData = new com.ibexmc.internal.data.Data(this);
        internalData.setDebugMode(false);
        config = new BazaarConfig(this);
        if (dependencies.hookWorldGuard()) {
            this.getLog().log("Bazaar", "onLoad","&aWorldGuard dependency found");
            WorldGuard.registerFlags();
        } else {
            this.getLog().log("Bazaar", "onLoad", "&cWorldGuard dependency not found");
        }
    }
    @Override
    public void onEnable() {


        bazaarData.loadLanguage(); // Load the language file
        ibexInternal = new Internal(this, internalName);
        internalData = new com.ibexmc.internal.data.Data(this);
        internalData.setDebugMode(false);
        File configFile = FileFunctions.getConfigYml();
        if (configFile.exists()) {
            config = new BazaarConfig(this);
            internalData.setDebugMode(getBazaarConfig().debugLog());
        } else {
            saveDefaultConfig();
            config = new BazaarConfig(this);
        }

        internalData.putHeader("Bazaar", getBazaarConfig().messagePrefix());

        debug = new Debug(instance, "Bazaar", internalData);

        locale = new Locale(this, "Bazaar", internalData);
        locale.load("Bazaar", this.getDataFolder() + File.separator + "lang.yml");
        message = new Message(instance, "Bazaar", internalData, locale);

        log = new Log(instance, "Bazaar");
        error = new Error(instance, "Bazaar");

        log.log("&e[>]&1 ==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1== &e[<]");
        log.log("                  &f" + displayName + " - " + getDescription().getVersion());
        log.log("&e[>]&1 ==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1==&f==&1== &e[<]");

        loadCommands();

        // Load Vault
        if (!dependencies.setupEconomy() ) {
            this.getLog().log("Bazaar", "Load Vault Error", StringFunctions.color("&cDISABLING PLUGIN &r- Vault & Economy plugin are &cREQUIRED&r for Bazaar use"));
            this.getBazaarData().setEnabled(false);
            dependencies.setVault(false);
        } else {
            this.getLog().log("Bazaar - &aVault dependency found");
            dependencies.setVault(true);
        }

        if (bazaarData.isEnabled()) {

            if (dependencies.hookDomain()) {
                this.getLog().log("Bazaar", "Dependency Found", "&aDomain dependency found");
            } else {
                this.getDebug().log("Bazaar", "onEnable", "&cMyriad Clans dependency not found - Clan shops will not function.  Install Myriad Clans to enable this functionality");
            }
            if (dependencies.hookMyriadClans()) {
                this.getLog().log("Bazaar", "Dependency Found", "&aMyriad Clans dependency found");
            } else {
                this.getDebug().log("Bazaar", "onEnable", "&cMyriad Clans dependency not found - Clan shops will not function.  Install Myriad Clans to enable this functionality");
            }

            switch (getBazaarConfig().getHologramType()) {
                case DECENT_HOLOGRAMS:
                    if (dependencies.hasDecentHolograms()) {
                        this.getLog().log("Bazaar", "Dependency Found", "&aDecent Holograms found");
                        bazaarData.setHasDecentHolograms(true);
                    } else {
                        this.getLog().log("Bazaar", "Dependency NOT Found", "&cDecent Holograms not found - Shop holograms will not show.  Either install Decent Holograms or change the hologram type in config.yml");
                    }
                    break;
            }


            if (dependencies.hasWorldGuard()) {
                this.getLog().log("Bazaar", "WorldGuard", "&aRegistering WorldGuard listener");
                Bukkit.getPluginManager().registerEvents(new WorldGuard(), this);
            }





            if (config.useMySQL()) {
                mysql = ibexInternal.getMySQL(
                        config.getMySQLHost(),
                        "" + config.getMySQLPort(),
                        config.getMySQLDatabase(),
                        config.getMySQLUsername(),
                        config.getMySQLPassword(),
                        config.getMySQLUseSSL()
                );
            }

            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        @Override
                        public void run() {
                            if (Bazaar.getInstance().getBazaarConfig().useMySQL()) {
                                com.ibexmc.bazaar.storage.db.MySQL mySQL = new com.ibexmc.bazaar.storage.db.MySQL(Bazaar.getInstance());
                                mySQL.checkTable();
                                mySQL.loadPlayerShops(false);
                            } else {
                                FileAccess fileAccess = new FileAccess(Bazaar.getInstance());
                                boolean loadFile = true;
                                for (Map.Entry<Location, Shop> chestEntry : bazaarData.getShops().entrySet()) {
                                    // Save the entry to a file
                                    fileAccess.savePlayerShop(chestEntry.getValue());
                                    loadFile = false;
                                }
                                if (loadFile) {
                                    fileAccess.loadPlayerShops();
                                }
                            }
                        }
                    }
            );

            this.api = new BazaarAPI(this);

            loadEventListeners();

            this.hologramManager = new HologramManager(this);

            startCleanup(); // Start the cleanup process

            int shopTargetTicks = getBazaarConfig().getShopTargetTicks();
            boolean showOnlyIfTargeted = getBazaarConfig().isShowOnlyIfTargeted();
            this.getDebug().log("Bazaar", "onEnable", "Shop Target Ticks = " + shopTargetTicks);
            this.getDebug().log("Bazaar", "onEnable", "Show only if targeted = " + showOnlyIfTargeted);
            if (showOnlyIfTargeted) {
                startShopTarget(shopTargetTicks);
            }
            int shopCleanupSeconds = getBazaarConfig().getShopCleanupSeconds();
            if (shopCleanupSeconds > 0) {
                startShopCleanup(shopCleanupSeconds * 20);
            }

            if (!StringFunctions.isNullOrEmpty(getBazaarConfig().getCommandShopAlias())) {
                ShopCommand shopCommand = new ShopCommand(this);
                CommandAdder commandAdder = new CommandAdder();
                commandAdder.addCommand(getBazaarConfig().getCommandShopAlias(), this, shopCommand, shopCommand);
            }

            File file = new File(
                    Bazaar.getInstance().getDataFolder() +
                            File.separator + "gui" + File.separator + "info.yml");
            InfoGUI infoGUI = new InfoGUI("info");
            infoGUI.loadGUI(this, file, "Bazaar");
            internalData.putIbexGUI("info", infoGUI.getSlots());
            internalData.putGUIName("info", infoGUI.getTitle());
            internalData.putGUISize("info", infoGUI.getSize());

        }

    }
    @Override
    public void onDisable() {
        //TODO: Save all
    }

}
