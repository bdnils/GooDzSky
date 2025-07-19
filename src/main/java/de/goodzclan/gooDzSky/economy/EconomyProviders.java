package de.goodzclan.gooDzSky.economy;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import de.goodzclan.core.spigot.player.PlayerData;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EconomyProviders implements EconomyProvider {

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        final PlayerData playerData = PlayerData.getPlayerDataByUUID(superiorPlayer.getUniqueId());
        if (playerData == null) return null;
        return new BigDecimal(playerData.getCoins());
    }

    @Override
    public EconomyProvider.EconomyResult depositMoney(SuperiorPlayer superiorPlayer, double v) {
        final PlayerData playerData = PlayerData.getPlayerDataByUUID(superiorPlayer.getUniqueId());
        if (playerData == null) return null;
        final BigInteger coins = BigInteger.valueOf((int) v);
        playerData.addCoins(coins);
        return new EconomyResult(coins.intValue());
    }

    @Override
    public EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, double v) {
        final PlayerData playerData = PlayerData.getPlayerDataByUUID(superiorPlayer.getUniqueId());
        if (playerData == null) return null;
        final BigInteger coins = BigInteger.valueOf((int) v);
        playerData.removeCoins(coins);
        return new EconomyResult(coins.intValue());
    }
}