package me.deadlight.ezchestshop.utils.objects;

import me.deadlight.ezchestshop.enums.Changes;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SqlQueue {

    private HashMap<Changes, Object> changesList = new HashMap<>();
    private Location location;
    private ShopSettings settings;
    private EzShop shop;
    private TradeShopSettings tradeSettings;
    private EzTradeShop tradeShop;



    public SqlQueue(Location location, ShopSettings settings, EzShop shop) {
        this.location = location;
        this.settings = settings;
        this.shop = shop;
    }

    public SqlQueue(Location location, TradeShopSettings tradeSettings, EzTradeShop tradeShop) {
        this.location = location;
        this.tradeSettings = tradeSettings;
        this.tradeShop = tradeShop;
    }

    public boolean isChanged() {
        return changesList.size() != 0;
    }

    /**
     * setting changes are through this method
     * @param change the type of change
     * @param object the new value of changed option
     */
    public void setChange(Changes change, Object object) {
        //validate changes
        if (validateChange(change, object)) {
            //no need to change anything plus and also have to remove it from the map
            changesList.remove(change);
            return;
        }
        this.changesList.put(change, object);
    }

    public void resetChangeList(ShopSettings newSettings, EzShop newShop) {
        this.changesList.clear();
        this.settings = newSettings;
        this.shop = newShop;
    }
    public void resetChangeList(TradeShopSettings newTradeSettings, EzTradeShop newTradeShop) {
        this.changesList.clear();
        this.tradeSettings = newTradeSettings;
        this.tradeShop = newTradeShop;
    }

    public HashMap<Changes, Object> getChangesList() {
        return changesList;
    }

    /**
     *
     * This function basically checks if the latest change is similar to the latest modified version in the shop settings,
     *
     * @param changes change enum
     * @param object the modified change
     * @return whether if it is the same with last modified version or not/ if true, then no need to change and we simply remove it from the map
     */
    private boolean validateChange(Changes changes, Object object) {
        if (shop != null && tradeShop == null) {
            // validate settings for a shop
            //FOR THE FUTURE SETTINGS, HAVE TO ADD IT HERE
            if (changes == Changes.BUY_PRICE) {
                double num = (double) object;
                return shop.getBuyPrice() == num;
            } else if (changes == Changes.SELL_PRICE) {
                double num = (double) object;
                return shop.getSellPrice() == num;
            } else if (changes == Changes.SHOP_OWNER) {
                String str = (String) object;
                return shop.getOwnerID().toString().equalsIgnoreCase(str);
            } else if (changes == Changes.ADMINS_LIST) {
                String str = (String) object;
                return settings.getAdmins().equalsIgnoreCase(str);
            } else if (changes == Changes.MESSAGE_TOGGLE) {
                //in persistent data container, we don't save booleans but integers for that, but because we only use it for SQL here, I'll go with boolean here too
                boolean bool = (boolean) object;
                return settings.isMsgtoggle() == bool;
            } else if (changes == Changes.ROTATION) {
                String str = (String) object;
                return settings.getRotation().equalsIgnoreCase(str);
            } else if (changes == Changes.SHAREINCOME) {
                boolean bool = (boolean) object;
                return settings.isShareincome() == bool;
            } else if (changes == Changes.DISABLE_BUY) {
                boolean bool = (boolean) object;
                return settings.isDbuy() == bool;
            } else if (changes == Changes.DISABLE_SELL) {
                boolean bool = (boolean) object;
                return settings.isDsell() == bool;
            } else if (changes == Changes.IS_ADMIN) {
                boolean bool = (boolean) object;
                return settings.isAdminshop() == bool;
            } else if (changes == Changes.CUSTOM_MESSAGES) {
                String str = (String) object;
                return settings.getCustomMessages().stream().collect(Collectors.joining("#,#")).equalsIgnoreCase(str);
            } else {
                //alright, we will have only SHOP_CREATE & SHOP_REMOVE which should not be given in SqlQueue object but I'll think about it seperately
                return false;
            }
        } else if (shop == null && tradeShop != null) {
            // validate settings for a trade shop
            //FOR THE FUTURE SETTINGS, HAVE TO ADD IT HERE
            if (changes == Changes.SHOP_OWNER) {
                String str = (String) object;
                return tradeShop.getOwnerID().toString().equalsIgnoreCase(str);
            } else if (changes == Changes.ADMINS_LIST) {
                String str = (String) object;
                return tradeSettings.getAdmins().equalsIgnoreCase(str);
            } else if (changes == Changes.MESSAGE_TOGGLE) {
                //in persistent data container, we don't save booleans but integers for that, but because we only use it for SQL here, I'll go with boolean here too
                boolean bool = (boolean) object;
                return tradeSettings.isMsgtoggle() == bool;
            } else if (changes == Changes.TRADE_DIRECTION) {
                String str = (String) object;
                return tradeSettings.getTradeDirection().toString().equalsIgnoreCase(str);
            } else if (changes == Changes.ROTATION) {
                String str = (String) object;
                return tradeSettings.getRotation().equalsIgnoreCase(str);
            } else if (changes == Changes.IS_ADMIN) {
                boolean bool = (boolean) object;
                return tradeSettings.isAdminshop() == bool;
            } else if (changes == Changes.CUSTOM_MESSAGES) {
                String str = (String) object;
                return tradeSettings.getCustomMessages().stream().collect(Collectors.joining("#,#")).equalsIgnoreCase(str);
            } else {
                //alright, we will have only SHOP_CREATE & SHOP_REMOVE which should not be given in SqlQueue object but I'll think about it seperately
                return false;
            }
        }
        // if it is not a shop or trade shop, then it is certainly not valid
        return false;
    }
    public Location getLocation() {
        return this.location;
    }


}
