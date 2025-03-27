package com.ibexmc.bazaar.util.log;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.internal.functions.StringFunctions;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class Log {
    //region Objects
    Bazaar plugin;
    //endregion
    //region Constructor
    public Log(Bazaar plugin) {
        this.plugin = plugin;
    }
    //endregion
    //region Methods

    /**
     * Logs a message to console
     * @param title Title of message
     * @param message Message
     */
    public void console(String title, String message) {
        ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
        ccs.sendMessage(
                ChatColor.DARK_PURPLE + "[" + plugin.getInternalName() + ".Log]" +
                        ChatColor.AQUA + "[" + title + "] " + ChatColor.WHITE + message);
    }

    /**
     * Logs a quick message to console, used for troubleshooting
     * @param message Message
     */
    public void quick(String message)
    {
        ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
        ccs.sendMessage(
                ChatColor.DARK_PURPLE + "[" + plugin.getInternalName() + ".Log]" +
                        ChatColor.AQUA + "[QUICK] " + ChatColor.WHITE + StringFunctions.color(message));
    }

    /**
     * Logs a quick message to console, used for troubleshooting
     * @param message Message
     * @param prefix Prefix to message
     */
    public void quick(String message, boolean prefix) {
        if (prefix) {
            quick(message);
        } else {
            ConsoleCommandSender ccs = plugin.getServer().getConsoleSender();
            ccs.sendMessage(StringFunctions.color(message));
        }

    }
    //endregion
}
