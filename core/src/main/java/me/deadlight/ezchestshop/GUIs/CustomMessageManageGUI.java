package me.deadlight.ezchestshop.GUIs;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.Data.GUI.ContainerGui;
import me.deadlight.ezchestshop.Data.GUI.ContainerGuiItem;
import me.deadlight.ezchestshop.Data.GUI.GuiData;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Utils.Holograms.ShopHologram;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
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

        Map<Location, String> customMessages = ShopSettings.getAllCustomMessages(ShopContainer.getShop(containerBlock.getLocation()).getOwnerID().toString());

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
                SettingsGUI settingsGUI = new SettingsGUI();
                settingsGUI.showGUI(player, containerBlock, isAdmin);
            });
            Utils.addItemIfEnoughSlots(paginatedGui, back.getSlot(), doorItem);
        }

        if (container.hasItem("hologram-message-item")) {
            for (Map.Entry<Location, String> entry : customMessages.entrySet()) {
                Location loc = entry.getKey();
                String message = entry.getValue();
                List<String> messages = Arrays.asList(message.split("#,#")).stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());

                ContainerGuiItem item = container.getItem("hologram-message-item");
                EzShop ezShop = ShopContainer.getShop(loc);
                if (ezShop != null) {
                    item.setName(lm.customMessageManagerShopEntryTitle(ezShop.getShopItem()));
                } else {
                    item.setName(lm.customMessageManagerShopEntryUnkownTitle());
                }
                item.setLore(lm.customMessageManagerShopEntryLore(loc, messages));

                GuiItem shopItem = new GuiItem(item.getItem(), event -> {
                    event.setCancelled(true);
                    if (event.isLeftClick()) {
                        showDeleteConfirm(player, containerBlock, isAdmin, loc);
                    } else if (event.isRightClick()) {
                        SettingsGUI.openCustomMessageEditor(player, loc);
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
                    SettingsGUI.openCustomMessageEditor(player, containerBlock.getLocation());
                }
            });
            Utils.addItemIfEnoughSlots(paginatedGui, modify.getSlot(), modifyItem);
        }

        paginatedGui.open(player);


    }

    private void showDeleteConfirm(Player player, Block containerBlock, boolean isAdmin, Location loc) {
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
            ShopContainer.getShopSettings(loc).setCustomMessages(new ArrayList<>());
            gui.close(player);
            ShopHologram.hideForAll(loc);
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
