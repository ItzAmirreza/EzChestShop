package me.deadlight.ezchestshop;
import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.handlers.ChestsManager;
import me.deadlight.ezchestshop.Commands.Ecsadmin;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.Data.SQLite.SQLite;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Listeners.*;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class EzChestShop extends JavaPlugin {

    private static EzChestShop plugin;
    private static FileConfiguration languages;

    private static Economy econ = null;

    private Database db;

    public boolean integrationWildChests = false;
    public ChestsManager wchests = null;

    public static boolean protocollib = false;


    @Override
    public void onEnable() {
        plugin = this;
        logConsole("&c[&eEzChestShop&c] &aEnabling EzChestShop - version 1.3.3");
        saveDefaultConfig();

        this.db = new SQLite(this);
        this.db.load();

        Config.loadConfig();
        // Plugin startup logic
        if (getServer().getBukkitVersion().equalsIgnoreCase("1.17-R0.1-SNAPSHOT")) {
            Utils.is1_17 = true;
            logConsole("&c[&eEzChestShop&c] &eInitializing 1.17 protocol change...");
        }
        if (getServer().getVersion().contains("1.17")) {
            Utils.family1_17 = true;
            logConsole("&c[&eEzChestShop&c] &e1.17 family protocol initialized.");
        }

        if (!setupEconomy() ) {
            logConsole("&c[&eEzChestShop&c] &4Cannot find vault or economy plugin. Self disabling... &ePlease note that you need vault and at least one economy plugin installed.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }


        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            protocollib = true;
            logConsole("&c[&eEzChestShop&c] &aProtocollib is installed. Enabling holograms functionality.");
        } else {
            logConsole("&c[&eEzChestShop&c] &eProtocollib is not installed. Plugin will not support holograms and floating items.");
        }

        loadLanguages();
        try {
            Utils.checkForConfigYMLupdate();
            Utils.checkForLanguagesYMLupdate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerListeners();
        registerCommands();
        registerTabCompleters();
        //metrics
        Metrics metrics = new Metrics(this, 10756);

        //integration boolean changer
        if (getServer().getPluginManager().getPlugin("WildChests") != null) {
            integrationWildChests = true;
            wchests = WildChestsAPI.getInstance().getChestsManager();
        }

        ShopContainer.queryShopsToMemory();

    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChestOpeningEvent(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerTransactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        //Add Config check over here, to change the Shop display varient.
        //PlayerLooking is less laggy but probably harder to spot.
        if (Config.holodistancing) {
            getServer().getPluginManager().registerEvents(new PlayerCloseToChestListener(), this);
        } else {
            getServer().getPluginManager().registerEvents(new PlayerLookingAtChestShop(), this);
            getServer().getPluginManager().registerEvents(new PlayerLeavingListener(), this);
        }

    }
    private void registerCommands() {
        getCommand("ecs").setExecutor(new MainCommands());
        getCommand("ecsadmin").setExecutor(new Ecsadmin());
    }

    private void registerTabCompleters() {
        getCommand("ecs").setTabCompleter(new MainCommands());
        getCommand("ecsadmin").setTabCompleter(new Ecsadmin());
    }


    public void loadLanguages() {
        LanguageManager lm = new LanguageManager();
        File customConfigFile = new File(getDataFolder(), "languages.yml");
        if (!customConfigFile.exists()) {
            logConsole("&c[&eEzChestShop&c] &eGenerating languages.yml file...");
            customConfigFile.getParentFile().mkdirs();
            saveResource("languages.yml", false);
            languages = YamlConfiguration.loadConfiguration(customConfigFile);
            lm.setLanguageConfig(languages);
            logConsole("&c[&eEzChestShop&c] &elanguages.yml successfully loaded");
        } else {
            languages = YamlConfiguration.loadConfiguration(customConfigFile);
            lm.setLanguageConfig(languages);
            logConsole("&c[&eEzChestShop&c] &elanguages.yml successfully loaded");
        }
    }





    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getDatabase().disconnect();

        try {
            for (Object object : Utils.onlinePackets) {

                if (object instanceof ASHologram) {
                    ASHologram hologram = (ASHologram) object;
                    hologram.destroy();
                    continue;
                }
                if (object instanceof FloatingItem) {
                    FloatingItem floatingItem = (FloatingItem) object;
                    floatingItem.destroy();
                }

            }
        } catch (Exception e) {

        }


    }


    public static EzChestShop getPlugin() {
        return plugin;
    }

    public void logConsole(String str) {
        getServer().getConsoleSender().sendMessage(Utils.color(str));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public Database getDatabase() {
        return this.db;
    }

    public static FileConfiguration getLanguages() {
        return languages;
    }

    public static void setLanguages(FileConfiguration file) {
        languages = file;
    }




}


