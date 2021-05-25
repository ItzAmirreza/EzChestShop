package me.deadlight.ezchestshop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class LanguageManager {
    public FileConfiguration languages;

    public LanguageManager() {
        languages = EzChestShop.getLanguages();
    }

    private String colorify(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public void setLanguageConfig(FileConfiguration config) {
        languages = config;
    }


    /**
     * @return returns buy price of GUI item in lore
     */
    public String initialBuyPrice(double buyprice) {

        return colorify(languages.getString("gui-initialbuyprice").replace("%buyprice%", String.valueOf(buyprice)));
    }/**
     * @return returns buy price of GUI item in lore
     */
    public String initialSellPrice(double sellprice) {

        return colorify(languages.getString("gui-initialsellprice").replace("%sellprice%", String.valueOf(sellprice)));
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

        return colorify(languages.getString("gui-button-sellone-lore").replace("%price%", String.valueOf(price)));
    }
    public String buttonSell64Title() {

        return colorify(languages.getString("gui-button-sell64-title"));
    }
    public String buttonSell64Lore(long price) {

        return colorify(languages.getString("gui-button-sell64-lore").replace("%price%", String.valueOf(price)));
    }

    public String buttonBuy1Title() {

        return colorify(languages.getString("gui-button-buyone-title"));
    }
    public String buttonBuy1Lore(long price) {

        return colorify(languages.getString("gui-button-buyone-lore").replace("%price%", String.valueOf(price)));
    }
    public String buttonBuy64Title() {

        return colorify(languages.getString("gui-button-buy64-title"));
    }
    public String buttonBuy64Lore(long price) {

        return colorify(languages.getString("gui-button-buy64-lore").replace("%price%", String.valueOf(price)));
    }

    public String buttonAdminView() {

        return colorify(languages.getString("gui-button-adminview"));
    }
    public String buttonStorage() {

        return colorify(languages.getString("gui-button-storage"));
    }

    public String messageSuccBuy(double price) {

        return colorify(languages.getString("message-successful-buy").replace("%price%", String.valueOf(price)));
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

        return colorify(languages.getString("message-successful-sell").replace("%price%", String.valueOf(price)));
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


































}
