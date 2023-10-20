package me.deadlight.ezchestshop.integrations;

import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import net.alex9849.arm.adapters.WGRegion;
import net.alex9849.arm.events.RestoreRegionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import net.alex9849.arm.events.UnsellRegionEvent;

public class AdvancedRegionMarket implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleRegionUnsell(UnsellRegionEvent event) {

        WGRegion region = event.getRegion().getRegion();

        for (EzShop shop : ShopContainer.getShops()) {
            if (region.contains(shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ())) {
                ShopContainer.deleteShop(shop.getLocation());
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleRegionRestore(RestoreRegionEvent event) {

        WGRegion region = event.getRegion().getRegion();

        for (EzShop shop : ShopContainer.getShops()) {
            if (region.contains(shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ())) {
                ShopContainer.deleteShop(shop.getLocation());
            }
        }

    }

}
