package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Holograms.ShopHologram;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Utils;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import me.deadlight.ezchestshop.Utils.WorldGuard.WorldGuardUtils;
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
                if (Utils.isShulkerBox(event.getBlock())) {
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


        }
    }

    private ItemMeta addLore(ItemMeta meta, PersistentDataContainer container) {
        if (Config.settings_add_shulkershop_lore) {
            List<String> nlore = lm.shulkerboxLore(Bukkit.getOfflinePlayer(UUID.fromString(getContainerString(container, "owner"))).getName(),
                    Utils.getFinalItemName(Utils.decodeItem(getContainerString(container, "item"))),
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
    }

}
