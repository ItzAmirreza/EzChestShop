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
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerLookingAtChestShop implements Listener {

    private HashMap<Player, Location> map = new HashMap<>();
    private static LanguageManager lm = new LanguageManager();


    private static HashMap<Location, List<Player>> playershopmap = new HashMap<>();

    @EventHandler
    public void onLook(PlayerMoveEvent event) {

        Block target = event.getPlayer().getTargetBlockExact(5);

        if (target != null) {
            if (Utils.isApplicableContainer(target)) {

                Inventory inventory = Utils.getBlockInventory(target);
                if (inventory instanceof DoubleChestInventory) {
                    //double chest

                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                    Chest leftchest = (Chest) doubleChest.getLeftSide();
                    Chest rightchest = (Chest) doubleChest.getRightSide();

                    if (leftchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)
                            || rightchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                        PersistentDataContainer rightone = null;

                        if (!leftchest.getPersistentDataContainer().isEmpty()) {
                            target = leftchest.getBlock();
                            rightone = leftchest.getPersistentDataContainer();
                        } else {
                            target = rightchest.getBlock();
                            rightone = rightchest.getPersistentDataContainer();
                        }

                        //show the hologram
//                        if (Utils.validateContainerValues(rightone, ShopContainer.getShop(target.getLocation()))) {
//                            EzChestShop.logConsole(
//                                    "[ECS] Something unexpected happened with this container's data, so this shop has been removed.");
//                            return;
//                        }
                        ItemStack thatItem = Utils.decodeItem(rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
                        double buy = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
                        double sell = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
                        boolean is_adminshop = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                PersistentDataType.INTEGER) == 1;
                        boolean is_dbuy = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"),
                                PersistentDataType.INTEGER) == 1;
                        boolean is_dsell = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"),
                                PersistentDataType.INTEGER) == 1;
                        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
                        String shopOwner = offlinePlayerOwner.getName();
                        if (shopOwner == null) {
                            shopOwner = ChatColor.RED + "Error";
                        }

                        Location holoLoc = getHoloLoc(target);

                        if (!isAlreadyLooking(event.getPlayer(), target) && Config.showholo && !isAlreadyPresenting(target.getLocation(), event.getPlayer())) {
                            showHologram(holoLoc, target.getLocation().clone(), thatItem, buy, sell, event.getPlayer(), is_adminshop, shopOwner, is_dbuy, is_dsell);
                        }
                        map.put(event.getPlayer(), target.getLocation());
                    }

                } else {
                    //not a double chest

                    PersistentDataContainer container = ((TileState)target.getState()).getPersistentDataContainer();
                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

//                        if (Utils.validateContainerValues(container, ShopContainer.getShop(target.getLocation()))) {
//                            EzChestShop.logConsole(
//                                    "[ECS] Something unexpected happened with this container's data, so this shop has been removed.");
//                            return;
//                        }
                        //show the hologram
                        ItemStack thatItem = Utils.decodeItem(container.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
                        double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
                        double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
                        boolean is_adminshop = container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                PersistentDataType.INTEGER) == 1;
                        boolean is_dbuy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"),
                                PersistentDataType.INTEGER) == 1;
                        boolean is_dsell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"),
                                PersistentDataType.INTEGER) == 1;
                        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
                        String shopOwner = offlinePlayerOwner.getName();
                        if (shopOwner == null) {
                            shopOwner = ChatColor.RED + "Error";
                        }
                        Location holoLoc = getHoloLoc(target);

                        if (!isAlreadyLooking(event.getPlayer(), target) && Config.showholo && !isAlreadyPresenting(target.getLocation(), event.getPlayer())) {
                            showHologram(holoLoc, target.getLocation().clone(), thatItem, buy, sell, event.getPlayer(), is_adminshop, shopOwner, is_dbuy, is_dsell);
                        }

                        map.put(event.getPlayer(), target.getLocation());
                    }
                }

            }
        }

    }



    private void showHologram(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy, double sell,
                              Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell) {

        List<ASHologram> holoTextList = new ArrayList<>();
        List<FloatingItem> holoItemList = new ArrayList<>();


        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        String itemname = "Error";
        itemname = Utils.getFinalItemName(thatItem);
        List<String> possibleCounts = Utils.calculatePossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()),
                Bukkit.getOfflinePlayer(UUID.fromString(((TileState) shopLocation.getBlock().getState()).getPersistentDataContainer()
                        .get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))), player.getInventory().getStorageContents(),
                Utils.getBlockInventory(shopLocation.getBlock()).getStorageContents(),
                buy, sell, thatItem);

        List<String> structure = new ArrayList<>(is_adminshop ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(shopLocation).getRotation().equals("down")) Collections.reverse(structure);
        int lines = structure.stream().filter(s -> s.startsWith("<itemdata") && !s.startsWith("<itemdataRest")).collect(Collectors.toList()).size();
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                FloatingItem floatingItem = new FloatingItem(player, thatItem, lineLocation);
                Utils.onlinePackets.add(floatingItem);
                holoItemList.add(floatingItem);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {

                String line = Utils.colorify(element.replace("%item%", itemname).replace("%buy%", Utils.formatNumber(buy, Utils.FormatType.HOLOGRAM)).
                        replace("%sell%", Utils.formatNumber(sell, Utils.FormatType.HOLOGRAM)).replace("%currency%", Config.currency)
                        .replace("%owner%", shop_owner).replace("%maxbuy%", possibleCounts.get(0)).replace("%maxsell%", possibleCounts.get(1))
                        .replace("%maxStackSize%", thatItem.getMaxStackSize() + "")
                        .replace("%stock%", Utils.howManyOfItemExists(Utils.getBlockInventory(shopLocation.getBlock()).getStorageContents(), thatItem) + "")
                        .replace("%capacity%", Utils.getBlockInventory(shopLocation.getBlock()).getSize() + ""));
                if (is_dbuy || is_dsell) {
                    line = line.replaceAll("<separator>.*?<\\/separator>", "");
                    if (is_dbuy && is_dsell) {
                        line = lm.disabledButtonTitle();
                    } else if (is_dbuy) {
                        line = line.replaceAll("<buy>.*?<\\/buy>", "").replaceAll("<sell>|<\\/sell>", "");
                    } else if (is_dsell) {
                        line = line.replaceAll("<sell>.*?<\\/sell>", "").replaceAll("<buy>|<\\/buy>", "");
                    }
                } else {
                    line = line.replaceAll("<separator>|<\\/separator>", "").replaceAll("<buy>|<\\/buy>", "").replaceAll("<sell>|<\\/sell>", "");
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
                    if (player.isSneaking()) {
                        ItemStack item = ShopContainer.getShop(shopLocation).getShopItem();
                        if (item.getType().name().contains("SHULKER_BOX") || item.getEnchantments().size() > 0) {
                            try {
                                int lineNum = Integer.parseInt(line.replaceAll("\\D", ""));
                                line = PlayerCloseToChestListener.getHologramItemData(lineNum, item, lines);
                            } catch (NumberFormatException e) {
                                line = PlayerCloseToChestListener.getHologramItemData(-1, item, lines);
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
                        if (!Utils.containsAtLeast(inv, shop.getShopItem(), 1)) {
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
                    holoTextList.add(hologram);
                }
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }

        List<Player> players = playershopmap.get(shopLocation);
        if (players == null)
            players = new ArrayList<>();
        players.add(player);
        playershopmap.put(shopLocation, players);



        Bukkit.getScheduler().scheduleAsyncDelayedTask(EzChestShop.getPlugin(), () -> {
            for (ASHologram holo : holoTextList) {
                holo.destroy();
                Utils.onlinePackets.remove(holo);
            }
            for (FloatingItem item : holoItemList) {
                item.destroy();
                Utils.onlinePackets.remove(item);
            }
            List<Player> players1 = playershopmap.get(shopLocation);
            if (players1 == null || players1.isEmpty()) {
                playershopmap.remove(shopLocation);
            } else {
                players1.remove(player);
                if (players1.isEmpty()) {
                    playershopmap.remove(shopLocation);
                } else {
                    playershopmap.put(shopLocation, players1);
                }
            }

        }, 20 * Config.holodelay);


    }



    private boolean isAlreadyLooking(Player player, Block block) {
        return map.get(player) != null && block.getLocation().equals(map.get(player));
    }

    private boolean isAlreadyPresenting(Location location, Player player) {
        return playershopmap.containsKey(location) && playershopmap.get(location).contains(player);
    }

    private Location getHoloLoc(Block containerBlock) {
        Location holoLoc;
        Inventory inventory = Utils.getBlockInventory(containerBlock);
        PersistentDataContainer container = ((TileState) containerBlock.getState()).getPersistentDataContainer();
        String rotation = container.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        rotation = Config.holo_rotation ? rotation : Config.settings_defaults_rotation;
        //Add rotation checks
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
            holoLoc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D).add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5);
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


}
