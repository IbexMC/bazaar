package com.ibexmc.bazaar.util;

import org.bukkit.entity.Player;

public class Perms {
    public static enum Type
    {
        ADMIN("bazaar.admin"),
        CREATE("bazaar.create"),
        CREATE_ADMIN("bazaar.create.admin"),
        CREATE_CLAN("bazaar.create.clan"),
        CREATE_SERVER("bazaar.create.server"),
        REMOVE("bazaar.remove"),
        REMOVE_ADMIN("bazaar.remove.admin"),
        REMOVE_SERVER("bazaar.remove.server"),
        BUY("bazaar.buy"),
        SELL("bazaar.sell")
        ;

        private String permType;

        Type(String permType) {
            this.permType = permType;
        }

        public String getType() {
            return this.permType;
        }
    }

    /**
     * Checks if the player has the permission type
     * @param player Player to check
     * @param permissionType Permission Type
     * @return If true, player has permission
     */
    public static boolean hasPermission(Player player, Type permissionType)
    {
        return player.isOp() ||
                player.hasPermission(permissionType.getType()) ||
                player.hasPermission(Type.ADMIN.getType());
    }
}
