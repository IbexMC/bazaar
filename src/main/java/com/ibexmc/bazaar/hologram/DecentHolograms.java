package com.ibexmc.bazaar.hologram;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.bazaar.shop.Shop;
import com.ibexmc.bazaar.shop.ShopManager;
import com.ibexmc.internal.functions.ColorFunctions;
import com.ibexmc.internal.functions.StringFunctions;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class DecentHolograms implements IHologram {

    private final Bazaar instance;

    public DecentHolograms(Bazaar instance) {
        this.instance = instance;
    }

    private List<String> getLines(Shop shop) {
        List<String> lines = new ArrayList<>();
        ItemStack itemStack = shop.getItemStack().clone();

        if (!shop.hasStock() && instance.getBazaarConfig().isShowOutOfStock() && !shop.isServerShop() && !shop.isAdminShop()) {
            if (shop.getBuyFromShopPrice() > 0) {
                lines.add(instance.getLocale().localeText("out_of_stock", "&4Out of Stock", null));
            }
        }
        if (!StringFunctions.isNullOrEmpty(shop.getClanTag())) {
            lines.add(ColorFunctions.color("&b" + shop.getClanTag()));
        }
        if (shop.isAdminShop()) {
            lines.add(instance.getLocale().localeText("admin_shop", "&5Admin Shop", null));
        }
        if (shop.isServerShop()) {
            lines.add(instance.getLocale().localeText("server_shop", "&5Server Shop", null));
        }
        lines.add(ShopManager.getItemNameLine(shop));
        if (!StringFunctions.isNullOrEmpty(shop.getItemDescriptionLine())) {
            lines.add(shop.getItemDescriptionLine());
        }
        if (instance.getBazaarConfig().isShowEnchantments()) {
            String enchantmentLine = ShopManager.getEnchantmentLine(shop);
            if (!StringFunctions.isNullOrEmpty(enchantmentLine)) {
                lines.add(enchantmentLine);
            }
        }
        if (itemStack.getType().equals(Material.POTION) || itemStack.getType().equals(Material.LINGERING_POTION) || itemStack.getType().equals(Material.SPLASH_POTION)) {
            if (instance.getBazaarConfig().isShowPotionType()) {
                String potionTypeLine = ShopManager.getPotionTypeLine(shop);
                if (!StringFunctions.isNullOrEmpty(potionTypeLine)) {
                    lines.add(potionTypeLine);
                }
            }
        }
        if (shop.hasStock() || instance.getBazaarConfig().isShowOutOfStockPrice()) {
            lines.add(ShopManager.getPriceLine(shop));
        }
        if (!instance.getBazaarConfig().isShowIconAlwaysTextIfTargeted()) {
            lines.add("#ICON: " + itemStack.getType().name());
        }

        return lines;
    }

    /**
     * Creates a Hologram for the shop
     * @param shop Shop to create hologram for
     */
    @Override
    public void createHologram(Shop shop) {
        if (!instance.getBazaarData().hasDecentHolograms()) {
            return;
        }
        Hologram existingHologram = DHAPI.getHologram(shop.getName());
        if (existingHologram != null) {
            existingHologram.destroy();
        }
        Hologram existingIconHologram = DHAPI.getHologram(shop.getName() + "_icon");
        if (existingIconHologram != null) {
            existingIconHologram.destroy();
        }
        double heightTextOnly = 0;
        if (instance.getBazaarConfig().isShowIconAlwaysTextIfTargeted()) {
            heightTextOnly = 0.5;
        }
        double heightOffset = 0;
        Block block = shop.getLocation().clone().getBlock();
        if (block.getType().equals(Material.BARREL)) {
            heightOffset = 0.2;
        }
        Location location = shop.getLocation().clone().add(0.5, 0.7 + heightTextOnly + heightOffset, 0.5);
        Location iconOnlyLocation = shop.getLocation().clone().add(0.5, 0.7 + heightOffset, 0.5);
        ItemStack itemStack = shop.getItemStack().clone();
        List<String> lines = getLines(shop);


        Hologram hologram = DHAPI.createHologram(shop.getName(), location, lines);
        if (instance.getBazaarConfig().isShowOnlyIfTargeted()) {
            hologram.hideAll();
        }
        hologram.setDownOrigin(true);
        hologram.setDisplayRange(10);
        hologram.realignLines();
        instance.getBazaarData().putDecentHologram(shop.getLocation().clone(), hologram);

        if (instance.getBazaarConfig().isShowIconAlwaysTextIfTargeted()) {
            List<String> iconOnlyLines = new ArrayList<>();
            iconOnlyLines.add("#ICON: " + itemStack.getType().name());
            Hologram hologramIcon = DHAPI.createHologram(shop.getName() + "_icon", iconOnlyLocation, iconOnlyLines);
            hologramIcon.setDownOrigin(true);
            hologramIcon.setDisplayRange(20);
            hologramIcon.realignLines();
        }
    }

    /**
     * Destroys a Hologram for the shop
     * @param shop Shop to destroy hologram for
     */
    @Override
    public void destroyHologram(Shop shop) {
        if (!instance.getBazaarData().hasDecentHolograms()) {
            return;
        }
        Hologram existingHologram = DHAPI.getHologram(shop.getName());
        if (existingHologram != null) {
            existingHologram.destroy();
            instance.getBazaarData().removeDecentHologram(shop.getLocation().clone());
        }
        Hologram existingIconHologram = DHAPI.getHologram(shop.getName() + "_icon");
        if (existingIconHologram != null) {
            existingIconHologram.destroy();
        }
    }

}