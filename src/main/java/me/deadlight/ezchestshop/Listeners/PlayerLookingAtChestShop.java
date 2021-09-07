package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.*;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerLookingAtChestShop implements Listener {

    private HashMap<Player, Block> map = new HashMap<>();


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
                            rightone = leftchest.getPersistentDataContainer();
                        } else {
                            rightone = rightchest.getPersistentDataContainer();
                        }

                        //show the hologram
                        ItemStack thatItem = Utils.getItem(rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
                        double buy = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
                        double sell = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
                        boolean is_adminshop = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                PersistentDataType.INTEGER) == 1;
                        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
                        String shopOwner = offlinePlayerOwner.getName();
                        if (shopOwner == null) {
                            shopOwner = ChatColor.RED + "Error";
                        }

                        Location holoLoc = leftchest.getLocation().add(0.5D, 0, 0.5D).add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5).add(0, 1, 0);

                        if (!isAlreadyLooking(event.getPlayer(), target) && Config.showholo && !isAlreadyPresenting(holoLoc, event.getPlayer().getName())) {
                            showHologram(holoLoc, target.getLocation().clone(), thatItem, buy, sell, event.getPlayer(), is_adminshop, shopOwner);
                        }

                    }

                } else {
                    //not a double chest

                    PersistentDataContainer container = ((TileState)target.getState()).getPersistentDataContainer();
                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {


                        //show the hologram
                        ItemStack thatItem = Utils.getItem(container.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
                        double buy = container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
                        double sell = container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
                        boolean is_adminshop = container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"),
                                PersistentDataType.INTEGER) == 1;
                        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
                        String shopOwner = offlinePlayerOwner.getName();
                        if (shopOwner == null) {
                            shopOwner = ChatColor.RED + "Error";
                        }
                        Location holoLoc = target.getLocation().clone().add(0.5, 1, 0.5);

                        if (!isAlreadyLooking(event.getPlayer(), target) && Config.showholo && !isAlreadyPresenting(holoLoc, event.getPlayer().getName())) {
                            showHologram(holoLoc, target.getLocation().clone(), thatItem, buy, sell, event.getPlayer(), is_adminshop, shopOwner);
                        }

                    }

                }

            }
        }
        map.put(event.getPlayer(), target);

    }



    private void showHologram(Location spawnLocation, Location shopLocation, ItemStack thatItem, double buy, double sell, Player player, boolean is_adminshop, String shop_owner) {

        List<ASHologram> holoTextList = new ArrayList<>();
        List<FloatingItem> holoItemList = new ArrayList<>();


        Location lineLocation = spawnLocation.clone().subtract(0, 0.1, 0);
        String itemname = "Error";
        itemname = Utils.getFinalItemName(thatItem);
        for (String element : is_adminshop ? Config.holostructure_admin : Config.holostructure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15, 0);
                FloatingItem floatingItem = new FloatingItem(player, thatItem, lineLocation);
                Utils.onlinePackets.add(floatingItem);
                holoItemList.add(floatingItem);
                lineLocation.add(0, 0.35, 0);
            } else {
                String line = Utils.color(element.replace("%item%", itemname).replace("%buy%", String.valueOf(buy)).
                        replace("%sell%", String.valueOf(sell)).replace("%currency%", Config.currency).replace("%owner%", shop_owner));
                ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                hologram.spawn();
                Utils.onlinePackets.add(hologram);
                holoTextList.add(hologram);
                lineLocation.add(0, 0.3, 0);
            }
        }

        List<Player> players = playershopmap.get(shopLocation);
        if (players == null)
            players = new ArrayList<>();
        players.add(player);
        playershopmap.put(shopLocation, players);



        Bukkit.getScheduler().scheduleAsyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (ASHologram holo : holoTextList) {
                    holo.destroy();
                    Utils.onlinePackets.remove(holo);
                }
                for (FloatingItem item : holoItemList) {
                    item.destroy();
                    Utils.onlinePackets.remove(item);
                }
                playershopmap.remove(spawnLocation);

            }
        }, 20 * Config.holodelay);


    }



    private boolean isAlreadyLooking(Player player, Block block) {
        if (map.get(player) != null) {
            if (block.getType() == map.get(player).getType()) {

                return true;
            } else {

                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isAlreadyPresenting(Location location, String playername) {

        if (playershopmap.containsKey(location)) {

            if (playershopmap.get(location).contains(playername)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }

    }


}
