package me.deadlight.ezchestshop.guis.shared;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.data.TradeShopContainer;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.guis.shop.SettingsGUI;
import me.deadlight.ezchestshop.guis.tradeshop.TradeSettingsGUI;
import me.deadlight.ezchestshop.utils.holograms.ShopHologram;
import me.deadlight.ezchestshop.utils.holograms.TradeShopHologram;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.objects.EzTradeShop;
import me.deadlight.ezchestshop.utils.objects.ShopSettings;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.objects.TradeShopSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomMessageManageGUI {

    LanguageManager lm = new LanguageManager();

    public void showGUI(Player player, Block containerBlock, boolean isAdmin) {

        ContainerGui container = GuiData.getMessageManager();
        PaginatedGui paginatedGui = Gui.paginated()
                .title(Component.text(lm.customMessageManagerTitle()))
                .rows(container.getRows())
                .pageSize(container.getRows() * 9 - 9)
                .create();
        paginatedGui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        boolean isShop = ShopContainer.isShop(containerBlock.getLocation());
        boolean isTradeShop = TradeShopContainer.isTradeShop(containerBlock.getLocation());

        Map<Location, String> customMessages = null;
        if (isShop) {
            customMessages = ShopSettings.getAllCustomMessages(ShopContainer.getShop(containerBlock.getLocation()).getOwnerID().toString());
        } else if (isTradeShop) {
            customMessages = TradeShopSettings.getAllCustomMessages(TradeShopContainer.getTradeShop(containerBlock.getLocation()).getOwnerID().toString());
        }

        // Fill the bottom bar:
        paginatedGui.getFiller().fillBottom(container.getBackground());

        // Previous item
        if (container.hasItem("previous")) {
        ContainerGuiItem previous = container.getItem("previous")
                .setName(lm.customMessageManagerPreviousPageTitle())
                .setLore(lm.customMessageManagerPreviousPageLore());
        GuiItem previousItem = new GuiItem(previous.getItem(), event -> {
            event.setCancelled(true);
            paginatedGui.previous();
        });
        Utils.addItemIfEnoughSlots(paginatedGui, previous.getSlot(), previousItem);
        }
        // Next item
        if (container.hasItem("next")) {
            ContainerGuiItem next = container.getItem("next")
                    .setName(lm.customMessageManagerNextPageTitle())
                    .setLore(lm.customMessageManagerNextPageLore());
            GuiItem nextItem = new GuiItem(next.getItem(), event -> {
                event.setCancelled(true);
                paginatedGui.next();
            });
            Utils.addItemIfEnoughSlots(paginatedGui, next.getSlot(), nextItem);
        }
        // Back item
        if (container.hasItem("back")) {
            ContainerGuiItem back = container.getItem("back")
                    .setName(lm.backToSettingsButton());
            GuiItem doorItem = new GuiItem(back.getItem(), event -> {
                event.setCancelled(true);
                if (isShop) {
                    SettingsGUI settingsGUI = new SettingsGUI();
                    settingsGUI.showGUI(player, containerBlock, isAdmin);
                } else if (isTradeShop) {
                    TradeSettingsGUI tradeShopSettingsGUI = new TradeSettingsGUI();
                    tradeShopSettingsGUI.showGUI(player, containerBlock, isAdmin);
                }
            });
            Utils.addItemIfEnoughSlots(paginatedGui, back.getSlot(), doorItem);
        }

        if (container.hasItem("hologram-message-item")) {
            for (Map.Entry<Location, String> entry : customMessages.entrySet()) {
                Location loc = entry.getKey();
                String message = entry.getValue();
                // skip empty lines.
                if (message.replace("#,#", "").trim().isEmpty()) {
                    continue;
                }
                List<String> messages = Arrays.asList(message.split("#,#")).stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());

                ContainerGuiItem item = container.getItem("hologram-message-item");
                if (isShop) {
                    EzShop shop = ShopContainer.getShop(loc);
                    if (shop != null) {
                        item.setName(lm.customMessageManagerShopEntryTitle(shop.getShopItem()));
                    } else {
                        item.setName(lm.customMessageManagerShopEntryUnkownTitle());
                    }
                } else if (isTradeShop) {
                    EzTradeShop tradeShop = TradeShopContainer.getTradeShop(loc);
                    if (tradeShop != null) {
                        item.setName(lm.customMessageManagerShopEntryTradeTitle(tradeShop.getItem1(), tradeShop.getItem2()));
                    } else {
                        item.setName(lm.customMessageManagerShopEntryUnkownTitle());
                    }
                }
                item.setLore(lm.customMessageManagerShopEntryLore(loc, messages));

                GuiItem shopItem = new GuiItem(item.getItem(), event -> {
                    event.setCancelled(true);
                    if (event.isLeftClick()) {
                        showDeleteConfirm(player, containerBlock, isAdmin, loc);
                    } else if (event.isRightClick()) {
                        if (isShop) {
                            SettingsGUI.openCustomMessageEditor(player, loc);
                        } else if (isTradeShop) {
                            TradeSettingsGUI.openCustomMessageEditor(player, loc);
                        }
                    }
                });

                paginatedGui.addItem(shopItem);
            }
        }

        if (container.hasItem("modify-current-hologram")) {
            ContainerGuiItem modify = container.getItem("modify-current-hologram")
                    .setName(lm.customMessageManagerModifyCurrentHologramTitle())
                    .setLore(lm.customMessageManagerModifyCurrentHologramLore());
            GuiItem modifyItem = new GuiItem(modify.getItem(), event -> {
                event.setCancelled(true);
                if (event.isLeftClick()) {
                    showDeleteConfirm(player, containerBlock, isAdmin, containerBlock.getLocation());
                } else if (event.isRightClick()) {
                    if (isShop) {
                        SettingsGUI.openCustomMessageEditor(player, containerBlock.getLocation());
                    } else if (isTradeShop) {
                        TradeSettingsGUI.openCustomMessageEditor(player, containerBlock.getLocation());
                    }
                }
            });
            Utils.addItemIfEnoughSlots(paginatedGui, modify.getSlot(), modifyItem);
        }

        paginatedGui.open(player);


    }

    private void showDeleteConfirm(Player player, Block containerBlock, boolean isAdmin, Location loc) {
        boolean isShop = ShopContainer.isShop(containerBlock.getLocation());
        boolean isTradeShop = TradeShopContainer.isTradeShop(containerBlock.getLocation());

        Gui gui = new Gui(3, lm.customMessageManagerConfirmDeleteGuiTitle());
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.colorify("&d"));
        glassis.setItemMeta(glassmeta);
        GuiItem glasses = new GuiItem(glassis, event -> {
            event.setCancelled(true);
        });
        gui.getFiller().fill(glasses);

        ItemStack confirm = new ItemStack(Material.LIME_WOOL, 1);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(lm.customMessageManagerConfirmDeleteTitle());
        confirmMeta.setLore(lm.customMessageManagerConfirmDeleteLore());
        confirm.setItemMeta(confirmMeta);
        GuiItem confirmItem = new GuiItem(confirm, event -> {
            event.setCancelled(true);
            if (isShop) {
                ShopContainer.getShopSettings(loc).setCustomMessages(new ArrayList<>());
                gui.close(player);
                ShopHologram.getHologram(loc, player).setCustomHologramMessage(new ArrayList<>());
            } else if (isTradeShop) {
                TradeShopContainer.getTradeShopSettings(loc).setCustomMessages(new ArrayList<>());
                gui.close(player);
                TradeShopHologram.getHologram(loc, player).setCustomHologramMessage(new ArrayList<>());
            } else {
                gui.close(player);
            }
        });
        gui.setItem(2, 5, confirmItem);

        ItemStack back = new ItemStack(Material.DARK_OAK_DOOR, 1);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(lm.customMessageManagerBackToCustomMessageManagerTitle());
        backMeta.setLore(lm.customMessageManagerBackToCustomMessageManagerLore());
        back.setItemMeta(backMeta);
        GuiItem backItem = new GuiItem(back, event -> {
            event.setCancelled(true);
            showGUI(player, containerBlock, isAdmin);
        });

        gui.setItem(3, 1, backItem);

        gui.open(player);

    }


}
