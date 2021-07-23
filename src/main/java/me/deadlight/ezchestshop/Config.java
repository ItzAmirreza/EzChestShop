package me.deadlight.ezchestshop;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static String currency;
    public static boolean showholo;
    public static String firstLine;
    public static String secondLine;
    public static int holodelay;


    public static void loadConfig() {
        //reloading config.yml

        EzChestShop.getPlugin().reloadConfig();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();
        currency = config.getString("economy.server-currency");
        showholo = config.getBoolean("show-holograms");
        firstLine = config.getString("hologram-first-line");
        secondLine = config.getString("hologram-second-line").replace("%currency%", Config.currency);
        holodelay = config.getInt("hologram-disappearance-delay");

    }
}
