package com.ibexmc.bazaar.shop.multishop;

import java.util.Map;

public interface IMultiShop {

    String getKey();

    MultiShop.Type getType();

    void setType(MultiShop.Type type);

    String getTitle();

    void setTitle(String title);

    int getSize();

    void setSize(int size);

    boolean hasItem(int slot);

    MultiShop.MultiShopItem getItem(int slot);

    Map<Integer, MultiShop.MultiShopItem> getItems();

    void setItems(Map<Integer, MultiShop.MultiShopItem> items);

}
