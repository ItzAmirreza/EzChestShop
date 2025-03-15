package me.deadlight.ezchestshop.listeners;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.events.PlayerTransactEvent;
import me.deadlight.ezchestshop.utils.holograms.BlockBoundHologram;
import me.deadlight.ezchestshop.utils.holograms.ShopHologram;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.RayTraceResult;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerCloseToChestListener implements Listener {


    private HashMap<Player, ShopHologram> inspectedShops = new HashMap<>();

    private HashMap<Player, Long> lastProcessed = new HashMap<>();
    private static final long COOLDOWN_MS = 250;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!Config.showholo || !hasMovedXYZ(event)) return;

        boolean alreadyRenderedHologram = false;
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastProcessed.getOrDefault(player, 0L);

        // Skip processing if cooldown hasn't elapsed
        if (currentTime - lastTime < COOLDOWN_MS) return;
        lastProcessed.put(player, currentTime);

        if (Config.holodistancing_show_item_first) {
            RayTraceResult result = player.rayTraceBlocks(5);
            boolean isLookingAtSameShop = false;
            // Make sure the player is looking at a shop
            if (result != null && Utils.isApplicableContainer(result.getHitBlock())) {
                Location loc = BlockBoundHologram.getShopChestLocation(result.getHitBlock());
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
                            inspectedShopHolo.showOnlyItem();
                            inspectedShopHolo.showAlwaysVisibleText();
                            inspectedShopHolo.removeInspectedShop();
                        }
                    }
                    // if the player is looking at a shop, and he is not inspecting it yet, then start inspecting it!
                    if (ShopHologram.hasHologram(loc, player) && !shopHolo.hasInspector()) {
                        shopHolo.showTextAfterItem();
                        shopHolo.setItemDataVisible(player.isSneaking());
                        shopHolo.setAsInspectedShop();
                        alreadyRenderedHologram = true;
                        isLookingAtSameShop = true;
                    }
                }
            }
            // if the player is not looking at a shop, then remove the old one if he was inspecting one
            if (ShopHologram.isPlayerInspectingShop(player) && !isLookingAtSameShop) {
                ShopHologram shopHolo = ShopHologram.getInspectedShopHologram(player);
                if (ShopContainer.isShop(shopHolo.getLocation())) {
                    shopHolo.showOnlyItem();
                    shopHolo.showAlwaysVisibleText();
                }
                shopHolo.removeInspectedShop();
            }
        }

        if (alreadyRenderedHologram) return;

        Location loc = player.getLocation();
        // Use chunk-based lookup (2-chunk radius ≈ 32 blocks)
        List<EzShop> shops = ShopContainer.getNearbyShops(loc, 2);
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
                if (ShopHologram.hasHologram(ezShop.getLocation(), player)) continue;

                Block target = ezShop.getLocation().getWorld().getBlockAt(ezShop.getLocation());
                if (target == null || !Utils.isApplicableContainer(target)) return;

                ShopHologram shopHolo = ShopHologram.getHologram(ezShop.getLocation(), player);
                if (Config.holodistancing_show_item_first) {
                    shopHolo.showOnlyItem();
                    shopHolo.showAlwaysVisibleText();
                } else shopHolo.show();


            }
            // Hide the Hologram that is too far away from the player
            else if (dist > Config.holodistancing_distance + 1
                    && dist < Config.holodistancing_distance + 3) {
                // Hide the Hologram
                if (ShopHologram.hasHologram(ezShop.getLocation(), player))
                    ShopHologram.getHologram(ezShop.getLocation(), player).hide();
            }
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ShopHologram.hideAll(player);
        inspectedShops.remove(player);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ShopHologram.hideAll(player);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (ShopHologram.isPlayerInspectingShop(player)) {
            ShopHologram shopHolo = ShopHologram.getInspectedShopHologram(player);
            if (shopHolo != null && ShopContainer.isShop(shopHolo.getLocation())) {
                shopHolo.setItemDataVisible(event.isSneaking());
            } else {
                if (shopHolo != null) shopHolo.removeInspectedShop();
                inspectedShops.remove(player);
            }
        } else if (!Config.holodistancing_show_item_first) {
            // When holodistancing_show_item_first is off, the shop needs to be queried separately.
            // It's less reactive but it works.
            if (!event.isSneaking() && inspectedShops.containsKey(player)) {
                ShopHologram shopHolo = inspectedShops.get(player);
                if (shopHolo != null && ShopContainer.isShop(shopHolo.getLocation()))
                    shopHolo.setItemDataVisible(false);
                else inspectedShops.remove(player);
            }
            RayTraceResult result = player.rayTraceBlocks(5);
            if (result == null)
                return;
            Block block = result.getHitBlock();
            if (block == null)
                return;
            Location loc = block.getLocation();
            if (ShopContainer.isShop(loc)) {
                ShopHologram shopHolo = ShopHologram.getHologram(loc, player);
                if (event.isSneaking()) {
                    shopHolo.setItemDataVisible(true);
                    inspectedShops.put(player, shopHolo);
                }
            }
        }
    }

    @EventHandler
    public void onShopContentsChangeByBlock(InventoryMoveItemEvent event) {
        if (!event.isCancelled() && ShopContainer.isShop(event.getDestination().getLocation())) {
            EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(event.getDestination().getLocation()), 1);
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
                EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(shopHolo.getLocation()), 1);
            });
        }
    }

    @EventHandler
    public void onShopCapacityChangeByBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && (event.getBlockPlaced().getType() == Material.CHEST || event.getBlockPlaced().getType() == Material.TRAPPED_CHEST)) {
            EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
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
//            if (ShopContainer.isShop(location)) {
//                EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(location), 1);
//            }
//        }
//    }

    @EventHandler
    public void onShopTransactionCapacityChange(PlayerTransactEvent event) {
        EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(event.getContainerBlock().getLocation()), 1);
    }

    private void inventoryModifyEventHandler(boolean cancelled, HumanEntity whoClicked) {
        if (!cancelled) {
            ShopHologram.getViewedHolograms((Player) whoClicked).forEach(shopHolo -> {
                EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> ShopHologram.updateInventoryReplacements(shopHolo.getLocation()), 1);
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