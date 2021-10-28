package me.deadlight.ezchestshop.Data;
import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.Enums.Changes;
import me.deadlight.ezchestshop.Events.PlayerTransactEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Objects.SqlQueue;
import me.deadlight.ezchestshop.Utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * ShopContainer - a tool to retrieve and store data regarding shops,
 * in memory for quick access and sqlite for long term storage.
 */

public class ShopContainer {

    private static Economy econ = EzChestShop.getEconomy();
    private static HashMap<Location, EzShop> shopMap = new HashMap<>();

    /**
     * Save all shops from the Database into memory,
     * so querying all shops is less resource expensive
     */
    public static void queryShopsToMemory() {
        Database db = EzChestShop.getPlugin().getDatabase();
        shopMap = db.queryShops();
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
        shopMap.remove(loc);
        EzShop.hideHologram(loc);
    }

    /**
     * Create a new Shop!
     *
     * @param loc the Location of the Shop.
     * @param p   the Owner of the Shop.
     */
    public static void createShop(Location loc, Player p, ItemStack item, double buyprice, double sellprice, boolean msgtoggle,
                                  boolean dbuy, boolean dsell, String admins, boolean shareincome,
                                  String trans, boolean adminshop, String rotation) {
        Database db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);
        String encodedItem = Utils.encodeItem(item);
        db.insertShop(sloc, p.getUniqueId().toString(), encodedItem == null ? "Error" : encodedItem, buyprice, sellprice, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop, rotation);
        ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop, rotation);
        EzShop shop = new EzShop(loc, p, item, buyprice, sellprice, settings);
        shopMap.put(loc, shop);
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
        String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? "top" : rotation;
        db.insertShop(sloc, owner, encodedItem == null ? "Error" : encodedItem, buyprice, sellprice, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop, rotation);

        ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop, rotation);
        EzShop shop = new EzShop(loc, owner, Utils.decodeItem(encodedItem), buyprice, sellprice, settings);
        shopMap.put(loc, shop);;
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
        String rotation = oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, rotation);
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
        return shopMap.containsKey(loc);
    }

    /**
     * Get all Shops from memory.
     *
     * @return a copy of all Shops as stored in memory.
     */
    public static List<Location> getShops() {
        return new ArrayList<>(shopMap.keySet());
    }

    /**
     * Get a Shop from Memory using it's location.
     *
     * @param loc
     * @return
     */
    public static EzShop getShop(Location loc) {
        return shopMap.get(loc);
    }

    /**
     * Get a Shop from Memory using it's location as a String.
     *
     * @param sloc
     * @return
     */
    public static EzShop getShop(String sloc) {
        return shopMap.get(Utils.StringtoLocation(sloc));
    }

    public static ShopSettings getShopSettings(Location loc) {
        if (shopMap.containsKey(loc)) {
            return shopMap.get(loc).getSettings();
        } else {

            //why we would need to use database data for getting settings? just setting them in database is enough
            PersistentDataContainer dataContainer = Utils.getDataContainer(loc.getBlock());
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
            String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
            rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
            ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, trans, adminshop, rotation);
            EzShop shop = new EzShop(loc, owner, Utils.decodeItem(encodedItem), buyprice, sellprice, settings);
            shopMap.put(loc, shop);
            return settings;
        }
    }

    public static void buyItem(Block containerBlock, double price, int count, ItemStack tthatItem, Player player, OfflinePlayer owner, PersistentDataContainer data) {
        ItemStack thatItem = tthatItem.clone();

        LanguageManager lm = new LanguageManager();
        //check for money

        if (Utils.getBlockInventory(containerBlock).containsAtLeast(thatItem , count)) {

            if (ifHasMoney(Bukkit.getOfflinePlayer(player.getUniqueId()), price)) {

                if (Utils.hasEnoughSpace(player, count, thatItem)) {

                    int stacks = (int) Math.ceil(count / (double) thatItem.getMaxStackSize());
                    int max_size = thatItem.getMaxStackSize();
                    for (int i = 0; i < stacks; i++) {
                        if (i + 1 == stacks) {
                            thatItem.setAmount(count % max_size == 0 ? max_size : count % max_size);
                        } else {
                            thatItem.setAmount(max_size);
                        }
                        player.getInventory().addItem(thatItem);
                    }
                    //For the transaction event
                    thatItem.setAmount(count);
                    Utils.getBlockInventory(containerBlock).removeItem(thatItem);
                    getandgive(Bukkit.getOfflinePlayer(player.getUniqueId()), price, owner);
                    sharedIncomeCheck(data, price);
                    transactionMessage(data, owner, player, price, true, tthatItem, count, containerBlock.getLocation().getBlock());
                    player.sendMessage(lm.messageSuccBuy(price));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);


                } else {
                    player.sendMessage(lm.fullinv());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
                }

            } else {

                player.sendMessage(lm.cannotAfford());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);

            }

        } else {
            player.sendMessage(lm.outofStock());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }
    }

    public static void sellItem(Block containerBlock, double price, int count, ItemStack tthatItem, Player player, OfflinePlayer owner, PersistentDataContainer data) {

        LanguageManager lm = new LanguageManager();

        ItemStack thatItem = tthatItem.clone();

        if (player.getInventory().containsAtLeast(thatItem, count)) {

            if (ifHasMoney(owner, price)) {

                if (Utils.containerHasEnoughSpace(Utils.getBlockInventory(containerBlock), count, thatItem)) {
                    int stacks = (int) Math.ceil(count / (double) thatItem.getMaxStackSize());
                    int max_size = thatItem.getMaxStackSize();
                    for (int i = 0; i < stacks; i++) {
                        if (i + 1 == stacks) {
                            thatItem.setAmount(count % max_size == 0 ? max_size : count % max_size);
                        } else {
                            thatItem.setAmount(max_size);
                        }
                        Utils.getBlockInventory(containerBlock).addItem(thatItem);
                    }
                    //For the transaction event
                    thatItem.setAmount(count);
                    player.getInventory().removeItem(thatItem);
                    getandgive(owner, price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                    transactionMessage(data, owner, Bukkit.getOfflinePlayer(player.getUniqueId()), price, false, tthatItem, count, containerBlock.getLocation().getBlock());
                    player.sendMessage(lm.messageSuccSell(price));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

                } else {
                    player.sendMessage(lm.chestIsFull());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
                }

            } else {

                player.sendMessage(lm.shopCannotAfford());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);

            }
        } else {
            player.sendMessage(lm.notEnoughItemToSell());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }

    }

    public static void buyServerItem(Block containerBlock, double price, int count, Player player, ItemStack tthatItem, PersistentDataContainer data) {
        ItemStack thatItem = tthatItem.clone();

        LanguageManager lm = new LanguageManager();
        //check for money

        if (ifHasMoney(Bukkit.getOfflinePlayer(player.getUniqueId()), price)) {

            if (Utils.hasEnoughSpace(player, count, thatItem)) {

                int stacks = (int) Math.ceil(count / (double) thatItem.getMaxStackSize());
                int max_size = thatItem.getMaxStackSize();
                for (int i = 0; i < stacks; i++) {
                    if (i + 1 == stacks) {
                        thatItem.setAmount(count % max_size == 0 ? max_size : count % max_size);
                    } else {
                        thatItem.setAmount(max_size);
                    }
                    player.getInventory().addItem(thatItem);
                }
                //For the transaction event
                thatItem.setAmount(count);
                take(price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(
                        data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
                        Bukkit.getOfflinePlayer(player.getUniqueId()), price, true, tthatItem, count, containerBlock);
                player.sendMessage(lm.messageSuccBuy(price));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

            } else {
                player.sendMessage(lm.fullinv());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            }

        } else {

            player.sendMessage(lm.cannotAfford());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);

        }



    }

    public static void sellServerItem(Block containerBlock, double price, int count, ItemStack tthatItem, Player player, PersistentDataContainer data) {

        LanguageManager lm = new LanguageManager();

        ItemStack thatItem = tthatItem.clone();

        if (player.getInventory().containsAtLeast(thatItem, count)) {

            thatItem.setAmount(count);
            deposit(price, Bukkit.getOfflinePlayer(player.getUniqueId()));
            transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(
                    data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
                    Bukkit.getOfflinePlayer(player.getUniqueId()), price, false, tthatItem, count, containerBlock);
            player.getInventory().removeItem(thatItem);
            player.sendMessage(lm.messageSuccSell(price));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

        } else {
            player.sendMessage(lm.notEnoughItemToSell());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }

    }


    private static void deposit(double price, OfflinePlayer deposit) {

        econ.depositPlayer(deposit, price);

    }
    private static void take(double price, OfflinePlayer deposit) {

        econ.withdrawPlayer(deposit, price);

    }

    private static boolean ifHasMoney(OfflinePlayer player, double price) {
        if (econ.has(player, price)) {
            return true;
        }
        return false;
    }

    private static void getandgive(OfflinePlayer withdraw, double price, OfflinePlayer deposit) {

        econ.withdrawPlayer(withdraw, price);
        econ.depositPlayer(deposit, price);


    }

    private static void transactionMessage(PersistentDataContainer data, OfflinePlayer owner, OfflinePlayer customer, double price, boolean isBuy, ItemStack item, int count, Block containerBlock) {

        //buying = True, Selling = False
        PlayerTransactEvent transactEvent = new PlayerTransactEvent(owner, customer, price, isBuy, item, count, Utils.getAdminsList(data), containerBlock);
        Bukkit.getPluginManager().callEvent(transactEvent);

    }

    private static void sharedIncomeCheck(PersistentDataContainer data, double price) {
        boolean isSharedIncome = data.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER) == 1;
        if (isSharedIncome) {
            UUID ownerUUID = UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING));
            List<UUID> adminsList = Utils.getAdminsList(data);
            double profit = price/(adminsList.size() + 1);
            if (adminsList.size() > 0) {
                if (econ.has(Bukkit.getOfflinePlayer(ownerUUID), profit * adminsList.size())) {
                    EconomyResponse details = econ.withdrawPlayer(Bukkit.getOfflinePlayer(ownerUUID), profit * adminsList.size());
                    if (details.transactionSuccess()) {
                        for (UUID adminUUID : adminsList) {
                            econ.depositPlayer(Bukkit.getOfflinePlayer(adminUUID), profit);
                        }
                    }
                }

            }
        }

    }




    public static void startSqlQueueTask() {
        Bukkit.getScheduler().runTaskTimer(EzChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {

                //now looping through all shops and executing mysql commands

                for (EzShop shop : shopMap.values()) {
                    if (shop.getSettings().getSqlQueue().isChanged()) {
                        Database db = EzChestShop.getPlugin().getDatabase();
                        //ok then it's time to execute the mysql thingys
                        SqlQueue queue = shop.getSettings().getSqlQueue();
                        HashMap<Changes, Object> changes = queue.getChangesList();
                        String sloc = shop.getSettings().getSloc();
                        for (Changes change : changes.keySet()) {
                            Object valueObject = changes.get(change);

                            //mysql job / you can get the value using Changes.
                            if (change.theClass == String.class) {
                                //well its string
                                String value = (String) valueObject;
                                db.setString("location", sloc, change.databaseValue, "shopdata", value);

                            } else if (change.theClass == Boolean.class) {
                                //well its boolean
                                boolean value = (Boolean) valueObject;
                                db.setBool("location", sloc, change.databaseValue, "shopdata", value);
                            }
                        }

                        //the last thing has to be clearing the SqlQueue object so don't remove this
                        queue.resetChangeList(shop.getSettings()); //giving new shop settings to keep the queue updated

                    }
                }

            }
        }, 0, 20 * 60); //for now leaving it as non-editable value
    }

    public static void saveSqlQueueCache() {
        for (EzShop shop : shopMap.values()) {
            if (shop.getSettings().getSqlQueue().isChanged()) {
                Database db = EzChestShop.getPlugin().getDatabase();
                //ok then it's time to execute the mysql thingys
                SqlQueue queue = shop.getSettings().getSqlQueue();
                HashMap<Changes, Object> changes = queue.getChangesList();
                String sloc = shop.getSettings().getSloc();
                for (Changes change : changes.keySet()) {
                    Object valueObject = changes.get(change);

                    //mysql job / you can get the value using Changes.
                    if (change.theClass == String.class) {
                        //well its string
                        String value = (String) valueObject;
                        db.setString("location", sloc, change.databaseValue, "shopdata", value);

                    } else if (change.theClass == Boolean.class) {
                        //well its boolean
                        boolean value = (Boolean) valueObject;
                        db.setBool("location", sloc, change.databaseValue, "shopdata", value);
                    }
                }

                //the last thing has to be clearing the SqlQueue object so don't remove this
                queue.resetChangeList(shop.getSettings()); //giving new shop settings to keep the queue updated

            }
        }
    }

/*

            db.getDouble("location", sloc,
                    "buyPrice", "shopdata");
            db.getDouble("location", sloc,
                    "sellPrice", "shopdata");
 */
}