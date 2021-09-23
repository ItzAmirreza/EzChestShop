package me.deadlight.ezchestshop.Tasks;

import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LoadedChunksTask {

    public static void startTask() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(EzChestShop.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Location shop : ShopContainer.getShops()) {

                    if (shop.isWorldLoaded()) {
                        if (shop.getChunk().isLoaded()) {
                            if (shop.getBlock().isEmpty() || !Utils.isApplicableContainer(shop.getBlock())) {
                                ShopContainer.deleteShop(shop);
                            }
                        }
                    }

                }
            }
        }, 0, 20 * 10);

    }

}
