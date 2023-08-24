package me.deadlight.ezchestshop.listeners;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.ShopCommandManager;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.guis.AdminShopGUI;
import me.deadlight.ezchestshop.guis.NonOwnerShopGUI;
import me.deadlight.ezchestshop.guis.OwnerShopGUI;
import me.deadlight.ezchestshop.guis.ServerShopGUI;
import me.deadlight.ezchestshop.utils.BlockOutline;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.worldguard.FlagRegistry;
import me.deadlight.ezchestshop.utils.worldguard.WorldGuardUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestOpeningListener implements Listener {

    private NonOwnerShopGUI nonOwnerShopGUI= new NonOwnerShopGUI();
    private OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
    private AdminShopGUI adminShopGUI = new AdminShopGUI();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChestOpening(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Material clickedType = event.getClickedBlock().getType();
        if (Utils.isApplicableContainer(clickedType)) {

            Block chestblock = event.getClickedBlock();
            if (EzChestShop.slimefun) {
                if (BlockStorage.hasBlockInfo(chestblock.getLocation())) {
                    ShopContainer.deleteShop(chestblock.getLocation());
                    return;
                }
            }
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
                // Load old shops into the Database when clicked
                if (!ShopContainer.isShop(loc)) {
                    ShopContainer.loadShop(loc, dataContainer);
                }

                List<BlockOutline> playerOutlinedShops = new ArrayList<>(Utils.activeOutlines.values());
                for (BlockOutline outline : playerOutlinedShops) {
                    if (outline == null) continue;
                    if (outline.player.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                        if (outline.block.getLocation().equals(loc)) {
                            outline.hideOutline();
                        }
                    }
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
                    Config.shopCommandManager.executeCommands(player, loc, ShopCommandManager.ShopType.ADMINSHOP, ShopCommandManager.ShopAction.OPEN, null);
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
                // At this point it is clear that some shop will open, so run opening commands here.
                Config.shopCommandManager.executeCommands(player, loc, ShopCommandManager.ShopType.SHOP, ShopCommandManager.ShopAction.OPEN, null);
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
