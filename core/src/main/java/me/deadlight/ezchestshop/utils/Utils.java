package me.deadlight.ezchestshop.utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.DatabaseManager;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.mysql.MySQL;
import me.deadlight.ezchestshop.data.sqlite.SQLite;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.enums.Database;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.objects.TransactionLogObject;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static List<Object> onlinePackets = new ArrayList<>();
    public static List<String> rotations = Arrays.asList("up", "north", "east", "south", "west", "down");

    public static HashMap<String, Block> blockBreakMap = new HashMap<>();
    public static ConcurrentHashMap<Integer, BlockOutline> activeOutlines = new ConcurrentHashMap<>(); //player uuid, list of outlines
    public static List<UUID> enabledOutlines = new ArrayList<>();
    private static LanguageManager lm = new LanguageManager();

    private static String discordLink;

    public static VersionUtils versionUtils;
    public static DatabaseManager databaseManager;

    static {
        try {
            if (isFolia()) {
                versionUtils = (VersionUtils) Class.forName("me.deadlight.ezchestshop.utils.v1_20_R3").newInstance();
            } else {
                String packageName = Utils.class.getPackage().getName();
                String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                versionUtils = (VersionUtils) Class.forName(packageName + "." + internalsName).newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException exception) {
            Bukkit.getLogger().log(Level.SEVERE, "EzChestShop could not find a valid implementation for this server version.");
        }
    }




    static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Store a ItemStack into a persistent Data Container using Base64 encoding.
     *
     * @param item
     * @param data
     * @throws IOException
     */
    public static void storeItem(ItemStack item, PersistentDataContainer data) throws IOException {

        String encodedItem = encodeItem(item);
        if (encodedItem != null) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING, encodedItem);
        }

    }

    /**
     * Encode a ItemStack into a Base64 encoded String
     *
     * @param item
     * @return
     */
    public static String encodeItem(ItemStack item) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeObject(item);

            os.flush();
            byte[] rawData = io.toByteArray();

            String encodedData = Base64.getEncoder().encodeToString(rawData);

            os.close();
            return encodedData;

        } catch (IOException ex) {
            System.out.println(ex);
            return null;
        }
    }

    /**
     * Decode a ItemStack from Base64 into a ItemStack
     *
     * @param encodedItem
     * @return
     */
    public static ItemStack decodeItem(String encodedItem) {

        byte[] rawData = Base64.getDecoder().decode(encodedItem);

        try {

            ByteArrayInputStream io = new ByteArrayInputStream(rawData);
            BukkitObjectInputStream in = new BukkitObjectInputStream(io);

            ItemStack thatItem = (ItemStack) in.readObject();

            in.close();

            return thatItem;

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
            return null;
        }

    }

    /**
     * Convert a Item to a Text Compount. Used in Text Component Builders to show
     * items in chat.
     *
     * @param itemStack
     * @return
     * @category ItemUtils
     */
    public static String ItemToTextCompoundString(ItemStack itemStack) {
        return versionUtils.ItemToTextCompoundString(itemStack);
    }

    /**
     * Get the Inventory of the given Block if it is a Chest, Barrel or any Shulker
     *
     * @param block
     * @return
     */
    public static Inventory getBlockInventory(Block block) {
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            return ((Chest) block.getState()).getInventory();
        } else if (block.getType() == Material.BARREL) {
            return ((Barrel) block.getState()).getInventory();
        } else if (isShulkerBox(block)) {
            return ((ShulkerBox) block.getState()).getInventory();
        } else
            return null;
    }

    /**
     * Check if the given Block is a Shulker box (dye check)
     *
     * @param block
     * @return
     */
    public static boolean isShulkerBox(Block block) {
        return isShulkerBox(block.getType());
    }

    /**
     * Check if the given Material is a Shulker box (dye check)
     *
     * @param type
     * @return
     */
    public static boolean isShulkerBox(Material type) {
        return Arrays.asList(Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
                Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
                Material.GREEN_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
                Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX).contains(type);
    }

    /**
     * Check if the given Block is a applicable Shop.
     *
     * @param block
     * @return
     */
    public static boolean isApplicableContainer(Block block) {
        return isApplicableContainer(block.getType());
    }

    /**
     * Check if the given Material is a applicable Shop.
     *
     * @param type
     * @return
     */
    public static boolean isApplicableContainer(Material type) {
        return (type == Material.CHEST && Config.container_chests)
                || (type == Material.TRAPPED_CHEST && Config.container_trapped_chests)
                || (type == Material.BARREL && Config.container_barrels)
                || (isShulkerBox(type) && Config.container_shulkers);
    }

    public static List<UUID> getAdminsList(PersistentDataContainer data) {

        String adminsString = data.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        // UUID@UUID@UUID
        assert adminsString != null;
        if (adminsString.equalsIgnoreCase("none")) {
            return new ArrayList<>();
        } else {
            String[] stringUUIDS = adminsString.split("@");
            List<UUID> finalList = new ArrayList<>();
            for (String uuidInString : stringUUIDS) {
                finalList.add(UUID.fromString(uuidInString));
            }
            return finalList;
        }
    }


    public static List<TransactionLogObject> getListOfTransactions(Location containerBlock) {
        return null;
    }

    public static String getFinalItemName(ItemStack item) {
        String itemname = "Error";
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                itemname = colorify(item.getItemMeta().getDisplayName());
            } else if (item.getType() == Material.ENCHANTED_BOOK
                    && ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().size() == 1) {
                EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) item.getItemMeta();

                Map.Entry<Enchantment,Integer> entry = emeta.getStoredEnchants().entrySet().iterator().next();
                itemname = lm.itemEnchantHologram(entry.getKey(), entry.getValue());
            } else if (item.getItemMeta().hasLocalizedName()) {
                itemname = item.getItemMeta().getLocalizedName();
            } else {
                itemname = Utils.capitalizeFirstSplit(item.getType().toString());
            }
        } else {
            itemname = Utils.capitalizeFirstSplit(item.getType().toString());
        }
        return colorify(itemname).trim();
    }

    /**
     * Convert a Location to a String
     *
     * @param loc
     * @return
     */
    public static String LocationtoString(Location loc) {
        if (loc == null)
            return null;
        String sloc = "";
        sloc += ("W:" + loc.getWorld().getName() + ",");
        sloc += ("X:" + loc.getX() + ",");
        sloc += ("Y:" + loc.getY() + ",");
        sloc += ("Z:" + loc.getZ());
        return sloc;
    }

    /**
     * Convert a Location to a String with the Location rounded as defined via the
     * decimal argument
     *
     * @param loc
     * @param decimals
     * @return
     */
    public static String LocationRoundedtoString(Location loc, int decimals) {
        if (loc == null)
            return null;
        String sloc = "";
        sloc += ("W:" + loc.getWorld().getName() + ",");
        if (decimals <= 0) {
            sloc += ("X:" + (int) round(loc.getX(), decimals) + ",");
            sloc += ("Y:" + (int) round(loc.getY(), decimals) + ",");
            sloc += ("Z:" + (int) round(loc.getZ(), decimals));
        } else {
            sloc += ("X:" + round(loc.getX(), decimals) + ",");
            sloc += ("Y:" + round(loc.getY(), decimals) + ",");
            sloc += ("Z:" + round(loc.getZ(), decimals));
        }
        return sloc;
    }

    /**
     * Convert a String to a Location
     *
     * @param sloc
     * @return
     */
    public static Location StringtoLocation(String sloc) {
        if (sloc == null)
            return null;
        String[] slocs = sloc.split(",");
        World w = Bukkit.getWorld(slocs[0].split(":")[1]);
        Double x = Double.valueOf(slocs[1].split(":")[1]);
        Double y = Double.valueOf(slocs[2].split(":")[1]);
        Double z = Double.valueOf(slocs[3].split(":")[1]);
        Location loc = new Location(w, x, y, z);

        if (sloc.contains("Yaw:") && sloc.contains("Pitch:")) {
            loc.setYaw(Float.valueOf(slocs[4].split(":")[1]));
            loc.setPitch(Float.valueOf(slocs[5].split(":")[1]));
        }
        return loc;
    }

    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * Check if a String can be safely converted into a numeric value.
     *
     * @param strNum
     * @return
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static <T> List<T> moveListElement(List<T> list, int index, boolean up) {
        if (up) {
            if (index == 0) {
                return list;
            }
            T element = list.get(index);
            list.remove(index);
            list.add(index - 1, element);
        } else {
            if (index == list.size() - 1) {
                return list;
            }
            T element = list.get(index);
            list.remove(index);
            list.add(index + 1, element);
        }
        return list;
    }

    /**
     * Get the max permission level of a permission object (e.g. player)
     *
     * @param permissible a object using the Permissible System e.g. a Player.
     * @param permission  a Permission String to check e.g. ecs.shops.limit.
     * @return the maximum int found, unless user is an Operator or has the
     * ecs.admin permission.
     * Then the returned result will be -1
     */
    public static int getMaxPermission(Permissible permissible, String permission) {
        return getMaxPermission(permissible, permission, 0);
    }

    /**
     * Get the max permission level of a permission object (e.g. player)
     *
     * @param permissible a object using the Permissible System e.g. a Player.
     * @param permission  a Permission String to check e.g. ecs.shops.limit.
     * @param defaultMax  the default max value to return if no permission is found
     * @return the maximum int found, unless user is an Operator or has the
     * ecs.admin permission.
     * Then the returned result will be -1
     */
    public static int getMaxPermission(Permissible permissible, String permission, int defaultMax) {
        if (permissible.isOp() || permissible.hasPermission("ecs.admin"))
            return -1;

        final AtomicInteger max = new AtomicInteger(defaultMax);

        permissible.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission)
                .map(String::toLowerCase).filter(value -> value.startsWith(permission))
                .map(value -> value.replace(permission, "")).forEach(value -> {
                    if (value.equalsIgnoreCase("*")) {
                        max.set(-1);
                        return;
                    }

                    if (max.get() == -1)
                        return;

                    try {
                        int amount = Integer.parseInt(value);

                        if (amount > max.get())
                            max.set(amount);
                    } catch (NumberFormatException ignored) {
                    }
                });

        return max.get();
    }

    //

    /**
     * Split a String by "_" and capitalize each First word, then join them together
     * via " "
     *
     * @param string
     * @return
     */
    public static String capitalizeFirstSplit(String string) {
        string = string.toLowerCase();
        String n_string = "";
        for (String s : string.split("_")) {
            n_string += s.subSequence(0, 1).toString().toUpperCase()
                    + s.subSequence(1, s.length()).toString().toLowerCase() + " ";
        }
        return n_string;
    }

    public static boolean hasEnoughSpace(Player player, int amount, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : player.getInventory().getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }

        return emptySlots >= amount;
    }

    public static int playerEmptyCount(ItemStack[] storageContents, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : storageContents) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }
        return emptySlots;
    }

    public static int containerEmptyCount(ItemStack[] storageContents, ItemStack item) {

        if (storageContents == null) {
            return Integer.MAX_VALUE;
        }

        int emptySlots = 0;
        for (ItemStack content : storageContents) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }
        return emptySlots;
    }

    public static int howManyOfItemExists(ItemStack[] itemStacks, ItemStack mainItem) {

        if (itemStacks == null) {
            return Integer.MAX_VALUE;
        }

        int amount = 0;
        for (ItemStack item : itemStacks) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (isSimilar(item, mainItem)) {
                amount += item.getAmount();
            }

        }
        return amount;

    }

    public static boolean containerHasEnoughSpace(Inventory container, int amount, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : container.getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }

        return emptySlots >= amount;
    }

    public static boolean amountCheck(int amount) {
        if (amount == 0) {
            return false;
        }

        if (amount < 0) {
            return false;
        }
        return true;
    }

    public static List<String> calculatePossibleAmount(OfflinePlayer offlineCustomer, OfflinePlayer offlineSeller,
                                                       ItemStack[] playerInventory, ItemStack[] storageInventory, double eachBuyPrice, double eachSellPrice,
                                                       ItemStack itemStack) {

        List<String> results = new ArrayList<>();

        String buyCount = calculateBuyPossibleAmount(offlineCustomer, playerInventory, storageInventory, eachBuyPrice,
                itemStack);
        String sellCount = calculateSellPossibleAmount(offlineSeller, playerInventory, storageInventory, eachSellPrice,
                itemStack);

        results.add(buyCount);
        results.add(sellCount);
        return results;
    }

    public static String calculateBuyPossibleAmount(OfflinePlayer offlinePlayer, ItemStack[] playerInventory,
                                                    ItemStack[] storageInventory, double eachBuyPrice, ItemStack itemStack) {
        // I was going to run this in async but maybe later...
        int possibleCount = 0;
        double buyerBalance =
                Config.useXP ? XPEconomy.getXP(offlinePlayer) : EzChestShop.getEconomy().getBalance(offlinePlayer);
        int emptyCount = playerEmptyCount(playerInventory, itemStack);
        int howManyExists = howManyOfItemExists(storageInventory, itemStack);

        for (int num = 0; num < emptyCount; num++) {
            if (possibleCount + 1 > howManyExists) {
                break;
            }
            possibleCount += 1;
        }

        int result = 0;
        for (int num = 0; num < possibleCount; num++) {
            result += 1;
            if ((num + 1) * eachBuyPrice > buyerBalance) {
                return String.valueOf(num);
            }
        }

        return String.valueOf(result);
    }

    public static HashMap<OfflinePlayer, Boolean> hasPlayedBefore = new HashMap<>();
    public static boolean hasPlayedBefore(OfflinePlayer player) {
        if (player == null) {
            return false;
        }
        if (hasPlayedBefore.containsKey(player)) {
            return hasPlayedBefore.get(player);
        } else {
            boolean result = player.hasPlayedBefore();
            hasPlayedBefore.put(player, result);
            return result;
        }
    }

    public static String calculateSellPossibleAmount(OfflinePlayer offlinePlayer, ItemStack[] playerInventory,
                                                     ItemStack[] storageInventory, double eachSellPrice, ItemStack itemStack) {

        int possibleCount = 0;
        double buyerBalance;
        if (offlinePlayer == null) {
            buyerBalance = Double.MAX_VALUE;
        } else {
            if (hasPlayedBefore(offlinePlayer)) {
                buyerBalance = Config.useXP ?
                            XPEconomy.getXP(offlinePlayer) :
                            EzChestShop.getEconomy().getBalance(offlinePlayer);
            } else {
                buyerBalance = 0;
            }
        }
        int emptyCount = containerEmptyCount(storageInventory, itemStack);
        int howManyExists = howManyOfItemExists(playerInventory, itemStack);

        for (int num = 0; num < emptyCount; num++) {
            if (possibleCount + 1 > howManyExists) {
                break;
            }
            possibleCount += 1;
        }

        int result = 0;
        for (int num = 0; num < possibleCount; num++) {
            result += 1;
            if ((num + 1) * eachSellPrice > buyerBalance) {
                return String.valueOf(num);
            }
        }

        return String.valueOf(result);
    }

    public static boolean containsAtLeast(Inventory inventory, ItemStack item, int amount) {
        if (item.getType() == Material.FIREWORK_ROCKET) {
            int count = 0;
            for (ItemStack content : inventory.getStorageContents()) {
                if (content == null || content.getType() == Material.AIR) {
                    continue;
                }
                if (isSimilar(content, item)) {
                    count += content.getAmount();
                }
            }
            return count >= amount;
        } else {
            return inventory.containsAtLeast(item, amount);
        }
    }

    /*
    Removes the given ItemStacks from the inventory.

    It will try to remove 'as much as possible' from the types and amounts you give as arguments.

    The returned HashMap contains what it couldn't remove, where the key is the index of the parameter, and the value is the ItemStack at that index of the varargs parameter. If all the given ItemStacks are removed, it will return an empty HashMap.

    It is known that in some implementations this method will also set the inputted argument amount to the number of that item not removed from slots.
     */
    public static HashMap<Integer, ItemStack> removeItem(@NotNull Inventory inventory, @NotNull ItemStack... stacks) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            if (stack.getType() == Material.FIREWORK_ROCKET) {
                int amount = stack.getAmount();
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack item = inventory.getItem(slot);
                    if (item == null || item.getType() == Material.AIR) {
                        continue;
                    }
                    if (isSimilar(item, stack)) {
                        int newAmount = item.getAmount() - amount;
                        if (newAmount > 0) {
                            item.setAmount(newAmount);
                            amount = 0;
                        } else {
                            amount = -newAmount;
                            inventory.setItem(slot, null);
                        }
                    }
                    if (amount <= 0) {
                        break;
                    }
                }
                if (amount > 0) {
                    stack.setAmount(amount);
                    leftover.put(i, stack);
                }
            } else {
                inventory.removeItem(stack);
            }
        }
        return leftover;
    }

    public static boolean isSimilar(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        } else if (stack1 == stack2) {
            return true;
        } else {
            if (stack1.getType() == Material.FIREWORK_ROCKET && stack2.getType() == Material.FIREWORK_ROCKET) {
                FireworkMeta meta1 = (FireworkMeta) stack1.getItemMeta();
                FireworkMeta meta2 = (FireworkMeta) stack2.getItemMeta();
                if (meta1 != null && meta2 != null) {
                    if (meta1.getEffects().size() != meta2.getEffects().size()) {
                        return false;
                    }
                    if (meta1.getPower() != meta2.getPower()) {
                        return false;
                    }
                    for (int i = 0; i < meta1.getEffects().size(); i++) {
                        if (!meta1.getEffects().get(i).equals(meta2.getEffects().get(i))) {
                            return false;
                        }
                    }
                    if (meta1.hasDisplayName() != meta2.hasDisplayName()) {
                        return false;
                    } else if (meta1.hasDisplayName()) {
                        if (!meta1.getDisplayName().equals(meta2.getDisplayName())) {
                            return false;
                        }
                    }

                    if (meta1.hasLore() != meta2.hasLore()) {
                        return false;
                    } else if (meta1.hasLore()) {
                        if (!meta1.getLore().equals(meta2.getLore())) {
                            return false;
                        }
                    }

                    if (meta1.hasCustomModelData() != meta2.hasCustomModelData()) {
                        return false;
                    } else if (meta1.hasCustomModelData()) {
                        if (meta1.getCustomModelData() != meta2.getCustomModelData()) {
                            return false;
                        }
                    }

                    if (meta1.hasEnchants() != meta2.hasEnchants()) {
                        return false;
                    } else if (meta1.hasEnchants()) {
                        if (!meta1.getEnchants().equals(meta2.getEnchants())) {
                            return false;
                        }
                    }

                    if (!meta1.getItemFlags().equals(meta2.getItemFlags())) {
                        return false;
                    }
                    if (meta1.getAttributeModifiers() != null) {
                        if (!meta1.getAttributeModifiers().equals(meta2.getAttributeModifiers())) {
                            return false;
                        }
                    } else if (meta2.getAttributeModifiers() != null) {
                        return false;
                    }

                    if (meta1.isUnbreakable() != meta2.isUnbreakable()) {
                        return false;
                    }
                }
            } else if (!stack1.isSimilar(stack2)) {
                return false;
            }
            return true;
        }
    }

    public static boolean isInteger(String str) {
        try {
            int num = Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getNextRotation(String current) {
        if (current == null)
            current = Config.settings_defaults_rotation;
        int i = rotations.indexOf(current);
        String result = i == rotations.size() - 1 ? rotations.get(0) : rotations.get(i + 1);
        return result;
    }

    public static String getPreviousRotation(String current) {
        if (current == null)
            current = Config.settings_defaults_rotation;
        int i = rotations.indexOf(current);
        String result = i == 0 ? rotations.get(rotations.size() - 1) : rotations.get(i - 1);
        return result;
    }

    /**
     * Apply & color translating, as well as #ffffff hex color encoding to a String.
     * Versions below 1.16 will only get the last hex color symbol applied to them.
     *
     * @param str
     * @return
     */
    public static String colorify(String str) {
        if (str == null)
            return null;
        return translateHexColorCodes("#", "", ChatColor.translateAlternateColorCodes('&', str));
    }

    /**
     * Apply hex color coding to a String. possibility to add a special start or end
     * tag to the String.
     * Versions below 1.16 will only get the last hex color symbol applied to them.
     *
     * @param startTag
     * @param endTag
     * @param message
     * @return
     */
    public static String translateHexColorCodes(String startTag, String endTag, String message) {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        final char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        return matcher.appendTail(buffer).toString();
    }

    public enum FormatType {
        GUI, CHAT, HOLOGRAM
    }

    public static String formatNumber(double number, FormatType type) {
        String result = "Error";
        DecimalFormat decimalFormat;
        switch (type) {
            case GUI:
                decimalFormat = new DecimalFormat(Config.display_numberformat_gui);
                result = decimalFormat.format(number);
                break;
            case CHAT:
                decimalFormat = new DecimalFormat(Config.display_numberformat_chat);
                result = decimalFormat.format(number);
                break;
            case HOLOGRAM:
                decimalFormat = new DecimalFormat(Config.display_numberformat_holo);
                result = decimalFormat.format(number);
                break;
        }
        EzChestShop.logDebug("Formatted number " + number + " to " + result);
        return result;
    }

    public static void sendVersionMessage(Player player) {
        player.spigot().sendMessage(
                new ComponentBuilder("Ez Chest Shop plugin, " + EzChestShop.getPlugin().getDescription().getVersion())
                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append("\nSpigot: ").color(net.md_5.bungee.api.ChatColor.GOLD).append("LINK")
                        .color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(colorify("&fClick to open the plugins Spigot page!"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                "https://www.spigotmc.org/resources/ez-chest-shop-ecs-1-14-x-1-17-x.90411/"))
                        .append("\nGitHub: ", ComponentBuilder.FormatRetention.NONE)
                        .color(net.md_5.bungee.api.ChatColor.RED).append("LINK")
                        .color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(
                                        colorify("&fClick to check out the plugins\n Open Source GitHub repository!"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/ItzAmirreza/EzChestShop"))
                        .create());
    }

    public static PersistentDataContainer getDataContainer(Block block) {
        PersistentDataContainer dataContainer = null;
        TileState state = (TileState) block.getState();
        Inventory inventory = Utils.getBlockInventory(block);

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            if (inventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                Chest chestleft = (Chest) doubleChest.getLeftSide();
                Chest chestright = (Chest) doubleChest.getRightSide();

                if (!chestleft.getPersistentDataContainer().isEmpty()) {
                    dataContainer = chestleft.getPersistentDataContainer();
                } else {
                    dataContainer = chestright.getPersistentDataContainer();
                }
            } else {
                dataContainer = state.getPersistentDataContainer();
            }
        } else if (block.getType() == Material.BARREL) {
            dataContainer = state.getPersistentDataContainer();
        } else if (Utils.isShulkerBox(block.getType())) {
            dataContainer = state.getPersistentDataContainer();
        }
        return dataContainer;
    }

    public static String getDiscordLink() {
        if (discordLink == null) {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(
                        "https://api.spiget.org/v2/resources/90411").openConnection();
                connection.setRequestMethod("GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    if (inputLine.contains("RGlzY29yZCBTZXJ2ZXI=")) {
                        inputLine = inputLine.replace("\"RGlzY29yZCBTZXJ2ZXI=\": \"", "").replace("\"", "")
                                .replace(",", "").trim();
                        discordLink = inputLine;
                        break;
                    }
                }
            } catch (Exception e) {
                discordLink = "https://discord.gg/rSfsqgCqBZ"; // Default discord Link if not found!
            }
        }
        return discordLink;

    }

    public static void recognizeDatabase() {
        if (Config.database_type == Database.SQLITE) {
            EzChestShop.logConsole("&c[&eEzChestShop&c] &eInitializing SQLite database...");
            //initialize SQLite
            databaseManager = new SQLite(EzChestShop.getPlugin());
            databaseManager.load();
            EzChestShop.logConsole("&c[&eEzChestShop&c] &aSQLite &7database initialized!");

        } else if (Config.database_type == Database.MYSQL) {
            EzChestShop.logConsole("&c[&eEzChestShop&c] &eInitializing MySQL database...");
            //initialize MySQL
            databaseManager = new MySQL(EzChestShop.getPlugin());
            databaseManager.load();
            EzChestShop.logConsole("&c[&eEzChestShop&c] &aMySQL &7database initialized!");
        } else {
            //shouldn't happen technically
        }
    }

    public static void addItemIfEnoughSlots (Gui gui,int slot, GuiItem item){
        if ((gui.getRows() * 9) > slot) {
            gui.setItem(slot, item);
        }
    }

    public static void addItemIfEnoughSlots (PaginatedGui gui,int slot, GuiItem item){
        if ((gui.getRows() * 9) > slot) {
            gui.setItem(slot, item);
        }
    }

    public static EzShop isPartOfTheChestShop(Location location) {

        Block block = location.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
                Chest left = (Chest) doubleChest.getLeftSide();
                Chest right = (Chest) doubleChest.getRightSide();
                //check if either of the chests is a shop
                if (ShopContainer.isShop(left.getLocation()) || ShopContainer.isShop(right.getLocation())) {
                    //return the part that is a shop
                    if (ShopContainer.isShop(left.getLocation())) {
                        return ShopContainer.getShop(left.getLocation());
                    } else {
                        return ShopContainer.getShop(right.getLocation());
                    }
                }
            } else {
                return null;
            }
        }
        return null;
    }


    public static List<UUID> getAdminsForShop(EzShop shop) {
        List<UUID> admins = new ArrayList<>();
        admins.add(shop.getOwnerID());
        String adminsString = shop.getSettings().getAdmins();
        if (adminsString == null) {
            return admins;
        }
        String[] adminList = adminsString.split("@");
        for (String admin : adminList) {
            if (!admin.equalsIgnoreCase("") && !admin.equalsIgnoreCase(" ") && !admin.equalsIgnoreCase("null") && !admin.equalsIgnoreCase("NULL")) {
                //check if its a valid uuid
                boolean isValid = true;
                try {
                    UUID.fromString(admin);
                } catch (IllegalArgumentException exception) {
                    isValid = false;
                }
                if (isValid) {
                    admins.add(UUID.fromString(admin));
                }
            }
        }

        return admins;
    }

    public static List<Block> getNearbyEmptyShopForAdmins(Player player) {
        List<Block> emptyShops = new ArrayList<>();
        //We gonna check the area the maximum of 5 chunks away from the player
        //We gonna check if the shop is for the owner or its admins
        //We gonna check if the shop is empty
        //We gonna check if the shop inventory has at least 1 item required for the shop
        //We gonna check if the buy is enabled for the shop (Basically if shop owner is selling)

        //first we get the shops
        List<EzShop> shops = ShopContainer.getShops();
        //then we check if the shop is for the owner or its admins
        for (EzShop shop : shops) {
            if (shop.getSettings().isDbuy()) {
                continue;
            }
            //new check for admin shops
            if (shop.getSettings().isAdminshop()) {
                continue;
            }

            if (shop.getOwnerID().equals(player.getUniqueId()) || getAdminsForShop(shop).contains(player.getUniqueId())) {
                //then we check if the shop is empty

                if (shop.getLocation() == null || shop.getLocation().getWorld() == null) {
                    continue;
                }

                if (!Utils.isApplicableContainer(shop.getLocation().getBlock())) {
                    continue;
                }

                if (Utils.getBlockInventory(shop.getLocation().getBlock()) == null) {
                    continue;
                }

                if (Utils.getBlockInventory(shop.getLocation().getBlock()).isEmpty()) {

                    //then we check if the shop is in the area
                    if (shop.getLocation().getWorld().equals(player.getWorld())) {
                        if (shop.getLocation().distance(player.getLocation()) <= 80) {
                            emptyShops.add(shop.getLocation().getBlock());
                        }
                    }
                } else {
                    //then we check if the shop inventory has at least 1 item required for the shop
                    ItemStack shopItem = shop.getShopItem().clone();
                    Inventory inventory = Utils.getBlockInventory(shop.getLocation().getBlock());
                    if (containsAtLeast(inventory, shopItem, 1)) {
                        continue;
                    }

                    //then we check if the shop is in the area
                    if (shop.getLocation().getWorld().equals(player.getWorld())) {
                        if (shop.getLocation().distance(player.getLocation()) <= 80) {
                            emptyShops.add(shop.getLocation().getBlock());
                        }
                    }
                }
            }
        }

        return emptyShops;
    }

    public static void sendActionBar(Player player, String message) {
        // Apply color codes to the message using ChatColor.translateAlternateColorCodes
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(coloredMessage));
    }

    public static boolean reInstallNamespacedKeyValues(PersistentDataContainer container, Location containerLocation) {

        EzShop shop = ShopContainer.getShop(containerLocation);
        if (shop == null) {
            return false; //false means the shop doesn't even exist in the database, so we don't need to do anything and send the message
        }

        container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, shop.getOwnerID().toString());
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE, shop.getBuyPrice());
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE, shop.getSellPrice());
        //add new settings data later
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, shop.getSettings().isMsgtoggle() ? 1 : 0);
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, shop.getSettings().isDbuy() ?
                (shop.getBuyPrice() == 0 ? 1 : (Config.settings_defaults_dbuy ? 1 : 0))
                : (Config.settings_defaults_dbuy ? 1 : 0));
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, shop.getSettings().isDsell() ?
                (shop.getSellPrice() == 0 ? 1 : (Config.settings_defaults_dsell ? 1 : 0))
                : (Config.settings_defaults_dsell ? 1 : 0));
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, shop.getSettings().getAdmins());
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, shop.getSettings().isShareincome() ? 1 : 0);
        //container.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, shop.getSettings().isAdminshop() ? 1 : 0);
        container.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, shop.getSettings().getRotation());

        return true;

    }


//    public static Object getRequestedData(Contain) {
//
//    }

}
