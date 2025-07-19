package de.goodzclan.gooDzSky.economy;

import de.goodzclan.core.spigot.player.PlayerData;
import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyBase;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;

public class AuctionhouseCurrency implements Currency {


        @Override
        public String getName() {
            return "GooDzCore";
        }

        @Override
        public void withdraw(OfflinePlayer player, double amountToTake) {
            final PlayerData playerData = PlayerData.getPlayerDataByUUID(player.getUniqueId());
            if (playerData == null) return;
            final BigInteger coins = BigInteger.valueOf((int) amountToTake);
            playerData.removeCoins(coins);
        }

        @Override
        public void add(OfflinePlayer player, double amountToAdd) {
            final PlayerData playerData = PlayerData.getPlayerDataByUUID(player.getUniqueId());
            if (playerData == null) return;
            final BigInteger coins = BigInteger.valueOf((int) amountToAdd);
            playerData.addCoins(coins);
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            final PlayerData playerData = PlayerData.getPlayerDataByUUID(player.getUniqueId());
            if (playerData == null) return 0;
            return playerData.getCoins().doubleValue();
        }

        @Override
        public String getId() {
            return "Münzen";
        }

        @Override
        public String getRequiredPlugin() {
            return "";
        }
    }

