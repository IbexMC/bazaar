package com.ibexmc.bazaar.hologram;

import com.ibexmc.bazaar.shop.Shop;

public interface IHologram {
    void createHologram(Shop shop);
    void destroyHologram(Shop shop);
}