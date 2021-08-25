package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static String currency;

    public static boolean showholo;
    public static String firstLine;
    public static String secondLine;
    public static int holodelay;
    public static boolean holodistancing;
    public static double holodistancing_distance;

    public static boolean container_chests;
    public static boolean container_trapped_chests;
    public static boolean container_barrels;
    public static boolean container_shulkers;

    public static boolean command_shop_alias;
    public static boolean command_adminshop_alias;

    public static boolean permissions_create_shop_enabled;



    public static void loadConfig() {
        //reloading config.yml

        EzChestShop.getPlugin().reloadConfig();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();
        currency = config.getString("economy.server-currency");

        showholo = config.getBoolean("shops.hologram.show-holograms");
        firstLine = config.getString("shops.hologram.hologram-first-line");
        secondLine = config.getString("shops.hologram.hologram-second-line").replace("%currency%", Config.currency);
        holodelay = config.getInt("shops.hologram.hologram-disappearance-delay");
        holodistancing = config.getBoolean("shops.hologram.distance.toggled");
        holodistancing_distance = config.getDouble("shops.hologram.distance.range");

        container_chests = config.getBoolean("shops.container.chests");
        container_trapped_chests = config.getBoolean("shops.container.trapped-chests");
        container_barrels = config.getBoolean("shops.container.barrels");
        container_shulkers = config.getBoolean("shops.container.shulkers");

        command_shop_alias = config.getBoolean("commands.alias.ecs-shop");
        command_adminshop_alias = config.getBoolean("commands.alias.ecsadmin-adminshop");

        permissions_create_shop_enabled = config.getBoolean("permissions.create-shops");
    }
}
