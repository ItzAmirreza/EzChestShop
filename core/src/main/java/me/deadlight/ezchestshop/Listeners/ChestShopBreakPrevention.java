package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Utils.Utils;
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
    public void onExplosion(EntityExplodeEvent event) {
        if (!Config.shopProtection) {
            return;
        }

        event.blockList().removeIf(block -> ShopContainer.isShop(block.getLocation()));
        event.blockList().removeIf(block -> Utils.isPartOfTheChestShop(block.getLocation()) != null);


    }


    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        if (!Config.shopProtection) {
            return;
        }
        if (ShopContainer.isShop(event.getBlock().getLocation()) || Utils.isPartOfTheChestShop(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!Config.shopProtection) {
            return;
        }
        for (Block block : event.getBlocks()) {
            if (ShopContainer.isShop(block.getLocation()) || Utils.isPartOfTheChestShop(block.getLocation()) != null) {
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
            if (ShopContainer.isShop(block.getLocation()) || Utils.isPartOfTheChestShop(block.getLocation()) != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

}
