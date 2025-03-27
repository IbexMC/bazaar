package com.ibexmc.bazaar.shop.multishop;

import com.ibexmc.bazaar.Bazaar;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;

public class MultiShopGenie extends MultiShop {

    private EntityType entityType;
    private boolean lockToCaller;
    private long expireTime;
    private int particleRed;
    private int particleGreen;
    private int particleBlue;
    private int particleSize;
    private int particleCount;

    public MultiShopGenie(Bazaar instance, String key) {
        super(instance, key);
    }

    public MultiShopGenie(Bazaar instance, String key, Type type, String title, int size, Map<Integer, MultiShopItem> items, Material unusedMaterial, String unusedName, List<String> unusedLore) {
        super(instance, key, type, title, size, items, unusedMaterial, unusedName, unusedLore);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public boolean isLockToCaller() {
        return lockToCaller;
    }

    public void setLockToCaller(boolean lockToCaller) {
        this.lockToCaller = lockToCaller;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public int getParticleRed() {
        return particleRed;
    }

    public void setParticleRed(int particleRed) {
        this.particleRed = particleRed;
    }

    public int getParticleGreen() {
        return particleGreen;
    }

    public void setParticleGreen(int particleGreen) {
        this.particleGreen = particleGreen;
    }

    public int getParticleBlue() {
        return particleBlue;
    }

    public void setParticleBlue(int particleBlue) {
        this.particleBlue = particleBlue;
    }

    public int getParticleSize() {
        return particleSize;
    }

    public void setParticleSize(int particleSize) {
        this.particleSize = particleSize;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setParticleCount(int particleCount) {
        this.particleCount = particleCount;
    }
}
