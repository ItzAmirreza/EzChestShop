package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Utils.ASHologramObject;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ShopContainer - a tool to retrieve and store data regarding shops,
 * in memory for quick access and sqlite for long term storage.
 */

public class ShopContainer {

    private static List<Location> shops = new ArrayList<>();

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
    public static void createShop(Location loc, Player p) {
        Database db = EzChestShop.getPlugin().getDatabase();
        db.prepareColumn("shopdata", "location", Utils.LocationtoString(loc));
        db.setString("location", Utils.LocationtoString(loc),
                "owner", "shopdata", p.getUniqueId().toString()
                        .replace("-", ""));
        shops.add(loc);
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
                "IS \"" + p.getUniqueId().toString()
                        .replace("-", "") + "\"").size();
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

}