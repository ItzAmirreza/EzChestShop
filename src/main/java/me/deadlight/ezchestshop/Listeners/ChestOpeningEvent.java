package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.AdminShopGUI;
import me.deadlight.ezchestshop.GUIs.NonOwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.OwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.ServerShopGUI;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;
import java.util.UUID;

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
                        Chest rightChest = null;
                        event.setCancelled(true);

                        if (!chestleft.getPersistentDataContainer().isEmpty()) {
                            rightone = chestleft.getPersistentDataContainer();
                            rightChest = chestleft;
                        } else {
                            rightone = chestright.getPersistentDataContainer();
                            rightChest = chestright;
                        }

                        ownerValueConvertor(rightChest);
                        insertNewValues(rightChest);
                        //String owner = Bukkit.getOfflinePlayer(UUID.fromString(rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();
                        String owneruuid = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
                        boolean isAdminShop = rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;

                        if (isAdminShop) {
                            ServerShopGUI serverShopGUI = new ServerShopGUI();
                            serverShopGUI.showGUI(event.getPlayer(), rightone, chest, rightChest);
                            return;
                        }
                        boolean isAdmin = isAdmin(rightone, event.getPlayer().getUniqueId().toString());

                        if (event.getPlayer().hasPermission("ecs.admin")) {
                            adminShopGUI.showGUI(event.getPlayer(), rightone, chest, rightChest);
                            return;
                        }

                        if (event.getPlayer().getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {

                            ownerShopGUI.showGUI(event.getPlayer(), rightone, chest, rightChest, isAdmin);

                        } else {

                            //not owner show default
                            nonOwnerShopGUI.showGUI(event.getPlayer(), rightone, chest);

                        }

                    }

                } else {

                    PersistentDataContainer container = state.getPersistentDataContainer();

                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                        event.setCancelled(true);

                        ownerValueConvertor(chest);
                        insertNewValues(chest);
                        boolean isAdminShop = container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;
                        //String owner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();
                        String owneruuid = container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
                        boolean isAdmin = isAdmin(container, event.getPlayer().getUniqueId().toString());

                        if (isAdminShop) {
                            ServerShopGUI serverShopGUI = new ServerShopGUI();
                            serverShopGUI.showGUI(event.getPlayer(), container, chest, chest);
                            return;
                        }

                        if (event.getPlayer().hasPermission("ecs.admin")) {
                            adminShopGUI.showGUI(event.getPlayer(), container, chest, chest);
                            return;
                        }

                        if (event.getPlayer().getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {
                            //owner show special gui
                            ownerShopGUI.showGUI(event.getPlayer(), container, chest, chest, isAdmin);

                        } else {

                            //not owner show default
                            nonOwnerShopGUI.showGUI(event.getPlayer(), container, chest);

                        }



                    }

                }

            }

        }

    }

    private void ownerValueConvertor(Chest chest) {
        PersistentDataContainer data = chest.getPersistentDataContainer();
        String shopOwner = data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        try {
            //is UUID
            UUID.fromString(shopOwner);
        } catch (Exception e) {
            //then its old owner value and I have to change it immediately!
            OfflinePlayer offplayer = Bukkit.getOfflinePlayer(shopOwner);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, offplayer.getUniqueId().toString());
            chest.update();
        }
    }

    private void insertNewValues(Chest chest) {
        //1.3.0 new values
        PersistentDataContainer data = chest.getPersistentDataContainer();
        if (!data.has(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER)) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
            chest.update();
        }
    }


    private boolean isAdmin(PersistentDataContainer data, String uuid) {
        UUID owneruuid = UUID.fromString(uuid);
        List<UUID> adminsUUID = Utils.getAdminsList(data);
        if (adminsUUID.contains(owneruuid)) {
            return true;
        } else {
            return false;
        }
    }




}
