package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {
    public FileConfiguration languages;

    public LanguageManager() {
        languages = EzChestShop.getLanguages();
    }

    private String colorify(String str) {
        return translateHexColorCodes("#", "", ChatColor.translateAlternateColorCodes('&', str));
    }

    public String translateHexColorCodes(String startTag, String endTag, String message)
    {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        final char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    public void setLanguageConfig(FileConfiguration config) {
        languages = config;
    }

    /**
     * @return returns buy price of GUI item in lore
     */

    public String initialBuyPrice(double buyprice) {

        return colorify(languages.getString("gui-initialbuyprice").replace("%buyprice%", String.valueOf(buyprice)).replace("%currency%", Config.currency));
    }/**
     * @return returns buy price of GUI item in lore
     */
    public String initialSellPrice(double sellprice) {

        return colorify(languages.getString("gui-initialsellprice").replace("%sellprice%", String.valueOf(sellprice)).replace("%currency%", Config.currency));
    }/**
     * @return returns buy price of GUI item in lore
     */
    public String guiAdminTitle(String shopowner) {

        return colorify(languages.getString("gui-admin-title").replace("%shopowner%", String.valueOf(shopowner)));
    }

    public String guiNonOwnerTitle(String shopowner) {

        return colorify(languages.getString("gui-nonowner-title").replace("%shopowner%", String.valueOf(shopowner)));
    }
    public String guiOwnerTitle(String shopowner) {

        return colorify(languages.getString("gui-owner-title").replace("%shopowner%", String.valueOf(shopowner)));
    }

    public String buttonSell1Title() {

        return colorify(languages.getString("gui-button-sellone-title"));
    }
    public String buttonSell1Lore(long price) {

        return colorify(languages.getString("gui-button-sellone-lore").replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String buttonSell64Title() {

        return colorify(languages.getString("gui-button-sell64-title"));
    }
    public String buttonSell64Lore(long price) {

        return colorify(languages.getString("gui-button-sell64-lore").replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }

    public String buttonBuy1Title() {

        return colorify(languages.getString("gui-button-buyone-title"));
    }
    public String buttonBuy1Lore(long price) {

        return colorify(languages.getString("gui-button-buyone-lore").replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String buttonBuy64Title() {

        return colorify(languages.getString("gui-button-buy64-title"));
    }
    public String buttonBuy64Lore(long price) {

        return colorify(languages.getString("gui-button-buy64-lore").replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }

    public String buttonAdminView() {

        return colorify(languages.getString("gui-button-adminview"));
    }
    public String buttonStorage() {

        return colorify(languages.getString("gui-button-storage"));
    }

    public String messageSuccBuy(double price) {

        return colorify(languages.getString("message-successful-buy").replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String fullinv() {

        return colorify(languages.getString("message-fullinv"));
    }
    public String cannotAfford() {

        return colorify(languages.getString("message-cannotafford"));
    }
    public String outofStock() {

        return colorify(languages.getString("message-outofstock"));
    }
    public String messageSuccSell(double price) {

        return colorify(languages.getString("message-successful-sell").replace("%price%", String.valueOf(price)).replace("%currency%", Config.currency));
    }
    public String shopCannotAfford() {

        return colorify(languages.getString("message-shopcannotafford"));
    }
    public String notEnoughItemToSell() {

        return colorify(languages.getString("message-notenoughitemtosell"));
    }
    public String chestIsFull() {

        return colorify(languages.getString("message-chestisFull"));
    }
    public String selfTransaction() {

        return colorify(languages.getString("message-selftransaction"));
    }
    //update 1.2.8 new messages

    public String negativePrice() {

        return colorify(languages.getString("commandmsg-negativeprice"));
    }
    public String notenoughARGS() {

        return colorify(languages.getString("commandmsg-notenoughargs"));
    }
    public String consoleNotAllowed() {

        return colorify(languages.getString("commandmsg-consolenotallowed"));
    }
    public String cmdHelp() {

        return colorify(languages.getString("commandmsg-help"));
    }
    public String alreadyAShop() {

        return colorify(languages.getString("commandmsg-alreadyashop"));
    }
    public String shopCreated() {

        return colorify(languages.getString("commandmsg-shopcreated"));
    }
    public String holdSomething() {

        return colorify(languages.getString("commandmsg-holdsomething"));
    }
    public String notAllowedToCreateOrRemove() {

        return colorify(languages.getString("commandmsg-notallowdtocreate"));
    }
    public String noChest() {

        return colorify(languages.getString("commandmsg-notchest"));
    }
    public String lookAtChest() {

        return colorify(languages.getString("commandmsg-lookatchest"));
    }
    public String chestShopRemoved() {

        return colorify(languages.getString("commandmsg-csremoved"));
    }
    public String notOwner() {

        return colorify(languages.getString("commandmsg-notowner"));
    }
    public String notAChestOrChestShop() {
        return colorify(languages.getString("commandmsg-notachestorcs"));
    }
    public String settingsButton() {
        return colorify(languages.getString("settingsButton"));
    }
    public String disabledButtonTitle() {
        return colorify(languages.getString("disabledButtonTitle"));
    }
    public String transactionButtonTitle() {
        return colorify(languages.getString("transactionButtonTitle"));
    }
    public String backToSettingsButton() {
        return colorify(languages.getString("backToSettingsButton"));
    }
    public String transactionPaperTitleBuy(String name) {
        return colorify(languages.getString("transactionPaperTitleBuy").replace("%player%", name));
    }
    public String transactionPaperTitleSell(String name) {
        return colorify(languages.getString("transactionPaperTitleSell").replace("%player%", name));
    }
    public String lessthanminute() {
        return colorify(languages.getString("lessthanminute"));
    }
    public String adminshopguititle() {
        return colorify(languages.getString("adminshopguititle"));
    }
    public String settingsGuiTitle() {
        return colorify(languages.getString("settingsGuiTitle"));
    }
    public String latestTransactionsButton() {
        return colorify(languages.getString("latestTransactionsButton"));
    }
    public String toggleTransactionMessageButton() {
        return colorify(languages.getString("toggleTransactionMessageButton"));
    }
    public String toggleTransactionMessageOnInChat() {
        return colorify(languages.getString("toggleTransactionMessageOnInChat"));
    }
    public String toggleTransactionMessageOffInChat() {
        return colorify(languages.getString("toggleTransactionMessageOffInChat"));
    }
    public String disableBuyingButtonTitle() {
        return colorify(languages.getString("disableBuyingButtonTitle"));
    }
    public String disableBuyingOnInChat() {
        return colorify(languages.getString("disableBuyingOnInChat"));
    }
    public String disableBuyingOffInChat() {
        return colorify(languages.getString("disableBuyingOffInChat"));
    }
    public String disableSellingButtonTitle() {
        return colorify(languages.getString("disableSellingButtonTitle"));
    }
    public String disableSellingOnInChat() {
        return colorify(languages.getString("disableSellingOnInChat"));
    }
    public String disableSellingOffInChat() {
        return colorify(languages.getString("disableSellingOffInChat"));
    }
    public String shopAdminsButtonTitle() {
        return colorify(languages.getString("shopAdminsButtonTitle"));
    }
    public String nobodyStatusAdmins() {
        return colorify(languages.getString("nobodyStatusAdmins"));
    }
    public String addingAdminWaiting() {
        return colorify(languages.getString("addingAdminWaiting"));
    }
    public String removingAdminWaiting() {
        return colorify(languages.getString("removingAdminWaiting"));
    }
    public String shareIncomeButtonTitle() {
        return colorify(languages.getString("shareIncomeButtonTitle"));
    }
    public String sharedIncomeOnInChat() {
        return colorify(languages.getString("sharedIncomeOnInChat"));
    }
    public String sharedIncomeOffInChat() {
        return colorify(languages.getString("sharedIncomeOffInChat"));
    }
    public String backToShopGuiButton() {
        return colorify(languages.getString("backToShopGuiButton"));
    }
    public String statusOn() {
        return colorify(languages.getString("statusOn"));
    }
    public String statusOff() {
        return colorify(languages.getString("statusOff"));
    }
    public String selfAdmin() {
        return colorify(languages.getString("selfAdmin"));
    }
    public String noPlayer() {
        return colorify(languages.getString("noPlayer"));
    }
    public String alreadyAdmin() {
        return colorify(languages.getString("alreadyAdmin"));
    }
    public String notInAdminList() {
        return colorify(languages.getString("notInAdminList"));
    }



    public String minutesago(long minutes) {
        return colorify(languages.getString("minutesago").replace("%minutes%", String.valueOf(minutes)));
    }
    public String hoursago(long hours) {
        return colorify(languages.getString("hoursago").replace("%hours%", String.valueOf(hours)));
    }
    public String daysago(long days) {
        return colorify(languages.getString("daysago").replace("%days%", String.valueOf(days)));
    }
    public String sucAdminAdded(String player) {
        return colorify(languages.getString("sucAdminAdded").replace("%player%", player));
    }
    public String sucAdminRemoved(String player) {
        return colorify(languages.getString("sucAdminRemoved").replace("%player%", player));
    }


    public List<String> disabledButtonLore() {
        String lm = languages.getString("disabledButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str));
        }
        return finalList;
    }
    public List<String> transactionPaperLoreBuy(String price, int count, String time) {
        String lm = languages.getString("transactionPaperLoreBuy");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%price%", price).replace("%count%", String.valueOf(count)).replace("%time%", time)));
        }
        return finalList;
    }
    public List<String> transactionPaperLoreSell(String price, int count, String time) {
        String lm = languages.getString("transactionPaperLoreSell");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%price%", price).replace("%count%", String.valueOf(count)).replace("%time%", time)));
        }
        return finalList;
    }
    public List<String> toggleTransactionMessageButtonLore(String status) {
        String lm = languages.getString("toggleTransactionMessageButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%status%", status)));
        }
        return finalList;
    }
    public List<String> disableBuyingButtonLore(String status) {
        String lm = languages.getString("disableBuyingButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%status%", status)));
        }
        return finalList;
    }
    public List<String> disableSellingButtonLore(String status) {
        String lm = languages.getString("disableSellingButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%status%", status)));
        }
        return finalList;
    }
    public List<String> shopAdminsButtonLore(String admins) {
        String lm = languages.getString("shopAdminsButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%admins%", admins)));
        }
        return finalList;
    }

    public List<String> shareIncomeButtonLore(String admins) {
        String lm = languages.getString("shareIncomeButtonLore");
        List<String> list = Arrays.asList(lm.split("\n"));
        List<String> finalList = new ArrayList<>();
        for (String str : list) {
            finalList.add(colorify(str.replace("%status%", admins)));
        }
        return finalList;
    }

    public String copiedShopSettings() { return colorify(languages.getString("copiedShopSettings")); }
    public String pastedShopSettings() { return colorify(languages.getString("pastedShopSettings")); }
    public String clearedAdmins() { return colorify(languages.getString("clearedAdmins")); }
    public String maxShopLimitReached(int max) { return colorify(languages.getString("maxShopLimitReached")).replace("%shoplimit%", "" + max); }
    public String disabledBuyingMessage() {
        return colorify(languages.getString("buyingIsDisabled"));
    }
    public String disabledSellingMessage() {
        return colorify(languages.getString("sellingIsDisabled"));
    }



































}
