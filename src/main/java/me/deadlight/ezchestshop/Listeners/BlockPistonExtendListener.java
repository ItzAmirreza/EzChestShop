package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Events.ShulkerShopDropEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockPistonExtendListener implements Listener {

    @EventHandler
    public void onExtend(BlockPistonExtendEvent event) {

        Block shulkerBlock = null;

        for (Block block : event.getBlocks()) {
            if (Utils.isShulkerBox(block)) {
                shulkerBlock = block;
                continue;
            }
        }
        if (shulkerBlock != null) {
            BlockState blockState = shulkerBlock.getState();
            TileState state = (TileState) blockState;
            PersistentDataContainer container = state.getPersistentDataContainer();

            if (Utils.isShulkerBox(shulkerBlock.getType())) {
                //it is a shulkerbox, now checking if its a shop
                Location shulkerLoc = shulkerBlock.getLocation();
                if (ShopContainer.isShop(shulkerLoc)) {
                    //congrats
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "drop"), PersistentDataType.INTEGER, 1);
                    state.update();
                    ItemStack shulkerItem = null;
                    Block finalShulkerBlock = shulkerBlock;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
                        @Override
                        public void run() {

                            Collection<Entity> entitiyList = finalShulkerBlock.getWorld().getNearbyEntities(shulkerLoc, 2, 2, 2);
                            entitiyList.forEach(entity -> {
                                if (entity instanceof Item) {
                                    Item item = (Item) entitiyList;
                                    ItemStack itemStack = item.getItemStack();
                                    if (Utils.isShulkerBox(itemStack.getType())) {
                                        //good, now validate that its the same shulker box that it was before
                                        if (item.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "drop"), PersistentDataType.INTEGER)) {
                                            //it is surely that shulker
                                            item.getPersistentDataContainer().remove(new NamespacedKey(EzChestShop.getPlugin(), "drop"));
                                            ShulkerShopDropEvent shopDropEvent = new ShulkerShopDropEvent(item, shulkerLoc);
                                            //idk if item also needs update after removing a persistent value (Have to check later) ^^^^
                                            Bukkit.getPluginManager().callEvent(shopDropEvent);
                                        }

                                    }
                                }
                            });


                        }
                    }, 5);
                }
            }
        }
    }




}
