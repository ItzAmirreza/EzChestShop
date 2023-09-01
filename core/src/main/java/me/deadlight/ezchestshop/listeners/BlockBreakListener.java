package me.deadlight.ezchestshop.listeners;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.TradeShopContainer;
import me.deadlight.ezchestshop.utils.BlockMaterialUtils;
import me.deadlight.ezchestshop.utils.ItemUtils;
import me.deadlight.ezchestshop.utils.holograms.ShopHologram;
import me.deadlight.ezchestshop.utils.holograms.TradeShopHologram;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.objects.EzTradeShop;
import me.deadlight.ezchestshop.utils.worldguard.FlagRegistry;
import me.deadlight.ezchestshop.utils.worldguard.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    private static LanguageManager lm = new LanguageManager();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {

        if (Utils.blockBreakMap.containsKey(event.getPlayer().getName())) {
            Collection<Entity> entityList = event.getBlock().getLocation().getWorld().getNearbyEntities(event.getBlock().getLocation(), 2, 2 ,2);
            for (Entity en : entityList) {
                if (en instanceof Item) {
                    Item item = (Item) en;
                    if (item.getItemStack().getType() == Material.CHEST || item.getItemStack().getType() == Material.TRAPPED_CHEST) {
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


        if (!event.isCancelled()) {
            preventShopBreak(event);
            if (event.isCancelled()) {
                return;
            }

            Location loc = event.getBlock().getLocation();
            boolean isPartOfShop = Utils.isPartOfTheChestShop(event.getBlock().getLocation()) != null;
            if (isPartOfShop) {
                loc = Utils.isPartOfTheChestShop(event.getBlock().getLocation()).getLocation();
            }
            if (ShopContainer.isShop(loc) || isPartOfShop) {
                if (BlockMaterialUtils.isShulkerBox(event.getBlock())) {
                    if (event.isDropItems()) {
                        event.setDropItems(false);
                        ItemStack shulker = event.getBlock().getDrops().stream().findFirst().get();
                        ItemMeta meta = shulker.getItemMeta();
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        PersistentDataContainer bcontainer = ((TileState) event.getBlock().getState()).getPersistentDataContainer();
                        if (bcontainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) != null) {
                            container = ShopContainer.copyContainerData(bcontainer, container);
                            meta = addLore(meta, container);
                            shulker.setItemMeta(meta);
                            loc.getWorld().dropItemNaturally(loc, shulker);
                            if (Config.holodistancing) {
                                ShopHologram.hideForAll(event.getBlock().getLocation());
                            }
                        }
                    }
                }
                ShopContainer.deleteShop(loc);
            }

            boolean isPartOfTradeShop = Utils.isPartOfTheChestTradeShop(loc) != null;
            if (isPartOfTradeShop) {
                loc = Utils.isPartOfTheChestTradeShop(loc).getLocation();
            }
            if (TradeShopContainer.isTradeShop(loc) || isPartOfTradeShop) {
                if (BlockMaterialUtils.isShulkerBox(event.getBlock())) {
                    if (event.isDropItems()) {
                        event.setDropItems(false);
                        ItemStack shulker = event.getBlock().getDrops().stream().findFirst().get();
                        ItemMeta meta = shulker.getItemMeta();
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        PersistentDataContainer bcontainer = ((TileState) event.getBlock().getState()).getPersistentDataContainer();
                        if (bcontainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) != null) {
                            container = TradeShopContainer.copyContainerData(bcontainer, container);
                            meta = addLore(meta, container);
                            shulker.setItemMeta(meta);
                            loc.getWorld().dropItemNaturally(loc, shulker);
                            if (Config.holodistancing) {
                                TradeShopHologram.hideForAll(event.getBlock().getLocation());
                            }
                        }
                    }
                }
                TradeShopContainer.deleteShop(loc);
            }
        }
    }

    private ItemMeta addLore(ItemMeta meta, PersistentDataContainer container) {
        if (Config.settings_add_shulkershop_lore) {
            List<String> nlore = lm.shulkerboxLore(Bukkit.getOfflinePlayer(UUID.fromString(getContainerString(container, "owner"))).getName(),
                    ItemUtils.getFinalItemName(ItemUtils.decodeItem(getContainerString(container, "item"))),
                    getContainerDouble(container, "buy"),
                    getContainerDouble(container, "sell"));
            meta.setLore(nlore);
        }
        return meta;
    }

    private String getContainerString(PersistentDataContainer container, String key) {
        return container.get(new NamespacedKey(EzChestShop.getPlugin(), key), PersistentDataType.STRING);
    }

    private Double getContainerDouble(PersistentDataContainer container, String key) {
        return container.get(new NamespacedKey(EzChestShop.getPlugin(), key), PersistentDataType.DOUBLE);
    }

    private void preventShopBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        boolean isPartOfShop = Utils.isPartOfTheChestShop(loc) != null;
        if (isPartOfShop) {
            loc = Utils.isPartOfTheChestShop(loc).getLocation();
        }
        if (ShopContainer.isShop(loc) || isPartOfShop) {
            boolean adminshop = ShopContainer.getShop(loc).getSettings().isAdminshop();
            Player player = event.getPlayer();
            if (EzChestShop.worldguard) {
                if (adminshop) {
                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_ADMIN_SHOP, player)) {
                        player.sendMessage(lm.notAllowedToCreateOrRemove());
                        event.setCancelled(true);
                    }
                } else {
                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_SHOP, player)) {
                        player.sendMessage(lm.notAllowedToCreateOrRemove());
                        event.setCancelled(true);
                    }
                }
            }

            //shop protection section
            if (Config.shopProtection) {

                if (!event.getPlayer().hasPermission("ecs.admin")) {
                    //check if player is owner of shop
                    EzShop shop = ShopContainer.getShop(loc);
                    if (!shop.getOwnerID().equals(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(lm.cannotDestroyShop());
                    }
                }
            }

        }
        boolean isPartOfTradeShop = Utils.isPartOfTheChestTradeShop(loc) != null;
        if (isPartOfTradeShop) {
            loc = Utils.isPartOfTheChestTradeShop(loc).getLocation();
        }
        if (TradeShopContainer.isTradeShop(loc) || isPartOfTradeShop) {
            boolean adminshop = TradeShopContainer.getTradeShop(loc).getSettings().isAdminshop();
            Player player = event.getPlayer();
            if (EzChestShop.worldguard) {
                if (adminshop) {
                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_ADMIN_TRADE_SHOP, player)) {
                        player.sendMessage(lm.notAllowedToCreateOrRemove());
                        event.setCancelled(true);
                    }
                } else {
                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_TRADE_SHOP, player)) {
                        player.sendMessage(lm.notAllowedToCreateOrRemove());
                        event.setCancelled(true);
                    }
                }
            }

            //shop protection section
            if (Config.shopProtection) {

                if (!event.getPlayer().hasPermission("ecs.admin")) {
                    //check if player is owner of shop
                    EzTradeShop tradeShop = TradeShopContainer.getTradeShop(loc);
                    if (!tradeShop.getOwnerID().equals(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(lm.cannotDestroyShop());
                    }
                }
            }

        }
    }

}
