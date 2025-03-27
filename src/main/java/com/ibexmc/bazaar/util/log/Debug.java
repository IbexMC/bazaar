package com.ibexmc.bazaar.util.log;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class Debug {

    //region Objects
    Bazaar plugin;
    boolean debugActive = false;
    //endregion
    //region Constructor
    public Debug(Bazaar plugin, boolean debugActive) {
        this.plugin = plugin;
        this.debugActive = debugActive;
    }
    //endregion
    //region Methods

    /**
     * Logs when a function has been entered
     * @param className Class name
     * @param functionName Function name
     */
    public void enter(String className, String functionName) {
        if (!debugActive) {
            return;
        }
        ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
        ccs.sendMessage(
                ChatColor.LIGHT_PURPLE + "[" + plugin.getInternalName() + ".Debug.EnterFunction]" +
                        ChatColor.AQUA + "[" + className + "." + functionName + "]");
    }

    /**
     * Logs an action
     * @param className Class Name
     * @param functionName Function Name
     * @param message Message to log
     * @param additional Additional information to log
     */
    public void log(String className, String functionName, String message, String additional) {
        if (!debugActive) {
            return;
        }
        if (additional.length() > 0) {
            additional = "\n" + "Additional Information:\n" + additional;
        }
        ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
        ccs.sendMessage(
                ChatColor.LIGHT_PURPLE + "[" + plugin.getInternalName() + ".Debug]" +
                        ChatColor.AQUA + "[" + className + "." + functionName + "] " + ChatColor.WHITE + message + additional);
    }
    //endregion
}
