package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.Enums.Database;
import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import scala.Int;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public static boolean settings_buy_greater_than_sell;
    public static boolean settings_add_shulkershop_lore;
    public static boolean settings_custom_amout_transactions;

    public static boolean settings_hologram_message_enabled;
    public static boolean settings_hologram_message_show_always;
    public static int settings_hologram_message_line_count_default;
    public static boolean settings_hologram_message_show_empty_shop_always;

    public static boolean command_shop_alias;
    public static boolean command_adminshop_alias;
    public static int command_checkprofit_lines_pp;

    public static boolean permissions_create_shop_enabled;
    public static boolean permission_hologram_message_limit;
    public static boolean permission_hologram_message_line_count;

    public static boolean check_for_removed_shops;

    public static String language;
    public static boolean debug_logging;

    public static boolean notify_updates;
    public static boolean notify_overlapping_gui_items;
    public static boolean notify_overflowing_gui_items;

    public static boolean worldguard_integration;
    public static Database database_type;
    public static String databasemysql_ip;
    public static int databasemysql_port;
    public static int databasemysql_maxpool;
    public static String databasemysqltables_prefix;
    public static String databasemysql_databasename;
    public static String databasemysql_username;
    public static String databasemysql_password;
    public static boolean databasemysql_use_ssl;
    public static String databasemongodb_connection_string;

    public static boolean shopProtection;



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
        settings_buy_greater_than_sell = config.getBoolean("shops.settings.buy-greater-than-sell");
        settings_add_shulkershop_lore = config.getBoolean("shops.settings.add-shulkershop-lore");
        settings_custom_amout_transactions = config.getBoolean("shops.settings.custom-amount-transactions");

        settings_hologram_message_enabled = config.getBoolean("shops.settings.hologram-messages.enabled");
        settings_hologram_message_show_always = config.getBoolean("shops.settings.hologram-messages.show-always");
        settings_hologram_message_line_count_default = config.getInt("shops.settings.hologram-messages.line-count-default");
        settings_hologram_message_show_empty_shop_always = config.getBoolean("shops.settings.hologram-messages.show-empty-shop-always");

        command_shop_alias = config.getBoolean("commands.alias.ecs-shop");
        command_adminshop_alias = config.getBoolean("commands.alias.ecsadmin-adminshop");
        command_checkprofit_lines_pp = config.getInt("commands.checkprofit-lines-per-page");

        permissions_create_shop_enabled = config.getBoolean("permissions.create-shops");
        permission_hologram_message_limit = config.getBoolean("permissions.hologram-message-limit");
        permission_hologram_message_line_count = config.getBoolean("permissions.hologram-message-line-count");

        check_for_removed_shops = config.getBoolean("tasks.check-for-removed-shops");

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

        notify_updates = config.getBoolean("other.notify-op-of-updates");
        notify_overlapping_gui_items = config.getBoolean("other.notify-op-of-overlapping-gui-items");
        notify_overflowing_gui_items = config.getBoolean("other.notify-op-of-overflowing-gui-items");
        worldguard_integration = config.getBoolean("integration.worldguard");
        database_type = Database.valueOf(config.getString("database.type").toUpperCase());
        databasemysql_ip = config.getString("database.mysql.ip");
        databasemysql_port = config.getInt("database.mysql.port");
        databasemysql_maxpool = config.getInt("database.mysql.max-pool");
        databasemysqltables_prefix = config.getString("database.mysql.tables-prefix");
        databasemysql_databasename = config.getString("database.mysql.database");
        databasemysql_username = config.getString("database.mysql.username");
        databasemysql_password = config.getString("database.mysql.password");
        databasemysql_use_ssl = config.getBoolean("database.mysql.ssl");
        databasemongodb_connection_string = config.getString("database.mongodb.connection-string");

        shopProtection = config.getBoolean("protection.prevent-shop-destruction", true);
    }


    //this one checks for the config.yml ima make one for language.yml
    public static void checkForConfigYMLupdate() throws IOException {

        YamlConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));

        //1.5.0 config update
        if (!fc.isBoolean("shops.settings.hologram-messages.enabled")) {
            fc.set("shops.settings.hologram-messages.enabled", true);
            fc.set("shops.settings.hologram-messages.show-always", false);
            fc.set("shops.settings.hologram-messages.line-count-default", 1);
            fc.set("permissions.hologram-message-limit", false);
            fc.set("permissions.hologram-message-line-count", false);

            fc.set("other.notify-op-of-updates", true); // has been in the plugin for a while, but never added to existing configs
            fc.set("other.notify-op-of-overlapping-gui-items", true);
            fc.set("other.notify-op-of-overflowing-gui-items", true);

            List<String> structure = new ArrayList<>(fc.getStringList("shops.hologram.holo-structure"));
            structure.addAll(0, Arrays.asList("<emptyShopInfo/>", "<custom1/>", "<custom2/>", "<custom3/>", "<custom4/>"));
            Integer index = structure.indexOf("[item]");
            structure.addAll(index == null ? structure.size() - 1 : index, Arrays.asList("<itemdata1/>", "<itemdata2/>", "<itemdata3/>", "<itemdata4/>", "<itemdataRest/>"));
            fc.set("shops.hologram.holo-structure", structure);

            List<String> structureAdmin = new ArrayList<>(fc.getStringList("shops.hologram.holo-structure-adminshop"));
            structureAdmin.addAll(0, Arrays.asList("<emptyShopInfo/>", "<custom1/>", "<custom2/>", "<custom3/>", "<custom4/>"));
            Integer indexAdmin = structureAdmin.indexOf("[item]");
            structureAdmin.addAll(indexAdmin == null ? structureAdmin.size() - 1 : indexAdmin, Arrays.asList("<itemdata1/>", "<itemdata2/>", "<itemdata3/>", "<itemdata4/>", "<itemdataRest/>"));
            fc.set("shops.hologram.holo-structure-adminshop", structureAdmin);

            fc.set("shop.settings.hologram-messages.show-empty-shop-always", true);
            //database update
            fc.set("database.type", "SQLite");
            fc.set("database.mysql.ip", "127.0.0.1");
            fc.set("database.mysql.port", 3306);
            fc.set("database.mysql.tables-prefix", "ecs_");
            fc.set("database.mysql.database", "TheDatabaseName");
            fc.set("database.mysql.usernmae", "TheUsername");
            fc.set("database.mysql.password", "ThePassword");
            fc.set("database.mysql.ssl", false);
            fc.set("database.mongodb.connection-string", "connection string");

            //integration update
            fc.set("integration.worldguard", true);

            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            Config.loadConfig();
        }

        //1.5.3 config update
        boolean updated1_5_3 = fc.isBoolean("protection.prevent-shop-destruction");
        if (!updated1_5_3) {
            fc.set("protection.prevent-shop-destruction", true);
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            Config.loadConfig();
        }

    }
}
