package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Pair;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerCloseToChestListener implements Listener {

    private static LanguageManager lm = new LanguageManager();
    public static HashMap<Player, List<Pair<Location, ASHologram>>> HoloTextMap = new HashMap<>();
    public static HashMap<Player, List<Pair<Location, FloatingItem>>> HoloItemMap = new HashMap<>();

    private static HashMap<Location, List<Player>> playershopmap = new HashMap<>();
    private static HashMap<Location, List<Player>> playershopTextmap = new HashMap<>();
    private static HashMap<UUID, Block> lastContainermap = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (Config.showholo) {
            boolean alreadyRenderedHologram = false;
            Player player = event.getPlayer();
            if (Config.holodistancing_show_item_first) {
                RayTraceResult result = player.rayTraceBlocks(5);
                if (result != null) {
                    Block target = result.getHitBlock();
                    Location loc = target.getLocation();
                    if (Utils.isApplicableContainer(target)) {
                        Inventory inventory = Utils.getBlockInventory(target);
                        if (inventory instanceof DoubleChestInventory) {
                            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                            Chest leftchest = (Chest) doubleChest.getLeftSide();
                            Chest rightchest = (Chest) doubleChest.getRightSide();

                            if (leftchest.getPersistentDataContainer().has(
                                    new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                                target = leftchest.getBlock();
                                loc = leftchest.getLocation();
                            } else if (rightchest.getPersistentDataContainer().has(
                                    new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                                target = rightchest.getBlock();
                                loc = rightchest.getLocation();
                            }
                        }
                        if (!isAlreadyPresentingText(loc, player)) {
                            if (ShopContainer.isShop(loc)) {
                                lastContainermap.put(player.getUniqueId(), target);
                                PersistentDataContainer container = ((TileState) target.getState())
                                        .getPersistentDataContainer();
//                                if (Utils.validateContainerValues(container, ShopContainer.getShop(loc))) {
//                                    EzChestShop.logConsole(
//                                            "[ECS] Something unexpected happened with this container's data, so this shop has been removed.");
//                                    return;
//                                }
                                Location holoLoc = getHoloLoc(target);
                                ItemStack thatItem = Utils
                                        .decodeItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                                "item"), PersistentDataType.STRING));

                                double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                                        PersistentDataType.DOUBLE);
                                double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                                        PersistentDataType.DOUBLE);
                                boolean is_adminshop = container.get(
                                        new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                        PersistentDataType.INTEGER) == 1;
                                boolean is_dbuy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"),
                                        PersistentDataType.INTEGER) == 1;
                                boolean is_dsell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"),
                                        PersistentDataType.INTEGER) == 1;

                                OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID
                                        .fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"),
                                                PersistentDataType.STRING)));
                                String shopOwner = offlinePlayerOwner.getName();
                                if (shopOwner == null) {
                                    shopOwner = ChatColor.RED + "Error";
                                }

                                // showHologramText(holoLoc, loc, thatItem, buy, sell, player, is_adminshop,
                                // shopOwner);
                                showHologramTextOnHover(holoLoc, loc, thatItem, buy, sell, player, is_adminshop,
                                        shopOwner, is_dbuy, is_dsell, player.isSneaking());
                                alreadyRenderedHologram = true;
                            } else {
                                if (Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always) {
                                    rerenderHologramText(player);
                                } else {
                                    hideHologramText(player, false);
                                }
                            }
                        }
                    } else {
                        if (Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always) {
                            rerenderHologramText(player);
                        } else {
                            hideHologramText(player, false);
                        }
                    }
                } else {
                    if (Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always) {
                        rerenderHologramText(player);
                    } else {
                        hideHologramText(player, false);
                    }
                }
            }

            if (!hasMovedXYZ(event) || alreadyRenderedHologram) {
                return;
            }

            Location loc = player.getLocation();
            ShopContainer.getShops().stream()
                    .filter(ezShop -> ezShop.getLocation() != null
                            && loc.getWorld().equals(ezShop.getLocation().getWorld())
                            && loc.distance(ezShop.getLocation()) < Config.holodistancing_distance + 5)
                    .forEach(ezShop -> {
                        if (EzChestShop.slimefun) {
                            if (BlockStorage.hasBlockInfo(ezShop.getLocation())) {
                                ShopContainer.deleteShop(ezShop.getLocation());
                                return;
                            }
                        }
                        double dist = loc.distance(ezShop.getLocation());
                        // Show the Hologram if Player close enough
                        if (dist < Config.holodistancing_distance) {
                            if (isAlreadyPresenting(ezShop.getLocation(), player))
                                return; // forEach -> acts as continue;

                            Block target = ezShop.getLocation().getWorld().getBlockAt(ezShop.getLocation());
                            if (target != null) {
                                if (Utils.isApplicableContainer(target)) {
                                    PersistentDataContainer container = ((TileState) target.getState())
                                            .getPersistentDataContainer();
//                                    if (Utils.validateContainerValues(container,
//                                            ShopContainer.getShop(target.getLocation()))) {
//                                        EzChestShop.logConsole(
//                                                "[ECS] Something unexpected happened with this container's data, so this shop has been removed.");
//                                        return;
//                                    }
                                    Location holoLoc = getHoloLoc(target);

                                    ItemStack thatItem = Utils
                                            .decodeItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                                    "item"), PersistentDataType.STRING));

                                    double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                                            PersistentDataType.DOUBLE);
                                    double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                                            PersistentDataType.DOUBLE);
                                    boolean is_adminshop = container.get(
                                            new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                            PersistentDataType.INTEGER) == 1;
                                    boolean is_dbuy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"),
                                            PersistentDataType.INTEGER) == 1;
                                    boolean is_dsell = container.get(
                                            new NamespacedKey(EzChestShop.getPlugin(), "dsell"),
                                            PersistentDataType.INTEGER) == 1;

                                    OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(
                                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"),
                                                    PersistentDataType.STRING)));
                                    String shopOwner = offlinePlayerOwner.getName();
                                    if (shopOwner == null) {
                                        shopOwner = ChatColor.RED + "Error";
                                    }

                                    showHologram(holoLoc, ezShop.getLocation(), thatItem, buy, sell, event.getPlayer(),
                                            is_adminshop, shopOwner, is_dbuy, is_dsell);
                                    // mark the shop as visible
                                    List<Player> players = playershopmap.get(ezShop.getLocation());
                                    if (players == null)
                                        players = new ArrayList<>();
                                    players.add(player);
                                    playershopmap.put(ezShop.getLocation(), players);

                                }
                            }

                        }
                        // Hide the Hologram that is too far away from the player
                        else if (dist > Config.holodistancing_distance + 1
                                && dist < Config.holodistancing_distance + 3) {
                            // Hide the Hologram - requires spawn Position of the Hologram and Position of
                            // the Shop
                            hideHologram(player, ezShop.getLocation(), true);
                        }
                    });
        }

        //This section is for outlines of emptyshops
//        if (Utils.enabledOutlines.contains(event.getPlayer().getUniqueId())) {
//
//            Not sure if implementing this will lag the big servers, so for now I'm not going to add it ...
//
//        }

    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc.getLocation() != null && loc.getWorld().equals(sloc.getLocation().getWorld())
                        && loc.distance(sloc.getLocation()) < Config.holodistancing_distance + 5)
                .forEach(sloc -> {
                    hideHologram(player, sloc.getLocation(), true);
                });
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getFrom();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc.getLocation() != null && loc.getWorld().equals(sloc.getLocation().getWorld())
                        && loc.distance(sloc.getLocation()) < Config.holodistancing_distance + 5)
                .forEach(sloc -> {
                    hideHologram(player, sloc.getLocation(), true);
                });
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        RayTraceResult result = player.rayTraceBlocks(5);
        if (result != null) {
            Block target = result.getHitBlock();
            Location loc = target.getLocation();
            if (Utils.isApplicableContainer(target)) {
                Inventory inventory = ((Container) target.getState()).getInventory();
                if (inventory instanceof DoubleChestInventory) {
                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                    Chest leftchest = (Chest) doubleChest.getLeftSide();
                    Chest rightchest = (Chest) doubleChest.getRightSide();

                    if (leftchest.getPersistentDataContainer().has(
                            new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                        target = leftchest.getBlock();
                        loc = leftchest.getLocation();
                    } else if (rightchest.getPersistentDataContainer().has(
                            new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                        target = rightchest.getBlock();
                        loc = rightchest.getLocation();
                    }
                }
                if (!ShopContainer.isShop(loc)) return;
                hideHologramText(player, loc, true);
                PersistentDataContainer container = ((TileState) target.getState())
                        .getPersistentDataContainer();
//                if (Utils.validateContainerValues(container, ShopContainer.getShop(loc))) {
//                    EzChestShop.logConsole(
//                            "[ECS] Something unexpected happened with this container's data, so this shop has been removed.");
//                    return;
//                }
                Location holoLoc = getHoloLoc(target);
                ItemStack thatItem = Utils
                        .decodeItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                "item"), PersistentDataType.STRING));

                double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                        PersistentDataType.DOUBLE);
                double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                        PersistentDataType.DOUBLE);
                boolean is_adminshop = container.get(
                        new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                        PersistentDataType.INTEGER) == 1;
                boolean is_dbuy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"),
                        PersistentDataType.INTEGER) == 1;
                boolean is_dsell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"),
                        PersistentDataType.INTEGER) == 1;

                OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID
                        .fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"),
                                PersistentDataType.STRING)));
                String shopOwner = offlinePlayerOwner.getName();
                if (shopOwner == null) {
                    shopOwner = ChatColor.RED + "Error";
                }

                lastContainermap.put(player.getUniqueId(), target);
                // showHologramText(holoLoc, loc, thatItem, buy, sell, player, is_adminshop,
                // shopOwner);
                showHologramTextOnHover(holoLoc, loc, thatItem, buy, sell, player, is_adminshop,
                        shopOwner, is_dbuy, is_dsell, event.isSneaking());
            }
        }
    }

    private void showHologram(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
            double sell, Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell) {

        showHologramItem(spawnLocation, shopLocation, thatItem, player, is_adminshop);
        showHologramText(spawnLocation, shopLocation, thatItem, buy, sell, player, is_adminshop, shop_owner,
                is_dbuy, is_dsell, player.isSneaking());

    }

    private void showHologramText(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
            double sell, Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell, boolean is_sneaking) {
        if (Config.holodistancing_show_item_first && !(Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always)) {
            return;
        }
        List<Pair<Location, ASHologram>> holoTextList = HoloTextMap.containsKey(player) ? HoloTextMap.get(player)
                : new ArrayList<>();
        // Count the amount of Holograms at the shopLocation
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        List<String> structure = new ArrayList<>(is_adminshop ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(shopLocation).getRotation().equals("down"))
            Collections.reverse(structure);
        int lines = structure.stream().filter(s -> s.startsWith("<itemdata") && !s.startsWith("<itemdataRest")).collect(Collectors.toList()).size();
        int i = 0;
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                String line = insertPlaceholders(element, thatItem, player, shopLocation, buy, sell, shop_owner);
                // Show only custom messages if item first and show messages always is true. Everything else is skipped.
                if (Config.holodistancing_show_item_first && !(Config.settings_hologram_message_show_empty_shop_always && line.contains("<emptyShopInfo/>"))) {
                    if (!((Config.settings_hologram_message_show_always && line.startsWith("<custom")))) {
                        continue;
                    }
                }
                if ((is_dbuy || is_dsell) && (line.contains("<buy>") || line.contains("<sell>"))) {
                    line = line.replaceAll("<separator>.*?<\\/separator>", "");
                    if (is_dbuy && is_dsell) {
                        if (i > 0) {
                            line = lm.disabledButtonTitle();
                        }
                    } else if (is_dbuy) {
                        line = line.replaceAll("<buy>.*?<\\/buy>", "").replaceAll("<sell>|<\\/sell>", "");
                    } else if (is_dsell) {
                        line = line.replaceAll("<sell>.*?<\\/sell>", "").replaceAll("<buy>|<\\/buy>", "");
                    }
                } else {
                    line = line.replaceAll("<separator>|<\\/separator>", "").replaceAll("<buy>|<\\/buy>", "")
                            .replaceAll("<sell>|<\\/sell>", "");
                }
                if (line.startsWith("<custom")) {
                    if (Config.settings_hologram_message_enabled) {
                        int customNum = Integer.parseInt(line.replaceAll("\\D", ""));
                        List<String> customMessages = ShopContainer.getShopSettings(shopLocation).getCustomMessages();

                        if (customNum > customMessages.size()) continue;
                        line = Utils.colorify(customMessages.get(customNum - 1));
                    } else {
                        continue;
                    }
                }
                if (line.startsWith("<itemdata")) {
                    if (is_sneaking) {
                        ItemStack item = ShopContainer.getShop(shopLocation).getShopItem();
                        if (item.getType().name().contains("SHULKER_BOX") || item.getEnchantments().size() > 0 ||
                                (item.getType() == Material.ENCHANTED_BOOK
                                && ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().size() > 1)) {
                            try {
                                int lineNum = Integer.parseInt(line.replaceAll("\\D", ""));
                                line = getHologramItemData(lineNum, item, lines);
                            } catch (NumberFormatException e) {
                                line = getHologramItemData(-1, item, lines);
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (line.contains("<emptyShopInfo/>")) {
                    EzShop shop = ShopContainer.getShop(shopLocation);
                    // Shops that are not selling anything should not show this message.
                    if (player.getUniqueId().equals(shop.getOwnerID()) && !is_dbuy && !is_adminshop) {
                        // Check if the shop is empty by getting the inventory of the block
                        Inventory inv = Utils.getBlockInventory(shopLocation.getBlock());
                        if (!inv.containsAtLeast(shop.getShopItem(), 1)) {
                            line = line.replace("<emptyShopInfo/>", lm.emptyShopHologramInfo());
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (line.trim().equals(""))
                    continue;
                if (!line.equals("<empty/>")) {
                    ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                    Utils.onlinePackets.add(hologram);
                    holoTextList.add(Pair.of(shopLocation, hologram));
                }
                i++;
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }
        // FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        // Save the Hologram Data so it can be removed later
        HoloTextMap.put(player, holoTextList);
    }

    private void showHologramTextOnHover(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
            double sell, Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell, boolean is_sneaking) {

        List<Pair<Location, ASHologram>> holoTextList = HoloTextMap.containsKey(player) ? HoloTextMap.get(player)
                : new ArrayList<>();

        if (!holoTextList.isEmpty()) {
            hideHologramText(player, shopLocation, true);
            holoTextList = HoloTextMap.get(player);
        }

        if (!HoloItemMap.containsKey(player) ||
                HoloItemMap.get(player).stream().filter(s -> s.getFirst().equals(shopLocation)).collect(Collectors.toList()).size() == 0) {
            showHologramItem(spawnLocation, shopLocation, thatItem, player, is_adminshop);
        }

        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        List<String> structure = new ArrayList<>(is_adminshop ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(shopLocation).getRotation().equals("down"))
            Collections.reverse(structure);
        int lines = structure.stream().filter(s -> s.startsWith("<itemdata") && !s.startsWith("<itemdataRest")).collect(Collectors.toList()).size();
        int i = 0;
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                String line = insertPlaceholders(element, thatItem, player, shopLocation, buy, sell, shop_owner);
                if (is_dbuy || is_dsell) {
                    line = line.replaceAll("<separator>.*?<\\/separator>", "");
                    if (is_dbuy && is_dsell) {
                        if (i > 0) {
                            continue;
                        }
                        line = lm.disabledButtonTitle();
                    } else if (is_dbuy) {
                        line = line.replaceAll("<buy>.*?<\\/buy>", "").replaceAll("<sell>|<\\/sell>", "");
                    } else if (is_dsell) {
                        line = line.replaceAll("<sell>.*?<\\/sell>", "").replaceAll("<buy>|<\\/buy>", "");
                    }
                } else {
                    line = line.replaceAll("<separator>|<\\/separator>", "").replaceAll("<buy>|<\\/buy>", "")
                            .replaceAll("<sell>|<\\/sell>", "");
                }
                if (line.startsWith("<custom")) {
                    if (Config.settings_hologram_message_enabled) {
                        int customNum = Integer.parseInt(line.replaceAll("\\D", ""));
                        List<String> customMessages = ShopContainer.getShopSettings(shopLocation).getCustomMessages();

                        if (customNum > customMessages.size()) continue;
                        line = Utils.colorify(customMessages.get(customNum - 1));
                    } else {
                        continue;
                    }
                }
                if (line.startsWith("<itemdata")) {
                    if (is_sneaking) {
                        ItemStack item = ShopContainer.getShop(shopLocation).getShopItem();
                        if (item.getType().name().contains("SHULKER_BOX") || item.getEnchantments().size() > 0 ||
                                (item.getType() == Material.ENCHANTED_BOOK
                                        && ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().size() > 1)) {
                            try {
                                int lineNum = Integer.parseInt(line.replaceAll("\\D", ""));
                                line = getHologramItemData(lineNum, item, lines);
                            } catch (NumberFormatException e) {
                                line = getHologramItemData(-1, item, lines);
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (line.contains("<emptyShopInfo/>")) {
                    EzShop shop = ShopContainer.getShop(shopLocation);
                    // Shops that are not selling anything should not show this message.
                    if (player.getUniqueId().equals(shop.getOwnerID()) && !is_dbuy && !is_adminshop) {
                        // Check if the shop is empty by getting the inventory of the block
                        Inventory inv = Utils.getBlockInventory(shopLocation.getBlock());
                        if (!inv.containsAtLeast(shop.getShopItem(), 1)) {
                            line = line.replace("<emptyShopInfo/>", lm.emptyShopHologramInfo());
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                if (line.trim().equals(""))
                    continue;
                if (!line.equals("<empty/>")) {
                    ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                    Utils.onlinePackets.add(hologram);
                    holoTextList.add(Pair.of(shopLocation, hologram));
                }
                i++;
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }

        List<Player> players = playershopTextmap.get(shopLocation);
        if (players == null)
            players = new ArrayList<>();
        players.add(player);
        playershopTextmap.put(shopLocation, players);

        // Save the Hologram Data so it can be removed later
        HoloTextMap.put(player, holoTextList);

    }

    public String insertPlaceholders(String rawText, ItemStack thatItem, Player player, Location shopLocation, double buy, double sell, String shop_owner) {
        String itemName = Utils.getFinalItemName(thatItem);
        List<String> possibleCounts = Utils.calculatePossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()),
                Bukkit.getOfflinePlayer(
                        UUID.fromString(((TileState) shopLocation.getBlock().getState()).getPersistentDataContainer()
                                .get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
                player.getInventory().getStorageContents(),
                Utils.getBlockInventory(shopLocation.getBlock()).getStorageContents(),
                buy, sell, thatItem);
        String line = Utils.colorify(rawText.replace("%item%", itemName)
                .replace("%buy%", Utils.formatNumber(buy, Utils.FormatType.HOLOGRAM))
                .replace("%sell%", Utils.formatNumber(sell, Utils.FormatType.HOLOGRAM))
                .replace("%currency%", Config.currency)
                .replace("%owner%", shop_owner).replace("%maxbuy%", possibleCounts.get(0))
                .replace("%maxsell%", possibleCounts.get(1))
                .replace("%maxStackSize%", thatItem.getMaxStackSize() + "")
        );
        return line;
    }

    private void showHologramItem(Location spawnLocation, Location shopLocation, ItemStack thatItem, Player player,
            boolean is_adminshop) {
        if (HoloItemMap.containsKey(player) &&
                HoloItemMap.get(player).stream().filter(s -> s.getFirst().equals(shopLocation)).collect(Collectors.toList()).size() != 0) {
            return;
        }
        List<Pair<Location, FloatingItem>> holoItemList = HoloItemMap.containsKey(player) ? HoloItemMap.get(player)
                : new ArrayList<>();
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        List<String> structure = new ArrayList<>(is_adminshop ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(shopLocation).getRotation().equals("down"))
            Collections.reverse(structure);
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                FloatingItem floatingItem = new FloatingItem(player, thatItem, lineLocation);
                Utils.onlinePackets.add(floatingItem);
                holoItemList.add(Pair.of(shopLocation, floatingItem));
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }
        // FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        // Save the Hologram Data so it can be removed later
        HoloItemMap.put(player, holoItemList);
    }

    public static void hideHologram(Location loc, boolean force) {
        if (Config.holodistancing) {
            Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(loc.getWorld()))
                    .filter(p -> p.getLocation().distance(loc) < Config.holodistancing_distance + 5).forEach(p -> {
                        hideHologram(p, loc, force);
                        if (Config.holo_rotation) {
                            List<Player> players = playershopTextmap.get(loc);
                            if (players != null) {
                                players.remove(p);
                                if (players.isEmpty()) {
                                    playershopTextmap.remove(loc);
                                } else {
                                    playershopTextmap.put(loc, players);
                                }
                            } else {
                                playershopTextmap.remove(loc);
                            }
                        }
                    });
        }
    }

    public static void hideHologram(Player p, Location loc, boolean force) {
        if (HoloTextMap.containsKey(p) && HoloTextMap.get(p).size() > 0) {
            new ArrayList<>(HoloTextMap.get(p)).stream().filter(holo -> loc.equals(holo.getFirst()))
                    .forEach(holo -> {
                        if ((Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always) && !force && ShopContainer.getShopSettings(holo.getFirst()).getCustomMessages().stream().map(s -> Utils.colorify(s))
                                .collect(Collectors.toList()).contains(holo.getSecond().getName())) {
                            return;
                        }
                        holo.getSecond().destroy();
                        Utils.onlinePackets.remove(holo.getSecond());

                        List<Player> players = playershopmap.get(loc);
                        if (players != null) {
                            players.remove(p);
                            if (players.isEmpty()) {
                                playershopmap.remove(loc);
                            } else {
                                playershopmap.put(loc, players);
                            }
                        } else {
                            playershopmap.remove(loc);
                        }

                        List<Pair<Location, ASHologram>> holoList = HoloTextMap.get(p);
                        holoList.remove(holo);
                        HoloTextMap.put(p, holoList);
                    });
        }
        if (HoloItemMap.containsKey(p) && HoloItemMap.get(p).size() > 0) {
            new ArrayList<>(HoloItemMap.get(p)).stream().filter(holo -> loc.equals(holo.getFirst()))
                    .forEach(holo -> {
                        holo.getSecond().destroy();
                        Utils.onlinePackets.remove(holo.getSecond());

                        List<Player> players = playershopmap.get(loc);
                        if (players != null) {
                            players.remove(p);
                            if (players.isEmpty()) {
                                playershopmap.remove(loc);
                            } else {
                                playershopmap.put(loc, players);
                            }
                        } else {
                            playershopmap.remove(loc);
                        }
                        List<Pair<Location, FloatingItem>> holoList = HoloItemMap.get(p);
                        holoList.remove(holo);
                        HoloItemMap.put(p, holoList);
                    });
        }
    }

    private void hideHologramText(Player player, boolean force) {
        if (HoloTextMap.containsKey(player) && HoloTextMap.get(player).size() > 0) {
            new ArrayList<>(HoloTextMap.get(player)).stream()
                    .forEach(holo -> {
                        List<String> holos = ShopContainer.getShopSettings(holo.getFirst()).getCustomMessages().stream().map(s -> Utils.colorify(s))
                                .collect(Collectors.toList());
                        if ((Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always) && !force
                                && (holos.contains(holo.getSecond().getName()) || ChatColor.stripColor(lm.emptyShopHologramInfo()).equals(ChatColor.stripColor(holo.getSecond().getName())))) {
                            return;
                        }
                        holo.getSecond().destroy();
                        Utils.onlinePackets.remove(holo.getSecond());
                        Location loc = holo.getFirst();
                        List<Player> players = playershopTextmap.get(loc);
                        if (players != null) {
                            players.remove(player);
                            if (players.isEmpty()) {
                                playershopTextmap.remove(loc);
                            } else {
                                playershopTextmap.put(loc, players);
                            }
                        } else {
                            playershopTextmap.remove(loc);
                        }
                        List<Pair<Location, ASHologram>> holoList = HoloTextMap.get(player);
                        holoList.remove(holo);
                        HoloTextMap.put(player, holoList);
                    });

        }
    }

    // Hide the Hologram text of a specific Location
    private void  hideHologramText(Player player, Location loc, boolean force) {
        if (HoloTextMap.containsKey(player) && HoloTextMap.get(player).size() > 0) {
            new ArrayList<>(HoloTextMap.get(player)).stream().filter(holo -> loc.equals(holo.getFirst()))
                    .forEach(holo -> {
                        List<String> holos = ShopContainer.getShopSettings(holo.getFirst()).getCustomMessages().stream().map(s -> Utils.colorify(s))
                                .collect(Collectors.toList());
                        if ((Config.settings_hologram_message_show_always || Config.settings_hologram_message_show_empty_shop_always) && !force
                                && (holos.contains(holo.getSecond().getName()) || ChatColor.stripColor(lm.emptyShopHologramInfo()).equals(ChatColor.stripColor(holo.getSecond().getName())))) {
                            return;
                        }
                        holo.getSecond().destroy();
                        Utils.onlinePackets.remove(holo.getSecond());
                        Location loc2 = holo.getFirst();
                        List<Player> players = playershopTextmap.get(loc2);
                        if (players != null) {
                            players.remove(player);
                            if (players.isEmpty()) {
                                playershopTextmap.remove(loc2);
                            } else {
                                playershopTextmap.put(loc2, players);
                            }
                        } else {
                            playershopTextmap.remove(loc2);
                        }
                        List<Pair<Location, ASHologram>> holoList = HoloTextMap.get(player);
                        holoList.remove(holo);
                        HoloTextMap.put(player, holoList);
                    });
        }
    }

    private void rerenderHologramText(Player player) {
        UUID uuid = player.getUniqueId();
        if (lastContainermap.get(uuid) != null) {
            Block b = lastContainermap.get(uuid);
            EzShop shop = ShopContainer.getShop(b.getLocation());
            if (shop == null) {
                hideHologramText(player, true);
                lastContainermap.put(uuid, null);
            } else {
                hideHologramText(player, b.getLocation(), true);
                lastContainermap.put(uuid, null);
                showHologramText(getHoloLoc(b), shop.getLocation(), shop.getShopItem(), shop.getBuyPrice(), shop.getSellPrice(), player,
                        shop.getSettings().isAdminshop(), shop.getOwnerID().toString(), shop.getSettings().isDbuy(), shop.getSettings().isDsell(), player.isSneaking());
            }
        } else {
            lastContainermap.put(uuid, null);
            hideHologramText(player, false);
        }
    }

    private boolean isAlreadyPresenting(Location location, Player player) {

        if (playershopmap.containsKey(location)) {

            if (playershopmap.get(location).contains(player)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }

    }

    private boolean isAlreadyPresentingText(Location location, Player player) {

        if (playershopTextmap.containsKey(location)) {

            if (playershopTextmap.get(location).contains(player)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }

    }

    private boolean hasMovedXYZ(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX())
            return true;
        if (from.getY() != to.getY())
            return true;
        if (from.getZ() != to.getZ())
            return true;
        return false;
    }

    private Location getHoloLoc(Block containerBlock) {
        Location holoLoc;
        Inventory inventory = Utils.getBlockInventory(containerBlock);
        PersistentDataContainer container = ((TileState) containerBlock.getState()).getPersistentDataContainer();
        String rotation = container.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"),
                PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        rotation = Config.holo_rotation ? rotation : Config.settings_defaults_rotation;
        // Add rotation checks
        switch (rotation) {
            case "north":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 0, -0.8));
                break;
            case "east":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0.8, 0, 0));
                break;
            case "south":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 0, 0.8));
                break;
            case "west":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(-0.8, 0, 0));
                break;
            case "down":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, -1.5, 0));
                break;
            default:
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 1, 0));
                break;
        }
        return holoLoc;
    }

    private Location getCentralLocation(Block containerBlock, Inventory inventory, Vector direction) {
        Location holoLoc;
        if (inventory instanceof DoubleChestInventory) {
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();
            holoLoc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D)
                    .add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5);
            if (direction.getY() == 0) {
                Location lloc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D);
                Location hloc = holoLoc.clone();
                double angle = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle = (-(angle / Math.PI) * 360.0d) / 2.0d + 180.0d;
                hloc = hloc.add(direction);
                double angle2 = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle2 = (-(angle2 / Math.PI) * 360.0d) / 2.0d + 180.0d;
                if (angle == angle2 || angle == angle2 - 180 || angle == angle2 + 180) {
                    holoLoc.add(direction.multiply(1.625));
                } else {
                    holoLoc.add(direction);
                }
            } else {
                holoLoc.add(direction);
            }

        } else {
            holoLoc = containerBlock.getLocation().clone().add(0.5D, 0, 0.5D).add(direction);
        }
        return holoLoc;
    }

    public static String getHologramItemData(int lineNum, ItemStack item, int lines) {
        String line = "";
        if (item.getType().name().contains("SHULKER_BOX")) {
            // Get the shulker box inventory
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                    Inventory inv = Bukkit.createInventory(null, 27, "Shulker Box");
                    inv.setContents(shulker.getInventory().getContents());

                    // Collect all the item counts into a map and sort them by count
                    Map<String, Integer> itemCounts = new HashMap<>();
                    for (ItemStack itemStack : inv.getContents()) {
                        if (itemStack != null) {
                            String itemName = Utils.getFinalItemName(itemStack);
                            if (itemCounts.containsKey(itemName)) {
                                itemCounts.put(itemName, itemCounts.get(itemName) + itemStack.getAmount());
                            } else {
                                itemCounts.put(itemName, itemStack.getAmount());
                            }
                        }
                    }
                    List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemCounts.entrySet());
                    sortedItems.sort(Map.Entry.comparingByValue());
                    Collections.reverse(sortedItems);

                    if (lineNum == -1 && sortedItems.size() - lines > 0) {
                        line = lm.shulkerboxItemHologramMore(sortedItems.size() - lines);
                    } else if (lineNum - 1 >= 0 && lineNum - 1 < sortedItems.size()) {
                        line = lm.shulkerboxItemHologram(sortedItems.get(lineNum - 1).getKey(), sortedItems.get(lineNum - 1).getValue());
                    } else {
                        line = "";
                    }
                }
            }
        } else if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) item.getItemMeta();
            // Get the enchantments
            Map<Enchantment, Integer> enchantments = emeta.getStoredEnchants();
            List<Map.Entry<Enchantment, Integer>> sortedEnchants = new ArrayList<>(enchantments.entrySet());
            sortedEnchants.sort(Map.Entry.comparingByValue());
            Collections.reverse(sortedEnchants);

            if (lineNum == -1 && sortedEnchants.size() - lines > 0) {
                line = lm.itemEnchantHologramMore((sortedEnchants.size() - lines));
            } else if (lineNum - 1 >= 0 && lineNum - 1 < sortedEnchants.size()) {
                line = lm.itemEnchantHologram(sortedEnchants.get(lineNum - 1).getKey(),
                        sortedEnchants.get(lineNum - 1).getValue());
            } else {
                line = "";
            }
        } else {
            // Get the enchantments
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            List<Map.Entry<Enchantment, Integer>> sortedEnchants = new ArrayList<>(enchantments.entrySet());
            sortedEnchants.sort(Map.Entry.comparingByValue());
            Collections.reverse(sortedEnchants);

            if (lineNum == -1 && sortedEnchants.size() - lines > 0) {
                line = lm.itemEnchantHologramMore((sortedEnchants.size() - lines));
            } else if (lineNum - 1 >= 0 && lineNum - 1 < sortedEnchants.size()) {
                line = lm.itemEnchantHologram(sortedEnchants.get(lineNum - 1).getKey(),
                        sortedEnchants.get(lineNum - 1).getValue());
            } else {
                line = "";
            }
        }
        return line;
    }

}
