package com.ibexmc.bazaar.shop.multishop;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class MultiShopChest extends MultiShop {

    private List<String> hologramLines;
    private Material hologramIcon;

    public MultiShopChest(Bazaar instance, String key) {
        super(instance, key);
    }

    public MultiShopChest(Bazaar instance, String key, Type type, String title, int size, Map<Integer, MultiShopItem> items, Material unusedMaterial, String unusedName, List<String> unusedLore) {
        super(instance, key, type, title, size, items, unusedMaterial, unusedName, unusedLore);
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public void setHologramLines(List<String> hologramLines) {
        this.hologramLines = hologramLines;
    }

    public Material getHologramIcon() {
        return hologramIcon;
    }

    public void setHologramIcon(Material hologramIcon) {
        this.hologramIcon = hologramIcon;
    }
}
