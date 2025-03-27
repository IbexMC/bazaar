package com.ibexmc.bazaar.gui;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.internal.functions.StringFunctions;
import com.ibexmc.internal.gui.BaseGUI;
import com.ibexmc.internal.gui.GUIButton;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;

public class InfoGUI extends BaseGUI {

    private Bazaar instance;

    public InfoGUI(String key) {
        super(key);
        this.instance = Bazaar.getInstance();
    }

    public InfoGUI(String key, String title, int size, Map<Integer, GUIButton> slots) {
        super(key, title, size, slots);
        this.instance = Bazaar.getInstance();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event);

        event.setCancelled(true);

        instance.getLog().log("Clicked slot " + event.getSlot());

        GUIButton button = instance.getInternalData().getIbexGUI("info").get(event.getSlot());
        if (button != null) {
            if (!StringFunctions.isNullOrEmpty(button.getCommand())) {
                instance.getLog().log("Clicked slot with command " + button.getCommand());
                switch (button.getCommand().toUpperCase()) {
                    case "BACK":
                        instance.getLog().log("Run BACK code");
                        break;
                    case "INFO":
                        instance.getLog().log("Run INFO code");
                        break;
                    case "WHO":
                        instance.getLog().log("Run WHO code");
                        break;
                    case "FLAGS":
                        instance.getLog().log("Run FLAGS code");
                        break;
                    case "RENT":
                        instance.getLog().log("Run RENT code");
                        break;
                }
            } else {
                instance.getLog().log("Clicked slot with NO command");
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);
    }
}
