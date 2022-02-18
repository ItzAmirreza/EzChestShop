package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Pair;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;


public class PlayerCloseToChestListener implements Listener {

    private static LanguageManager lm = new LanguageManager();
    public static HashMap<Player, List<Pair<Location, ASHologram>>> HoloTextMap = new HashMap<>();
    public static HashMap<Player, List<Pair<Location, FloatingItem>>> HoloItemMap = new HashMap<>();

    private static HashMap<Location, List<Player>> playershopmap = new HashMap<>();
    private static HashMap<Location, List<Player>> playershopTextmap = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (Config.showholo) {
            Player player = event.getPlayer();
            if (Config.holodistancing_show_item_first) {
                RayTraceResult result =player.rayTraceBlocks(5);
                if (result != null) {
                    Block target = result.getHitBlock();
                    Location loc = target.getLocation();
                    if (Utils.isApplicableContainer(target)) {
                        Inventory inventory = Utils.getBlockInventory(target);
                        if (inventory instanceof DoubleChestInventory) {
                            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                            Chest leftchest = (Chest) doubleChest.getLeftSide();
                            Chest rightchest = (Chest) doubleChest.getRightSide();

                            if (leftchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                                target = leftchest.getBlock();
                                loc = leftchest.getLocation();
                            }
                            else if (rightchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)){
                                target = rightchest.getBlock();
                                loc = rightchest.getLocation();
                            }
                        }
                        if (!isAlreadyPresentingText(loc, player)) {
                            if (ShopContainer.isShop(loc)) {
                                PersistentDataContainer container = ((TileState) target.getState()).getPersistentDataContainer();
                                if (Utils.validateContainerValues(container, ShopContainer.getShop(loc))) {
                                    System.out.println("[ECS] Something unexpected happened with container's data, shop got removed.");
                                    return;
                                }
                                Location holoLoc = getHoloLoc(target);
                                ItemStack thatItem = Utils.decodeItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                        "item"), PersistentDataType.STRING));

                                double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                                        PersistentDataType.DOUBLE);
                                double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                                        PersistentDataType.DOUBLE);
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

                                //showHologramText(holoLoc, loc, thatItem, buy, sell, player, is_adminshop, shopOwner);
                                showHologramTextOnHover(holoLoc, loc, thatItem, buy, sell, player, is_adminshop, shopOwner, is_dbuy, is_dsell);
                            } else {
                                hideHologramText(player);
                            }
                        }
                    } else {
                        hideHologramText(player);
                    }
                } else {
                    hideHologramText(player);
                }
            }

            if (!hasMovedXYZ(event))
                return;

            Location loc = player.getLocation();
            ShopContainer.getShops().stream()
                    .filter(ezShop -> ezShop.getLocation() != null && loc.getWorld().equals(ezShop.getLocation().getWorld()) && loc.distance(ezShop.getLocation()) < Config.holodistancing_distance + 5)
                    .forEach(ezShop -> {
                        if (EzChestShop.slimefun) {
                            if (BlockStorage.hasBlockInfo(ezShop.getLocation())) {
                                ShopContainer.deleteShop(ezShop.getLocation());
                                return;
                            }
                        }
                        double dist = loc.distance(ezShop.getLocation());
                        //Show the Hologram if Player close enough
                        if (dist < Config.holodistancing_distance) {
                            if (isAlreadyPresenting(ezShop.getLocation(), player))
                                return; //forEach -> acts as continue;

                            Block target = ezShop.getLocation().getWorld().getBlockAt(ezShop.getLocation());
                            if (target != null) {
                                if (Utils.isApplicableContainer(target)) {
                                    PersistentDataContainer container = ((TileState)target.getState()).getPersistentDataContainer();
                                    if (Utils.validateContainerValues(container, ShopContainer.getShop(target.getLocation()))) {
                                        System.out.println("[ECS] Something unexpected happened with container's data, shop got removed.");
                                        return;
                                    }
                                    Location holoLoc = getHoloLoc(target);


                                    ItemStack thatItem = Utils.decodeItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                            "item"), PersistentDataType.STRING));

                                    double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                                            PersistentDataType.DOUBLE);
                                    double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                                            PersistentDataType.DOUBLE);
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


                                    showHologram(holoLoc, ezShop.getLocation(), thatItem, buy, sell, event.getPlayer(), is_adminshop, shopOwner, is_dbuy, is_dsell);
                                    //mark the shop as visible
                                    List<Player> players = playershopmap.get(ezShop.getLocation());
                                    if (players == null)
                                        players = new ArrayList<>();
                                    players.add(player);
                                    playershopmap.put(ezShop.getLocation(), players);

                                }
                            }

                        }
                        //Hide the Hologram that is too far away from the player
                        else if (dist > Config.holodistancing_distance + 1 && dist < Config.holodistancing_distance + 3){
                            //Hide the Hologram - requires spawn Position of the Hologram and Position of the Shop
                            hideHologram(player, ezShop.getLocation());
                        }
                    });
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc.getLocation() != null && loc.getWorld().equals(sloc.getLocation().getWorld()) && loc.distance(sloc.getLocation()) < Config.holodistancing_distance + 5)
                .forEach(sloc -> {
                    hideHologram(player, sloc.getLocation());
                });
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getFrom();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc.getLocation() != null && loc.getWorld().equals(sloc.getLocation().getWorld()) && loc.distance(sloc.getLocation()) < Config.holodistancing_distance + 5)
                .forEach(sloc -> {
                    hideHologram(player, sloc.getLocation());
                });
    }

    private void showHologram(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
                              double sell, Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell) {

        showHologramItem(spawnLocation, shopLocation, thatItem, player, is_adminshop);
        if (!Config.holodistancing_show_item_first)
            showHologramText(spawnLocation, shopLocation, thatItem, buy, sell, player, is_adminshop, shop_owner, is_dbuy, is_dsell);

    }

    private void showHologramText(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
                                  double sell, Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell) {
        List<Pair<Location, ASHologram>> holoTextList = HoloTextMap.containsKey(player) ? HoloTextMap.get(player) : new ArrayList<>();
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        String itemname = "Error";
        itemname = Utils.getFinalItemName(thatItem);
        List<String> structure = new ArrayList<>(is_adminshop ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(shopLocation).getRotation().equals("down")) Collections.reverse(structure);
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                String line = Utils.colorify(element.replace("%item%", itemname).replace("%buy%", Utils.formatNumber(buy, Utils.FormatType.HOLOGRAM)).
                        replace("%sell%", Utils.formatNumber(sell, Utils.FormatType.HOLOGRAM)).replace("%currency%", Config.currency).replace("%owner%", shop_owner));
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
                ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                hologram.spawn();
                Utils.onlinePackets.add(hologram);
                holoTextList.add(Pair.of(shopLocation, hologram));
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }
        //FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        //Save the Hologram Data so it can be removed later
        HoloTextMap.put(player, holoTextList);
    }

    private void showHologramTextOnHover(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
                                         double sell, Player player, boolean is_adminshop, String shop_owner, boolean is_dbuy, boolean is_dsell) {

        List<Pair<Location, ASHologram>> holoTextList = HoloTextMap.containsKey(player) ? HoloTextMap.get(player) : new ArrayList<>();

        if (!holoTextList.isEmpty()) {
            hideHologramText(player);
            holoTextList = new ArrayList<>();
        }

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
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                String line = Utils.colorify(element.replace("%item%", itemname).replace("%buy%", Utils.formatNumber(buy, Utils.FormatType.HOLOGRAM)).
                        replace("%sell%", Utils.formatNumber(sell, Utils.FormatType.HOLOGRAM)).replace("%currency%", Config.currency)
                        .replace("%owner%", shop_owner).replace("%maxbuy%", possibleCounts.get(0)).replace("%maxsell%", possibleCounts.get(1)));
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
                if (line.trim().equals(""))
                    continue;
                if (!line.equals("<empty>")) {
                    ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                    hologram.spawn();
                    Utils.onlinePackets.add(hologram);
                    holoTextList.add(Pair.of(shopLocation, hologram));
                }
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }

        List<Player> players = playershopTextmap.get(shopLocation);
        if (players == null)
            players = new ArrayList<>();
        players.add(player);
        playershopTextmap.put(shopLocation, players);


        //Save the Hologram Data so it can be removed later
        HoloTextMap.put(player, holoTextList);

    }

    private void showHologramItem(Location spawnLocation, Location shopLocation, ItemStack thatItem, Player player, boolean is_adminshop) {
        List<Pair<Location, FloatingItem>> holoItemList = HoloItemMap.containsKey(player) ? HoloItemMap.get(player) : new ArrayList<>();
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        List<String> structure = new ArrayList<>(is_adminshop ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(shopLocation).getRotation().equals("down")) Collections.reverse(structure);
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
        //FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        //Save the Hologram Data so it can be removed later
        HoloItemMap.put(player, holoItemList);
    }


    public static void hideHologram(Location loc) {
        if (Config.holodistancing) {
            Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(loc.getWorld())).filter(p -> p.getLocation().distance(loc) < Config.holodistancing_distance + 5).forEach(p -> {
                hideHologram(p, loc);
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

    public static void hideHologram(Player p, Location loc) {
        if (HoloTextMap.containsKey(p) && HoloTextMap.get(p).size() > 0) {
            new ArrayList<>(HoloTextMap.get(p)).stream().filter(holo -> loc.equals(holo.getFirst()))
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

    private void hideHologramText(Player player) {
        if (HoloTextMap.containsKey(player) && HoloTextMap.get(player).size() > 0) {
            new ArrayList<>(HoloTextMap.get(player)).stream()
                    .forEach(holo -> {
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
                    });
            HoloTextMap.clear();

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
