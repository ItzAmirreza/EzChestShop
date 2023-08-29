package me.deadlight.ezchestshop.data.gui;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.listeners.UpdateChecker;
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
        TRADE_SHOP_GUI,
        TRADE_SHOP_SETTINGS,
        TRANSACTION_LOGS,
        HOLOGRAM_MESSAGES_MANAGER
    }

    private static ContainerGui logs;
    private static ContainerGui shop;
    private static ContainerGui settings;
    private static ContainerGui tradeShop;
    private static ContainerGui tradeShopSettings;
    private static ContainerGui messageManager;

    private static FileConfiguration config;

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
        tradeShop = new ContainerGui(guisConfig, "trade-shop-gui");
        tradeShopSettings = new ContainerGui(guisConfig, "trade-shop-settings");
        logs = new ContainerGui(guisConfig, "transaction-logs");
        messageManager = new ContainerGui(guisConfig, "hologram-messages-manager");
        config = guisConfig;
        new UpdateChecker().resetGuiCheck();
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

    public static ContainerGui getTradeShop() {
        return tradeShop.clone();
    }

    public static ContainerGui getTradeShopSettings() {
        return tradeShopSettings.clone();
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
            case TRADE_SHOP_GUI:
                return getTradeShop();
            case TRADE_SHOP_SETTINGS:
                return getTradeShopSettings();
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
        boolean changed = false;

        // Use this once needed, it's tested and works.
//        if (!fc.contains("shop-settings.items.coupon")) {
//            fc.set("shop-settings.items.coupon.row", 3);
//            fc.set("shop-settings.items.coupon.column", 2);
//            fc.set("shop-settings.items.coupon.material", "PAPER");
//            fc.set("shop-settings.items.coupon.enchanted", false);
//            changed = true;
//        }

        //TODO write updater for guis.yml

        if (changed) {
            EzChestShop.logConsole("&c[&eEzChestShop&c] &eUpdating guis.yml file...");
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
            loadGuiData();
        }
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
            case TRADE_SHOP_GUI:
                return Arrays.asList(
                        Arrays.asList("storage", "admin-view"),
                        Arrays.asList("trade-direction-item1toitem2", "trade-direction-item2toitem1",
                                "trade-direction-both", "trade-direction-disabled"));
            case TRADE_SHOP_SETTINGS:
                return Arrays.asList(
                        Arrays.asList("toggle-transaction-message-off", "toggle-transaction-message-on"),
                        Arrays.asList("toggle-trade-direction-item1toitem2", "toggle-trade-direction-item2toitem1",
                                "toggle-trade-direction-both", "toggle-trade-direction-disabled"),
                        Arrays.asList("hologram-rotation-all", "hologram-rotation-up", "hologram-rotation-down",
                                "hologram-rotation-east", "hologram-rotation-west", "hologram-rotation-north",
                                "hologram-rotation-south"));
            default:
                return null;
        }
    }

}
