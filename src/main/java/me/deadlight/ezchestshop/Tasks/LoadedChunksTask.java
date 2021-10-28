package me.deadlight.ezchestshop.Tasks;

import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LoadedChunksTask {

    public static void startTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(EzChestShop.getPlugin(), (Runnable)new Runnable() {
            @Override
            public void run() {
                //fix credited to Huke
                for (final Location shop : ShopContainer.getShops()) {
                    final World world = shop.getWorld();
                    if (world != null && world.isChunkLoaded(shop.getBlockX() >> 4, shop.getBlockZ() >> 4) && (shop.getBlock().isEmpty() || !Utils.isApplicableContainer(shop.getBlock()))) {
                        ShopContainer.deleteShop(shop);
                    }
                }
            }
        }, 0L, 200L);
    }

}
