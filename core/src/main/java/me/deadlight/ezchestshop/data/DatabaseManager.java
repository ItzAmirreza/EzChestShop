package me.deadlight.ezchestshop.data;
import me.deadlight.ezchestshop.utils.Objects.EzShop;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class DatabaseManager {

    public abstract void load();
    public abstract void disconnect();
    public abstract HashMap<Location, EzShop> queryShops();
    public abstract void deleteEntry(String primary_key, String key, String table);
    public abstract void insertShop(String sloc, String owner, String item, double buyprice, double sellprice, boolean msgtoggle,
                                    boolean dbuy, boolean dsell, String admins, boolean shareincome, boolean adminshop, String rotation, List<String> customMessages);
    public abstract String getString(String primary_key, String key, String column, String table);
    public abstract void setString(String primary_key, String key, String column, String table, String data);
    public abstract void setInt(String primary_key, String key, String column, String table, int data);
    public abstract void setBool(String primary_key, String key, String column, String table, Boolean data);
    public abstract void setDouble(String primary_key, String key, String column, String table, double data);
      public abstract boolean hasPlayer(String table, UUID key);
    public abstract boolean hasTable(String table);
    public abstract void preparePlayerData(String table, String uuid);

}
