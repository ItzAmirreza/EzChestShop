package me.deadlight.ezchestshop;
import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.handlers.ChestsManager;
import me.deadlight.ezchestshop.Commands.CommandCheckProfits;
import me.deadlight.ezchestshop.Commands.EcsAdmin;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.Data.SQLite.SQLite;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Listeners.*;
import me.deadlight.ezchestshop.Tasks.LoadedChunksTask;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.CommandRegister;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Exceptions.CommandFetchException;
import me.deadlight.ezchestshop.Utils.UpdateChecker;
import me.deadlight.ezchestshop.Utils.Utils;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class EzChestShop extends JavaPlugin {

    private static EzChestShop plugin;
    private static FileConfiguration languages;

    private static Economy econ = null;

    private Database db;

    public boolean integrationWildChests = false;
    public ChestsManager wchests = null;

    public static boolean protocollib = false;
    public static boolean slimefun = false;
    public static boolean worldguard = false;


    @Override
    public void onLoad() {
        // Adds Custom Flags to WorldGuard!
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldguard = true;
            FlagRegistry.onLoad();
        }
    }

    @Override
    public void onEnable() {

        plugin = this;
        logConsole("&c[&eEzChestShop&c] &aEnabling EzChestShop - version " + this.getDescription().getVersion());
        saveDefaultConfig();

        this.db = new SQLite(this);
        this.db.load();

        Config.loadConfig();
        // Plugin startup logic
        if (!(getServer().getVersion().contains("1.18") || getServer().getVersion().contains("1.17"))) {
            logConsole("&c[&eEzChestShop&c] &4This is an 1.18/1.17 only version, your build is outdated! Self disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            logConsole("&c[&eEzChestShop&c] &eCurrent Protocol version initialized.");
        }

        if (!setupEconomy() ) {
            logConsole("&c[&eEzChestShop&c] &4Cannot find vault or economy plugin. Self disabling... &ePlease note that you need vault and at least one economy plugin installed.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Config.checkForConfigYMLupdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LanguageManager.loadLanguages();
        try {
            LanguageManager.checkForLanguagesYMLupdate();
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

        if (getServer().getPluginManager().getPlugin("Slimefun") != null) {
            slimefun = true;
            logConsole("&c[&eEzChestShop&c] &eSlimefun integration initialized.");
        }


        ShopContainer.queryShopsToMemory();
        ShopContainer.startSqlQueueTask();
        if (Config.check_for_removed_shops) {
            LoadedChunksTask.startTask();
        }

        UpdateChecker checker = new UpdateChecker();
        checker.check();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChestOpeningListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerTransactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPistonExtendListener(), this);
        getServer().getPluginManager().registerEvents(new SQLite(this), this);
        getServer().getPluginManager().registerEvents(new CommandCheckProfits(), this);
        getServer().getPluginManager().registerEvents(new UpdateChecker(), this);
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
        PluginCommand ecs = getCommand("ecs");
        PluginCommand ecsadmin = getCommand("ecsadmin");
        CommandRegister register = new CommandRegister();
        try {
            if (Config.command_shop_alias) {
                register.registerCommandAlias(ecs, "shop");
            }
            if (Config.command_adminshop_alias) {
                register.registerCommandAlias(ecsadmin, "adminshop");
            }
        } catch (CommandFetchException e) {
            e.printStackTrace();
        }
        ecs.setExecutor(new MainCommands());
        ecsadmin.setExecutor(new EcsAdmin());
        getCommand("checkprofits").setExecutor(new CommandCheckProfits());
    }

    private void registerTabCompleters() {
        getCommand("ecs").setTabCompleter(new MainCommands());
        getCommand("ecsadmin").setTabCompleter(new EcsAdmin());
        getCommand("checkprofits").setTabCompleter(new CommandCheckProfits());
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getScheduler().cancelTasks(this);
        logConsole("&c[&eEzChestShop&c] &bSaving remained sql cache...");
        ShopContainer.saveSqlQueueCache();
        logConsole("&c[&eEzChestShop&c] &aCompleted. ");

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

    public static void logConsole(String str) {
        EzChestShop.getPlugin().getServer().getConsoleSender().sendMessage(Utils.colorify(str));
    }

    public static void logDebug(String str) {
        if (Config.debug_logging)
            EzChestShop.getPlugin().getServer().getConsoleSender().sendMessage("[Ecs-Debug] " + Utils.colorify(str));
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




}


