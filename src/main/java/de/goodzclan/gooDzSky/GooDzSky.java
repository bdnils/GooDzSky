package de.goodzclan.gooDzSky;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import de.goodzclan.gooDzSky.commands.DungeonCommand;
import de.goodzclan.gooDzSky.economy.AuctionhouseCurrency;
import de.goodzclan.gooDzSky.economy.EconomyProviders;
import de.goodzclan.gooDzSky.listener.MobListener;
import de.goodzclan.gooDzSky.manager.DungeonManager;
import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyBase;
import info.preva1l.fadah.currency.CurrencyRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public class GooDzSky extends JavaPlugin {

    private DungeonManager dungeonManager;
    private EconomyProviders economyProviders = new EconomyProviders();
    private CurrencyBase currencyBase = new AuctionhouseCurrency();

    @Override
    public void onEnable() {
        // Überprüfen, ob ModelEngine geladen ist
        if (getServer().getPluginManager().getPlugin("ModelEngine") == null) {
            getLogger().severe("ModelEngine nicht gefunden! Das Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SuperiorSkyblockAPI.getProviders().setEconomyProvider(economyProviders);
        SuperiorSkyblockAPI.getProviders().setBankEconomyProvider(economyProviders);
        CurrencyRegistry.register(new AuctionhouseCurrency());

        // Manager initialisieren
        this.dungeonManager = new DungeonManager(this);

        // Befehle und Listener registrieren
        getCommand("dungeon").setExecutor(new DungeonCommand(dungeonManager));
        getServer().getPluginManager().registerEvents(new MobListener(dungeonManager), this);

        getLogger().info("GooDzSky ist online!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GooDzSky wurde deaktiviert.");
    }
}