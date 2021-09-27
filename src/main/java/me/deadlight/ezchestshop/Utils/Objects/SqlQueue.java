package me.deadlight.ezchestshop.Utils.Objects;

import me.deadlight.ezchestshop.Enums.Changes;
import org.bukkit.Location;

import java.util.HashMap;
public class SqlQueue {

    private HashMap<Changes, Object> changesList = new HashMap<>();
    private Location location;
    private ShopSettings settings;



    public SqlQueue(Location location, ShopSettings settings) {
        this.location = location;
        this.settings = settings;
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

    public void resetChangeList(ShopSettings newSettings) {
        this.changesList.clear();
        this.settings = newSettings;
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
        //FOR THE FUTURE SETTINGS, HAVE TO ADD IT HERE
        if (changes == Changes.ADMINS) {
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
        } else if (changes == Changes.TRANSACTIONS) {
            String str = (String) object;
            return settings.getTrans().equalsIgnoreCase(str);
        } else if (changes == Changes.DISABLE_BUY) {
            boolean bool = (boolean) object;
            return settings.isDbuy() == bool;
        } else if (changes == Changes.DISABLE_SELL) {
            boolean bool = (boolean) object;
            return settings.isDsell() == bool;
        } else {
            //alright, we will have only SHOP_CREATE & SHOP_REMOVE which should not be given in SqlQueue object but I'll think about it seperately
            return false;
        }
    }
    public Location getLocation() {
        return this.location;
    }


}
