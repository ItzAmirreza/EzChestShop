package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.AdminShopGUI;
import me.deadlight.ezchestshop.GUIs.NonOwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.OwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.ServerShopGUI;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.*;
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

public class ChestOpeningListener implements Listener {

    private NonOwnerShopGUI nonOwnerShopGUI= new NonOwnerShopGUI();
    private OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
    private AdminShopGUI adminShopGUI = new AdminShopGUI();

    @EventHandler
    public void onChestOpening(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Material clickedType = event.getClickedBlock().getType();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (clickedType == Material.CHEST || clickedType == Material.BARREL || Utils.isShulkerBox(clickedType))) {

            Block chestblock = event.getClickedBlock();
            PersistentDataContainer dataContainer = null;
            Location loc = chestblock.getLocation();
            TileState state = (TileState) chestblock.getState();
            Inventory inventory = Utils.getBlockInventory(chestblock);

            if (clickedType == Material.CHEST) {
                if (inventory instanceof DoubleChestInventory) {
                    DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                    Chest chestleft = (Chest) doubleChest.getLeftSide();
                    Chest chestright = (Chest) doubleChest.getRightSide();


                    if (!chestleft.getPersistentDataContainer().isEmpty()) {
                        dataContainer = chestleft.getPersistentDataContainer();
                        chestblock = chestleft.getBlock();
                    } else {
                        dataContainer = chestright.getPersistentDataContainer();
                        chestblock = chestright.getBlock();
                    }
                    loc  = chestblock.getLocation();
                } else {
                    dataContainer = state.getPersistentDataContainer();
                }
            } else if (clickedType == Material.BARREL) {
                dataContainer = state.getPersistentDataContainer();
            } else if (Utils.isShulkerBox(clickedType)) {
                dataContainer = state.getPersistentDataContainer();
            }


            if (dataContainer.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                event.setCancelled(true);
                ownerValueConvertor(chestblock);
                insertNewValues(chestblock);


                // Load old shops into the Database when clicked
                if (!ShopContainer.isShop(loc)) {
                    ShopContainer.loadShop(loc, dataContainer);
                }

                //String owner = Bukkit.getOfflinePlayer(UUID.fromString(rightone.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();
                String owneruuid = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
                boolean isAdminShop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;

                if (isAdminShop) {
                    ServerShopGUI serverShopGUI = new ServerShopGUI();
                    serverShopGUI.showGUI(event.getPlayer(), dataContainer, chestblock);
                    return;
                }
                boolean isAdmin = isAdmin(dataContainer, event.getPlayer().getUniqueId().toString());

                if (event.getPlayer().hasPermission("ecs.admin")) {
                    adminShopGUI.showGUI(event.getPlayer(), dataContainer, chestblock);
                    return;
                }

                if (event.getPlayer().getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {

                    ownerShopGUI.showGUI(event.getPlayer(), dataContainer, chestblock, isAdmin);

                } else {

                    //not owner show default
                    nonOwnerShopGUI.showGUI(event.getPlayer(), dataContainer, chestblock);

                }

            }

        }

    }

    private void ownerValueConvertor(Block chest) {
        PersistentDataContainer data = ((TileState)chest.getState()).getPersistentDataContainer();
        String shopOwner = data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        try {
            //is UUID
            UUID.fromString(shopOwner);
        } catch (Exception e) {
            //then its old owner value and I have to change it immediately!
            OfflinePlayer offplayer = Bukkit.getOfflinePlayer(shopOwner);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, offplayer.getUniqueId().toString());
            chest.getState().update();
        }
    }

    private void insertNewValues(Block chest) {
        //1.3.0 new values
        PersistentDataContainer data = ((TileState)chest.getState()).getPersistentDataContainer();
        if (!data.has(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER)) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
            chest.getState().update();
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
