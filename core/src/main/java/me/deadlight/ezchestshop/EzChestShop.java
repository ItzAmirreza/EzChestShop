package me.deadlight.ezchestshop;
import me.deadlight.ezchestshop.Commands.CommandCheckProfits;
import me.deadlight.ezchestshop.Commands.EcsAdmin;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.DatabaseManager;
import me.deadlight.ezchestshop.Data.GUI.GuiData;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Listeners.*;
import me.deadlight.ezchestshop.Tasks.LoadedChunksTask;
import me.deadlight.ezchestshop.Utils.*;
import me.deadlight.ezchestshop.Utils.Exceptions.CommandFetchException;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class EzChestShop extends JavaPlugin {

    private static EzChestShop plugin;

    private static Economy econ = null;

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

        Config.loadConfig();

        // load database
        if (Config.database_type != null) {
            Utils.recognizeDatabase();
        } else {
            logConsole(
                    "&c[&eEzChestShop&c] &cDatabase type not specified/or is wrong in config.yml! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Plugin startup logic
        if (!(getServer().getVersion().contains("1.19") || getServer().getVersion().contains("1.18")
                || getServer().getVersion().contains("1.17") || getServer().getVersion().contains("1.16")
                || getServer().getVersion().contains("1.15") || getServer().getVersion().contains("1.14"))) {
            logConsole("&c[&eEzChestShop&c] &4This plugin only supports 1.14 - 1.19!, &cself disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            logConsole("&c[&eEzChestShop&c] &eCurrent Protocol version initialized.");
        }

        if (!setupEconomy()) {
            logConsole(
                    "&c[&eEzChestShop&c] &4Cannot find vault or economy plugin. Self disabling... &ePlease note that you need vault and at least one economy plugin installed.");
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

        GuiData.loadGuiData();
        try {
            GuiData.checkForGuiDataYMLupdate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerListeners();
        registerCommands();
        registerTabCompleters();
        // metrics
        Metrics metrics = new Metrics(this, 10756);
        metrics.addCustomChart(new Metrics.SimplePie("database_type", () -> {
            return Config.database_type.toString();
        }));
        metrics.addCustomChart(new Metrics.SimplePie("update_notification", () -> {
            return String.valueOf(Config.notify_updates);
        }));
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> {
            return Config.language;
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_shops", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // (This is useless as there is already a player chart by default.)
                return ShopContainer.getShops().size();
            }
        }));
        metrics.addCustomChart(new Metrics.AdvancedPie("materials_used", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
               for (EzShop shop : ShopContainer.getShops()) {
                        String itemMaterial = shop.getShopItem().getType().toString();
                        if (valueMap.containsKey(itemMaterial)) {
                            valueMap.put(itemMaterial, valueMap.get(itemMaterial) + 1);
                        } else {
                            valueMap.put(itemMaterial, 1);
                        }
                    }
                return valueMap;
            }

        }));
        metrics.addCustomChart(new Metrics.AdvancedPie("rotation_used", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (EzShop shop : ShopContainer.getShops()) {
                    String rotation = shop.getSettings().getRotation();
                    if (valueMap.containsKey(rotation)) {
                        valueMap.put(rotation, valueMap.get(rotation) + 1);
                    } else {
                        valueMap.put(rotation, 1);
                    }
                }

                return valueMap;
            }

        }));
        metrics.addCustomChart(new Metrics.AdvancedPie("is_admin_shop", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (EzShop shop : ShopContainer.getShops()) {
                    boolean adminshop = shop.getSettings().isAdminshop();
                    if (valueMap.containsKey(String.valueOf(adminshop))) {
                        valueMap.put(String.valueOf(adminshop), valueMap.get(String.valueOf(adminshop)) + 1);
                    } else {
                        valueMap.put(String.valueOf(adminshop), 1);
                    }
                }

                return valueMap;
            }

        }));

        metrics.addCustomChart(new Metrics.AdvancedPie("disabled_buy_count", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (EzShop shop : ShopContainer.getShops()) {
                    boolean disabledBuy = shop.getSettings().isDbuy();
                    if (valueMap.containsKey(String.valueOf(disabledBuy))) {
                        valueMap.put(String.valueOf(disabledBuy), valueMap.get(String.valueOf(disabledBuy)) + 1);
                    } else {
                        valueMap.put(String.valueOf(disabledBuy), 1);
                    }
                }

                return valueMap;
            }

        }));

        metrics.addCustomChart(new Metrics.AdvancedPie("disabled_sell_count", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (EzShop shop : ShopContainer.getShops()) {
                    boolean disabledSell = shop.getSettings().isDsell();
                    if (valueMap.containsKey(String.valueOf(disabledSell))) {
                        valueMap.put(String.valueOf(disabledSell), valueMap.get(String.valueOf(disabledSell)) + 1);
                    } else {
                        valueMap.put(String.valueOf(disabledSell), 1);
                    }
                }

                return valueMap;
            }

        }));

        metrics.addCustomChart(new Metrics.AdvancedPie("message_toggle_count", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (EzShop shop : ShopContainer.getShops()) {
                    boolean messageToggle = shop.getSettings().isMsgtoggle();
                    if (valueMap.containsKey(String.valueOf(messageToggle))) {
                        valueMap.put(String.valueOf(messageToggle), valueMap.get(String.valueOf(messageToggle)) + 1);
                    } else {
                        valueMap.put(String.valueOf(messageToggle), 1);
                    }
                }

                return valueMap;
            }

        }));

        metrics.addCustomChart(new Metrics.AdvancedPie("shared_income_count", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (EzShop shop : ShopContainer.getShops()) {
                    boolean sharedIncome = shop.getSettings().isShareincome();
                    if (valueMap.containsKey(String.valueOf(sharedIncome))) {
                        valueMap.put(String.valueOf(sharedIncome), valueMap.get(String.valueOf(sharedIncome)) + 1);
                    } else {
                        valueMap.put(String.valueOf(sharedIncome), 1);
                    }
                }

                return valueMap;
            }

        }));

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
        getServer().getPluginManager().registerEvents(new CommandCheckProfits(), this);
        getServer().getPluginManager().registerEvents(new UpdateChecker(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ChestShopBreakPrevention(), this);
        // Add Config check over here, to change the Shop display varient.
        // PlayerLooking is less laggy but probably harder to spot.
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

        getDatabase().disconnect();

        logConsole("&c[&eEzChestShop&c] &aCompleted. ");

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
        } catch (Exception ignored) {

        }

        try {
            for (BlockOutline outline : Utils.activeOutlines.values()) {
                outline.hideOutline();
            }

            Utils.activeOutlines.clear();
            Utils.enabledOutlines.clear();

        } catch (Exception ignored) {

        }

        logConsole("&c[&eEzChestShop&c] &4Plugin is now disabled. ");

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

    public DatabaseManager getDatabase() {
        return Utils.databaseManager;
    }

}
