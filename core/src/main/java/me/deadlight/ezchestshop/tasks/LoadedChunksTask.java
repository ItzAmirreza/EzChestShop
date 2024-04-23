package me.deadlight.ezchestshop.tasks;

import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class LoadedChunksTask {

    public static void startTask() {
        EzChestShop.getScheduler().scheduleSyncRepeatingTask(() -> {
            //fix credited to Huke
            for (final EzShop shop : ShopContainer.getShops()) {
                final World world = shop.getLocation().getWorld();
                if (world != null && world.isChunkLoaded(shop.getLocation().getBlockX() >> 4, shop.getLocation().getBlockZ() >> 4) && (shop.getLocation().getBlock().isEmpty() || !Utils.isApplicableContainer(shop.getLocation().getBlock()))) {
                    ShopContainer.deleteShop(shop.getLocation());
                }
            }
        }, 0L, 200L);
    }

}
