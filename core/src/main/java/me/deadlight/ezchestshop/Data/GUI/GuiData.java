package me.deadlight.ezchestshop.Data.GUI;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiData {

    public enum GuiType {
        SHOP_GUI,
        SHOP_SETTINGS,
        TRANSACTION_LOGS,
        HOLOGRAM_MESSAGES_MANAGER
    }

    private static ContainerGui logs;
    private static ContainerGui shop;
    private static ContainerGui settings;
    private static ContainerGui messageManager;

    private static FileConfiguration config;

    //TODO implement a way to upgrade GUIs and apply our changes, without disturbing player settings.
    // solution (probably):
    // If a gui is identical value wise with our gui, update it automatically. If it differs,
    // send the OPs a message that a new GUI element is available and should be implemented using the
    // /ecsadmin configure-guis command.
    // New Idea:
    // Just add the data! Multiple items can be in multiple places, so we just need a way to add them like in
    // the config.yml. This way, we don't have to deal with any problems at all and server owners just need to
    // move the items around if they think they don't fit in.

    public static void loadGuiData() {
        File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml");
        if (!customConfigFile.exists()) {
            EzChestShop.logConsole("&c[&eEzChestShop&c] &eGenerating guis.yml file...");
            customConfigFile.getParentFile().mkdirs();
            EzChestShop.getPlugin().saveResource("guis.yml", false);
            customConfigFile = new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml");
        }
        FileConfiguration guisConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        shop = new ContainerGui(guisConfig, "shop-gui");
        settings = new ContainerGui(guisConfig, "shop-settings");
        logs = new ContainerGui(guisConfig, "transaction-logs");
        messageManager = new ContainerGui(guisConfig, "hologram-messages-manager");
        config = guisConfig;
    }

    public static ContainerGui getLogs() {
        return logs.clone();
    }

    public static ContainerGui getShop() {
        return shop.clone();
    }

    public static ContainerGui getSettings() {
        return settings.clone();
    }

    public static ContainerGui getMessageManager() {
        return messageManager.clone();
    }

    public static ContainerGui getViaType(GuiType type) {
        switch (type) {
            case SHOP_GUI:
                return getShop();
            case SHOP_SETTINGS:
                return getSettings();
            case TRANSACTION_LOGS:
                return getLogs();
            case HOLOGRAM_MESSAGES_MANAGER:
                return getMessageManager();
            default:
                return null;
        }
    }

    public static void checkForGuiDataYMLupdate() throws IOException {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));

        //TODO implement the updater. Once needed.

        fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
        loadGuiData();
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    /**
     * This method is used to compare GUIs for updates. It is responsible for alerting admins when a UI has
     * unusually overlapping items. This method lists all "usual" overlaps.
     * @param type
     * @return
     */
    public static List<List<String>> getAllowedDefaultOverlappingItems(GuiType type) {
        switch (type) {
            case SHOP_GUI:
                return Arrays.asList(Arrays.asList("storage", "admin-view"));
            case SHOP_SETTINGS:
                return Arrays.asList(
                        Arrays.asList("toggle-transaction-message-off", "toggle-transaction-message-on"),
                        Arrays.asList("disable-buy-off", "disable-buy-on"),
                        Arrays.asList("disable-sell-off", "disable-sell-on"),
                        Arrays.asList("hologram-rotation-all", "hologram-rotation-up", "hologram-rotation-down",
                                "hologram-rotation-east", "hologram-rotation-west", "hologram-rotation-north",
                                "hologram-rotation-south"),
                        Arrays.asList("share-income-off", "share-income-on"));
            default:
                return null;
        }
    }

}
