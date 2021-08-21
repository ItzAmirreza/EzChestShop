package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.*;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import java.util.HashMap;

public class PlayerLookingAtChestShop implements Listener {

    private HashMap<Player, Block> map = new HashMap<>();


    private HashMap<Location, String> playershopmap = new HashMap<>();

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

                    if (leftchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || rightchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

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

                        Location holoLoc = leftchest.getLocation().add(0.5D, 0, 0.5D).add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5).add(0, 1, 0);

                        if (!isAlreadyLooking(event.getPlayer(), target) && Config.showholo && !isAlreadyPresenting(holoLoc, event.getPlayer().getName())) {
                            showHologram(holoLoc,thatItem, buy, sell, event.getPlayer());
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
                        Location holoLoc = target.getLocation().clone().add(0.5, 1, 0.5);

                        if (!isAlreadyLooking(event.getPlayer(), target) && Config.showholo && !isAlreadyPresenting(holoLoc, event.getPlayer().getName())) {
                            showHologram(holoLoc,thatItem, buy, sell, event.getPlayer());
                        }

                    }

                }

            }
        }
        map.put(event.getPlayer(), target);

    }



    private void showHologram(Location spawnLocation, ItemStack thatItem, double buy, double sell, Player player) {

        playershopmap.put(spawnLocation, player.getName());

        Location secondLineLocation = spawnLocation.clone().add(0, 1.3, 0).subtract(0, 1, 0);

        Location thirdLocation = secondLineLocation.clone().subtract(0, 0.4, 0);

        Location floatingItemLocation = thirdLocation.clone().add(0, 1, 0);

        String itemname = "Error";


        //using ASHologram class
        itemname = Utils.getFinalItemName(thatItem);
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


        Bukkit.getScheduler().scheduleAsyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {
                hologram.destroy();
                Utils.onlinePackets.remove(hologram);
                hologram2.destroy();
                Utils.onlinePackets.remove(hologram2);
                floatingItem.destroy();
                Utils.onlinePackets.remove(floatingItem);
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

            if (playershopmap.get(location).equalsIgnoreCase(playername)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }

    }


}
