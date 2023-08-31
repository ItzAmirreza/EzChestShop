package me.deadlight.ezchestshop.data;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.enums.Changes;
import me.deadlight.ezchestshop.events.PlayerTransactEvent;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.WebhookSender;
import me.deadlight.ezchestshop.utils.XPEconomy;
import me.deadlight.ezchestshop.utils.holograms.ShopHologram;
import me.deadlight.ezchestshop.utils.holograms.TradeShopHologram;
import me.deadlight.ezchestshop.utils.objects.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

public class TradeShopContainer {

    private static HashMap<Location, EzTradeShop> tradeShopMap = new HashMap<>();

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Save all shops and tradeshops from the Database into memory,
     * so querying all shops is less resource expensive
     */
    public static void queryShopsToMemory() {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        tradeShopMap = db.queryTradeShops();
    }

    /**
     * Delete a Shop or Tradeshop at a given Location.
     *
     * @param loc the Location of the Shop.
     */
    public static void deleteShop(Location loc) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        db.deleteEntry("location", Utils.LocationtoString(loc),
                "tradeshopdata");
        tradeShopMap.remove(loc);


        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TradeShopHologram.hasHologram(loc, p))
                TradeShopHologram.hideForAll(loc);
        }
    }

    /**
     * Create a new trade Shop!
     *
     * @param loc the Location of the trade Shop.
     * @param p   the Owner of the trade Shop.
     */
    public static void createTradeShop(Location loc, Player p, ItemStack item1, ItemStack item2, boolean msgtoggle,
                                       TradeShopSettings.TradeDirection tradeDirection, String admins, boolean adminshop, String rotation) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);
        String encodedItem1 = Utils.encodeItem(item1);
        String encodedItem2 = Utils.encodeItem(item2);
        db.insertTradeShop(sloc, p.getUniqueId().toString(), encodedItem1 == null ? "Error" : encodedItem1, encodedItem2 == null ? "Error" : encodedItem2, msgtoggle, tradeDirection, admins, adminshop, rotation, new ArrayList<>());
        TradeShopSettings settings = new TradeShopSettings(sloc, msgtoggle, tradeDirection, admins, adminshop, rotation, new ArrayList<>());
        EzTradeShop shop = new EzTradeShop(loc, p, item1, item2, settings);
        tradeShopMap.put(loc, shop);
        //TODO Discord webhook for trade shops!
//        EzChestShop.getPlugin().getServer().getScheduler().runTaskAsynchronously(
//                EzChestShop.getPlugin(), () -> {
//
//                    try {
//                        WebhookSender.sendDiscordNewShopAlert(
//                                p.getName(),
//                                //Show buying price in string if dbuy is false, otherwise show "Disabled"
//                                dbuy ? "Disabled" : String.valueOf(buyprice),
//                                dsell ? "Disabled" : String.valueOf(sellprice),
//                                //Show Item name if it has custom name, otherwise show localized name
//                                item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name(),
//                                item.getType().name(),
//                                //Display Current Time Like This: 2023/5/1 | 23:10:23
//                                formatter.format(java.time.LocalDateTime.now()).replace("T", " | "),
//                                //Display shop location as this: world, x, y, z
//                                loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
//                        );
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//        );
    }

    public static void loadTradeShop(Location loc, PersistentDataContainer dataContainer) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        String sloc = Utils.LocationtoString(loc);
        boolean msgtoggle = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
        TradeShopSettings.TradeDirection tradeDirection = TradeShopSettings.TradeDirection.valueOf(
                dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"), PersistentDataType.STRING));
        String admins = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        boolean adminshop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;
        String owner = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        String encodedItem1 = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item1"), PersistentDataType.STRING);
        String encodedItem2 = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item2"), PersistentDataType.STRING);
        String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? "top" : rotation;
        db.insertTradeShop(sloc, owner, encodedItem1 == null ? "Error" : encodedItem1,
                encodedItem2 == null ? "Error" : encodedItem2, msgtoggle, tradeDirection, admins, adminshop, rotation, new ArrayList<>());

        TradeShopSettings settings = new TradeShopSettings(sloc, msgtoggle, tradeDirection, admins, adminshop, rotation, new ArrayList<>());
        EzTradeShop shop = new EzTradeShop(loc, owner, Utils.decodeItem(encodedItem1), Utils.decodeItem(encodedItem2), settings);
        tradeShopMap.put(loc, shop);
    }

    public static PersistentDataContainer copyContainerData(PersistentDataContainer oldContainer, PersistentDataContainer newContainer) {
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING));
        //add new settings data later
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"), PersistentDataType.STRING));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "item1"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item1"), PersistentDataType.STRING));
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "item2"), PersistentDataType.STRING,
                oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item2"), PersistentDataType.STRING));
        String rotation = oldContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        newContainer.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, rotation);
        return newContainer;
    }

    public static List<EzTradeShop> getTradeShopFromOwner(UUID uuid){
        List<EzTradeShop> ezTradeShops = new ArrayList<>();

        for (EzTradeShop tradeShop : tradeShopMap.values()) {
            // no admin shop and shop owned by this player.
            if(!tradeShop.getSettings().isAdminshop() && tradeShop.getOwnerID().equals(uuid)){
                ezTradeShops.add(tradeShop);
            }
        }

        return ezTradeShops;
    }

    /**
     * Query the Database to retrieve all Shops a player owns.
     *
     * @param p the Player to query
     * @return the amount of shops a player owns.
     */
    public static int getShopCount(Player p) {
        return getTradeShopFromOwner(p.getUniqueId()).size();
    }

    /**
     * Check if a Location is a Trade Shop
     *
     * @param loc the Location to be checked
     * @return a boolean based on the outcome.
     */
    public static boolean isTradeShop(Location loc) {
        return tradeShopMap.containsKey(loc);
    }

    /**
     * Get all Trade Shops from memory.
     *
     * @return a copy of all Trade Shops as stored in memory.
     */
    public static List<EzTradeShop> getTradeShops() {
        return new ArrayList<>(tradeShopMap.values());
    }

    public static EzTradeShop getTradeShop(Location location) {
        if (isTradeShop(location)) {
            return tradeShopMap.get(location);
        }
        return null;
    }

    public static TradeShopSettings getTradeShopSettings(Location loc) {
        if (tradeShopMap.containsKey(loc)) {
            return tradeShopMap.get(loc).getSettings();
        } else {
            PersistentDataContainer dataContainer = Utils.getDataContainer(loc.getBlock());
            String sloc = Utils.LocationtoString(loc);
            boolean msgtoggle = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
            TradeShopSettings.TradeDirection tradeDirection = TradeShopSettings.TradeDirection.valueOf(
                    dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"), PersistentDataType.STRING));
            String admins = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
            boolean adminshop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;

            String owner = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
            String encodedItem1 = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING);
            String encodedItem2 = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "item2"), PersistentDataType.STRING);
            String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
            rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
            TradeShopSettings settings = new TradeShopSettings(sloc, msgtoggle, tradeDirection, admins, adminshop, rotation, new ArrayList<>());
            EzTradeShop shop = new EzTradeShop(loc, owner, Utils.decodeItem(encodedItem1), Utils.decodeItem(encodedItem2), settings);
            tradeShopMap.put(loc, shop);
            return settings;
        }
    }

    /**
     * Run a transaction. The player has clicked in Item1, so he wants Item1 and gives Item2.
     * @param containerBlock the container block of the shop
     * @param count the amount of times this transaction should be run (count * amount of items)
     * @param shop_item1 the item the player wants
     * @param shop_item2 the item the player gives
     * @param player the player who is buying
     * @param owner the owner of the shop
     * @param data the data container of the shop
     */
    public static void buyItem1(Block containerBlock, int count, ItemStack shop_item1, ItemStack shop_item2, Player player, OfflinePlayer owner, PersistentDataContainer data) {

        ItemStack item1 = shop_item1.clone();
        int item1_total_amount = item1.getAmount() * count;
        ItemStack item2 = shop_item2.clone();
        int item2_total_amount = item2.getAmount() * count;


        LanguageManager lm = new LanguageManager();
        //check if the shop has enough item1's to sell
        if (!Utils.containsAtLeast(Utils.getBlockInventory(containerBlock), item1 , item1_total_amount)) {
            player.sendMessage(lm.outofStock());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        //check if the player has enough item2's to pay
        if (!Utils.containsAtLeast(player.getInventory(), item2, item2_total_amount)) {
            player.sendMessage(lm.cannotAfford());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        // check if the player has enough space to fit the item1's
        if (!Utils.hasEnoughSpace(player, item1_total_amount, item1)) {
            player.sendMessage(lm.fullinv());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        // check if the container has enough space to store the item2's
        if (!Utils.containerHasEnoughSpace(Utils.getBlockInventory(containerBlock), item2_total_amount, item2)) {
            player.sendMessage(lm.chestIsFull());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }
        // item1 is the item the player wants, so give it to the player
        moveItemBetweenContainers(item1_total_amount, item1, Utils.getBlockInventory(containerBlock), player.getInventory());
        // item2 is the item the player gives, so give it to the container
        moveItemBetweenContainers(item2_total_amount, item2, player.getInventory(), Utils.getBlockInventory(containerBlock));
        //TODO send the transaction message and execute the commands
//        transactionMessage(data, owner, player, price, true, item1, count, containerBlock.getLocation().getBlock());
//        player.sendMessage(lm.messageSuccBuy(price));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
//        Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
//                ShopCommandManager.ShopType.SHOP, ShopCommandManager.ShopAction.BUY, count + "");
    }

    public static void buyItem2(Block containerBlock, int count, ItemStack shop_item1, ItemStack shop_item2, Player player, OfflinePlayer owner, PersistentDataContainer data) {
        //TODO extract the code from buyItem1 and buyItem2 into a method, as they are almost the same
        ItemStack item1 = shop_item2.clone();
        int item1_total_amount = item1.getAmount() * count;
        ItemStack item2 = shop_item1.clone();
        int item2_total_amount = item2.getAmount() * count;


        LanguageManager lm = new LanguageManager();
        //check if the shop has enough item1's to sell
        if (!Utils.containsAtLeast(Utils.getBlockInventory(containerBlock), item1 , item1_total_amount)) {
            player.sendMessage(lm.outofStock());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        //check if the player has enough item2's to pay
        if (!Utils.containsAtLeast(player.getInventory(), item2, item2_total_amount)) {
            player.sendMessage(lm.cannotAfford());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        // check if the player has enough space to fit the item1's
        if (!Utils.hasEnoughSpace(player, item1_total_amount, item1)) {
            player.sendMessage(lm.fullinv());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        // check if the container has enough space to store the item2's
        if (!Utils.containerHasEnoughSpace(Utils.getBlockInventory(containerBlock), item2_total_amount, item2)) {
            player.sendMessage(lm.chestIsFull());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }
        // item1 is the item the player wants, so give it to the player
        moveItemBetweenContainers(item1_total_amount, item1, Utils.getBlockInventory(containerBlock), player.getInventory());
        // item2 is the item the player gives, so give it to the container
        moveItemBetweenContainers(item2_total_amount, item2, player.getInventory(), Utils.getBlockInventory(containerBlock));
        //TODO send the transaction message and execute the commands
//        transactionMessage(data, owner, player, price, true, item1, count, containerBlock.getLocation().getBlock());
//        player.sendMessage(lm.messageSuccBuy(price));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
//        Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
//                ShopCommandManager.ShopType.SHOP, ShopCommandManager.ShopAction.BUY, count + "");
    }

    public static void buyServerItem1(Block containerBlock, int count, ItemStack shop_item1, ItemStack shop_item2, Player player, PersistentDataContainer data) {

        ItemStack item1 = shop_item1.clone();
        int item1_total_amount = item1.getAmount() * count;
        ItemStack item2 = shop_item2.clone();
        int item2_total_amount = item2.getAmount() * count;

        LanguageManager lm = new LanguageManager();
        //check for money


        //check if the player has enough item2's to pay
        if (!Utils.containsAtLeast(player.getInventory(), item2, item2_total_amount)) {
            player.sendMessage(lm.cannotAfford());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        // check if the player has enough space to fit the item1's
        if (!Utils.hasEnoughSpace(player, item1_total_amount, item1)) {
            player.sendMessage(lm.fullinv());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        int stacks = (int) Math.ceil(item1_total_amount / (double) item1.getMaxStackSize());
        int maxStackSize = item1.getMaxStackSize();
        for (int i = 0; i < stacks; i++) {
            if (i + 1 == stacks) {
                item1.setAmount(item1_total_amount % maxStackSize == 0 ? maxStackSize : item1_total_amount % maxStackSize);
            } else {
                item1.setAmount(maxStackSize);
            }
            player.getInventory().addItem(item1);
        }
        // remove the item2's from the player
        item2.setAmount(item2_total_amount);
        Utils.removeItem(player.getInventory(), item2);

//        transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(
//                data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
//                Bukkit.getOfflinePlayer(player.getUniqueId()), price, true, tthatItem, count, containerBlock);
//        player.sendMessage(lm.messageSuccBuy(price));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
//        Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
//                ShopCommandManager.ShopType.ADMINSHOP, ShopCommandManager.ShopAction.BUY, count + "");
    }

    public static void buyServerItem2(Block containerBlock, int count, ItemStack shop_item1, ItemStack shop_item2, Player player, PersistentDataContainer data) {
        //TODO extract the code from buyServerItem1 and buyServerItem2 into a method, as they are almost the same
        ItemStack item1 = shop_item2.clone();
        int item1_total_amount = item1.getAmount() * count;
        ItemStack item2 = shop_item1.clone();
        int item2_total_amount = item2.getAmount() * count;

        LanguageManager lm = new LanguageManager();
        //check for money


        //check if the player has enough item2's to pay
        if (!Utils.containsAtLeast(player.getInventory(), item2, item2_total_amount)) {
            player.sendMessage(lm.cannotAfford());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        // check if the player has enough space to fit the item1's
        if (!Utils.hasEnoughSpace(player, item1_total_amount, item1)) {
            player.sendMessage(lm.fullinv());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            return;
        }

        int stacks = (int) Math.ceil(item1_total_amount / (double) item1.getMaxStackSize());
        int maxStackSize = item1.getMaxStackSize();
        for (int i = 0; i < stacks; i++) {
            if (i + 1 == stacks) {
                item1.setAmount(item1_total_amount % maxStackSize == 0 ? maxStackSize : item1_total_amount % maxStackSize);
            } else {
                item1.setAmount(maxStackSize);
            }
            player.getInventory().addItem(item1);
        }
        // remove the item1's from the container
        item2.setAmount(item2_total_amount);
        Utils.removeItem(player.getInventory(), item2);

//        transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(
//                data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
//                Bukkit.getOfflinePlayer(player.getUniqueId()), price, true, tthatItem, count, containerBlock);
//        player.sendMessage(lm.messageSuccBuy(price));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
//        Config.shopCommandManager.executeCommands(player, containerBlock.getLocation(),
//                ShopCommandManager.ShopType.ADMINSHOP, ShopCommandManager.ShopAction.BUY, count + "");

    }

    private static void moveItemBetweenContainers(int amount, ItemStack item, Inventory from, Inventory to) {
        int stacks = (int) Math.ceil(amount / (double) item.getMaxStackSize());
        int maxStackSize = item.getMaxStackSize();
        for (int i = 0; i < stacks; i++) {
            if (i + 1 == stacks) {
                item.setAmount(amount % maxStackSize == 0 ? maxStackSize : amount % maxStackSize);
            } else {
                item.setAmount(maxStackSize);
            }
            to.addItem(item);
        }
        // remove the item1's from the container
        item.setAmount(amount);
        Utils.removeItem(from, item);
    }


    private static void transactionMessage(PersistentDataContainer data, OfflinePlayer owner, OfflinePlayer customer, double price, boolean isBuy, ItemStack item, int count, Block containerBlock) {

        //buying = True, Selling = False
        PlayerTransactEvent transactEvent = new PlayerTransactEvent(owner, customer, price, isBuy, item, count, Utils.getAdminsList(data), containerBlock);
        Bukkit.getPluginManager().callEvent(transactEvent);

    }




    public static void transferOwner(BlockState state, OfflinePlayer newOwner) {
        Location loc = state.getLocation();
        if (isTradeShop(loc)) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, newOwner.getUniqueId().toString());
            EzTradeShop tradeShop = getTradeShop(loc);
            tradeShop.getSqlQueue().setChange(Changes.SHOP_OWNER, newOwner.getUniqueId().toString());
            tradeShop.setOwner(newOwner);
            state.update();
        }
    }

    public static void changeItemAmount(BlockState state, int newAmount, boolean isItem1) {
        Location loc = state.getLocation();
        if (isTradeShop(loc)) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            EzTradeShop tradeShop = getTradeShop(loc);
            if (isItem1) {
                ItemStack item1 = tradeShop.getItem1().clone();
                item1.setAmount(newAmount);
                String encodedItem = Utils.encodeItem(item1);
                if (encodedItem != null) {
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "item1"), PersistentDataType.STRING, encodedItem);
                }
                tradeShop.getSqlQueue().setChange(Changes.ITEM1, encodedItem);
                tradeShop.setItem1(item1);
            } else {
                ItemStack item2 = tradeShop.getItem2().clone();
                item2.setAmount(newAmount);
                String encodedItem = Utils.encodeItem(item2);
                if (encodedItem != null) {
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "item2"), PersistentDataType.STRING, encodedItem);
                }
                tradeShop.getSqlQueue().setChange(Changes.ITEM2, encodedItem);
                tradeShop.setItem2(item2);
            }
            state.update();
        }
    }


    public static void startSqlQueueTask() {
        Bukkit.getScheduler().runTaskTimer(EzChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {

                //now looping through all shops and executing mysql commands

                for (EzTradeShop tradeShop : tradeShopMap.values()) {
                    if (tradeShop.getSettings().getSqlQueue().isChanged()) {
                        runSqlTask(tradeShop, tradeShop.getSettings().getSqlQueue());
                    }
                    if (tradeShop.getSqlQueue().isChanged()) {
                        runSqlTask(tradeShop, tradeShop.getSqlQueue());
                    }
                }

            }
        }, 0, 20 * 60); //for now leaving it as non-editable value
    }

    public static void saveSqlQueueCache() { //This part needs to change, it causes lag for big servers, have to save all changes in one query only!
        for (EzTradeShop ezTradeShop : tradeShopMap.values()) {
            if (ezTradeShop.getSettings().getSqlQueue().isChanged()) {
                runSqlTask(ezTradeShop, ezTradeShop.getSettings().getSqlQueue());
            }
            if (ezTradeShop.getSqlQueue().isChanged()) {
                runSqlTask(ezTradeShop, ezTradeShop.getSqlQueue());
            }
        }
    }

    private static void runSqlTask(EzTradeShop tradeShop, SqlQueue queue) {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        //ok then it's time to execute the mysql thingys
        HashMap<Changes, Object> changes = queue.getChangesList();
        String sloc = tradeShop.getSettings().getSloc();
        for (Changes change : changes.keySet()) {
            Object valueObject = changes.get(change);

            //mysql job / you can get the value using Changes.
            if (change.theClass == String.class) {
                //well its string
                String value = (String) valueObject;
                db.setString("location", sloc, change.databaseValue, "tradeshopdata", value);

            } else if (change.theClass == Boolean.class) {
                //well its boolean
                boolean value = (Boolean) valueObject;
                db.setBool("location", sloc, change.databaseValue, "tradeshopdata", value);
            } else if (change.theClass == Double.class) {
                //well its double
                double value = (Double) valueObject;
                db.setDouble("location", sloc, change.databaseValue, "tradeshopdata", value);
            }
        }

        //the last thing has to be clearing the SqlQueue object so don't remove this
        queue.resetChangeList(tradeShop.getSettings(), tradeShop); //giving new shop settings to keep the queue updated

    }
}