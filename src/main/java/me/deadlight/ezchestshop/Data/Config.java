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
    public static boolean container_barrels;
    public static boolean container_shulkers;


    public static void loadConfig() {
        //reloading config.yml

        EzChestShop.getPlugin().reloadConfig();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();
        currency = config.getString("economy.server-currency");

        showholo = config.getBoolean("hologram.show-holograms");
        firstLine = config.getString("hologram.hologram-first-line");
        secondLine = config.getString("hologram.hologram-second-line").replace("%currency%", Config.currency);
        holodelay = config.getInt("hologram.hologram-disappearance-delay");
        holodistancing = config.getBoolean("hologram.distance.toggled");
        holodistancing_distance = config.getDouble("hologram.distance.range");

        container_chests = config.getBoolean("container.chests");
        container_barrels = config.getBoolean("container.barrels");
        container_shulkers = config.getBoolean("container.shulkers");

    }
}
