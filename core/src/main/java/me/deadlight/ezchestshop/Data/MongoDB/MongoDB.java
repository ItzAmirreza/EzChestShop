package me.deadlight.ezchestshop.Data.MongoDB;

import me.deadlight.ezchestshop.Data.DatabaseManager;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MongoDB extends DatabaseManager {
    EzChestShop plugin;

    public MongoDB(EzChestShop instance) {
        this.plugin = instance;
    }


    @Override
    public void load() {
        //first connect to the database

        //then create a database named "ezchestshop" if it doesn't exist

    }

    @Override
    public void disconnect() {

    }

    @Override
    public HashMap<Location, EzShop> queryShops() {
        return null;
    }

    @Override
    public void deleteEntry(String primary_key, String key, String table) {

    }

    @Override
    public void insertShop(String sloc, String owner, String item, double buyprice, double sellprice, boolean msgtoggle, boolean dbuy, boolean dsell, String admins, boolean shareincome, boolean adminshop, String rotation, List<String> customMessages) {

    }

    @Override
    public String getString(String primary_key, String key, String column, String table) {
        return null;
    }

    @Override
    public void setString(String primary_key, String key, String column, String table, String data) {

    }

    @Override
    public void setInt(String primary_key, String key, String column, String table, int data) {

    }

    @Override
    public void setBool(String primary_key, String key, String column, String table, Boolean data) {

    }

    @Override
    public void setDouble(String primary_key, String key, String column, String table, double data) {

    }


    @Override
    public boolean hasPlayer(String table, UUID key) {
        return false;
    }

    @Override
    public boolean hasTable(String table) {
        return false;
    }

    @Override
    public void preparePlayerData(String table, String uuid) {

    }
}
