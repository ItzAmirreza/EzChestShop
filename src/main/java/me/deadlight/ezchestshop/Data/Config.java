package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Config {

    public static String currency;

    public static boolean showholo;
    public static List<String> holostructure;
    public static List<String> holostructure_admin;
    public static double holo_linespacing;
    public static int holodelay;
    public static boolean holo_rotation;
    public static boolean holodistancing;
    public static double holodistancing_distance;
    public static boolean holodistancing_show_item_first;


    public static boolean container_chests;
    public static boolean container_trapped_chests;
    public static boolean container_barrels;
    public static boolean container_shulkers;

    public static String display_numberformat_gui;
    public static String display_numberformat_chat;
    public static String display_numberformat_holo;

    public static boolean settings_defaults_transactions;
    public static boolean settings_defaults_dbuy;
    public static boolean settings_defaults_dsell;
    public static String settings_defaults_rotation;
    public static boolean settings_defaults_shareprofits;

    public static boolean settings_zero_equals_disabled;
    public static boolean settings_buy_greater_then_sell;
    public static boolean settings_add_shulkershop_lore;

    public static boolean command_shop_alias;
    public static boolean command_adminshop_alias;
    public static int command_checkprofit_lines_pp;

    public static boolean permissions_create_shop_enabled;

    public static String language;
    public static boolean debug_logging;



    public static void loadConfig() {
        //reloading config.yml

        EzChestShop.getPlugin().reloadConfig();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();
        currency = config.getString("economy.server-currency");

        showholo = config.getBoolean("shops.hologram.show-holograms");
        holostructure = config.getStringList("shops.hologram.holo-structure");
        Collections.reverse(holostructure);
        holostructure_admin = config.getStringList("shops.hologram.holo-structure-adminshop");
        Collections.reverse(holostructure_admin);
        holo_linespacing = config.getDouble("shops.hologram.holo-line-spacing");
        holodelay = config.getInt("shops.hologram.hologram-disappearance-delay");
        holo_rotation = config.getBoolean("shops.hologram.allow-rotation");
        holodistancing = config.getBoolean("shops.hologram.distance.toggled");
        holodistancing_distance = config.getDouble("shops.hologram.distance.range");
        holodistancing_show_item_first = config.getBoolean("shops.hologram.distance.show-items-first");

        container_chests = config.getBoolean("shops.container.chests");
        container_trapped_chests = config.getBoolean("shops.container.trapped-chests");
        container_barrels = config.getBoolean("shops.container.barrels");
        container_shulkers = config.getBoolean("shops.container.shulkers");

        display_numberformat_gui = config.getString("shops.display.number-format.gui");
        display_numberformat_chat = config.getString("shops.display.number-format.chat");
        display_numberformat_holo = config.getString("shops.display.number-format.hologram");

        settings_defaults_transactions = config.getBoolean("shops.settings.defaults.transaction-message");
        settings_defaults_dbuy = config.getBoolean("shops.settings.defaults.disable-buying");
        settings_defaults_dsell = config.getBoolean("shops.settings.defaults.disable-selling");
        settings_defaults_rotation = config.getString("shops.settings.defaults.rotation");
        settings_defaults_shareprofits = config.getBoolean("shops.settings.defaults.share-profit");

        settings_zero_equals_disabled = config.getBoolean("shops.settings.zero-price-equals-disabled");
        settings_buy_greater_then_sell = config.getBoolean("shops.settings.buy-greater-then-sell");
        settings_add_shulkershop_lore = config.getBoolean("shops.settings.add-shulkershop-lore");

        command_shop_alias = config.getBoolean("commands.alias.ecs-shop");
        command_adminshop_alias = config.getBoolean("commands.alias.ecsadmin-adminshop");
        command_checkprofit_lines_pp = config.getInt("commands.checkprofit-lines-per-page");

        permissions_create_shop_enabled = config.getBoolean("permissions.create-shops");

        language = config.getString("language");
        if (!LanguageManager.getSupportedLanguages().contains(language)) {
            if (LanguageManager.getFoundlanguages().contains(language + ".yml")) {
                EzChestShop.logConsole("&c[&eEzChestShop&c]&e Using externally created language: " + language + ".");
            } else {
                EzChestShop.logConsole("&c[&eEzChestShop&c] Error. Non supported language: " + language + ". Switching to Locale_EN.");
                language = "Locale_EN";
            }
        }
        debug_logging = config.getBoolean("debug.logging");
    }


    //this one checks for the config.yml ima make one for language.yml
    public static void checkForConfigYMLupdate() throws IOException {

        YamlConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
        //update 1.3.3 new config file model update constructed by ElitoGame
        boolean isOldConfigModel = fc.isBoolean("show-holograms");
        boolean isUsingOldhologramLineSystem = fc.isString("shops.hologram.hologram-first-line");
        //if true, then we have to implement the new config model and delete old ones
        if (isOldConfigModel) {
            //getting current values of configs
            //show-holograms
            boolean show_holograms = fc.getBoolean("show-holograms");
            String hologram_first_line = fc.getString("hologram-first-line");
            String hologram_second_line = fc.getString("hologram-second-line");
            int hologram_disappearance_delay = fc.getInt("hologram-disappearance-delay");


            fc.set("show-holograms", null);
            fc.set("hologram-first-line", null);
            fc.set("hologram-second-line", null);
            fc.set("hologram-disappearance-delay", null);

            fc.set("shops.hologram.show-holograms", show_holograms);
            fc.set("shops.hologram.hologram-first-line", hologram_first_line);
            fc.set("shops.hologram.hologram-second-line", hologram_second_line);
            fc.set("shops.hologram.hologram-disappearance-delay", hologram_disappearance_delay);
            //new hologram settings:
            fc.set("shops.hologram.distance.toggled", true);
            fc.set("shops.hologram.distance.range", 10.0);

            //new containers:
            fc.set("shops.container.chests", true);
            fc.set("shops.container.trapped-chests", true);
            fc.set("shops.container.barrels", true);
            fc.set("shops.container.shulkers", true);

            //new commands section:
            fc.set("commands.alias.ecs-shop", false);
            fc.set("commands.alias.ecsadmin-adminshop", false);

            //new permissions section:
            fc.set("permissions.create-shops", false);

            //new economy config section
            fc.set("economy.server-currency", "$");

            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            Config.loadConfig();

        }

        if (isUsingOldhologramLineSystem) {
            EzChestShop.getPlugin().logConsole("Updated Hologram List!");

            fc.set("shops.hologram.hologram-first-line", null);
            fc.set("shops.hologram.hologram-second-line", null);

            List<String> holo = Arrays.asList("<buy>&fBuy: &a%buy% %currency%</buy><separator> &f| </separator><sell>&fSell: &c%sell% %currency%</sell>", "&d%item%", "[item]");

            fc.set("shops.hologram.holo-structure", holo);
            fc.set("shops.hologram.holo-structure-adminshop", new ArrayList<>(holo));
            fc.set("shops.hologram.holo-line-spacing", 1);
            fc.set("shops.hologram.distance.show-items-first", true);

            fc.set("shops.hologram.allow-rotation", true);

            fc.set("shops.display.number-format.gui", "###,###.##");
            fc.set("shops.display.number-format.chat", "###,###.##");
            fc.set("shops.display.number-format.hologram", "###,###.##");

            fc.set("shops.settings.defaults.transaction-message", false);
            fc.set("shops.settings.defaults.disable-buying", false);
            fc.set("shops.settings.defaults.disable-selling", false);
            fc.set("shops.settings.defaults.rotation", "up");
            fc.set("shops.settings.defaults.share-profit", false);

            fc.set("shops.settings.zero-price-equals-disabled", true);
            fc.set("shops.settings.buy-greater-then-sell", true);
            fc.set("shops.settings.add-shulkershop-lore", true);

            fc.set("commands.checkprofit-lines-per-page", 4);

            fc.set("language", "Locale_EN");

            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            Config.loadConfig();
        }

        //well then its already an updated config, no need to change

    }
}
