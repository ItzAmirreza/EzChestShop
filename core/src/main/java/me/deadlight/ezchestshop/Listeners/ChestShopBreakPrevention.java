package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ChestShopBreakPrevention implements Listener {


    //BlockBreak of this section is handled in BlockBreakListener.java
    @EventHandler
    public void explosion(EntityExplodeEvent event) {
        if (!Config.shopProtection) {
            return;
        }
        //check if any of the blocks that are going to be destroyed are shops
        for (Block block : event.blockList()) {
            if (ShopContainer.isShop(block.getLocation())) {
                event.setCancelled(true);
                break;
            }
        }

    }


    @EventHandler
    public void burn(BlockBurnEvent event) {
        if (!Config.shopProtection) {
            return;
        }
        if (ShopContainer.isShop(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!Config.shopProtection) {
            return;
        }
        for (Block block : event.getBlocks()) {
            if (ShopContainer.isShop(block.getLocation())) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!Config.shopProtection) {
            return;
        }
        for (Block block : event.getBlocks()) {
            if (ShopContainer.isShop(block.getLocation())) {
                event.setCancelled(true);
                break;
            }
        }
    }

}
