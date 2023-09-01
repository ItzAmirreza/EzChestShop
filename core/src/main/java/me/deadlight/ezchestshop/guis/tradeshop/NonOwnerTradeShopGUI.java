package me.deadlight.ezchestshop.guis.tradeshop;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.data.TradeShopContainer;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.utils.SignMenuFactory;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.objects.EzTradeShop;
import me.deadlight.ezchestshop.utils.objects.TradeShopSettings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
//mojodi
//balance

public class NonOwnerTradeShopGUI {

    public NonOwnerTradeShopGUI() {}

    public void showGUI(Player player, PersistentDataContainer data, Block containerBlock) {
        LanguageManager lm = new LanguageManager();
        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
        String shopOwner = offlinePlayerOwner.getName();
        EzTradeShop tradeShop = TradeShopContainer.getTradeShop(containerBlock.getLocation());
        if (shopOwner == null) {
            boolean result = Utils.reInstallNamespacedKeyValues(data, containerBlock.getLocation());
            if (!result) {
                player.sendMessage(lm.chestShopProblem());
                return;
            }
            containerBlock.getState().update();
            EzShop shop = ShopContainer.getShop(containerBlock.getLocation());
            shopOwner = Bukkit.getOfflinePlayer(shop.getOwnerID()).getName();
            if (shopOwner == null) {
                player.sendMessage(lm.chestShopProblem());
                System.out.println("EzChestShop ERROR: Shop owner is STILL null. Please report this to the EzChestShop developer for further investigation.");
                return;
            }
        }

        ContainerGui container = GuiData.getTradeShop();

        Gui gui = new Gui(container.getRows(), lm.guiNonOwnerTitle(shopOwner));
        gui.getFiller().fill(container.getBackground());

        ItemStack shop_item1 = ItemUtils.decodeItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item1"), PersistentDataType.STRING)).clone();
        ItemStack shop_item2 = ItemUtils.decodeItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item2"), PersistentDataType.STRING)).clone();
        if (container.hasItem("item1")) {
            ItemStack item1 = shop_item1.clone();
            ItemMeta item1meta = item1.getItemMeta();
            // Set the lore and keep the old one if available
            if (item1meta.hasLore()) {
                List<String> prevLore = item1meta.getLore();
                prevLore.add("");
                List<String> mainItemLore = Arrays.asList("");
                prevLore.addAll(mainItemLore);
                item1meta.setLore(prevLore);
            } else {
                List<String> mainItemLore = Arrays.asList("");
                item1meta.setLore(mainItemLore);
            }
            item1.setItemMeta(item1meta);
            GuiItem guiitem = new GuiItem(item1, event -> {
                event.setCancelled(true);
                if (tradeShop.getSettings().getTradeDirection() == TradeShopSettings.TradeDirection.BOTH ||
                        tradeShop.getSettings().getTradeDirection() == TradeShopSettings.TradeDirection.ITEM2_TO_ITEM1) {
                    TradeShopContainer.buyItem1(containerBlock, 1, shop_item1, shop_item2, player, offlinePlayerOwner, data);
                    showGUI(player, data, containerBlock);
                }
            });
            InventoryUtils.addItemIfEnoughSlots(gui, container.getItem("item1").getSlot(), guiitem);
        }
        if (container.hasItem("item2")) {
            ItemStack item2 = shop_item2.clone();
            ItemMeta item2meta = item2.getItemMeta();
            // Set the lore and keep the old one if available
            if (item2meta.hasLore()) {
                List<String> prevLore = item2meta.getLore();
                prevLore.add("");
                List<String> mainItemLore = Arrays.asList("");
                prevLore.addAll(mainItemLore);
                item2meta.setLore(prevLore);
            } else {
                List<String> mainItemLore = Arrays.asList("");
                item2meta.setLore(mainItemLore);
            }
            item2.setItemMeta(item2meta);
            GuiItem guiitem = new GuiItem(item2, event -> {
                event.setCancelled(true);
                if (tradeShop.getSettings().getTradeDirection() == TradeShopSettings.TradeDirection.BOTH ||
                        tradeShop.getSettings().getTradeDirection() == TradeShopSettings.TradeDirection.ITEM1_TO_ITEM2) {
                    TradeShopContainer.buyItem2(containerBlock, 1, shop_item1, shop_item2, player, offlinePlayerOwner, data);
                    showGUI(player, data, containerBlock);
                }
            });
            InventoryUtils.addItemIfEnoughSlots(gui, container.getItem("item2").getSlot(), guiitem);
        }

        if (container.hasItem("trade-direction-item1toitem2") || container.hasItem("trade-direction-item2toitem1") ||
                container.hasItem("trade-direction-both") || container.hasItem("trade-direction-disabled")) {
            ItemStack tradeDirectionItem;
            ContainerGuiItem tradeDirectionContainerGuiItem;
            switch (tradeShop.getSettings().getTradeDirection()) {
                case ITEM1_TO_ITEM2:
                    tradeDirectionContainerGuiItem = container.getItem("trade-direction-item1toitem2");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    break;
                case ITEM2_TO_ITEM1:
                    tradeDirectionContainerGuiItem = container.getItem("trade-direction-item2toitem1");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    break;
                case BOTH:
                    tradeDirectionContainerGuiItem = container.getItem("trade-direction-both");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    break;
                default:
                    tradeDirectionContainerGuiItem = container.getItem("trade-direction-disabled");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
            }
            GuiItem tradeDirectionGuiItem = new GuiItem(tradeDirectionItem, event -> {
                event.setCancelled(true);
            });
            InventoryUtils.addItemIfEnoughSlots(gui, tradeDirectionContainerGuiItem.getSlot(), tradeDirectionGuiItem);
        }

        container.getItemKeys().forEach(key -> {
            if (key.startsWith("decorative-")) {

                ContainerGuiItem decorativeItemStack = container.getItem(key).setName(StringUtils.colorify("&d"));

                GuiItem buyItem = new GuiItem(decorativeItemStack.getItem(), event -> {
                    event.setCancelled(true);
                });

                InventoryUtils.addItemIfEnoughSlots(gui, decorativeItemStack.getSlot(), buyItem);
            }
        });


        gui.open(player);



    }
}
