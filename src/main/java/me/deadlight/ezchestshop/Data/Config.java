package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Config {

    public static String currency;

    public static boolean showholo;
    public static List<String> holostructure;
    public static List<String> holostructure_admin;
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
        holostructure = config.getStringList("shops.hologram.holo-structure");
        Collections.reverse(holostructure);
        holostructure_admin = config.getStringList("shops.hologram.holo-structure-adminshop");
        Collections.reverse(holostructure_admin);
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
