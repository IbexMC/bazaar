##1.3.6
Moved CommandAdder to ibex_internal and removed local copy

##1.3.5
Bug Fixes
- | was showing after buy price even when no sell price existed
- Tweaked the config code to address issues with hologram display speed

##1.3.4
Bug Fix
- Startup issue when no Bazaar folder exists

##1.3.3

###New Features & Bug Fixes

Bug fixes & Misc. Changes
- Language not loading via ibex_internal - Corrected
- Moved all Message, Logging, and Locale to ibex_internal
- Created a lines builder in DecentHolograms code
- When using sell price only, price was not listed

Log transactions to console
- Add the following to config.yml to enable

      log_sales_to_console: true

- Add the following to the lang.yml


    console_log_player_shop_buy: "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the shop at <%location%> belonging to <%owner%>"
    console_log_player_clan_buy: "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the clan shop at <%location%> belonging to <%clan%>"
    console_log_player_admin_buy: "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the admin shop at <%location%>"
    console_log_player_server_buy: "<%buyer%> bought <%amount%> of <%itemname%> for <%price%> from the server shop at <%location%>"
    console_log_player_shop_sell: "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the shop at <%location%> belonging to <%owner%>"
    console_log_player_clan_sell: "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the clan shop at <%location%> belonging to <%clan%>"
    console_log_player_admin_sell: "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the admin shop at <%location%>"
    console_log_player_server_sell: "<%seller%> sold <%amount%> of <%itemname%> for <%price%> to the server shop at <%location%>"

Use local translations - Item name/Enchantments can be pulled in local language, 1.17+ only
- Add the following to config.yml to enable

      use_local_translate: true

Defines how many ticks between shop target lookups
- Add the following to config.yml to enable

      shop_target_ticks: 20



Added show text only if targeted option
- Modify the hologram section of config.yml to include the last option


    hologram:
    # This is your choice of hologram plugin.  Currently, only DecentHolograms is supported due
    # to the issues of getting updates for HolographicDisplays, as well as the ProtocolLib
    # requirement for showing when targeted.  HolographicDisplays should still work (<1.20), but will
    # not be supported.  Support may return depending on updates.
    # Additional hologram plugins may be added in the future.
    # Valid entries:
    # 0 = None
    # 1 = DecentHolograms (recommended - https://www.spigotmc.org/resources/decentholograms-1-8-1-20-2-papi-support-no-dependencies.96927/)
    # 2 = HolographicDisplays (currently unsupported as no update past 1.20.0 is available)
    # The selected plugin MUST be installed
    plugin: 1
    # If true, when the item is out of stock, an Out of Stock message will be added to the hologram
    show_out_of_stock: true
    # If true, the price line will show when out of stock
    show_price_out_of_stock: true
    # If true, shows whatever enchantments the item has
    show_enchantments: true
    # If true, the holograms will only show if the shop is targeted
    # This may cause performance issues for large servers as it has to loop through all online players
    # Defaults to false so holograms show at all times.
    show_only_if_targeted: false

    # If true, the holograms will only show if the shop is targeted
    # This may cause performance issues for large servers as it has to loop through all online players
    # Defaults to false so holograms show at all times.
    show_only_if_targeted: false


Added command alias code
- This code allows an additional user defined command to be used instead of /shop and allows aliasing of all arguments
- Add the following to config.yml


    # This section of the config allows you to create an alias (language specific or otherwise) to replace
    # the /shop command and any of the included arguments.  Leaving the entry as a blank string like "" will
    # prevent that argument being aliased at all.
    commands:
    shop:
    # Alias is the command that will replace /shop - If blank, no aliasing will be done
    alias: ""
    # Create is the '/shop create' alias
    create: ""
    # Create_admin is the 'admin' alias used in /shop create -admin
    create_admin: ""
    # Create_server is the 'server' alias used in /shop create -server
    create_server: "server"
    # Create_clan is the 'clan' alias used in /shop create -clan
    create_clan: "clan"
    # Create_cooldown is the 'c' (cooldown) alias used in /shop create -c
    create_cooldown: "c"
    # Remove is the '/shop remove' alias
    remove: ""
    # Cancel is the '/shop cancel' alias
    cancel: ""
    # Debug is the '/shop debug' alias
    debug: ""
    # Help is the '/shop help' alias
    help: ""
    # Reload is the '/shop reload' alias
    reload: ""
    # Version is the '/shop version' alias
    version: ""
    # Transfer_to_database is the '/shop transfer_to_database' alias
    transfer_to_database: ""
    # Transfer_to_file is the '/shop transfer_to_file' alias
    transfer_to_file: ""

##1.3.0

Moved a lot of internal code a shaded utility plugin, reducing duplicating code across projects

Added Decent Holograms support and uses it as default: https://www.spigotmc.org/resources/decentholograms-1-8-1-20-2-papi-support-no-dependencies.96927

Rewrite of Holographic Displays code

Holograms will always show within x blocks by default, change the config option hologram.show_only_if_targeted = true to change this behavior

Added config section for hologram support, including showing out of stock messages, and showing/hiding the price line of holograms if out of stock

    hologram:
    # This is your choice of hologram plugin.  Currently, only DecentHolograms is supported due
    # to the issues of getting updates for HolographicDisplays, as well as the ProtocolLib
    # requirement for showing when targeted.  HolographicDisplays should still work (<1.20), but will
    # not be supported.  Support may return depending on updates.
    # Additional hologram plugins may be added in the future.
    # Valid entries:
    # 0 = None
    # 1 = DecentHolograms (recommended - https://www.spigotmc.org/resources/decentholograms-1-8-1-20-2-papi-support-no-dependencies.96927/)
    # 2 = HolographicDisplays (currently unsupported as no update past 1.20.0 is available)
    # The selected plugin MUST be installed
    plugin: 1
    # If true, when the item is out of stock, an Out of Stock message will be added to the hologram
    show_out_of_stock: true
    # If true, the price line will show when out of stock
    show_price_out_of_stock: true
    # If true, shows whatever enchantments the item has
    show_enchantments: true
    # If true, the holograms will only show if the shop is targeted
    # This may cause performance issues for large servers as it has to loop through all online players
    # Defaults to false so holograms show at all times.
    show_only_if_targeted: false

Added MySQL support, with included config changes:

    # If you wish to save to MySQL database instead of file, make sure to fill this section in with the appropriate
    # information, and set use = true
    # If you have use set to true, your MySQL database MUST be accessible or Bazaar will error out
    # If you do not wish to use MySQL, leave use = false, and the shop data will be saved in your data folder
    mysql:
    # If true, Bazaar will attempt to use the database
    use: false
    # Your hostname/IP address
    host: "your_host"
    # Your MySQL port (default is 3306)
    port: 3306
    # Your database name, defaults to "ibex" but can be any existing database
    # Database MUST exist before running
    database: "ibex"
    # Your username and password must have permission to your database
    username: "your_username"
    password: "your_password"

If switching from File > MySQL or MySQL > File, it is preferred that you recreate your shops, however, there are two unsupported commands:
Both commands REQUIRE you to be in MySQL mode to run, and can only be run from console

    # This command will transfer from file to database
    /shop transfer_to_database

    # This command will transfer from database to files
    /shop transfer_to_file

The database used is a generic database to be used by all Ibex plugins, if you wish to access the database, be sure to include WHERE plugin_name = 'bazaar'


Below are some useful SQL queries:

    # Creating the ibex database
    CREATE DATABASE `ibex` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

    # Creating the ibex_plugin table
    CREATE TABLE `ibex_plugin` (
       `ibex_id` int NOT NULL AUTO_INCREMENT,
       `plugin_name` varchar(45) NOT NULL,
       `key_type` varchar(50) NOT NULL,
       `ibex_key` varchar(255) NOT NULL,
       `ibex_value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`ibex_id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    # Rekeying the ibex_plugin table
    # Should only be needed if you get close to the ibex_id max int value (which is unlikely)
    
    CREATE TABLE IF NOT EXISTS `temp_ibex_plugin` (`ibex_id` int NOT NULL AUTO_INCREMENT,`plugin_name` varchar(45) NOT NULL,`key_type` varchar(50) NOT NULL,`ibex_key` varchar(255) NOT NULL,`ibex_value` varchar(255) DEFAULT NULL, PRIMARY KEY (`ibex_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
    INSERT INTO temp_ibex_plugin (plugin_name, key_type, ibex_key, ibex_value) SELECT plugin_name, key_type, ibex_key, ibex_value FROM ibex_plugin;
    TRUNCATE TABLE ibex_plugin;
    INSERT INTO ibex_plugin (plugin_name, key_type, ibex_key, ibex_value) SELECT plugin_name, key_type, ibex_key, ibex_value FROM temp_ibex_plugin;
    DROP TABLE temp_ibex_plugin;


## 1.2.3

Added API calls:

    /**
    * Gets a set of all shop locations
    * @return Set of shop locations
    */
    Set<Location> getAllShopLocations()

    /**
     * Gets a set of all shop locations owned by the player specified
     * @param uuid Player unique identifier
     * @return Set of shop locations
     */
    Set<Location> getPlayerShopLocations(UUID uuid)

    /**
     * Gets a set of all admin shop locations
     * @return Set of admin shop locations
     */
    Set<Location> getAdminShopLocations()

    /**
     * Gets a set of all server shop locations
     * @return Set of server shop locations
     */
    Set<Location> getServerShopLocations()

    /**
     * Gets a set of all clan shop locations for the clan tag provided
     * @param tag Clan tag
     * @return Set of clan shop locations
     */
    Set<Location> getClanShopLocations(String tag)

    /**
     * Removes all the shop locations owned by the player specified
     * @param uuid Player unique identifier
     */
    void removeAllPlayerShops(UUID uuid)

    /**
     * Gets a shop at the specified location
     * @param location Location of shop
     * @return Shop object - null if not found
     */
    Shop getShop(Location location)

    /**
     * Replaces the shop at the specified location
     * @param location Location of shop
     * @param shop Shop object
     */
    void putShop(Location location, Shop shop)

## 1.2.2

Bug fixes

## 1.2.1

Bug fixes to /shop command

## 1.2.0

Adds WorldGuard integration, allowing server owners to restrict creating or accessing Bazaar shops in their WorldGuard regions.

If you wish to change the default flag names or defaults, add the following to config.yml

    # WorldGuard flag name and values (true = allow, false = deny)
    # worldguard.create_shop - Controls whether a shop can be created in the region
    # worldguard.use_admin_shop - Controls whether players can access admin shops in the region
    # worldguard.use_player_shop - Controls whether players can access player shops in the region
    # worldguard.use_clan_shop - Controls whether player can access clan shops in the region
    worldguard:
      create_shop:
        flag: "create-shop"
        default: false
      use_admin_shop:
        flag: "use-admin-shop"
        default: false
      use_player_shop:
        flag: "use-shop"
        default: false
      use_clan_shop:
        flag: "use-clan-shop"
        default: false

Add following to lang.yml:

    worldguard_create_shop_deny: "&cSorry, you are not allowed to create shops here"
    worldguard_admin_shop_deny: "&cSorry, you are not allowed to use Admin shops here"
    worldguard_player_shop_deny: "&cSorry, you are not allowed to use player shops here"
    worldguard_clan_shop_deny: "&cSorry, you are not allowed to use Clan shops here"