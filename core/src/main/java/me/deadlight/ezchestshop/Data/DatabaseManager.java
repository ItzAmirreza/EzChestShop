package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;

public abstract class DatabaseManager {

    abstract void load();
    abstract void disconnect();
    abstract HashMap<Location, EzShop> queryShops();
    abstract void deleteShop(String location);
    abstract void insertShop(String location, String uuid, String item, double buyPrice, double sellPrice);
    abstract String getString();
    abstract void setString();
    abstract void setInt();
    abstract void setBoolean();
    abstract void setLong();
    abstract void setDouble();
    abstract List<String> getKeysByExpresion();
    abstract List<String> getKeysWithValueByExpresion();

}
