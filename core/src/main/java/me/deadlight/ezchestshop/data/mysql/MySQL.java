package me.deadlight.ezchestshop.data.mysql;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.DatabaseManager;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.objects.ShopSettings;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import tr.zeltuv.ezql.objects.*;
import tr.zeltuv.ezql.settings.EzqlCredentials;

import java.util.*;
import java.util.stream.Collectors;

public class MySQL extends DatabaseManager {
    EzChestShop plugin;

    private EzqlDatabase database;

    private EzqlTable shopdata;
    private EzqlTable playerdata;

    private String prefix;

    public MySQL(EzChestShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void load() {
        //first connect to the database

        //then create a database named that is stated in the config,
        //if it doesn't exist, then stop the plugin and tell the user to create the database manually

        String host = Config.databasemysql_ip;
        String user = Config.databasemysql_username;
        String password = Config.databasemysql_password;
        String databaseName = Config.databasemysql_databasename;
        int port = Config.databasemysql_port;
        int maxpool = Config.databasemysql_maxpool;
        boolean useSSL = Config.databasemysql_use_ssl;

        EzqlCredentials ezqlCredentials = new EzqlCredentials(
                host,
                databaseName,
                user,
                password,
                maxpool,
                port,
                useSSL
        );

        database = new EzqlDatabase(ezqlCredentials);

        try {
            database.connect();
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        createTables();
    }

    public void createTables() {
        prefix= Config.databasemysqltables_prefix;

        shopdata = database.addTable(prefix + "shopdata",
                EzqlColumn.get(DataType.VARCHAR, "location", 64, true),
                EzqlColumn.get(DataType.VARCHAR, "owner", 38),
                EzqlColumn.get(DataType.BLOB, "item"),
                EzqlColumn.get(DataType.DOUBLE, "buyPrice"),
                EzqlColumn.get(DataType.DOUBLE, "sellPrice"),
                EzqlColumn.get(DataType.TINYINT, "msgToggle"),
                EzqlColumn.get(DataType.TINYINT, "buyDisabled"),
                EzqlColumn.get(DataType.TINYINT, "sellDisabled"),
                EzqlColumn.get(DataType.VARCHAR, "admins", 526),
                EzqlColumn.get(DataType.TINYINT, "shareIncome"),
                EzqlColumn.get(DataType.TINYINT, "adminshop"),
                EzqlColumn.get(DataType.VARCHAR, "rotation", 2000),
                EzqlColumn.get(DataType.VARCHAR, "customMessages", 2000)
        );

        playerdata = database.addTable(prefix + "playerdata",
                EzqlColumn.get(DataType.VARCHAR, "uuid", 36, true),
                EzqlColumn.get(DataType.TEXT, "checkprofits")
        );
    }

    @Override
    public void disconnect() {
        database.disconnect();
    }

    @Override
    public HashMap<Location, EzShop> queryShops() {
        HashMap<Location, EzShop> ezShopMap = new HashMap<>();

        for (EzqlRow row : shopdata.getAllRows()) {

            String slocation = row.getValue("location");
            String customMessages = row.getValue("customMessages") == null ? "" : row.getValue("customMessages");

            ezShopMap.put(Utils.StringtoLocation(slocation),
                    new EzShop(Utils.StringtoLocation(slocation),
                            (String) row.getValue("owner"),
                            Utils.decodeItem(row.getValue("item")),

                            row.getValue("buyPrice"),
                            row.getValue("sellPrice"),
                            new ShopSettings(slocation,
                                    asBool(row.getValue("msgToggle")),
                                    asBool(row.getValue("buyDisabled")),
                                    asBool(row.getValue("sellDisabled")),
                                    row.getValue("admins"),
                                    asBool(row.getValue("shareIncome")),
                                    asBool(row.getValue("adminshop")),
                                    row.getValue("rotation"),
                                    Arrays.asList(customMessages.split("#,#")))));
        }

        return ezShopMap;
    }

    public boolean asBool(int i){
        return i ==1;
    }

    @Override
    public void deleteEntry(String primary_key, String key, String table) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->ezqlTable.removeRows(primary_key, key));
    }

    @Override
    public void insertShop(String sloc, String owner, String item, double buyprice, double sellprice, boolean msgtoggle, boolean dbuy, boolean dsell, String admins, boolean shareincome, boolean adminshop, String rotation, List<String> customMessages) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->this.shopdata.pushRow(
                sloc,
                owner,
                item,
                buyprice,
                sellprice,
                msgtoggle,
                dbuy,
                dsell,
                admins,
                shareincome,
                adminshop,
                rotation,
                customMessages.stream().collect(Collectors.joining("#,#"))
        ));
    }

    @Override
    public String getString(String primary_key, String key, String column, String table) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        return ezqlTable.getSingleValue(column, primary_key, key, String.class);
    }

    @Override
    public void setString(String primary_key, String key, String column, String table, String data) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->ezqlTable.updateRow(primary_key, key, new EzqlRow(column, data)));
    }

    @Override
    public void setInt(String primary_key, String key, String column, String table, int data) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->ezqlTable.updateRow(primary_key, key, new EzqlRow(column, data)));
    }

    @Override
    public void setBool(String primary_key, String key, String column, String table, Boolean data) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->ezqlTable.updateRow(primary_key, key, new EzqlRow(column, data ? 1 : 0)));
    }

    @Override
    public void setDouble(String primary_key, String key, String column, String table, double data) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->ezqlTable.updateRow(primary_key, key, new EzqlRow(column, data)));
    }


    @Override
    public boolean hasPlayer(String table, UUID key) {
        EzqlTable ezqlTable = database.getTable(prefix+table);

        return ezqlTable.exists("uuid",key.toString());
    }

    @Override
    public boolean hasTable(String table) {
        return database.hasTable(prefix+table);
    }

    @Override
    public void preparePlayerData(String table, String uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->playerdata.pushRow(uuid,""));
    }
}
