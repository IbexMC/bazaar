package com.ibexmc.bazaar.livedata;

import com.ibexmc.bazaar.Bazaar;
import com.ibexmc.myriad.Myriad;

import java.util.UUID;

public class ClanTasks {

    public static class ClanTransactionResult {
        private boolean success;
        private String returnText;

        public ClanTransactionResult(boolean success, String returnText) {
            this.success = success;
            this.returnText = returnText;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReturnText() {
            return returnText;
        }
    }

    public static String getClanTagByUUID(UUID uuid) {
        Bazaar instance = Bazaar.getInstance();
        instance.getDebug().log("ClanTasks", "getClanTagByUUID", "UUID: " + uuid);
        Myriad myriad = Bazaar.getInstance().getDependencies().getMyriadClans();
        return myriad.getAPI().getPlayerClanTag(uuid);
    }
    public static com.ibexmc.myriad.clan.Clan getClanFromTag(String clanTag) {
        Myriad myriad = Bazaar.getInstance().getDependencies().getMyriadClans();
        return myriad.getAPI().getClanFromTag(clanTag);
    }
    public static boolean sufficientFundsAvailable(String clanTag, Double amount) {
        boolean returnValue = false;
        com.ibexmc.myriad.clan.Clan clan = getClanFromTag(clanTag);
        if (clan != null) {
            Myriad myriad = Bazaar.getInstance().getDependencies().getMyriadClans();
            if (clan.getCash() >= amount) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static double balanceClan(String clanTag) {
        com.ibexmc.myriad.clan.Clan clan = getClanFromTag(clanTag);
        if (clan != null) {
            return clan.getCash();
        } else {
            return -1;
        }
    }
    public static ClanTransactionResult withdrawFundsFromClan(String clanTag, Double amount) {
        Bazaar instance = Bazaar.getInstance();
        com.ibexmc.myriad.clan.Clan clan = getClanFromTag(clanTag);
        if (clan != null) {
            Myriad myriad = Bazaar.getInstance().getDependencies().getMyriadClans();
            if (clan.getCash() >= amount) {
                clan.setCash(clan.getCash() - amount);
                if (myriad.getAPI().modifyClan(clan)) {
                    return new ClanTransactionResult(true, "");
                } else {
                    return new ClanTransactionResult(false, "Unexpected Error");
                }
            } else {
                return new ClanTransactionResult(false, instance.getLocale().localeText("insufficient_clan_funds", "Insufficient funds in clan wallet", null));
            }
        } else {
            return new ClanTransactionResult(false, instance.getLocale().localeText("invalid_clan", "Invalid Clan", null));
        }
    }
    public static ClanTransactionResult addFundsToClan(String clanTag, Double amount) {
        Bazaar instance = Bazaar.getInstance();
        com.ibexmc.myriad.clan.Clan clan = getClanFromTag(clanTag);
        if (clan != null) {
            Myriad myriad = Bazaar.getInstance().getDependencies().getMyriadClans();
            clan.setCash(clan.getCash() + amount);
            if (myriad.getAPI().modifyClan(clan)) {
                return new ClanTransactionResult(true, "");
            } else {
                return new ClanTransactionResult(false, "Unexpected Error");
            }
        } else {
            return new ClanTransactionResult(false, instance.getLocale().localeText("invalid_clan", "Invalid Clan", null));
        }
    }
}
