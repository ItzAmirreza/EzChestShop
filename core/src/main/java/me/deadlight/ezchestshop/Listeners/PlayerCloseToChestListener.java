package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Holograms.ShopHologram;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Pair;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
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
        if (Config.showholo) {
            boolean alreadyRenderedHologram = false;
            Player player = event.getPlayer();
            if (Config.holodistancing_show_item_first) {
                RayTraceResult result = player.rayTraceBlocks(5);
                boolean isLookingAtSameShop = false;
                // Make sure the player is looking at a shop
                if (result != null) {
                    Block target = result.getHitBlock();
                    if (Utils.isApplicableContainer(target)) {
                        Location loc = getShopChestLocation(target);
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
                                    inspectedShopHolo.removeInspectedShop();
                                }
                            }
                            // if the player is looking at a shop and he is not inspecting it yet, then start inspecting it!
                            if (ShopHologram.hasHologram(loc, player) && !shopHolo.hasInspector()) {
                                EzChestShop.logDebug("----------------------");
                                EzChestShop.logDebug("Player is looking at shop");
                                shopHolo.hide();
                                shopHolo.show();
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
                    if (target != null) {
                        if (Utils.isApplicableContainer(target)) {

                            if (Config.holodistancing_show_item_first) {
                                EzChestShop.logDebug("----------------------");
                                EzChestShop.logDebug("Player: " + player.getName() + " is close to a shop");
                                ShopHologram.getHologram(ezShop.getLocation(), player).showOnlyItem();
                            } else {
                                EzChestShop.logDebug("----------------------");
                                EzChestShop.logDebug("Player: " + player.getName() + " is close to a shop and item first is off.");
                                ShopHologram.getHologram(ezShop.getLocation(), player).show();
                            }

                        }
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

    private Location getShopChestLocation(Block target ) {
        Location loc = target.getLocation();
        Inventory inventory = ((Container) target.getState()).getInventory();
        if (inventory instanceof DoubleChestInventory) {
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();

            if (leftchest.getPersistentDataContainer().has(
                    new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                loc = leftchest.getLocation();
            } else if (rightchest.getPersistentDataContainer().has(
                    new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                loc = rightchest.getLocation();
            }
        }
        return loc;
    }

}