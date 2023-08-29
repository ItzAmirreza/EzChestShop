package me.deadlight.ezchestshop.data;
import me.deadlight.ezchestshop.enums.Changes;
import me.deadlight.ezchestshop.events.PlayerTransactEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.holograms.ShopHologram;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.objects.ShopSettings;
import me.deadlight.ezchestshop.utils.objects.SqlQueue;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.WebhookSender;
import me.deadlight.ezchestshop.utils.XPEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * ShopContainer - a tool to retrieve and store data regarding shops,
 * in memory for quick access and sqlite for long term storage.
 */

public class ShopContainer {

    private static Economy econ = EzChestShop.getEconomy();
    private static HashMap<Location, EzShop> shopMap = new HashMap<>();

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Save all shops from the Database into memory,
     * so querying all shops is less resource expensive
     */
    public static void queryShopsToMemory() {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        shopMap = db.queryShops();
    }

    /**
     * Delete a Shop at a given Location.
     *
     * @param loc the Location of the Shop.
     */
    public static void deleteShop(Location loc) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        db.deleteEntry("location", Utils.LocationtoString(loc),
                "shopdata");
        shopMap.remove(loc);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ShopHologram.hasHologram(loc, p))
                ShopHologram.hideForAll(loc);
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
                                   boolean adminshop, String rotation) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);
        String encodedItem = Utils.encodeItem(item);
        db.insertShop(sloc, p.getUniqueId().toString(), encodedItem == null ? "Error" : encodedItem, buyprice, sellprice, msgtoggle, dbuy, dsell, admins, shareincome, adminshop, rotation, new ArrayList<>());
        ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, adminshop, rotation, new ArrayList<>());
        EzShop shop = new EzShop(loc, p, item, buyprice, sellprice, settings);
        shopMap.put(loc, shop);
        EzChestShop.getPlugin().getServer().getScheduler().runTaskAsynchronously(
                EzChestShop.getPlugin(), () -> {

                    try {
                        WebhookSender.sendDiscordNewShopAlert(
                                p.getName(),
                                //Show buying price in string if dbuy is false, otherwise show "Disabled"
                                dbuy ? "Disabled" : String.valueOf(buyprice),
                                dsell ? "Disabled" : String.valueOf(sellprice),
                                //Show Item name if it has custom name, otherwise show localized name
                                item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name(),
                                item.getType().name(),
                                //Display Current Time Like This: 2023/5/1 | 23:10:23
                                formatter.format(java.time.LocalDateTime.now()).replace("T", " | "),
                                //Display shop location as this: world, x, y, z
                                loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
        );

    }

    public static void loadShop(Location loc, PersistentDataContainer dataContainer) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);
        boolean msgtoggle = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
        boolean dbuy = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean dsell = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;
        String admins = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        boolean shareincome = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER) == 1;
        boolean adminshop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;
        String owner = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        String encodedItem = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING);
        double buyprice = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        double sellprice = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? "top" : rotation;
        db.insertShop(sloc, owner, encodedItem == null ? "Error" : encodedItem, buyprice, sellprice, msgtoggle, dbuy, dsell, admins, shareincome, adminshop, rotation, new ArrayList<>());

        ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, adminshop, rotation, new ArrayList<>());
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
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        String rotation = oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, rotation);
        return newContainer;
    }

    public static List<EzShop> getShopFromOwner(UUID uuid){
        List<EzShop> ezShops = new ArrayList<>();

        for (EzShop shop : shopMap.values()) {
            // no admin shop and shop owned by this player.
            if(!shop.getSettings().isAdminshop() && shop.getOwnerID().equals(uuid)){
                ezShops.add(shop);
            }
        }

        return ezShops;
    }

    /**
     * Query the Database to retrieve all Shops a player owns.
     *
     * @param p the Player to query
     * @return the amount of shops a player owns.
     */
    public static int getShopCount(Player p) {
        return getShopFromOwner(p.getUniqueId()).size();
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
    public static List<EzShop> getShops() {
        return new ArrayList<>(shopMap.values());
    }

    public static EzShop getShop(Location location) {
        if (isShop(location)) {
            return shopMap.get(location);
        }
        return null;
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
            boolean adminshop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;

            String owner = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
            String encodedItem = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING);
            double buyprice = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
            double sellprice = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
            String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
            rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
            ShopSettings settings = new ShopSettings(sloc, msgtoggle, dbuy, dsell, admins, shareincome, adminshop, rotation, new ArrayList<>());
            EzShop shop = new EzShop(loc, owner, Utils.decodeItem(encodedItem), buyprice, sellprice, settings);
            shopMap.put(loc, shop);
            return settings;
        }
    }

    public static void buyItem(Block containerBlock, double price, int count, ItemStack tthatItem, Player player, OfflinePlayer owner, PersistentDataContainer data) {
        ItemStack thatItem = tthatItem.clone();

        LanguageManager lm = new LanguageManager();
        //check for money
        if (Utils.containsAtLeast(Utils.getBlockInventory(containerBlock), thatItem , count)) {

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
                    Utils.removeItem(Utils.getBlockInventory(containerBlock), thatItem);
                    getandgive(Bukkit.getOfflinePlayer(player.getUniqueId()), price, owner);
                    sharedIncomeCheck(data, price);
                    transactionMessage(data, owner, player, price, true, tthatItem, count, containerBlock.getLocation().getBlock());
                    player.sendMessage(lm.messageSuccBuy(price));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
                    Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
                            ShopCommandManager.ShopType.SHOP, ShopCommandManager.ShopAction.BUY, count + "");

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

        if (Utils.containsAtLeast(player.getInventory(), thatItem, count)) {

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
                    Utils.removeItem(player.getInventory(), thatItem);
                    getandgive(owner, price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                    transactionMessage(data, owner, Bukkit.getOfflinePlayer(player.getUniqueId()), price, false, tthatItem, count, containerBlock.getLocation().getBlock());
                    player.sendMessage(lm.messageSuccSell(price));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
                    Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
                            ShopCommandManager.ShopType.SHOP, ShopCommandManager.ShopAction.SELL, count + "");

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
                withdraw(price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(
                        data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
                        Bukkit.getOfflinePlayer(player.getUniqueId()), price, true, tthatItem, count, containerBlock);
                player.sendMessage(lm.messageSuccBuy(price));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
                Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
                        ShopCommandManager.ShopType.ADMINSHOP, ShopCommandManager.ShopAction.BUY, count + "");

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

        if (Utils.containsAtLeast(player.getInventory(), thatItem, count)) {

            thatItem.setAmount(count);
            deposit(price, Bukkit.getOfflinePlayer(player.getUniqueId()));
            transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(
                    data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
                    Bukkit.getOfflinePlayer(player.getUniqueId()), price, false, tthatItem, count, containerBlock);
            Utils.removeItem(player.getInventory(), thatItem);
            player.sendMessage(lm.messageSuccSell(price));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
            Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
                    ShopCommandManager.ShopType.ADMINSHOP, ShopCommandManager.ShopAction.SELL, count + "");

        } else {
            player.sendMessage(lm.notEnoughItemToSell());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }

    }

    private static void deposit(double price, OfflinePlayer deposit) {

        if (Config.useXP) {
            XPEconomy.depositPlayer(deposit, price);
        } else {
            econ.depositPlayer(deposit, price);
        }

    }

    private static boolean withdraw(double price, OfflinePlayer deposit) {

        if (Config.useXP) {
            return XPEconomy.withDrawPlayer(deposit, price);
        } else {
            return econ.withdrawPlayer(deposit, price).transactionSuccess();
        }

    }

    private static boolean ifHasMoney(OfflinePlayer player, double price) {
        if (Config.useXP) {
            return XPEconomy.has(player, price);
        } else  {
            double balance = econ.getBalance(player);
            return !(balance < price);
        }
    }

    private static void getandgive(OfflinePlayer withdraw, double price, OfflinePlayer deposit) {

        withdraw(price, withdraw);
        deposit(price, deposit);

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
                if (ifHasMoney(Bukkit.getOfflinePlayer(ownerUUID), profit * adminsList.size())) {
                    boolean succesful = withdraw(profit * adminsList.size(), Bukkit.getOfflinePlayer(ownerUUID));
                    if (succesful) {
                        for (UUID adminUUID : adminsList) {
                            deposit(profit, Bukkit.getOfflinePlayer(adminUUID));
                        }
                    }
                }

            }
        }

    }



    public static void transferOwner(BlockState state, OfflinePlayer newOwner) {
        Location loc = state.getLocation();
        if (isShop(loc)) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, newOwner.getUniqueId().toString());
            EzShop shop = getShop(loc);
            shop.getSqlQueue().setChange(Changes.SHOP_OWNER, newOwner.getUniqueId().toString());
            shop.setOwner(newOwner);
            state.update();
        }
    }

    public static void changePrice(BlockState state, double newPrice, boolean isBuyPrice) {
        Location loc = state.getLocation();
        if (isShop(loc)) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            container.set(new NamespacedKey(EzChestShop.getPlugin(), isBuyPrice ? "buy" : "sell"), PersistentDataType.DOUBLE, newPrice);
            EzShop shop = getShop(loc);
            shop.getSqlQueue().setChange(isBuyPrice ? Changes.BUY_PRICE : Changes.SELL_PRICE, newPrice);
            if (isBuyPrice) {
                shop.setBuyPrice(newPrice);
            } else {
                shop.setSellPrice(newPrice);
            }
            state.update();
        }
    }


    public static void startSqlQueueTask() {
        Bukkit.getScheduler().runTaskTimer(EzChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {

                //now looping through all shops and executing mysql commands

                for (EzShop shop : shopMap.values()) {
                    if (shop.getSettings().getSqlQueue().isChanged()) {
                        runSqlTask(shop, shop.getSettings().getSqlQueue());
                    }
                    if (shop.getSqlQueue().isChanged()) {
                        runSqlTask(shop, shop.getSqlQueue());
                    }
                }

            }
        }, 0, 20 * 60); //for now leaving it as non-editable value
    }

    public static void saveSqlQueueCache() { //This part needs to change, it causes lag for big servers, have to save all changes in one query only!
        for (EzShop shop : shopMap.values()) {
            if (shop.getSettings().getSqlQueue().isChanged()) {
                runSqlTask(shop, shop.getSettings().getSqlQueue());
            }
            if (shop.getSqlQueue().isChanged()) {
                runSqlTask(shop, shop.getSqlQueue());
            }
        }
    }

    private static void runSqlTask(EzShop shop, SqlQueue queue) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        //ok then it's time to execute the mysql thingys
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
            } else if (change.theClass == Double.class) {
                //well its double
                double value = (Double) valueObject;
                db.setDouble("location", sloc, change.databaseValue, "shopdata", value);
            }
        }

        //the last thing has to be clearing the SqlQueue object so don't remove this
        queue.resetChangeList(shop.getSettings(), shop); //giving new shop settings to keep the queue updated

    }
}