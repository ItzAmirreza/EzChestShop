package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class BlockBreakListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        if (Utils.blockBreakMap.containsKey(event.getPlayer().getName())) {
            Collection<Entity> entityList = event.getBlock().getLocation().getWorld().getNearbyEntities(event.getBlock().getLocation(), 2, 2 ,2);
            for (Entity en : entityList) {
                if (en instanceof Item) {
                    Item item = (Item) en;
                    if (item.getItemStack().getType() == Material.CHEST || item.getItemStack().getType() == Material.TRAPPED_CHEST) {
                        en.remove();
                    }
                }
            }
            if (event.isCancelled()) {

                Utils.blockBreakMap.remove(event.getPlayer().getName());

            } else {
                if (Utils.blockBreakMap.containsKey(event.getPlayer().getName())) {

                    event.setCancelled(true);
                }
            }
        }
        if (!event.isCancelled()) {
            Location loc = event.getBlock().getLocation();
            if (ShopContainer.isShop(loc)) {
                ShopContainer.deleteShop(loc);
                if (Utils.isShulkerBox(event.getBlock())) {
                    if (event.isDropItems()) {
                        event.setDropItems(false);
                        ItemStack shulker = event.getBlock().getDrops().stream().findFirst().get();
                        ItemMeta meta = shulker.getItemMeta();
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        PersistentDataContainer bcontainer = ((TileState) event.getBlock().getState()).getPersistentDataContainer();
                        if (bcontainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) != null) {
                            container = ShopContainer.copyContainerData(bcontainer, container);
                            shulker.setItemMeta(meta);
                            loc.getWorld().dropItemNaturally(loc, shulker);
                            if (Config.holodistancing) {
                                PlayerCloseToChestListener.hideHologram(event.getBlock().getLocation());
                            }
                        }
                    }
                }
            }
        }
    }
}
