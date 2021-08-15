package me.deadlight.ezchestshop.Data;
import me.deadlight.ezchestshop.Commands.Ecsadmin;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.ChatListener;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {
    private static FileConfiguration languageConfig;
    
    private static List<String> supported_locales = Arrays.asList("Locale_EN", "Locale_DE", "Locale_ES", "Locale_CN");

    public static List<String> getSupportedLanguages() {
        return supported_locales;
    }

    public static void loadLanguages() {
        LanguageManager lm = new LanguageManager();
        for (String locale : LanguageManager.getSupportedLanguages()) {
            File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder() + File.separator + "translations", locale + ".yml");
            if (!customConfigFile.exists()) {
                EzChestShop.logConsole("&c[&eEzChestShop&c] &eGenerating " + locale + " file...");
                customConfigFile.getParentFile().mkdirs();
                EzChestShop.getPlugin().saveResource("translations/" + locale + ".yml", false);
            }
        }
        File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder() + File.separator + "translations", Config.language + ".yml");
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
        Ecsadmin.updateLM(newLanguage);
    }

    public static void checkForLanguagesYMLupdate() throws IOException {
        boolean changes = false;
        for (String local : getSupportedLanguages()) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(),
                    "translations/" + local + ".yml"));
            FileConfiguration fc_internal = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(EzChestShop.getPlugin().getResource("translations/" + local + ".yml")));
            for (String key : fc_internal.getKeys(false)) {
                if (!fc.isString(key)) {
                    fc.set(key, fc_internal.getString(key));
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
     * @return returns buy price of GUI item in lore
     */
    public String initialBuyPrice(double buyprice) {
        return Utils.colorify(getString("gui-initialbuyprice").replace("%buyprice%",
                String.valueOf(buyprice)).replace("%currency%", Config.currency));
    }
    /**
     * @return returns buy price of GUI item in lore
     */
    public String initialSellPrice(double sellprice) {

        return Utils.colorify(getString("gui-initialsellprice").replace("%sellprice%",
                String.valueOf(sellprice)).replace("%currency%", Config.currency));
    }
    /**
     * @return returns buy price of GUI item in lore
     */
    public String guiAdminTitle(String shopowner) {

        return Utils.colorify(getString("gui-admin-title").replace("%shopowner%",
                String.valueOf(shopowner)));
    }

    public String guiNonOwnerTitle(String shopowner) {

        return Utils.colorify(getString("gui-nonowner-title").replace("%shopowner%",
                String.valueOf(shopowner)));
    }
    public String guiOwnerTitle(String shopowner) {

        return Utils.colorify(getString("gui-owner-title").replace("%shopowner%",
                String.valueOf(shopowner)));
    }

    public String buttonSell1Title() {

        return Utils.colorify(getString("gui-button-sellone-title"));
    }
    public String buttonSell1Lore(long price) {

        return Utils.colorify(getString("gui-button-sellone-lore").replace("%price%",
                String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String buttonSell64Title() {

        return Utils.colorify(getString("gui-button-sell64-title"));
    }
    public String buttonSell64Lore(long price) {

        return Utils.colorify(getString("gui-button-sell64-lore").replace("%price%",
                String.valueOf(price)).replace("%currency%", Config.currency));
    }

    public String buttonBuy1Title() {

        return Utils.colorify(getString("gui-button-buyone-title"));
    }
    public String buttonBuy1Lore(long price) {

        return Utils.colorify(getString("gui-button-buyone-lore").replace("%price%",
                String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String buttonBuy64Title() {

        return Utils.colorify(getString("gui-button-buy64-title"));
    }
    public String buttonBuy64Lore(long price) {

        return Utils.colorify(getString("gui-button-buy64-lore").replace("%price%",
                String.valueOf(price)).replace("%currency%", Config.currency));
    }

    public String buttonAdminView() {

        return Utils.colorify(getString("gui-button-adminview"));
    }
    public String buttonStorage() {

        return Utils.colorify(getString("gui-button-storage"));
    }

    public String messageSuccBuy(double price) {

        return Utils.colorify(getString("message-successful-buy").replace("%price%",
                String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String fullinv() {

        return Utils.colorify(getString("message-fullinv"));
    }
    public String cannotAfford() {

        return Utils.colorify(getString("message-cannotafford"));
    }
    public String outofStock() {

        return Utils.colorify(getString("message-outofstock"));
    }
    public String messageSuccSell(double price) {

        return Utils.colorify(getString("message-successful-sell").replace("%price%",
                String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String shopCannotAfford() {

        return Utils.colorify(getString("message-shopcannotafford"));
    }
    public String notEnoughItemToSell() {

        return Utils.colorify(getString("message-notenoughitemtosell"));
    }
    public String chestIsFull() {

        return Utils.colorify(getString("message-chestisFull"));
    }
    public String selfTransaction() {

        return Utils.colorify(getString("message-selftransaction"));
    }
    //update 1.2.8 new messages

    public String negativePrice() {

        return Utils.colorify(getString("commandmsg-negativeprice"));
    }
    public String notenoughARGS() {

        return Utils.colorify(getString("commandmsg-notenoughargs"));
    }
    public String consoleNotAllowed() {

        return Utils.colorify(getString("commandmsg-consolenotallowed"));
    }
    public String cmdHelp() {

        return Utils.colorify(getString("commandmsg-help"));
    }
    public String alreadyAShop() {

        return Utils.colorify(getString("commandmsg-alreadyashop"));
    }
    public String shopCreated() {

        return Utils.colorify(getString("commandmsg-shopcreated"));
    }
    public String holdSomething() {

        return Utils.colorify(getString("commandmsg-holdsomething"));
    }
    public String notAllowedToCreateOrRemove() {

        return Utils.colorify(getString("commandmsg-notallowdtocreate"));
    }
    public String noChest() {

        return Utils.colorify(getString("commandmsg-notchest"));
    }
    public String lookAtChest() {

        return Utils.colorify(getString("commandmsg-lookatchest"));
    }
    public String chestShopRemoved() {

        return Utils.colorify(getString("commandmsg-csremoved"));
    }
    public String notOwner() {

        return Utils.colorify(getString("commandmsg-notowner"));
    }
    public String notAChestOrChestShop() {
        return Utils.colorify(getString("commandmsg-notachestorcs"));
    }
    public String settingsButton() {
        return Utils.colorify(getString("settingsButton"));
    }
    public String disabledButtonTitle() {
        return Utils.colorify(getString("disabledButtonTitle"));
    }
    public String transactionButtonTitle() {
        return Utils.colorify(getString("transactionButtonTitle"));
    }
    public String backToSettingsButton() {
        return Utils.colorify(getString("backToSettingsButton"));
    }
    public String transactionPaperTitleBuy(String name) {
        return Utils.colorify(getString("transactionPaperTitleBuy").replace("%player%", name));
    }
    public String transactionPaperTitleSell(String name) {
        return Utils.colorify(getString("transactionPaperTitleSell").replace("%player%", name));
    }
    public String lessthanminute() {
        return Utils.colorify(getString("lessthanminute"));
    }
    public String adminshopguititle() {
        return Utils.colorify(getString("adminshopguititle"));
    }
    public String settingsGuiTitle() {
        return Utils.colorify(getString("settingsGuiTitle"));
    }
    public String latestTransactionsButton() {
        return Utils.colorify(getString("latestTransactionsButton"));
    }
    public String toggleTransactionMessageButton() {
        return Utils.colorify(getString("toggleTransactionMessageButton"));
    }
    public String toggleTransactionMessageOnInChat() {
        return Utils.colorify(getString("toggleTransactionMessageOnInChat"));
    }
    public String toggleTransactionMessageOffInChat() {
        return Utils.colorify(getString("toggleTransactionMessageOffInChat"));
    }
    public String disableBuyingButtonTitle() {
        return Utils.colorify(getString("disableBuyingButtonTitle"));
    }
    public String disableBuyingOnInChat() {
        return Utils.colorify(getString("disableBuyingOnInChat"));
    }
    public String disableBuyingOffInChat() {
        return Utils.colorify(getString("disableBuyingOffInChat"));
    }
    public String disableSellingButtonTitle() {
        return Utils.colorify(getString("disableSellingButtonTitle"));
    }
    public String disableSellingOnInChat() {
        return Utils.colorify(getString("disableSellingOnInChat"));
    }
    public String disableSellingOffInChat() {
        return Utils.colorify(getString("disableSellingOffInChat"));
    }
    public String shopAdminsButtonTitle() {
        return Utils.colorify(getString("shopAdminsButtonTitle"));
    }
    public String nobodyStatusAdmins() {
        return Utils.colorify(getString("nobodyStatusAdmins"));
    }
    public String addingAdminWaiting() {
        return Utils.colorify(getString("addingAdminWaiting"));
    }
    public String removingAdminWaiting() {
        return Utils.colorify(getString("removingAdminWaiting"));
    }
    public String shareIncomeButtonTitle() {
        return Utils.colorify(getString("shareIncomeButtonTitle"));
    }
    public String sharedIncomeOnInChat() {
        return Utils.colorify(getString("sharedIncomeOnInChat"));
    }
    public String sharedIncomeOffInChat() {
        return Utils.colorify(getString("sharedIncomeOffInChat"));
    }
    public String backToShopGuiButton() {
        return Utils.colorify(getString("backToShopGuiButton"));
    }
    public String statusOn() {
        return Utils.colorify(getString("statusOn"));
    }
    public String statusOff() {
        return Utils.colorify(getString("statusOff"));
    }
    public String selfAdmin() {
        return Utils.colorify(getString("selfAdmin"));
    }
    public String noPlayer() {
        return Utils.colorify(getString("noPlayer"));
    }
    public String alreadyAdmin() {
        return Utils.colorify(getString("alreadyAdmin"));
    }
    public String notInAdminList() {
        return Utils.colorify(getString("notInAdminList"));
    }



    public String minutesago(long minutes) {
        return Utils.colorify(getString("minutesago").replace("%minutes%", String.valueOf(minutes)));
    }
    public String hoursago(long hours) {
        return Utils.colorify(getString("hoursago").replace("%hours%", String.valueOf(hours)));
    }
    public String daysago(long days) {
        return Utils.colorify(getString("daysago").replace("%days%", String.valueOf(days)));
    }
    public String sucAdminAdded(String player) {
        return Utils.colorify(getString("sucAdminAdded").replace("%player%", player));
    }
    public String sucAdminRemoved(String player) {
        return Utils.colorify(getString("sucAdminRemoved").replace("%player%", player));
    }


    public List<String> disabledButtonLore() {
        String lm = getString("disabledButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str));
        }
        return finalList;
    }
    public List<String> transactionPaperLoreBuy(String price, int count, String time) {
        String lm = getString("transactionPaperLoreBuy");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%price%", price).replace("%count%",
                    String.valueOf(count)).replace("%time%", time)));
        }
        return finalList;
    }
    public List<String> transactionPaperLoreSell(String price, int count, String time) {
        String lm = getString("transactionPaperLoreSell");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%price%", price).replace("%count%",
                    String.valueOf(count)).replace("%time%", time)));
        }
        return finalList;
    }
    public List<String> toggleTransactionMessageButtonLore(String status) {
        String lm = getString("toggleTransactionMessageButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%status%", status)));
        }
        return finalList;
    }
    public List<String> disableBuyingButtonLore(String status) {
        String lm = getString("disableBuyingButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%status%", status)));
        }
        return finalList;
    }
    public List<String> disableSellingButtonLore(String status) {
        String lm = getString("disableSellingButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%status%", status)));
        }
        return finalList;
    }
    public List<String> shopAdminsButtonLore(String admins) {
        String lm = getString("shopAdminsButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%admins%", admins)));
        }
        return finalList;
    }

    public List<String> shareIncomeButtonLore(String admins) {
        String lm = getString("shareIncomeButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(Utils.colorify(str.replace("%status%", admins)));
        }
        return finalList;
    }



































}
