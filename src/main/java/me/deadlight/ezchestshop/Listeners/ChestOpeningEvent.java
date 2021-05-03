package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.AdminShopGUI;
import me.deadlight.ezchestshop.GUIs.NonOwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.OwnerShopGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ChestOpeningEvent implements Listener {

    private NonOwnerShopGUI nonOwnerShopGUI= new NonOwnerShopGUI();
    private OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
    private AdminShopGUI adminShopGUI = new AdminShopGUI();

    @EventHandler
    public void onChestOpening(PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {

            Block chestblock = event.getClickedBlock();


            if (chestblock.getType() == Material.CHEST) {

                TileState state = (TileState) chestblock.getState();

                Chest chest = (Chest) chestblock.getState();

                Inventory inventory = chest.getInventory();
                if (inventory instanceof DoubleChestInventory) {


                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                    Chest chestleft = (Chest) doubleChest.getLeftSide();
                    Chest chestright = (Chest) doubleChest.getRightSide();


                    if (chestleft.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || chestright.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                        PersistentDataContainer rightone = null;
                        event.setCancelled(true);

                        if (!chestleft.getPersistentDataContainer().isEmpty()) {
                            rightone = chestleft.getPersistentDataContainer();
                        } else {
                            rightone = chestright.getPersistentDataContainer();
                        }

                        String owner = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

                        if (event.getPlayer().getName().equalsIgnoreCase(owner)) {

                            ownerShopGUI.showGUI(event.getPlayer(), rightone, chest);


                        } else {

                            //not owner show default
                            if (event.getPlayer().hasPermission("ecs.admin")) {
                                adminShopGUI.showGUI(event.getPlayer(), rightone, chest);
                                return;
                            }
                            nonOwnerShopGUI.showGUI(event.getPlayer(), rightone, chest);

                        }

                    }

                } else {

                    PersistentDataContainer container = state.getPersistentDataContainer();
                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                        event.setCancelled(true);


                        String owner = container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

                        if (event.getPlayer().getName().equalsIgnoreCase(owner)) {
                            //owner show special gui
                            ownerShopGUI.showGUI(event.getPlayer(), container, chest);


                        } else {

                            //not owner show default
                            if (event.getPlayer().hasPermission("ecs.admin")) {
                                adminShopGUI.showGUI(event.getPlayer(), container, chest);
                                return;
                            }
                            nonOwnerShopGUI.showGUI(event.getPlayer(), container, chest);

                        }



                    }

                }

            }

        }

    }

}
