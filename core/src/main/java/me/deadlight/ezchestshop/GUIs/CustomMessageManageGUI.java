package me.deadlight.ezchestshop.GUIs;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
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
        PaginatedGui paginatedGui = Gui.paginated()
                .title(Component.text(lm.customMessageManagerTitle()))
                .rows(6)
                .pageSize(45)
                .create();

        Map<Location, String> customMessages = ShopSettings.getAllCustomMessages(ShopContainer.getShop(containerBlock.getLocation()).getOwnerID().toString());

        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.colorify("&d"));
        glassis.setItemMeta(glassmeta);

        GuiItem glasses = new GuiItem(glassis, event -> {
            // Handle your click action here
            event.setCancelled(true);
        });

        // Fill the bottom bar:
        paginatedGui.getFiller().fillBottom(glasses);

        // Previous item
        ItemStack previous = new ItemStack(Material.ARROW, 1);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(lm.customMessageManagerPreviousPageTitle());
        previousMeta.setLore(lm.customMessageManagerPreviousPageLore());
        previous.setItemMeta(previousMeta);
        GuiItem previousItem = new GuiItem(previous, event -> {
            event.setCancelled(true);
            paginatedGui.previous();
        });
        paginatedGui.setItem(6, 3, previousItem);
        // Next item
        ItemStack next = new ItemStack(Material.ARROW, 1);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(lm.customMessageManagerNextPageTitle());
        nextMeta.setLore(lm.customMessageManagerNextPageLore());
        next.setItemMeta(nextMeta);
        GuiItem nextItem = new GuiItem(next, event -> {
            event.setCancelled(true);
            paginatedGui.next();
        });
        paginatedGui.setItem(6, 7, nextItem);
        // Back item
        ItemStack door = new ItemStack(Material.DARK_OAK_DOOR, 1);
        ItemMeta doorMeta = door.getItemMeta();
        doorMeta.setDisplayName(lm.backToSettingsButton());
        door.setItemMeta(doorMeta);
        paginatedGui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });
        GuiItem doorItem = new GuiItem(door, event -> {
            event.setCancelled(true);
            SettingsGUI settingsGUI = new SettingsGUI();
            settingsGUI.showGUI(player, containerBlock, isAdmin);
        });
        paginatedGui.setItem(6, 1, doorItem);

        for (Map.Entry<Location, String> entry : customMessages.entrySet()) {
            Location loc = entry.getKey();
            String message = entry.getValue();
            List<String> messages = Arrays.asList(message.split("#,#")).stream().map(s -> Utils.colorify(s)).collect(Collectors.toList());

            ItemStack shop = new ItemStack(Material.CHEST, 1);
            ItemMeta shopMeta = shop.getItemMeta();
            EzShop ezShop = ShopContainer.getShop(loc);
            if (ezShop != null) {
                shopMeta.setDisplayName(lm.customMessageManagerShopEntryTitle(ezShop.getShopItem()));
            } else {
                shopMeta.setDisplayName(lm.customMessageManagerShopEntryUnkownTitle());
            }
            shopMeta.setLore(lm.customMessageManagerShopEntryLore(loc, messages));

            shop.setItemMeta(shopMeta);

            GuiItem shopItem = new GuiItem(shop, event -> {
                event.setCancelled(true);
                showDeleteConfirm(player, containerBlock, isAdmin, loc);
            });

            paginatedGui.addItem(shopItem);
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
            PlayerCloseToChestListener.hideHologram(loc, true);
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
