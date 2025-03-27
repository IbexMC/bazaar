package com.ibexmc.bazaar.util;

import com.ibexmc.internal.functions.StringFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import com.ibexmc.bazaar.Bazaar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Logging {

    private static Bazaar instance = Bazaar.getInstance();

    public static void transaction(String code, String message, Map<String, String> placeHolders) {
        if (!code.equalsIgnoreCase("na")) {
            if (Bazaar.getInstance().getBazaarData().checkLocaleContainsCode(code)) {
                String newMessage = Bazaar.getInstance().getBazaarData().getLocaleTextByCode(code);
                if (!newMessage.equals("")) {
                    message = newMessage;
                }
            }
        }
        if (message != null) {
            if (placeHolders != null) {
                for (Map.Entry<String, String> params : placeHolders.entrySet()) {
                    message = message.replace(params.getKey(), params.getValue());
                }
            }
            ConsoleCommandSender consoleCommandSender = instance.getServer().getConsoleSender();
            consoleCommandSender.sendMessage(StringFunctions.color(message));
        }
    }

}
