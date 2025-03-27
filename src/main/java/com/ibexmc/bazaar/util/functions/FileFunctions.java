package com.ibexmc.bazaar.util.functions;

import com.ibexmc.bazaar.util.log.Error;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.ibexmc.bazaar.Bazaar;

import java.io.File;
import java.io.IOException;

public class FileFunctions {

    /**
     * Gets the config.yml file - If not found, will try to create from resources, if not found there
     * will create a new blank file
     * @return Config File object
     */
    public static File getConfigYml() {
        File configFile = new File(
                Bazaar.getInstance().getDataFolder() +
                        File.separator +
                        "config.yml"
        );
        if (!configFile.exists()) {
            if (Bazaar.getInstance().getResource("config.yml") != null) {
                Bazaar.getInstance().saveResource("config.yml", false);
            } else {
                try {
                    configFile.createNewFile();
                    FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
                    try {
                        configuration.save(configFile);
                    } catch (IOException e) {
                        Error error = new Error(Bazaar.getInstance());
                        error.save(
                                "gCY1",
                                "FileFunctions",
                                "getConfigYml",
                                "Unexpected Error getting Config",
                                e.getMessage(),
                                Error.Severity.URGENT,
                                e.getStackTrace()
                        );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return configFile;
    }

    /**
     * Gets the filename from the Location provided
     * @param loc Location to get filename from
     * @return Filename for the shop
     */
    public static String getFilenameFromLocation(Location loc) {
        if (loc == null) {
            return "";
        }
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ() + ".yml";
    }

}
