package me.deadlight.ezchestshop.Data;

import de.themoep.minedown.MineDown;
import me.deadlight.ezchestshop.Commands.EcsAdmin;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.ChatListener;
import me.deadlight.ezchestshop.Utils.Objects.CheckProfitEntry;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Utils;
import net.md_5.bungee.api.chat.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageManager {

    private static FileConfiguration languageConfig;
    private static Economy econ = EzChestShop.getEconomy();

    private static List<String> supported_locales = Arrays.asList("Locale_EN", "Locale_DE", "Locale_ES", "Locale_CN", "Locale_FA", "Locale_PL");
    private static List<String> found_locales = new ArrayList<>();

    public static List<String> getSupportedLanguages() {
        return supported_locales;
    }

    public static List<String> getFoundlanguages() {
        File dir = new File(EzChestShop.getPlugin().getDataFolder() + File.separator + "translations");
        return Arrays.asList(dir.listFiles()).stream().map(f -> f.getName()).collect(Collectors.toList());
    }

    public static void loadLanguages() {
        for (String locale : LanguageManager.getSupportedLanguages()) {
            File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder() + File.separator + "translations",
                    locale + ".yml");
            if (!customConfigFile.exists()) {
                EzChestShop.logConsole("&c[&eEzChestShop&c] &eGenerating " + locale + " file...");
                customConfigFile.getParentFile().mkdirs();
                EzChestShop.getPlugin().saveResource("translations/" + locale + ".yml", false);
            }
        }
        File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder() + File.separator + "translations",
                Config.language + ".yml");
        languageConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        EzChestShop.logConsole("&c[&eEzChestShop&c] &e" + Config.language + " successfully loaded");
    }

    public static void reloadLanguages() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(),
                "translations/" + Config.language + ".yml"));
        languageConfig = fc;
        LanguageManager newLanguage = new LanguageManager();
        MainCommands.updateLM(newLanguage);
        ChatListener.updateLM(newLanguage);
        EcsAdmin.updateLM(newLanguage);
    }

    public static void checkForLanguagesYMLupdate() throws IOException {
        boolean changes = false;
        for (String local : getSupportedLanguages()) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(),
                    "translations/" + local + ".yml"));
            applySpecialChanges(fc, local);
            FileConfiguration fc_internal = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(EzChestShop.getPlugin().getResource("translations/" + local + ".yml")));
            for (String key : fc_internal.getKeys(true)) {
                if (!fc.contains(key)) {
                    fc.set(key, fc_internal.get(key));
                    changes = true;
                }
            }
            if (changes) {
                fc.options().copyHeader(true);
                fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "translations/" + local + ".yml"));
            }
        }
        if (changes) {
            reloadLanguages();
            EzChestShop.logConsole("&c[&eEzChestShop&c]&r &bUpdated Local files.");
        }
    }

    /**
     * If custom changes like a restructure of the Language Files are required,
     * add them in this method using a similar approach as used in Config.checkForConfigYMLupdate();
     * <br><br>
     * Don't forget to set the changes variable to true inside your if statement,
     * else your changes won't be updated.
     * @param fc
     * @param local
     */
    public static void applySpecialChanges(FileConfiguration fc, String local) {
        boolean changes = false;


        if (changes) {
            fc.options().copyHeader(true);
            try {
                fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "translations/" + local + ".yml"));
            } catch (Exception e) {}
        }
    }

    /**
     * Custom getString method to avoid missing language errors. The plugin checks...
     * <ol>
     *     <li>if the external defined Locale contains the requested string</li>
     *     <li>else if the internal defined Locale contains the requested string</li>
     *     <li>else if the external Locale_EN contains the requested string</li>
     *     <li>else if the internal Locale_EN contains the requested string</li>
     * </ol>
     * if all of these fail, null shall be returned, else the first match will.
     * @param string the path to request.
     * @return a String or one of it's fallbacks.
     */
    private String getString(String string) {
        String result = languageConfig.getString(string);
        if (result == null) {
            result = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(EzChestShop.getPlugin().
                            getResource("translations/" + Config.language + ".yml")))
                    .getString(string);
            if (result == null) {
                FileConfiguration fc = YamlConfiguration.loadConfiguration(
                        new File(EzChestShop.getPlugin().getDataFolder(),
                                "translations/Locale_EN.yml"));
                if (result == null) {
                    result = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(EzChestShop.getPlugin().
                                    getResource("translations/Locale_EN.yml")))
                            .getString(string);
                }
            }
        }
        return result;
    }

    /**
     * Custom getString method to avoid missing language errors. The plugin checks...
     * <ol>
     *     <li>if the external defined Locale contains the requested string</li>
     *     <li>else if the internal defined Locale contains the requested string</li>
     *     <li>else if the external Locale_EN contains the requested string</li>
     *     <li>else if the internal Locale_EN contains the requested string</li>
     * </ol>
     * if all of these fail, null shall be returned, else the first match will.
     * @param string the path to request.
     * @return a String or one of it's fallbacks.
     */
    private List<String> getList(String string) {
        List result = languageConfig.getList(string);
        if (result == null || result.isEmpty()) {
            result = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(EzChestShop.getPlugin().
                            getResource("translations/" + Config.language + ".yml")))
                    .getStringList(string);
            if (result == null || result.isEmpty()) {
                FileConfiguration fc = YamlConfiguration.loadConfiguration(
                        new File(EzChestShop.getPlugin().getDataFolder(),
                                "translations/Locale_EN.yml"));
                if (result == null || result.isEmpty()) {
                    result = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(EzChestShop.getPlugin().
                                    getResource("translations/Locale_EN.yml")))
                            .getStringList(string);
                }
            }
        }
        return new ArrayList<>(result);
    }



    //shop-gui.
    /**
     * @return returns buy price of GUI item in lore
     */
    public String initialBuyPrice(double price) {

        return Utils.colorify(getString("shop-gui.initialbuyprice").replace("%buyprice%",
                Utils.formatNumber(price, Utils.FormatType.GUI)).replace("%currency%", Config.currency));
    }/**
     * @return returns buy price of GUI item in lore
     */
    public String initialSellPrice(double price) {

        return Utils.colorify(getString("shop-gui.initialsellprice").replace("%sellprice%",
                Utils.formatNumber(price, Utils.FormatType.GUI)).replace("%currency%", Config.currency));
    }/**
     * @return returns buy price of GUI item in lore
     */
    public String guiAdminTitle(String shopowner) {

        return Utils.colorify(getString("shop-gui.admin-title").replace("%shopowner%", String.valueOf(shopowner)));
    }
    public String guiNonOwnerTitle(String shopowner) {

        return Utils.colorify(getString("shop-gui.nonowner-title").replace("%shopowner%", String.valueOf(shopowner)));
    }
    public String guiOwnerTitle(String shopowner) {

        return Utils.colorify(getString("shop-gui.owner-title").replace("%shopowner%", String.valueOf(shopowner)));
    }
    public String adminshopguititle() {
        return Utils.colorify(getString("shop-gui.adminshop-title"));
    }


    //shop-gui.buttons.
    public String buttonSell1Title() {

        return Utils.colorify(getString("shop-gui.buttons.sellone-title"));
    }
    public List<String> buttonSell1Lore(double price) {
        return getList("shop-gui.buttons.sellone-lore").stream().map(s -> Utils.colorify(s.replace("%price%",
                Utils.formatNumber(price, Utils.FormatType.GUI)).replace("%currency%", Config.currency)))
                .collect(Collectors.toList());
    }
    public String buttonSell64Title() {

        return Utils.colorify(getString("shop-gui.buttons.sell64-title"));
    }
    public List<String> buttonSell64Lore(double price) {
        return getList("shop-gui.buttons.sell64-lore").stream().map(s -> Utils.colorify(s.replace("%price%",
                Utils.formatNumber(price, Utils.FormatType.GUI)).replace("%currency%", Config.currency)))
                .collect(Collectors.toList());
    }

    public String buttonBuy1Title() {

        return Utils.colorify(getString("shop-gui.buttons.buyone-title"));
    }
    public List<String> buttonBuy1Lore(double price) {
        return getList("shop-gui.buttons.buyone-lore").stream().map(s -> Utils.colorify(s.replace("%price%",
                Utils.formatNumber(price, Utils.FormatType.GUI)).replace("%currency%", Config.currency)))
                .collect(Collectors.toList());
    }
    public String buttonBuy64Title() {

        return Utils.colorify(getString("shop-gui.buttons.buy64-title"));
    }
    public List<String> buttonBuy64Lore(double price) {
        return getList("shop-gui.buttons.buy64-lore").stream().map(s -> Utils.colorify(s.replace("%price%",
                Utils.formatNumber(price, Utils.FormatType.GUI)).replace("%currency%", Config.currency)))
                .collect(Collectors.toList());
    }

    public String buttonAdminView() {

        return Utils.colorify(getString("shop-gui.buttons.adminview"));
    }
    public String buttonStorage() {

        return Utils.colorify(getString("shop-gui.buttons.storage"));
    }
    public String settingsButton() {
        return Utils.colorify(getString("shop-gui.buttons.settings"));
    }
    public String disabledButtonTitle() {
        return Utils.colorify(getString("shop-gui.buttons.disabled-title"));
    }
    public List<String> disabledButtonLore() {
        return getList("shop-gui.buttons.disabled-lore").stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }


    //transactions.
    public String transactionButtonTitle() {
        return Utils.colorify(getString("transactions.transactionButtonTitle"));
    }
    public String backToSettingsButton() {
        return Utils.colorify(getString("transactions.backToSettingsButton"));
    }
    public String transactionPaperTitleBuy(String name) {
        return Utils.colorify(getString("transactions.PaperTitleBuy").replace("%player%", name));
    }
    public String transactionPaperTitleSell(String name) {
        return Utils.colorify(getString("transactions.PaperTitleSell").replace("%player%", name));
    }
    public List<String> transactionPaperLoreBuy(String price, int count, String time) {
        return getList("transactions.PaperLoreBuy").stream().map(s ->
                Utils.colorify(s.replace("%price%", Utils.formatNumber(Double.valueOf(price),
                Utils.FormatType.GUI)).replace("%currency%", Config.currency).replace("%count%", String.valueOf(count)).replace("%time%", time)))
                .collect(Collectors.toList());
    }
    public List<String> transactionPaperLoreSell(String price, int count, String time) {
        return getList("transactions.PaperLoreSell").stream().map(s ->
                Utils.colorify(s.replace("%price%", Utils.formatNumber(Double.valueOf(price),
                Utils.FormatType.GUI)).replace("%currency%", Config.currency).replace("%count%", String.valueOf(count)).replace("%time%", time)))
                .collect(Collectors.toList());
    }

    public String lessthanminute() {
        return Utils.colorify(getString("transactions.lessthanminute"));
    }
    public String minutesago(long minutes) {
        return Utils.colorify(getString("transactions.minutesago").replace("%minutes%", String.valueOf(minutes)));
    }
    public String hoursago(long hours) {
        return Utils.colorify(getString("transactions.hoursago").replace("%hours%", String.valueOf(hours)));
    }
    public String daysago(long days) {
        return Utils.colorify(getString("transactions.daysago").replace("%days%", String.valueOf(days)));
    }

    public String transactionBuyInform(String player, int amount, String item, double price) {
        return Utils.colorify(getString("transactions.player-inform-buy").replace("%player%", player).replace("%amount%",
                String.valueOf(amount)).replace("%item%", item).replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String transactionSellInform(String player, int amount, String item, double price) {
        return Utils.colorify(getString("transactions.player-inform-sell").replace("%player%", player).replace("%amount%",
                        String.valueOf(amount)).replace("%item%", item).replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }


    //settings.
    public String settingsGuiTitle() {
        return Utils.colorify(getString("settings.GuiTitle"));
    }
    public String statusOn() {
        return Utils.colorify(getString("settings.statusOn"));
    }
    public String statusOff() {
        return Utils.colorify(getString("settings.statusOff"));
    }
    //settings.buttons.
    //settings.buttons.toggleTransactions.
    public String toggleTransactionMessageButton() {
        return Utils.colorify(getString("settings.buttons.toggleTransactions.Title"));
    }
    public List<String> toggleTransactionMessageButtonLore(String status) {
        return getList("settings.buttons.toggleTransactions.Lore").stream()
                .map(s -> Utils.colorify(s.replace("%status%", status))).collect(Collectors.toList());
    }
    //settings.buttons.disableBuying.
    public String disableBuyingButtonTitle() {
        return Utils.colorify(getString("settings.buttons.disableBuying.Title"));
    }
    public List<String> disableBuyingButtonLore(String status) {
        return getList("settings.buttons.disableBuying.Lore").stream()
                .map(s -> Utils.colorify(s.replace("%status%", status))).collect(Collectors.toList());
    }
    //settings.buttons.disableSelling.
    public String disableSellingButtonTitle() {
        return Utils.colorify(getString("settings.buttons.disableSelling.Title"));
    }
    public List<String> disableSellingButtonLore(String status) {
        return getList("settings.buttons.disableSelling.Lore").stream()
                .map(s -> Utils.colorify(s.replace("%status%", status))).collect(Collectors.toList());
    }
    //settings.buttons.shopAdmins.
    public String shopAdminsButtonTitle() {
        return Utils.colorify(getString("settings.buttons.shopAdmins.Title"));
    }
    public String nobodyStatusAdmins() {
        return Utils.colorify(getString("settings.buttons.shopAdmins.nobodyStatusAdmins"));
    }
    public List<String> shopAdminsButtonLore(String admins) {
        return getList("settings.buttons.shopAdmins.Lore").stream()
                .map(s -> Utils.colorify(s.replace("%admins%", admins))).collect(Collectors.toList());
    }
    //settings.buttons.sharedIncome.
    public String shareIncomeButtonTitle() {
        return Utils.colorify(getString("settings.buttons.sharedIncome.Title"));
    }
    public List<String> shareIncomeButtonLore(String admins) {
        return getList("settings.buttons.sharedIncome.Lore").stream()
                .map(s -> Utils.colorify(s.replace("%status%", admins))).collect(Collectors.toList());
    }
    //settings.buttons.hologramRotation.
    public String rotateHologramButtonTitle() {return Utils.colorify(getString("settings.buttons.hologramRotation.Title"));}
    public List<String> rotateHologramButtonLore(String rotation) {
        return getList("settings.buttons.hologramRotation.Lore").stream()
                .map(s -> Utils.colorify(s.replace("%rotations%", formatRotations(rotation))
                        .replace("%rotation%", rotationFromData(rotation)))).collect(Collectors.toList());
    }
    //settings.buttons.changePrices
    public String changePricesButtonTitle() {
        return Utils.colorify(getString("settings.buttons.changePrices.Title"));
    }
    public List<String> changePricesButtonLore() {
        return getList("settings.buttons.changePrices.Lore").stream()
                .map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }
    public List<String> changePriceSingGUI(boolean isBuy) {
        return getList("settings.buttons.changePrices." + (isBuy ? "SignPlaceholderBuy" : "SignPlaceholderSell")).stream()
                .map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }
    //settings.buttons.hologramMessage
    public String hologramMessageButtonTitle() {
        return Utils.colorify(getString("settings.buttons.hologramMessage.Title"));
    }
    public List<String> hologramMessageButtonLore(Player player, String ownerID) {
        int lines = Config.settings_hologram_message_line_count_default;
        if (Config.permission_hologram_message_line_count) {
            int maxShops = Utils.getMaxPermission(player, "ecs.shops.hologram.messages.lines.");
            maxShops = maxShops == -1 ? 4 : maxShops == 0 ? 1 : maxShops;
            lines = maxShops;
        }
        int maxMessages = Utils.getMaxPermission(player, "ecs.shops.hologram.messages.limit.");
        int currentMessages = ShopSettings.getAllCustomMessages(ownerID).size();
        String lineMsg = lines == 1 ? "1" : "1-" + lines;
        boolean hasNoMaxShopLimit = maxMessages == -1;
        boolean hasPermissionLimit = Config.permission_hologram_message_limit;
        // if hasNoMaxShopLimit and hasPermissionLimit are true, value should be false.
        // if hasNoMaxShopLimit is true and hasPermissionLimit is false, value should be false.
        // if hasNoMaxShopLimit is false and hasPermissionLimit is true, value should be true.
        // if hasNoMaxShopLimit is false and hasPermissionLimit is false, value should be false.
        boolean value = hasNoMaxShopLimit && hasPermissionLimit ? false : hasNoMaxShopLimit ? false : hasPermissionLimit;
        return getList("settings.buttons.hologramMessage.Lore").stream()
                .filter(s -> ((value) || !s.startsWith("<ifhasmax>")))
                .map(s -> Utils.colorify(s).replace("%lineNumbers%", lineMsg).replace("%messagesleft%",  "" + (maxMessages - currentMessages))
                        .replace("<ifhasmax>", "").replace("</ifhasmax>", "")).collect(Collectors.toList());
    }
    public List<String> hologramMessageButtonLoreMaxReached(Player player) {
        int maxMessages = Utils.getMaxPermission(player, "ecs.shops.hologram.messages.limit.");
        return getList("settings.buttons.hologramMessage.LoreMaxReached").stream()
                .map(s -> Utils.colorify(s.replace("%maxLines%", "" + maxMessages))).collect(Collectors.toList());
    }
    public List<String> hologramMessageSingGUI(Player player, Location loc) {
        int lines = Config.settings_hologram_message_line_count_default;
        if (Config.permission_hologram_message_line_count) {
            int maxShops = Utils.getMaxPermission(player, "ecs.shops.hologram.messages.lines.");
            maxShops = maxShops == -1 ? 4 : maxShops == 0 ? 1 : maxShops;
            lines = maxShops;
        }
        // Add as many white slots as a player can use. Then fill the rest with the placeholder.
        List<String> output = new ArrayList<>();
        ShopSettings settings = ShopContainer.getShopSettings(loc);
        for (int i = 0; i < lines; i++) {
            if (settings.getCustomMessages().size() > i) {
                output.add(settings.getCustomMessages().get(i));
            } else {
                output.add("");
            }
        }
        for (int i = 0; i < 4 - lines; i++) {
            output.add(Utils.colorify(getString("settings.buttons.hologramMessage.SignPlaceholderTexts")));
        }
        return output;
    }
    //settings.buttons.other.
    public String backToShopGuiButton() {
        return Utils.colorify(getString("settings.buttons.other.backToShopGuiButton"));
    }
    public String latestTransactionsTitle() {
        return Utils.colorify(getString("settings.buttons.other.latestTransactionsTitle"));
    }


    //settings.chat.
    //settings.chat.toggleTransactions.
    public String toggleTransactionMessageOnInChat() {
        return Utils.colorify(getString("settings.chat.toggleTransactions.MessageOn"));
    }
    public String toggleTransactionMessageOffInChat() {
        return Utils.colorify(getString("settings.chat.toggleTransactions.MessageOff"));
    }
    //settings.chat.disableBuying.
    public String disableBuyingOnInChat() {
        return Utils.colorify(getString("settings.chat.disableBuying.MessageOn"));
    }
    public String disableBuyingOffInChat() {
        return Utils.colorify(getString("settings.chat.disableBuying.MessageOff"));
    }
    //settings.chat.disableSelling.
    public String disableSellingOnInChat() {
        return Utils.colorify(getString("settings.chat.disableSelling.MessageOn"));
    }
    public String disableSellingOffInChat() {
        return Utils.colorify(getString("settings.chat.disableSelling.MessageOff"));
    }
    //settings.chat.shopAdmins.
    public String addingAdminWaiting() {
        return Utils.colorify(getString("settings.chat.shopAdmins.addingAdminWaiting"));
    }
    public String removingAdminWaiting() {
        return Utils.colorify(getString("settings.chat.shopAdmins.removingAdminWaiting"));
    }
    public String selfAdmin() {
        return Utils.colorify(getString("settings.chat.shopAdmins.selfAdmin"));
    }
    public String sucAdminAdded(String player) {
        return Utils.colorify(getString("settings.chat.shopAdmins.sucAdminAdded").replace("%player%", player));
    }
    public String alreadyAdmin() {
        return Utils.colorify(getString("settings.chat.shopAdmins.alreadyAdmin"));
    }
    public String noPlayer() {
        return Utils.colorify(getString("settings.chat.shopAdmins.noPlayer"));
    }
    public String sucAdminRemoved(String player) {
        return Utils.colorify(getString("settings.chat.shopAdmins.sucAdminRemoved").replace("%player%", player));
    }
    public String notInAdminList() {
        return Utils.colorify(getString("settings.chat.shopAdmins.notInAdminList"));
    }
    public String clearedAdmins() { return Utils.colorify(getString("settings.chat.shopAdmins.clearedAdmins")); }
    //settings.chat.sharedIncome.
    public String sharedIncomeOnInChat() {
        return Utils.colorify(getString("settings.chat.sharedIncome.MessageOn"));
    }
    public String sharedIncomeOffInChat() {
        return Utils.colorify(getString("settings.chat.sharedIncome.MessageOff"));
    }
    //settings.chat.copy-paste.
    public BaseComponent[] copiedShopSettings(String hover) { return MineDown.parse(
            getString("settings.chat.copy-paste.copiedShopSettings").replace("%settings%", hover)); }
    public String pastedShopSettings() { return Utils.colorify(getString("settings.chat.copy-paste.pastedShopSettings")); }
    //settings.chat.hologramRotation.
    public String rotateHologramInChat(String rotation) {
        return Utils.colorify(getString("settings.chat.hologramRotation.rotateHologramInChat")
                .replace("%rotation%", rotationFromData(rotation)));
    }
    public String formatRotations(String rotation) {
        if (rotation == null) rotation = Config.settings_defaults_rotation;
        String result = "";
        for (String option : Utils.rotations) {
            if (option.equalsIgnoreCase(rotation)) {
                result += ChatColor.GOLD + "" + ChatColor.UNDERLINE + ChatColor.stripColor(rotationFromData(option)) +
                        ChatColor.RESET + " ";
            } else {
                result += rotationFromData(option) + " ";
            }
        }
        return result;
    }
    public String rotationFromData(String rotation) {
        switch (rotation) {
            case "north":
                return rotationNorth();
            case "east":
                return rotationEast();
            case "south":
                return rotationSouth();
            case "west":
                return rotationWest();
            case "down":
                return rotationDown();
            default:
                return rotationUp();
        }
    }
    //settings.chat.hologramRotation.rotation.
    public String rotationUp() {return Utils.colorify(getString("settings.chat.hologramRotation.rotation.Up"));}
    public String rotationNorth() {return Utils.colorify(getString("settings.chat.hologramRotation.rotation.North"));}
    public String rotationEast() {return Utils.colorify(getString("settings.chat.hologramRotation.rotation.East"));}
    public String rotationSouth() {return Utils.colorify(getString("settings.chat.hologramRotation.rotation.South"));}
    public String rotationWest() {return Utils.colorify(getString("settings.chat.hologramRotation.rotation.West"));}
    public String rotationDown() {return Utils.colorify(getString("settings.chat.hologramRotation.rotation.Down"));}

    //customMessageManager.
    public String customMessageManagerTitle() {
        return Utils.colorify(getString("customMessageManager.GuiTitle"));
    }
    public String customMessageManagerConfirmDeleteGuiTitle() {
        return Utils.colorify(getString("customMessageManager.ConfirmDeleteGuiTitle"));
    }
    //customMessageManager.buttons.
    //customMessageManager.buttons.previousPage.
    public String customMessageManagerPreviousPageTitle() {
        return Utils.colorify(getString("customMessageManager.buttons.previousPage.Title"));
    }
    public List<String> customMessageManagerPreviousPageLore() {
        return getList("customMessageManager.buttons.previousPage.Lore").stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }
    //customMessageManager.buttons.nextPage.
    public String customMessageManagerNextPageTitle() {
        return Utils.colorify(getString("customMessageManager.buttons.nextPage.Title"));
    }
    public List<String> customMessageManagerNextPageLore() {
        return getList("customMessageManager.buttons.nextPage.Lore").stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }
    //customMessageManager.buttons.shopEntry.
    public String customMessageManagerShopEntryTitle(ItemStack item) {;
        return Utils.colorify(getString("customMessageManager.buttons.shopEntry.Title").replace("%shopitem%", Utils.getFinalItemName(item)));
    }
    public String customMessageManagerShopEntryUnkownTitle() {
        return Utils.colorify(getString("customMessageManager.buttons.shopEntry.UnkownTitle"));
    }
    public List<String> customMessageManagerShopEntryLore(Location loc, List<String> messages) {
        List<String> lore = new ArrayList<>(getList("customMessageManager.buttons.shopEntry.Lore"));
        // find the placeholder %hologram-messages% in the lore and replace it with the messages List.
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains("%hologram-messages%")) {
                String oldLine = lore.get(i);
                lore.remove(i);
                for (String message : messages) {
                    lore.add(i, oldLine.replace("%hologram-messages%", message));
                    i++;
                }
            }
        }
        return lore.stream()
                .map(s -> Utils.colorify(s).replace("%x%", "" + loc.getX()).replace("%y%", "" + loc.getY()).replace("%z%", "" + loc.getZ())
                ).collect(Collectors.toList());
    }
    //customMessageManager.buttons.confirmDelete.
    public String customMessageManagerConfirmDeleteTitle() {
        return Utils.colorify(getString("customMessageManager.buttons.confirmDelete.Title"));
    }
    public List<String> customMessageManagerConfirmDeleteLore() {
        return getList("customMessageManager.buttons.confirmDelete.Lore").stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }
    //customMessageManager.buttons.backToCustomMessageManager.
    public String customMessageManagerBackToCustomMessageManagerTitle() {
        return Utils.colorify(getString("customMessageManager.buttons.backToCustomMessageManager.Title"));
    }
    public List<String> customMessageManagerBackToCustomMessageManagerLore() {
        return getList("customMessageManager.buttons.backToCustomMessageManager.Lore").stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());
    }

    //customBuySell.
    public String customAmountSignTitle() {
        return Utils.colorify(getString("customBuySell.gui-customAmountSign-title"));
    }
    public List<String> customAmountSignLore(String possibleBuyAmount, String possibleSellAmount) {
        return getList("customBuySell.gui-customAmountSign-lore").stream()
                .map(s -> Utils.colorify(s.replace("%buycount%", possibleBuyAmount).
                        replace("%sellcount%", possibleSellAmount))).collect(Collectors.toList());
    }
    //customBuySell.singEditorGui.
    public List<String> signEditorGuiBuy(String max) {
        List<String> list = getList("customBuySell.signEditorGui.buy");
        list.add(0, "");
        return list.stream().limit(4).map(s -> Utils.colorify(s.replace("%max%", max))).collect(Collectors.toList());
    }
    public List<String> signEditorGuiSell(String max) {
        List<String> list = getList("customBuySell.signEditorGui.sell");
        list.add(0, "");
        return list.stream().limit(4).map(s -> Utils.colorify(s.replace("%max%", max))).collect(Collectors.toList());
    }
    //customBuySell.errors.
    public String disabledBuyingMessage() {
        return Utils.colorify(getString("customBuySell.errors.buyingIsDisabled"));
    }
    public String disabledSellingMessage() {
        return Utils.colorify(getString("customBuySell.errors.sellingIsDisabled"));
    }
    public String wrongInput() {
        return Utils.colorify(getString("customBuySell.errors.wrongInput"));
    }
    public String enterTheAmount() {
        return Utils.colorify(getString("customBuySell.errors.enterTheAmount"));
    }
    public String unsupportedInteger() {
        return Utils.colorify(getString("customBuySell.errors.unsupportedInteger"));
    }


    //shop-messages.
    public String messageSuccBuy(double price) {
        return Utils.colorify(getString("shop-messages.successful-buy")
                .replace("%price%", Utils.formatNumber(price, Utils.FormatType.CHAT))
                .replace("%currency%", Config.currency));
    }
    public String fullinv() {
        return Utils.colorify(getString("shop-messages.fullinv"));
    }
    public String cannotAfford() {
        return Utils.colorify(getString("shop-messages.cannotafford"));
    }
    public String outofStock() {
        return Utils.colorify(getString("shop-messages.outofstock"));
    }
    public String messageSuccSell(double price) {
        return Utils.colorify(getString("shop-messages.successful-sell")
                .replace("%price%", Utils.formatNumber(price, Utils.FormatType.CHAT))
                .replace("%currency%", Config.currency));
    }
    public String shopCannotAfford() {
        return Utils.colorify(getString("shop-messages.shopcannotafford"));
    }
    public String notEnoughItemToSell() {
        return Utils.colorify(getString("shop-messages.notenoughitemtosell"));
    }
    public String chestIsFull() {
        return Utils.colorify(getString("shop-messages.chestisFull"));
    }
    public String selfTransaction() {
        return Utils.colorify(getString("shop-messages.selftransaction"));
    }
    public String chestShopProblem() {
        return Utils.colorify(getString("shop-messages.openingShopProblem"));
    }


    //command-messages.
    public String negativePrice() {
        return Utils.colorify(getString("command-messages.negativeprice"));
    }
    public String notenoughARGS() {
        return Utils.colorify(getString("command-messages.notenoughargs"));
    }
    public String consoleNotAllowed() {
        return Utils.colorify(getString("command-messages.consolenotallowed"));
    }
    public BaseComponent[] cmdHelp() {
        return MineDown.parse(new ArrayList<>(getList("command-messages.help")).stream()
                .map(s -> Utils.colorify(s)).collect(Collectors.joining("\n")));
    }
    public BaseComponent[] cmdadminviewHelp() {
        return MineDown.parse(new ArrayList<>(getList("command-messages.help-admin-view-addition")).stream()
                .map(s -> Utils.colorify(s)).collect(Collectors.joining("\n")));
    }
    public BaseComponent[] cmdadminHelp() {
        return MineDown.parse(new ArrayList<>(getList("command-messages.adminhelp")).stream()
                .map(s -> Utils.colorify(s)).collect(Collectors.joining("\n")).replace("%discord_link%", Utils.getDiscordLink()));
    }
    public String alreadyAShop() {
        return Utils.colorify(getString("command-messages.alreadyashop"));
    }
    public String shopCreated() {
        return Utils.colorify(getString("command-messages.shopcreated"));
    }
    public String holdSomething() {
        return Utils.colorify(getString("command-messages.holdsomething"));
    }
    public String notAllowedToCreateOrRemove() {
        return Utils.colorify(getString("command-messages.notallowdtocreate"));
    }
    public String noChest() {
        return Utils.colorify(getString("command-messages.notchest"));
    }
    public String lookAtChest() {
        return Utils.colorify(getString("command-messages.lookatchest"));
    }
    public String chestShopRemoved() {
        return Utils.colorify(getString("command-messages.csremoved"));
    }
    public String notOwner() {
        return Utils.colorify(getString("command-messages.notowner"));
    }
    public String notAChestOrChestShop() {
        return Utils.colorify(getString("command-messages.notachestorcs"));
    }
    public String maxShopLimitReached(int max) {
        return Utils.colorify(getString("command-messages.maxShopLimitReached")).replace("%shoplimit%", "" + max);
    }
    public String slimeFunBlockNotSupported() {
        return Utils.colorify(getString("command-messages.slimeFunBlockNotSupported"));
    }
    public String buyGreaterThanSellRequired() {
        return Utils.colorify(getString("command-messages.buypriceGreaterThanSellRequired"));
    }
    public String invalidShopItem() {
        return Utils.colorify(getString("command-messages.invalidShopItem"));
    }
    public String shopTransferred(String targetPlayer) {
        return Utils.colorify(getString("command-messages.shopTransferred").replaceAll("%player%", targetPlayer));
    }
    public BaseComponent[] shopTransferConfirm(String targetPlayer, boolean isAdmin) {
        String msg = getString("command-messages.shopTransferConfirm");
        if (isAdmin) msg = msg.replace("/ecs settings", "/ecsadmin");
        return MineDown.parse(Utils.colorify(msg.replace("%player%", targetPlayer)));
    }
    public String shopBuyPriceUpdated() {
        return Utils.colorify(getString("command-messages.buyPriceUpdated"));
    }
    public String shopSellPriceUpdated() {
        return Utils.colorify(getString("command-messages.sellPriceUpdated"));
    }


    //checkprofits.
    public BaseComponent[] checkProfitsLandingpage(Player player, double buyCost, int buyAmount, double sellCost, int sellAmount) {
        return MineDown.parse( getList("checkprofits.landing-menu").stream().collect(Collectors.joining("\n"))
                .replace("%currency%", Config.currency).replace("%balance%", Utils.formatNumber(
                        EzChestShop.getEconomy() == null ? 0.0 : EzChestShop.getEconomy().getBalance(player), Utils.FormatType.CHAT))
                .replace("%income%","" + buyCost)
                .replace("%sales%", "" + buyAmount)
                .replace("%cost%","" + sellCost)
                .replace("%purchases%", "" + sellAmount)
                .replace("%total%", "" + (buyCost - sellCost)));
    }
    public BaseComponent[] checkProfitsDetailpage(Player player, List<CheckProfitEntry> checkProfitEntries, int page, int pages) {
        ComponentBuilder compb = new ComponentBuilder("");
        //Header
        compb.append(MineDown.parse(getList("checkprofits.details-menu.header").stream()
                .collect(Collectors.joining("\n")))).append("\n");
        //Content
        for (int i = 0; i < Config.command_checkprofit_lines_pp; i++) {
            int index = i + ((page - 1) * Config.command_checkprofit_lines_pp);
            if (index == checkProfitEntries.size())
                break;
            CheckProfitEntry checkProfitEntry = checkProfitEntries.get(i + ((page - 1) * Config.command_checkprofit_lines_pp));
            //EzChestShop.logDebug(Utils.getFinalItemName(checkProfitEntry.getItem()) + ": " + Utils.encodeItem(checkProfitEntry.getItem()));
            String[] details = getList("checkprofits.details-menu.content").stream()
                    .collect(Collectors.joining("\n")).split("%item%");
            for (int j = 0; j < details.length; j++) {
                compb.append(MineDown.parse(details[j].replace("%currency%", Config.currency)
                        .replace("%income%","" + checkProfitEntry.getBuyPrice())
                        .replace("%sales%", "" + checkProfitEntry.getBuyAmount())
                        .replace("%unit_income%", "" + checkProfitEntry.getBuyUnitPrice())
                        .replace("%cost%","" + checkProfitEntry.getSellPrice())
                        .replace("%purchases%", "" + checkProfitEntry.getSellAmount())
                        .replace("%unit_cost%", "" + checkProfitEntry.getSellUnitPrice())
                ), ComponentBuilder.FormatRetention.NONE);
                if (details.length - 1 != j) {
                    compb.append(Utils.getFinalItemName(checkProfitEntry.getItem())).event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[] {
                            new TextComponent(Utils.ItemToTextCompoundString(checkProfitEntry.getItem())) }));
                } else {
                    compb.append("\n");
                }
            }
        }
        //Footer
        compb.append(MineDown.parse(getList("checkprofits.details-menu.footer").stream().map(s -> {
            s = s.replace("%page%","" + page).replace("%pages%", "" + pages);
            if (page > 1) {
                s = s.replace("%button_previous%", "[← ](" + getButtonPrevious() +
                        " run_command=/cp p " + (page - 1) + ")");
            } else {
                s = s.replace("%button_previous%", "");
            }
            if (page < pages) {
                s = s.replace("%button_next%", "[ →](" + getButtonNext() + " run_command=/cp p " + (page + 1) + ")");
            }else {
                s = s.replace("%button_next%", "");
            }
            return s;
        }).collect(Collectors.joining("\n"))), ComponentBuilder.FormatRetention.NONE);

        return compb.create();
    }
    public String getButtonNext() {
        return Utils.colorify(getString("checkprofits.details-menu.hover-extra.button-next"));
    }
    public String getButtonPrevious() {
        return Utils.colorify(getString("checkprofits.details-menu.hover-extra.button-previous"));
    }
    public BaseComponent[] confirmProfitClear() {
        return MineDown.parse( getList("checkprofits.confirm-clear").stream().collect(Collectors.joining("\n")));
    }
    public String confirmProfitClearSuccess() {
        return Utils.colorify(getString("checkprofits.confirm-clear-success"));
    }
    public BaseComponent[] joinProfitNotification() {
        return MineDown.parse( getList("checkprofits.join-notification").stream().collect(Collectors.joining("\n")));
    }


    //shulkershop-dropped-lore.
    public List<String> shulkerboxLore(String owner, String item, double buy, double sell) {
        List<String> list = getList("shulkershop-dropped-lore").stream().map(s -> Utils.colorify(s).replace("%owner%", owner)
                .replace("%item%", item).replace("%buy_price%", Utils.formatNumber(buy, Utils.FormatType.GUI))
                .replace("%sell_price%", Utils.formatNumber(sell, Utils.FormatType.GUI))
                .replace("%currency%", Config.currency)).collect(Collectors.toList());
        return list;
    }

    //Other
    public BaseComponent[] updateNotification(String curV, String newV) {
        return new ComponentBuilder("").append(TextComponent.fromLegacyText(
                Utils.colorify(getString("other.update-notifications").replace("%current_version%", curV).replace("%new_version%", newV))))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/ez-chest-shop-ecs-1-14-x-1-17-x.90411/"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.colorify("&cClick to check out the Spigot Page!")))).create();
    }



































}
