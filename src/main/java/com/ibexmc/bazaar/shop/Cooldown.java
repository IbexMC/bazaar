package com.ibexmc.bazaar.shop;

import org.bukkit.Location;

import java.sql.Timestamp;
import java.util.UUID;

public class Cooldown {
    private UUID uuid;
    private Location location;
    private Timestamp timestamp;
    private int cooldownLength;

    public Cooldown(UUID uuid, Location location, Timestamp timestamp, int cooldownLength) {
        this.uuid = uuid;
        this.location = location;
        this.timestamp = timestamp;
        this.cooldownLength = cooldownLength;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getCooldownLength() {
        return cooldownLength;
    }

    public void setCooldownLength(int cooldownLength) {
        this.cooldownLength = cooldownLength;
    }
}
