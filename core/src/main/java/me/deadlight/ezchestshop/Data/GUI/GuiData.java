package me.deadlight.ezchestshop.Data.GUI;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

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

    public static FileConfiguration getConfig() {
        return config;
    }

}
