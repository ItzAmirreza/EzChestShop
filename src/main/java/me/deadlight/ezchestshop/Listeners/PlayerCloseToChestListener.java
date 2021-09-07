package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerCloseToChestListener implements Listener {

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
                        if (!isAlreadyPresentingText(loc, player)) {
                            if (ShopContainer.isShop(loc)) {
                                Inventory inventory = Utils.getBlockInventory(target);
                                Location holoLoc;
                                if (inventory instanceof DoubleChestInventory) {
                                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                                    Chest leftchest = (Chest) doubleChest.getLeftSide();
                                    Chest rightchest = (Chest) doubleChest.getRightSide();
                                    holoLoc = leftchest.getLocation().add(0.5D, 0, 0.5D).add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5).add(0, 1, 0);
                                } else {
                                    holoLoc = target.getLocation().clone().add(0.5, 1, 0.5);
                                }

                                PersistentDataContainer container = ((TileState) target.getState()).getPersistentDataContainer();
                                ItemStack thatItem = Utils.getItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                        "item"), PersistentDataType.STRING));

                                double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                                        PersistentDataType.DOUBLE);
                                double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                                        PersistentDataType.DOUBLE);
                                boolean is_adminshop = container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                        PersistentDataType.INTEGER) == 1;

                                OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
                                String shopOwner = offlinePlayerOwner.getName();
                                if (shopOwner == null) {
                                    shopOwner = ChatColor.RED + "Error";
                                }

                                //showHologramText(holoLoc, loc, thatItem, buy, sell, player, is_adminshop, shopOwner);
                                showHologramTextOnHover(holoLoc, loc, thatItem, buy, sell, player, is_adminshop, shopOwner);
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
                    .filter(sloc -> sloc != null && loc.getWorld().equals(sloc.getWorld()) && loc.distance(sloc) < Config.holodistancing_distance + 5)
                    .forEach(sloc -> {
                        double dist = loc.distance(sloc);
                        //Show the Hologram if Player close enough
                        if (dist < Config.holodistancing_distance) {
                            if (isAlreadyPresenting(sloc, player))
                                return; //forEach -> acts as continue;

                            Block target = sloc.getWorld().getBlockAt(sloc);
                            if (target != null) {
                                if (Utils.isApplicableContainer(target)) {
                                    Inventory inventory = Utils.getBlockInventory(target);
                                    Location holoLoc;
                                    if (inventory instanceof DoubleChestInventory) {
                                        DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                                        Chest leftchest = (Chest) doubleChest.getLeftSide();
                                        Chest rightchest = (Chest) doubleChest.getRightSide();
                                        holoLoc = leftchest.getLocation().add(0.5D, 0, 0.5D).add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5).add(0, 1, 0);
                                    } else {
                                        holoLoc = target.getLocation().clone().add(0.5, 1, 0.5);
                                    }

                                    PersistentDataContainer container = ((TileState)target.getState()).getPersistentDataContainer();
                                    ItemStack thatItem = Utils.getItem(container.get(new NamespacedKey(EzChestShop.getPlugin(),
                                            "item"), PersistentDataType.STRING));

                                    double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"),
                                            PersistentDataType.DOUBLE);
                                    double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"),
                                            PersistentDataType.DOUBLE);
                                    boolean is_adminshop = container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                            PersistentDataType.INTEGER) == 1;

                                    OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
                                    String shopOwner = offlinePlayerOwner.getName();
                                    if (shopOwner == null) {
                                        shopOwner = ChatColor.RED + "Error";
                                    }


                                    showHologram(holoLoc, sloc, thatItem, buy, sell, event.getPlayer(), is_adminshop, shopOwner);
                                    //mark the shop as visible
                                    List<Player> players = playershopmap.get(sloc);
                                    if (players == null)
                                        players = new ArrayList<>();
                                    players.add(player);
                                    playershopmap.put(sloc, players);

                                }
                            }

                        }
                        //Hide the Hologram that is too far away from the player
                        else if (dist > Config.holodistancing_distance + 1 && dist < Config.holodistancing_distance + 3){
                            //Hide the Hologram - requires spawn Position of the Hologram and Position of the Shop
                            hideHologram(player, sloc);
                        }
                    });
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc != null && loc.getWorld().equals(sloc.getWorld()) && loc.distance(sloc) < Config.holodistancing_distance + 5)
                .forEach(sloc -> {
                    hideHologram(player, sloc);
                });
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getFrom();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc != null && loc.getWorld().equals(sloc.getWorld()) && loc.distance(sloc) < Config.holodistancing_distance + 5)
                .forEach(sloc -> {
                    hideHologram(player, sloc);
                });
    }

    private void showHologram(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
                              double sell, Player player, boolean is_adminshop, String shop_owner) {

        showHologramItem(spawnLocation, shopLocation, thatItem, player, is_adminshop);
        if (!Config.holodistancing_show_item_first)
            showHologramText(spawnLocation, shopLocation, thatItem, buy, sell, player, is_adminshop, shop_owner);

    }

    private void showHologramText(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy,
                                  double sell, Player player, boolean is_adminshop, String shop_owner) {
        List<Pair<Location, ASHologram>> holoTextList = HoloTextMap.containsKey(player) ? HoloTextMap.get(player) : new ArrayList<>();
        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        String itemname = "Error";
        itemname = Utils.getFinalItemName(thatItem);
        for (String element : is_adminshop ? Config.holostructure_admin : Config.holostructure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15, 0);
                lineLocation.add(0, 0.35, 0);
            } else {
                String line = Utils.color(element.replace("%item%", itemname).replace("%buy%", String.valueOf(buy)).
                        replace("%sell%", String.valueOf(sell)).replace("%currency%", Config.currency).replace("%owner%", shop_owner));
                ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                hologram.spawn();
                Utils.onlinePackets.add(hologram);
                holoTextList.add(Pair.of(shopLocation, hologram));
                lineLocation.add(0, 0.3, 0);
            }
        }
        //FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        //Save the Hologram Data so it can be removed later
        HoloTextMap.put(player, holoTextList);
    }

    private void showHologramTextOnHover(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy, double sell, Player player, boolean is_adminshop, String shop_owner) {

        List<Pair<Location, ASHologram>> holoTextList = HoloTextMap.containsKey(player) ? HoloTextMap.get(player) : new ArrayList<>();

        if (!holoTextList.isEmpty()) {
            hideHologramText(player);
            holoTextList = new ArrayList<>();
        }

        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        String itemname = "Error";
        itemname = Utils.getFinalItemName(thatItem);
        for (String element : is_adminshop ? Config.holostructure_admin : Config.holostructure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15, 0);
                lineLocation.add(0, 0.35, 0);
            } else {
                String line = Utils.color(element.replace("%item%", itemname).replace("%buy%", String.valueOf(buy)).
                        replace("%sell%", String.valueOf(sell)).replace("%currency%", Config.currency).replace("%owner%", shop_owner));
                ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                hologram.spawn();
                Utils.onlinePackets.add(hologram);
                holoTextList.add(Pair.of(shopLocation, hologram));
                lineLocation.add(0, 0.3, 0);
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
        String itemname = "Error";
        itemname = Utils.getFinalItemName(thatItem);
        for (String element : is_adminshop ? Config.holostructure_admin : Config.holostructure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15, 0);
                FloatingItem floatingItem = new FloatingItem(player, thatItem, lineLocation);
                Utils.onlinePackets.add(floatingItem);
                holoItemList.add(Pair.of(shopLocation, floatingItem));
                lineLocation.add(0, 0.35, 0);
            } else {
                lineLocation.add(0, 0.3, 0);
            }
        }
        //FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        //Save the Hologram Data so it can be removed later
        HoloItemMap.put(player, holoItemList);
    }



    public static void hideHologram(Player p, Location loc) {
        if (HoloTextMap.containsKey(p) && HoloTextMap.get(p).size() > 0) {
            new ArrayList<>(HoloTextMap.get(p)).stream().filter(holo -> loc.equals(holo.getLeft()))
                    .forEach(holo -> {
                holo.getValue().destroy();
                Utils.onlinePackets.remove(holo.getValue());

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
            new ArrayList<>(HoloItemMap.get(p)).stream().filter(holo -> loc.equals(holo.getKey()))
                    .forEach(holo -> {
                holo.getValue().destroy();
                Utils.onlinePackets.remove(holo.getValue());

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
                        holo.getValue().destroy();
                        Utils.onlinePackets.remove(holo.getValue());
                        Location loc = holo.getKey();
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

}
