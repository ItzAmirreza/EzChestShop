package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.Objects.ASHologramObject;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerCloseToChestListener implements Listener {

    public static HashMap<Player, List<ASHologramObject>> map = new HashMap<>();

    private static HashMap<Location, List<Player>> playershopmap = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        if (!hasMovedXYZ(event))
            return;
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


                            showHologram(holoLoc, sloc, thatItem, buy, sell, event.getPlayer());
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
                              double sell, Player player) {

        Location secondLineLocation = spawnLocation.clone().add(0, 0.3, 0).subtract(0, 1, 0);

        Location thirdLocation = secondLineLocation.clone().subtract(0, 0.4, 0);

        Location floatingItemLocation = thirdLocation.clone().add(0, 2, 0);

        String itemname = "Error";


        //using ASHologram class
        if (thatItem.getItemMeta().hasDisplayName()) {
            itemname = Utils.color(thatItem.getItemMeta().getDisplayName());
        } else {
            itemname = Utils.capitalizeFirstSplit(thatItem.getType().toString());
        }
        String finalfirstline = Utils.color(Config.firstLine.replace("%item%", itemname).replace("%buy%", String.valueOf(buy)).replace("%sell%", String.valueOf(sell)));

        ASHologram hologram = new ASHologram(player, finalfirstline, EntityType.ARMOR_STAND, secondLineLocation, false);
        hologram.spawn();
        Utils.onlinePackets.add(hologram);

        ASHologram hologram2 = new ASHologram(player, Utils.color(Config.secondLine.replace("%buy%", String.valueOf(buy)).replace("%sell%", String.valueOf(sell)).replace("%item%", itemname)), EntityType.ARMOR_STAND, thirdLocation, false);
        hologram2.spawn();
        Utils.onlinePackets.add(hologram2);

        FloatingItem floatingItem = new FloatingItem(player, thatItem, floatingItemLocation);
        Utils.onlinePackets.add(floatingItem);
        //FakeAdvancement outOfStock = new FakeAdvancement(player, thatItem);

        //Save the Hologram Data so it can be removed later
        List<ASHologramObject> holoList = map.containsKey(player) ? map.get(player) : new ArrayList<>();
        holoList.add(new ASHologramObject(hologram, hologram2, floatingItem, shopLocation));
        map.put(player, holoList);

    }

    public static void hideHologram(Player p, Location loc) {
        if (map.containsKey(p) && map.get(p).size() > 0) {
            map.get(p).stream().filter(holo -> loc.equals(holo.getLocation()))
                    .findFirst().ifPresent(holo -> {
                holo.getHologram().destroy();
                Utils.onlinePackets.remove(holo.getHologram());
                holo.getHologram2().destroy();
                Utils.onlinePackets.remove(holo.getHologram2());
                holo.getFloatingItem().destroy();
                Utils.onlinePackets.remove(holo.getFloatingItem());

                List<Player> players = playershopmap.get(loc);
                players.remove(p);
                if (players.isEmpty())
                    playershopmap.remove(loc);
                List<ASHologramObject> holoList = map.get(p);
                holoList.remove(holo);
                map.put(p, holoList);
            });
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
