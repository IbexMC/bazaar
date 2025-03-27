package com.ibexmc.bazaar.shop.multishop;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiShop implements IMultiShop {

    private Bazaar instance;
    private String key;
    private MultiShop.Type type;
    private String title;
    private int size;
    private Map<Integer, MultiShop.MultiShopItem> items = new HashMap<>();
    private Material unusedMaterial;
    private String unusedName;
    private List<String> unusedLore;

    public MultiShop(Bazaar instance, String key) {
        this.instance = instance;
        this.key = key;
    }

    public MultiShop(Bazaar instance, String key, Type type, String title, int size, Map<Integer, MultiShopItem> items, Material unusedMaterial, String unusedName, List<String> unusedLore) {
        this.instance = instance;
        this.key = key;
        this.type = type;
        this.title = title;
        this.size = size;
        this.items = items;
        this.unusedMaterial = unusedMaterial;
        this.unusedName = unusedName;
        this.unusedLore = unusedLore;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean hasItem(int slot) {
        return items.containsKey(slot);
    }

    @Override
    public MultiShopItem getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public Map<Integer, MultiShopItem> getItems() {
        return items;
    }

    @Override
    public void setItems(Map<Integer, MultiShopItem> items) {
        this.items = items;
    }

    public class MultiShopItem {

        private ItemStack itemStack;
        private float buyPrice;
        private float sellPrice;
        private int amount;

        public MultiShopItem() {

        }
        public MultiShopItem(ItemStack itemStack, float buyPrice, float sellPrice, int amount) {
            this.itemStack = itemStack;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.amount = amount;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public void setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public float getBuyPrice() {
            return buyPrice;
        }

        public void setBuyPrice(float buyPrice) {
            this.buyPrice = buyPrice;
        }

        public float getSellPrice() {
            return sellPrice;
        }

        public void setSellPrice(float sellPrice) {
            this.sellPrice = sellPrice;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
    public enum Type {
        CHEST,
        GENIE
    }
}
