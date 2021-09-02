package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ShopContainer - a tool to retrieve and store data regarding shops,
 * in memory for quick access and sqlite for long term storage.
 */

public class ShopContainer {

    private static List<Location> shops = new ArrayList<>();
    private static HashMap<Location, ShopSettings> shopSettings = new HashMap<>();

    /**
     * Save all shops from the Database into memory,
     * so querying all shops is less resource expensive
     */
    public static void queryShopsToMemory() {
        Database db = EzChestShop.getPlugin().getDatabase();
        for (String sloc : db.getKeys("location", "shopdata")) {
            shops.add(Utils.StringtoLocation(sloc));
        }
    }

    /**
     * Delete a Shop at a given Location.
     *
     * @param loc the Location of the Shop.
     */
    public static void deleteShop(Location loc) {
        Database db = EzChestShop.getPlugin().getDatabase();
        db.deleteEntry("location", Utils.LocationtoString(loc),
                "shopdata");
        shops.remove(loc);

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerCloseToChestListener.hideHologram(p, loc);
        }
    }

    /**
     * Create a new Shop!
     *
     * @param loc the Location of the Shop.
     * @param p   the Owner of the Shop.
     */
    public static void createShop(Location loc, Player p, ItemStack item, double buyprice, double sellprice, boolean msgtoggle,
                                  boolean dbuy, boolean dsell, String admins, boolean shareincome,
                                  String trans, boolean adminshop) {
        Database db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);
        String encodedItem = Utils.encodeItem(item);
        db.insertShop(sloc, p.getUniqueId().toString(), encodedItem == null ? "Error" : encodedItem, buyprice, sellprice, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop);
        ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop);
        shopSettings.put(loc, settings);
        shops.add(loc);
    }

    public static void loadShop(Location loc, PersistentDataContainer dataContainer) {
        Database db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);

        boolean msgtoggle = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
        boolean dbuy = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean dsell = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;
        String admins = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        boolean shareincome = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER) == 1;
        String trans = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        boolean adminshop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;

        String owner = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        String encodedItem = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING);
        double buyprice = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        double sellprice = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        db.insertShop(sloc, owner, encodedItem == null ? "Error" : encodedItem, buyprice, sellprice, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop);

        ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop);
        shopSettings.put(loc, settings);
        shops.add(loc);
    }

    public static PersistentDataContainer copyContainerData(PersistentDataContainer oldContainer, PersistentDataContainer newContainer) {
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE));
        //add new settings data later
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        return newContainer;
    }

    /**
     * Query the Database to retrieve all Shops a player owns.
     *
     * @param p the Player to query
     * @return the amount of shops a player owns.
     */
    public static int getShopCount(Player p) {
        Database db = EzChestShop.getPlugin().getDatabase();
        return db.getKeysByExpresiion("location", "owner", "shopdata",
                "IS \"" + p.getUniqueId().toString() + "\"").size();
    }

    /**
     * Check if a Location is a Shop
     *
     * @param loc the Location to be checked
     * @return a boolean based on the outcome.
     */
    public static boolean isShop(Location loc) {
        return shops.contains(loc);
    }

    /**
     * Get all Shops from memory.
     *
     * @return a copy of all Shops as stored in memory.
     */
    public static List<Location> getShops() {
        return new ArrayList<>(shops);
    }

    public static ShopSettings getShopSettings(Location loc) {
        if (shopSettings.containsKey(loc)) {
            return shopSettings.get(loc);
        } else {
            Database db = EzChestShop.getPlugin().getDatabase();
            String sloc = Utils.LocationtoString(loc);
            boolean msgtoggle = db.getBool("location", sloc,
                    "msgToggle", "shopdata");
            boolean dbuy =  db.getBool("location", sloc,
                    "buyDisabled", "shopdata");
            boolean dsell = db.getBool("location", sloc,
                    "sellDisabled", "shopdata");
            String admins = db.getString("location", sloc,
                    "admins", "shopdata");
            boolean shareincome = db.getBool("location", sloc,
                    "shareIncome", "shopdata");
            String trans = db.getString("location", sloc,
                    "transactions", "shopdata");
            boolean adminshop = db.getBool("location", sloc,
                    "adminshop", "shopdata");
            ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop);
            shopSettings.put(loc, settings);
            return settings;
        }
    }
/*

            db.getDouble("location", sloc,
                    "buyPrice", "shopdata");
            db.getDouble("location", sloc,
                    "sellPrice", "shopdata");
 */
}