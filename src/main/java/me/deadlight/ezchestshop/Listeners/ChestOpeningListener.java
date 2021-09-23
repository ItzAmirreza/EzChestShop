package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.AdminShopGUI;
import me.deadlight.ezchestshop.GUIs.NonOwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.OwnerShopGUI;
import me.deadlight.ezchestshop.GUIs.ServerShopGUI;
import me.deadlight.ezchestshop.Utils.Utils;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import me.deadlight.ezchestshop.Utils.WorldGuard.WorldGuardUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Material clickedType = event.getClickedBlock().getType();
        if (Utils.isApplicableContainer(clickedType)) {

            Block chestblock = event.getClickedBlock();
            PersistentDataContainer dataContainer = null;
            Location loc = chestblock.getLocation();
            TileState state = (TileState) chestblock.getState();
            Inventory inventory = Utils.getBlockInventory(chestblock);

            if (clickedType == Material.CHEST || clickedType == Material.TRAPPED_CHEST) {
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

                Player player = event.getPlayer();
                
                if (isAdminShop) {
                    if (EzChestShop.worldguard) {
                        if (!WorldGuardUtils.queryStateFlag(FlagRegistry.USE_ADMIN_SHOP, player) && !player.isOp()) {
                            return;
                        }
                    }
                    ServerShopGUI serverShopGUI = new ServerShopGUI();
                    serverShopGUI.showGUI(player, dataContainer, chestblock);
                    return;
                }
                boolean isAdmin = isAdmin(dataContainer, player.getUniqueId().toString());
                
                if (EzChestShop.worldguard) {
                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.USE_SHOP, player) && !player.isOp() && !(isAdmin || player.getUniqueId().toString().equalsIgnoreCase(owneruuid))) {
                        return;
                    }
                }
                if (player.hasPermission("ecs.admin") || player.hasPermission("ecs.admin.view")) {
                    adminShopGUI.showGUI(player, dataContainer, chestblock);
                    return;
                }

                if (player.getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {

                    ownerShopGUI.showGUI(player, dataContainer, chestblock, isAdmin);

                } else {

                    //not owner show default
                    nonOwnerShopGUI.showGUI(player, dataContainer, chestblock);

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

    private void insertNewValues(Block containerBlock) {
        //1.3.0 new values
        TileState state = ((TileState)containerBlock.getState());
        PersistentDataContainer data = state.getPersistentDataContainer();
        if (!data.has(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER)) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
            state.update();
        }
        //hologram update values
        if (!data.has(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING)) {
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, Config.settings_defaults_rotation);
            state.update();
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
