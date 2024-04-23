package me.deadlight.ezchestshop.listeners;

import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Directional;
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

    @EventHandler
    public void onBlockDispenserPlace(BlockDispenseEvent  event) {
        BlockState state = event.getBlock().getState();
        if (state instanceof Dispenser) {
            Dispenser dispenser = ((Dispenser) state);
            final Directional directional = (Directional) dispenser.getBlockData();
            EzChestShop.getScheduler().scheduleSyncDelayedTask(() -> {
                Block block = event.getBlock().getRelative(directional.getFacing());
                ItemStack item = event.getItem();
                placeBlock(block, item);
            }, 5);
        }
    }

    private void placeBlock(Block block, ItemStack shulker) {
        if (Utils.isShulkerBox(shulker.getType()) && Utils.isShulkerBox(block)) {
            if (shulker.hasItemMeta()) {
                ItemMeta meta = shulker.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) != null) {
                    TileState state = ((TileState) block.getState());
                    PersistentDataContainer bcontainer = state.getPersistentDataContainer();
                    bcontainer = ShopContainer.copyContainerData(container, bcontainer);
                    state.update();
                    ShopContainer.loadShop(block.getLocation(), bcontainer);
                }
            }
        }
    }
}
