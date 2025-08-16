package de.goodzclan.gooDzSky;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import de.goodzclan.core.spigot.database.DatabaseHandler;
import de.goodzclan.core.spigot.player.PlayerData;
import de.goodzclan.gooDzSky.battlepass.BattlePassManager;
import de.goodzclan.gooDzSky.battlepass.BattlePassRepository;
import de.goodzclan.gooDzSky.battlepass.PlayerBattlePassData;
import de.goodzclan.gooDzSky.commands.BattlePassCommand;
import de.goodzclan.gooDzSky.commands.PayCommand;
import de.goodzclan.gooDzSky.commands.ShopCommand;
import de.goodzclan.gooDzSky.commands.WarpCommand;
import de.goodzclan.gooDzSky.economy.AuctionhouseCurrency;
import de.goodzclan.gooDzSky.economy.EconomyProviders;
import de.goodzclan.gooDzSky.listener.*;
import de.goodzclan.gooDzSky.manager.*;
import info.preva1l.fadah.currency.CurrencyBase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigInteger;
import java.util.*;

public class GooDzSky extends JavaPlugin {

    private BattlePassManager battlePassManager;
    private BattlePassCommand battlePassCommand;

    // Speichert die Battle Pass Daten der Spieler, solange sie online sind
    private final Map<UUID, PlayerBattlePassData> playerBattlePassData = new HashMap<>();

    // Ein eindeutiger Schlüssel, um Daten in Items zu speichern
    private DatabaseHandler dbHandler;
    public static NamespacedKey BATTLEPASS_LEVEL_KEY;
    public static NamespacedKey BATTLEPASS_ACTION_KEY;
    private BattlePassRepository battlePassRepository;

    private DungeonManager dungeonManager;
    private final EconomyProviders economyProviders = new EconomyProviders();
    private static GooDzSky instance;
    private WarpManager warpManager;
    private ShopManager shopManager;
    private ItemStack voteRewardItem;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        BATTLEPASS_LEVEL_KEY = new NamespacedKey(this, "battlepass_level");
        BATTLEPASS_ACTION_KEY = new NamespacedKey(this, "battlepass_action");
        this.dbHandler = new DatabaseHandler();
        dbHandler.initDatabases();
        dbHandler.initCollections();
        this.battlePassRepository = new BattlePassRepository(this, dbHandler);
        // Manager initialisieren
        this.shopManager = new ShopManager(this);
        shopManager.load(); // Konfiguration laden

        new SkyblockScoreboard(this);
        SuperiorSkyblockAPI.getProviders().setEconomyProvider(economyProviders);
        SuperiorSkyblockAPI.getProviders().setBankEconomyProvider(economyProviders);
        // ... (Dein restlicher Code hier bleibt gleich)

        LootboxManager lootboxManager = new LootboxManager();
        lootboxManager.loadLootboxes(getConfig());
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(lootboxManager), this);

        this.dungeonManager = new DungeonManager(this);
        warpManager = new WarpManager(getDataFolder());
        this.battlePassManager = new BattlePassManager(this);
        this.battlePassManager.load();
        this.battlePassCommand = new BattlePassCommand(this, battlePassManager);
        getCommand("battlepass").setExecutor(battlePassCommand);
        getServer().getPluginManager().registerEvents(new BattlePassGUIListener(this, battlePassManager), this);

        // Listener für das Laden/Entladen der Spielerdaten
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        // Befehle und Listener registrieren
        getServer().getPluginManager().registerEvents(new MobListener(dungeonManager), this);
        getServer().getPluginManager().registerEvents(new FirstJoinListener(this), this);

        WarpCommand warpCommand = new WarpCommand(warpManager);
        getCommand("warp").setExecutor(warpCommand);
        getCommand("warp").setTabCompleter(warpCommand);

        ShopCommand shopCommand = new ShopCommand(shopManager); // ShopManager übergeben
        getCommand("shop").setExecutor(shopCommand);
        getServer().getPluginManager().registerEvents(new ShopListener(this, shopManager, shopCommand), this); // Manager & Command übergeben

        getCommand("pay").setExecutor(new PayCommand());
        loadVoteReward();

        // Registriere den neuen VoteListener, aber nur, wenn Votifier auch installiert ist
        if (getServer().getPluginManager().getPlugin("Votifier") != null) {
            getServer().getPluginManager().registerEvents(new VoteListener(this), this);
            getLogger().info("Votifier gefunden. Vote-Belohnungen sind aktiviert.");
        } else {
            getLogger().warning("Votifier wurde nicht gefunden. Vote-Belohnungen sind deaktiviert.");
        }

        getLogger().info("GooDzSky ist online!");
    }

    // ... (Dein restlicher Code: onDisable, getInstance, depositToYourAPI, etc. bleibt gleich)
    @Override
    public void onDisable() {
        getLogger().info("GooDzSky wurde deaktiviert.");
    }

    public static GooDzSky getInstance() {
        return instance;
    }
    public void depositToYourAPI(Player player, double amount) {
        PlayerData playerData = PlayerData.getPlayerDataByPlayer(player);
        playerData.addCoins(BigInteger.valueOf((long) amount));
        player.sendMessage("§a[Shop] §7Du hast §e" + amount + " Coins §7erhalten.");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }
    public boolean withdrawFromYourAPI(Player player, double amount) {
        PlayerData playerData = PlayerData.getPlayerDataByPlayer(player);
        if (playerData.getCoins().compareTo(BigInteger.valueOf((long) amount)) < 0) {
            player.sendMessage("§c[Shop] §7Du hast nicht genug Coins!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }
        playerData.removeCoins(BigInteger.valueOf((long) amount));
        player.sendMessage("§a[Shop] §7Dir wurden §e" + amount + " Coins §7abgezogen.");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.8f);
        return true;
    }

    public void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }
    public PlayerBattlePassData getPlayerBattlePassData(Player player) {
        return playerBattlePassData.get(player.getUniqueId());
    }

    public void loadPlayerBattlePassData(Player player) {
        battlePassRepository.loadPlayerDataAsync(player.getUniqueId(), data -> {
            playerBattlePassData.put(player.getUniqueId(), data);
            getLogger().info("BattlePass-Daten für " + player.getName() + " geladen. Level: " + data.getCurrentLevel());
        });
    }

    public void unloadPlayerBattlePassData(Player player) {
        PlayerBattlePassData data = playerBattlePassData.get(player.getUniqueId());
        if (data != null) {
            battlePassRepository.savePlayerDataAsync(player.getUniqueId(), data);
            playerBattlePassData.remove(player.getUniqueId());
            getLogger().info("BattlePass-Daten für " + player.getName() + " gespeichert und entladen.");
        }
    }
    private void loadVoteReward() {
        saveDefaultConfig(); // Stellt sicher, dass die config.yml existiert und die neuen Sektionen hinzufügt
        if (!getConfig().contains("voting.reward")) {
            getLogger().warning("Der 'voting.reward' Abschnitt in der config.yml fehlt.");
            return;
        }

        try {
            String path = "voting.reward.";
            Material material = Material.valueOf(getConfig().getString(path + "material", "STONE"));
            int amount = getConfig().getInt(path + "amount", 1);
            String name = ChatColor.translateAlternateColorCodes('&', getConfig().getString(path + "name", "&cVote-Belohnung"));
            boolean isEnchanted = getConfig().getBoolean(path + "enchanted", false);

            List<String> lore = new ArrayList<>();
            for (String line : getConfig().getStringList(path + "lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            this.voteRewardItem = new ItemStack(material, amount);
            ItemMeta meta = voteRewardItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(lore);

                if (isEnchanted) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                voteRewardItem.setItemMeta(meta);
            }
        } catch (Exception e) {
            getLogger().severe("Fehler beim Laden der Vote-Belohnung aus der config.yml!");
            e.printStackTrace();
            this.voteRewardItem = null;
        }
    }

    // Getter, damit der Listener auf das Item zugreifen kann
    public ItemStack getVoteRewardItem() {
        return (voteRewardItem != null) ? voteRewardItem.clone() : null;
    }


    public BattlePassCommand getBattlePassCommand() {
        return battlePassCommand;
    }
}