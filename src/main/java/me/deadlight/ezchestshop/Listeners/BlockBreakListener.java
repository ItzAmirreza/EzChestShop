package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredListener;

import java.util.Collection;

public class BlockBreakListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (Utils.blockBreakMap.containsKey(event.getPlayer().getName())) {
            Collection<Entity> entityList = event.getBlock().getLocation().getWorld().getNearbyEntities(event.getBlock().getLocation(), 2, 2 ,2);
            for (Entity en : entityList) {
                if (en instanceof Item) {
                    Item item = (Item) en;
                    if (item.getItemStack().getType() == Material.CHEST) {
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
    }
}
