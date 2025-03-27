package com.ibexmc.bazaar.commands;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.gui.InfoGUI;
import com.ibexmc.bazaar.util.*;
import com.ibexmc.internal.functions.ItemFunctions;
import com.ibexmc.internal.functions.StringFunctions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


import java.util.*;


public class ShopCommand implements CommandExecutor, TabCompleter {

    private final Bazaar instance;
    private final String shopCancel;
    private final String shopCreate;
    private final String shopDebug;
    private final String shopRemove;
    private final String shopHelp;
    private final String shopReload;
    private final String shopTransferToDb;
    private final String shopTransferToFile;
    private final String shopVersion;
    
    public ShopCommand(Bazaar instance) {
        this.instance = instance;
        shopCancel = instance.getBazaarConfig().getCommandShopCancel().toLowerCase();
        shopCreate = instance.getBazaarConfig().getCommandShopCreate().toLowerCase();
        shopDebug = instance.getBazaarConfig().getCommandShopDebug().toLowerCase();
        shopRemove = instance.getBazaarConfig().getCommandShopRemove().toLowerCase();
        shopHelp = instance.getBazaarConfig().getCommandShopHelp().toLowerCase();
        shopReload = instance.getBazaarConfig().getCommandShopReload().toLowerCase();
        shopTransferToDb = instance.getBazaarConfig().getCommandShopTransferToDB().toLowerCase();
        shopTransferToFile = instance.getBazaarConfig().getCommandShopTransferToFile().toLowerCase();
        shopVersion = instance.getBazaarConfig().getCommandShopVersion().toLowerCase();
    }
    @Override
    public List<String> onTabComplete (CommandSender sender, Command cmd, String label, String[] args) {
        StringBuilder allArguments = new StringBuilder();
        int argumentCount = -1; // -1 as we want the argument at 0 to have a count of 0
        if (args.length > 0) // Check to make sure we have some arguments
        {
            for (Object o : args) {
                allArguments.append(o.toString());
                allArguments.append(" ");
                argumentCount++;
            }
        }
        if (!Bazaar.getInstance().getBazaarData().isEnabled()) {
            return new ArrayList<>();
        }
        List<String> completions = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (argumentCount == 0) {
                if (label.equalsIgnoreCase("Shops") || label.equalsIgnoreCase("bazaar")) {
                    completions.add("help");
                    if (Perms.hasPermission(player, Perms.Type.CREATE)) {
                        completions.add("create");
                    }
                    if (Perms.hasPermission(player, Perms.Type.REMOVE)) {
                        completions.add("remove");
                    }
                    if (Perms.hasPermission(player, Perms.Type.CREATE) || Perms.hasPermission(player, Perms.Type.REMOVE)) {
                        completions.add("cancel");
                    }
                    if (Perms.hasPermission(player, Perms.Type.ADMIN)) {
                        completions.add("debug");
                        completions.add("reload");
                        completions.add("version");
                    }
                } else {
                    completions.add(shopHelp);
                    if (Perms.hasPermission(player, Perms.Type.CREATE)) {
                        completions.add(shopCreate);
                    }
                    if (Perms.hasPermission(player, Perms.Type.REMOVE)) {
                        completions.add(shopRemove);
                    }
                    if (Perms.hasPermission(player, Perms.Type.CREATE) || Perms.hasPermission(player, Perms.Type.REMOVE)) {
                        completions.add(shopCancel);
                    }
                    if (Perms.hasPermission(player, Perms.Type.ADMIN)) {
                        completions.add(shopDebug);
                        completions.add(shopReload);
                        completions.add(shopVersion);
                    }
                }

            }
        }
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!instance.getBazaarData().isEnabled()) {
            instance.getMessage().send(sender,"bazaar_disabled","&cBazaar is not available",null,true);
            return true;
        }

        Map<Integer, String> arguments = StringFunctions.getArguments(args);

        StringBuilder allArguments = new StringBuilder();
        if (args.length > 0) {
            for (Object o : args) {
                allArguments.append(o.toString());
                allArguments.append(" ");
            }
        }

        CommandFunctions commandFunctions = new CommandFunctions(instance);

        if (arguments.containsKey(0)) {

            instance.getDebug().log(
                    "ShopCommand",
                    "onCommand",
                    "Command called with arguments: " + allArguments
            );

            String arg0 = args[0].toLowerCase();

            if (arg0.equals(shopCancel) || arg0.equals("cancel")) {
                commandFunctions.cancel(sender);
            } else if (arg0.equals(shopCreate) || arg0.equals("create")) {
                commandFunctions.create(sender, arguments);
            } else if (arg0.equals(shopDebug) || arg0.equals("debug")) {
                commandFunctions.debug(sender);
            } else if (arg0.equals(shopRemove) || arg0.equals("remove")) {
                commandFunctions.remove(sender);
            } else if (arg0.equals(shopHelp) || arg0.equals("help")) {
                commandFunctions.help(sender);
            } else if (arg0.equals(shopReload) || arg0.equals("reload")) {
                commandFunctions.reload(sender);
            } else if (arg0.equals(shopTransferToDb) || arg0.equals("transfer_to_database")) {
                commandFunctions.transferToDatabase(sender);
            } else if (arg0.equals(shopTransferToFile) || arg0.equals("transfer_to_file")) {
                commandFunctions.transferToFile(sender);
            } else if (arg0.equals(shopVersion) || arg0.equals("version")) {
                commandFunctions.version(sender);
            } else if (arg0.equals("test")) {
                InfoGUI infoGUI = new InfoGUI(
                        "info",
                        instance.getInternalData().getGUIName("info"),
                        instance.getInternalData().getGUISize("info"),
                        instance.getInternalData().getIbexGUI("info")
                );
                ((Player) sender).openInventory(infoGUI.getInventory());
            } else if (arg0.equals("item")) {
                commandFunctions.serialize(sender);
            } else {
                instance.getMessage().send(sender,"shop_default","&cERROR &f- Unknown command",null,true);
                commandFunctions.help(sender);
            }
        } else {
            commandFunctions.help(sender);
        }

        return true;
    }
}
