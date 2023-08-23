package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Events.PlayerTransactEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Holograms.BlockBoundHologram;
import me.deadlight.ezchestshop.Utils.Holograms.ShopHologram;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Pair;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Debug;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerCloseToChestListener implements Listener {


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!Config.showholo) {
            return;
        }
        boolean alreadyRenderedHologram = false;
        Player player = event.getPlayer();
        if (Config.holodistancing_show_item_first) {
            RayTraceResult result = player.rayTraceBlocks(5);
            boolean isLookingAtSameShop = false;
            // Make sure the player is looking at a shop
            if (result != null) {
                Block target = result.getHitBlock();
                if (Utils.isApplicableContainer(target)) {
                    Location loc = BlockBoundHologram.getShopChestLocation(target);
                    if (ShopContainer.isShop(loc)) {
                        // Create a shop Hologram, so it can be used later
                        // required to be called here, cause the inspection needs it already.
                        ShopHologram shopHolo = ShopHologram.getHologram(loc, player);

                        // if the player is looking directly at a shop, he is inspecting it.
                        // If he has been inspecting a shop before, then we need to check if he is looking at the same shop
                        // or a different one.
                        if (ShopHologram.isPlayerInspectingShop(player)) {
                            if (ShopHologram.getInspectedShopHologram(player).getLocation().equals(loc)) {
                                // if the player is looking at the same shop, then don't do anything
                                isLookingAtSameShop = true;
                            } else {
                                // if the player is looking at a different shop, then remove the old one
                                // and only show the item
                                ShopHologram inspectedShopHolo = ShopHologram.getInspectedShopHologram(player);
                                EzChestShop.logDebug("----------------------");
                                EzChestShop.logDebug("Player is now looking at a different shop");
                                inspectedShopHolo.showOnlyItem();
                                inspectedShopHolo.showAlwaysVisibleText();
                                inspectedShopHolo.removeInspectedShop();
                            }
                        }
                        // if the player is looking at a shop, and he is not inspecting it yet, then start inspecting it!
                        if (ShopHologram.hasHologram(loc, player) && !shopHolo.hasInspector()) {
                            EzChestShop.logDebug("----------------------");
                            EzChestShop.logDebug("Player is looking at shop");
                            shopHolo.showTextAfterItem();
                            shopHolo.setItemDataVisible(player.isSneaking());
                            shopHolo.setAsInspectedShop();
                            alreadyRenderedHologram = true;
                            isLookingAtSameShop = true;
                        }
                    }
                }
            }
            // if the player is not looking at a shop, then remove the old one if he was inspecting one
            if (ShopHologram.isPlayerInspectingShop(player) && !isLookingAtSameShop) {
                ShopHologram shopHolo = ShopHologram.getInspectedShopHologram(player);
                if (ShopContainer.isShop(shopHolo.getLocation())) {
                    EzChestShop.logDebug("----------------------");
                    EzChestShop.logDebug("Player is no longer inspecting shop");
                    shopHolo.showOnlyItem();
                    shopHolo.showAlwaysVisibleText();
                }
                shopHolo.removeInspectedShop();
            }
        }

        if (alreadyRenderedHologram || !hasMovedXYZ(event)) {
            return;
        }

        Location loc = player.getLocation();
        List<EzShop> shops = ShopContainer.getShops().stream()
                .filter(ezShop -> ezShop.getLocation() != null
                        && loc.getWorld().equals(ezShop.getLocation().getWorld())
                        && loc.distance(ezShop.getLocation()) < Config.holodistancing_distance + 5)
                .collect(Collectors.toList());
        for (EzShop ezShop : shops) {
            if (EzChestShop.slimefun) {
                if (BlockStorage.hasBlockInfo(ezShop.getLocation())) {
                    ShopContainer.deleteShop(ezShop.getLocation());
                    continue;
                }
            }
            double dist = loc.distance(ezShop.getLocation());
            // Show the Hologram if Player close enough
            if (dist < Config.holodistancing_distance) {
                if (ShopHologram.hasHologram(ezShop.getLocation(), player))
                    continue;

                Block target = ezShop.getLocation().getWorld().getBlockAt(ezShop.getLocation());
                if (target == null || !Utils.isApplicableContainer(target)) {
                    return;
                }
                if (Config.holodistancing_show_item_first) {
                    EzChestShop.logDebug("----------------------");
                    EzChestShop.logDebug("Player: " + player.getName() + " is close to a shop");
                    ShopHologram shopHologram = ShopHologram.getHologram(ezShop.getLocation(), player);
                    shopHologram.showOnlyItem();
                    shopHologram.showAlwaysVisibleText();
                } else {
                    EzChestShop.logDebug("----------------------");
                    EzChestShop.logDebug("Player: " + player.getName() + " is close to a shop and item first is off.");
                    ShopHologram shopHolo = ShopHologram.getHologram(ezShop.getLocation(), player);
                    shopHolo.show();
                    shopHolo.setItemDataVisible(player.isSneaking());
                }

            }
            // Hide the Hologram that is too far away from the player
            else if (dist > Config.holodistancing_distance + 1
                    && dist < Config.holodistancing_distance + 3) {
                // Hide the Hologram
                if (ShopHologram.hasHologram(ezShop.getLocation(), player)) {
                    EzChestShop.logDebug("----------------------");
                    EzChestShop.logDebug("Player: " + player.getName() + " is out of reach of shop");
                    ShopHologram.getHologram(ezShop.getLocation(), player).hide();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        EzChestShop.logDebug("----------------------");
        EzChestShop.logDebug("Player: " + player.getName() + " logged out");
        ShopHologram.hideAll(player);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        EzChestShop.logDebug("----------------------");
        EzChestShop.logDebug("Player: " + player.getName() + " teleported");
        ShopHologram.hideAll(player);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (ShopHologram.isPlayerInspectingShop(player)) {
            ShopHologram shopHolo = ShopHologram.getInspectedShopHologram(player);
            if (event.isSneaking()) {
                shopHolo.setItemDataVisible(true);
            } else {
                shopHolo.setItemDataVisible(false);
            }
        }
    }

    @EventHandler
    public void onShopContentsChangeByBlock(InventoryMoveItemEvent event) {
        if (!event.isCancelled() && ShopContainer.isShop(event.getDestination().getLocation())) {
            EzChestShop.logDebug("Hopper Destination: " + event.getDestination().getLocation());
            Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(event.getDestination().getLocation()), 1);
        }
    }

    @EventHandler
    public void onInventoryChangeByPlayerItemClick(InventoryClickEvent event) {
        inventoryModifyEventHandler(event.isCancelled(), event.getWhoClicked());
    }

    @EventHandler
    public void onInventoryChangeByPlayerItemDrag(InventoryDragEvent event) {
        inventoryModifyEventHandler(event.isCancelled(), event.getWhoClicked());
    }

    @EventHandler
    public void onInventoryChangeByPlayerItemDrop(PlayerDropItemEvent event) {
        inventoryModifyEventHandler(event.isCancelled(), event.getPlayer());
    }

    @EventHandler
    public void onInventoryChangeByPlayerItemPickup(EntityPickupItemEvent event) {
        if (!event.isCancelled() && event.getEntity().getType() == EntityType.PLAYER) {
            ShopHologram.getViewedHolograms((Player) event.getEntity()).forEach(shopHolo -> {
                Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(shopHolo.getLocation()), 1);
            });
        }
    }

    @EventHandler
    public void onShopCapacityChangeByBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && (event.getBlockPlaced().getType() == Material.CHEST || event.getBlockPlaced().getType() == Material.TRAPPED_CHEST)) {
            Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                Location location = BlockBoundHologram.getShopChestLocation(event.getBlockPlaced());
                if (ShopContainer.isShop(location)) {
                    ShopHologram.updateInventoryReplacements(location);
                }
            }, 1);
        }
    }

    //TODO, breaking blocks doesn't update the hologram, in fact the hologram gets hidden and the shop needs to be reopened to show the hologram again at all.
    // this is because the shop gets removed in BlockBreakListener.
//    @EventHandler
//    public void onShopCapacityChange(BlockBreakEvent event) {
//        if (!event.isCancelled() && (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST)) {
//            Location location = BlockBoundHologram.getShopChestLocation(event.getBlock());
//            EzChestShop.logDebug("ShopChestLocation (break): " + location);
//            if (ShopContainer.isShop(location)) {
//                Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(location), 1);
//            }
//        }
//    }

    @EventHandler
    public void onShopTransactionCapacityChange(PlayerTransactEvent event) {
        Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(event.getContainerBlock().getLocation()), 1);
    }

    private void inventoryModifyEventHandler(boolean cancelled, HumanEntity whoClicked) {
        if (!cancelled) {
            ShopHologram.getViewedHolograms((Player) whoClicked).forEach(shopHolo -> {
                Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(shopHolo.getLocation()), 1);
            });
        }
    }

    private boolean hasMovedXYZ(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX())
            return true;
        if (from.getY() != to.getY())
            return true;
        if (from.getZ() != to.getZ())
            return true;
        return false;
    }


}