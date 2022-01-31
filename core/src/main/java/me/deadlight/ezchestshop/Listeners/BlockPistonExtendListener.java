package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Events.ShulkerShopDropEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import me.deadlight.ezchestshop.Utils.WorldGuard.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BlockPistonExtendListener implements Listener {


    private static LanguageManager lm = new LanguageManager();
    private static HashMap<String, String> lockMap = new HashMap<>();
    private static List<String> lockList = new ArrayList<>();
    private static HashMap<String, PersistentDataContainer> lockContainerMap = new HashMap<>();
    private static HashMap<String, Location> lockLocationMap = new HashMap<>();

    @EventHandler
    public void onExtend(BlockPistonExtendEvent event) {

        for (Block block : event.getBlocks()) {
            if (Utils.isShulkerBox(block)) {
                BlockState blockState = block.getState();
                TileState state = (TileState) blockState;
                PersistentDataContainer container = state.getPersistentDataContainer();

                if (Utils.isShulkerBox(block.getType())) {
                    //it is a shulkerbox, now checking if its a shop
                    Location shulkerLoc = block.getLocation();
                    if (ShopContainer.isShop(shulkerLoc)) {
                        boolean adminshop = container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;
                        if (EzChestShop.worldguard) {
                            if (adminshop) {
                                if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_ADMIN_SHOP, shulkerLoc)) {
                                    event.setCancelled(true);
                                    return;
                                }
                            } else {
                                if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_SHOP, shulkerLoc)) {
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }
                        //congrats
                        //Add the Lock for dropped item recognition later
                        UUID uuid = UUID.randomUUID();
                        lockMap.put(uuid.toString(), ((ShulkerBox) state).getLock());
                        lockList.add(uuid.toString());
                        lockContainerMap.put(uuid.toString(), container);
                        lockLocationMap.put(uuid.toString(), shulkerLoc);
                        ((ShulkerBox) state).setLock(uuid.toString());
                        state.update();

                        ShopContainer.deleteShop(shulkerLoc);
                        if (Config.holodistancing) {
                            PlayerCloseToChestListener.hideHologram(shulkerLoc);
                        }
                        Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(), () -> {

                            Collection<Entity> entitiyList = block.getWorld().getNearbyEntities(shulkerLoc, 2, 2, 2);
                            entitiyList.forEach(entity -> {
                                if (entity instanceof Item) {
                                    Item item = (Item) entity;
                                    ItemStack itemStack = item.getItemStack();
                                    if (Utils.isShulkerBox(itemStack.getType())) {
                                        //get the lock
                                        BlockStateMeta bsm = (BlockStateMeta) itemStack.getItemMeta();
                                        ShulkerBox box = (ShulkerBox) bsm.getBlockState();
                                        String lock = box.getLock();
                                        //good, now validate that its the same shulker box that it was before
                                        if (lock != null && lockList.contains(lock)) {
                                            //it is surely that shulker
                                            //reset the lock
                                            box.setLock(lockMap.get(lock));
                                            box.update();
                                            bsm.setBlockState(box);
                                            itemStack.setItemMeta(bsm);
                                            lockList.remove(lock);
                                            lockMap.remove(lock);
                                            lockContainerMap.remove(lock);
                                            lockLocationMap.remove(lock);

                                            //copy the new data over
                                            ItemMeta meta = itemStack.getItemMeta();
                                            PersistentDataContainer metaContainer = meta.getPersistentDataContainer();
                                            metaContainer = ShopContainer.copyContainerData(container, metaContainer);
                                            meta = addLore(meta, metaContainer);
                                            itemStack.setItemMeta(meta);

                                            //Call the Event
                                            item.setItemStack(itemStack);
                                            ShulkerShopDropEvent shopDropEvent = new ShulkerShopDropEvent(item, shulkerLoc);
                                            //idk if item also needs update after removing a persistent value (Have to check later) ^^^^
                                            Bukkit.getPluginManager().callEvent(shopDropEvent);

                                        }
                                    }
                                }
                            });


                        }, 5);
                    }
                }
            }
        }
    }

    @EventHandler
    public void InventoryItemPickup(InventoryPickupItemEvent event) {
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        if (Utils.isShulkerBox(itemStack.getType())) {
            //get the lock
            BlockStateMeta bsm = (BlockStateMeta) itemStack.getItemMeta();
            ShulkerBox box = (ShulkerBox) bsm.getBlockState();
            String lock = box.getLock();
            //good, now validate that its the same shulker box that it was before
            if (lock != null && lockList.contains(lock)) {
                //it is surely that shulker

                PersistentDataContainer container = lockContainerMap.get(lock);
                Location shulkerLoc = lockLocationMap.get(lock);
                //reset the lock
                box.setLock(lockMap.get(lock));
                box.update();
                bsm.setBlockState(box);
                itemStack.setItemMeta(bsm);
                lockList.remove(lock);
                lockMap.remove(lock);
                lockContainerMap.remove(lock);
                lockLocationMap.remove(lock);

                //copy the new data over
                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer metaContainer = meta.getPersistentDataContainer();
                metaContainer = ShopContainer.copyContainerData(container, metaContainer);
                meta = addLore(meta, metaContainer);
                itemStack.setItemMeta(meta);

                //Call the Event
                item.setItemStack(itemStack);
                ShulkerShopDropEvent shopDropEvent = new ShulkerShopDropEvent(item, shulkerLoc);
                //idk if item also needs update after removing a persistent value (Have to check later) ^^^^
                Bukkit.getPluginManager().callEvent(shopDropEvent);

            }
        }
    }


    private ItemMeta addLore(ItemMeta meta, PersistentDataContainer container) {
        if (Config.settings_add_shulkershop_lore) {
            List<String> nlore = lm.shulkerboxLore(Bukkit.getOfflinePlayer(UUID.fromString(getContainerString(container, "owner"))).getName(),
                    Utils.getFinalItemName(Utils.decodeItem(getContainerString(container, "item"))),
                    getContainerDouble(container, "buy"),
                    getContainerDouble(container, "sell"));
            meta.setLore(nlore);
        }
        return meta;
    }

    private String getContainerString(PersistentDataContainer container, String key) {
        return container.get(new NamespacedKey(EzChestShop.getPlugin(), key), PersistentDataType.STRING);
    }

    private Double getContainerDouble(PersistentDataContainer container, String key) {
        return container.get(new NamespacedKey(EzChestShop.getPlugin(), key), PersistentDataType.DOUBLE);
    }


}
