package me.deadlight.ezchestshop.utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.data.*;
import me.deadlight.ezchestshop.data.mysql.MySQL;
import me.deadlight.ezchestshop.data.sqlite.SQLite;
import me.deadlight.ezchestshop.enums.Database;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.objects.EzTradeShop;
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

    private static String discordLink;

    public static VersionUtils versionUtils;
    public static DatabaseManager databaseManager;

    static {
        try {
            String packageName = Utils.class.getPackage().getName();
            String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            versionUtils = (VersionUtils) Class.forName(packageName + "." + internalsName).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | ClassCastException exception) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "EzChestShop could not find a valid implementation for this server version.");
        }
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



    public static void sendVersionMessage(Player player) {
        player.spigot().sendMessage(
                new ComponentBuilder("Ez Chest Shop plugin, " + EzChestShop.getPlugin().getDescription().getVersion())
                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append("\nSpigot: ").color(net.md_5.bungee.api.ChatColor.GOLD).append("LINK")
                        .color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(StringUtils.colorify("&fClick to open the plugins Spigot page!"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                "https://www.spigotmc.org/resources/ez-chest-shop-ecs-1-14-x-1-17-x.90411/"))
                        .append("\nGitHub: ", ComponentBuilder.FormatRetention.NONE)
                        .color(net.md_5.bungee.api.ChatColor.RED).append("LINK")
                        .color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText(
                                        StringUtils.colorify("&fClick to check out the plugins\n Open Source GitHub repository!"))))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/ItzAmirreza/EzChestShop"))
                        .create());
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

    public static EzTradeShop isPartOfTheChestTradeShop(Location location) {

        Block block = location.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) block.getState();
            if (chest.getInventory().getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
                Chest left = (Chest) doubleChest.getLeftSide();
                Chest right = (Chest) doubleChest.getRightSide();
                //check if either of the chests is a shop
                if (TradeShopContainer.isTradeShop(left.getLocation()) || TradeShopContainer.isTradeShop(right.getLocation())) {
                    //return the part that is a shop
                    if (TradeShopContainer.isTradeShop(left.getLocation())) {
                        return TradeShopContainer.getTradeShop(left.getLocation());
                    } else {
                        return TradeShopContainer.getTradeShop(right.getLocation());
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

                if (!BlockMaterialUtils.isApplicableContainer(shop.getLocation().getBlock())) {
                    continue;
                }

                if (BlockMaterialUtils.getBlockInventory(shop.getLocation().getBlock()) == null) {
                    continue;
                }

                if (BlockMaterialUtils.getBlockInventory(shop.getLocation().getBlock()).isEmpty()) {

                    //then we check if the shop is in the area
                    if (shop.getLocation().getWorld().equals(player.getWorld())) {
                        if (shop.getLocation().distance(player.getLocation()) <= 80) {
                            emptyShops.add(shop.getLocation().getBlock());
                        }
                    }
                } else {
                    //then we check if the shop inventory has at least 1 item required for the shop
                    ItemStack shopItem = shop.getShopItem().clone();
                    Inventory inventory = BlockMaterialUtils.getBlockInventory(shop.getLocation().getBlock());
                    if (InventoryUtils.containsAtLeast(inventory, shopItem, 1)) {
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
}
