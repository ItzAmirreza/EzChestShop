package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
    public static String language;
    public static boolean debug_logging;


    public static void loadConfig() {
        //reloading/loading config.yml

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

        language = config.getString("language");
        if (!LanguageManager.getSupportedLanguages().contains(language)) {
            EzChestShop.logConsole("&c[&eEzChestShop&c] Error. Non supported language: " + language + ". Switching to Locale_EN.");
            language = "Locale_EN";
        }
        debug_logging = config.getBoolean("debug.logging");

    }

    //this one checks for the config.yml ima make one for language.yml
    public static void checkForConfigYMLupdate() throws IOException {
        //update 1.3.3 new config file model update constructed by ElitoGame
        boolean isOldConfigModel = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).isBoolean("show-holograms");
        //if true, then we have to implement the new config model and delete old ones
        if (isOldConfigModel) {
            //getting current values of configs
            //show-holograms
            boolean show_holograms = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getBoolean("show-holograms");
            String hologram_first_line = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getString("hologram-first-line");
            String hologram_second_line = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getString("hologram-second-line");
            int hologram_disappearance_delay = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).getInt("hologram-disappearance-delay");

            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));

            fc.set("show-holograms", null);
            fc.set("hologram-first-line", null);
            fc.set("hologram-second-line", null);
            fc.set("hologram-disappearance-delay", null);

            fc.set("hologram.show-holograms", show_holograms);
            fc.set("hologram.hologram-first-line", hologram_first_line);
            fc.set("hologram.hologram-second-line", hologram_second_line);
            fc.set("hologram.hologram-disappearance-delay", hologram_disappearance_delay);

            //new economy config section
            fc.set("economy.server-currency", "$");

            fc.set("container.chests", true);
            fc.set("container.barrels", true);
            fc.set("container.shulkers", true);

            fc.set("language", "Locale_EN");
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            Config.loadConfig();

        }

        //well then its already an updated config, no need to change
    }

}
