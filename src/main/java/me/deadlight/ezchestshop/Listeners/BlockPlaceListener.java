package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        placeBlock(block, item);

    }

    private void placeBlock(Block block, ItemStack shulker) {
        if (Utils.isShulkerBox(shulker.getType())) {
            if (shulker.hasItemMeta()) {
                ItemMeta meta = shulker.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) != null) {
                    TileState state = ((TileState) block.getState());
                    PersistentDataContainer bcontainer = state.getPersistentDataContainer();
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER));
                    bcontainer.set(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING,
                            container.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
                    state.update();
                    ShopContainer.loadShop(block.getLocation(), bcontainer);
                }
            }
        }
    }
}
