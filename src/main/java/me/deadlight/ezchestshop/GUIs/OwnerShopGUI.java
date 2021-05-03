package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.LanguageManager;
import me.deadlight.ezchestshop.Packets.WrapperPlayServerCustomSoundEffect;
import me.deadlight.ezchestshop.Utils;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
//success player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
//fail player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
//storage player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 0.5f);

public class OwnerShopGUI {
    private Economy econ = EzChestShop.getEconomy();

    public OwnerShopGUI() {}


    public void showGUI(Player player, PersistentDataContainer data, Chest chest) {

        LanguageManager lm = new LanguageManager();

        String shopOwner = data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        double sellPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        double buyPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);

        ItemStack mainitem = Utils.getItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        ItemStack guiMainItem = mainitem.clone();
        ItemMeta mainmeta = guiMainItem.getItemMeta();
        List<String> mainItemLore = Arrays.asList(lm.initialBuyPrice(buyPrice), lm.initialSellPrice(sellPrice));
        mainmeta.setLore(mainItemLore);
        guiMainItem.setItemMeta(mainmeta);
        GuiItem guiitem = new GuiItem(guiMainItem, event -> {
            event.setCancelled(true);
        });

        Gui gui = new Gui(3, lm.guiOwnerTitle(shopOwner));
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName("");
        glassis.setItemMeta(glassmeta);

        GuiItem glasses = new GuiItem(glassis, event -> {
            // Handle your click action here
            event.setCancelled(true);
        });

        ItemStack oneSellIS = new ItemStack(Material.RED_DYE, 1);
        ItemMeta meta = oneSellIS.getItemMeta();
        meta.setDisplayName(lm.buttonSell1Title());
        List<String> lores = Arrays.asList(lm.buttonSell1Lore(roundDecimals(sellPrice)));
        meta.setLore(lores);
        oneSellIS.setItemMeta(meta);

        GuiItem oneSell = new GuiItem(oneSellIS, event -> {
            // sell things
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            player.sendMessage(lm.selfTransaction());
        });

        ItemStack moreSellIS = new ItemStack(Material.RED_DYE, 64);
        ItemMeta meta2 = moreSellIS.getItemMeta();
        meta2.setDisplayName(lm.buttonSell64Title());
        List<String> lores2 = Arrays.asList(lm.buttonSell64Lore(roundDecimals(sellPrice * 64)));
        meta2.setLore(lores2);
        moreSellIS.setItemMeta(meta2);

        GuiItem moreSell = new GuiItem(moreSellIS, event -> {

            event.setCancelled(true);
            //sell things
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            player.sendMessage(lm.selfTransaction());
        });

        //buy 1x

        ItemStack oneBuyIS = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta3 = oneBuyIS.getItemMeta();
        meta3.setDisplayName(lm.buttonBuy1Title());
        List<String> lores3 = Arrays.asList(lm.buttonBuy1Lore(roundDecimals(buyPrice)));
        meta3.setLore(lores3);
        oneBuyIS.setItemMeta(meta3);

        GuiItem oneBuy = new GuiItem(oneBuyIS, event -> {
            //buy things
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            player.sendMessage(lm.selfTransaction());
        });


        ItemStack moreBuyIS = new ItemStack(Material.LIME_DYE, 64);
        ItemMeta meta4 = moreBuyIS.getItemMeta();
        meta4.setDisplayName(lm.buttonBuy64Title());
        List<String> lores4 = Arrays.asList(lm.buttonBuy64Lore(roundDecimals(buyPrice * 64)));
        meta4.setLore(lores4);
        moreBuyIS.setItemMeta(meta4);

        GuiItem moreBuy = new GuiItem(moreBuyIS, event -> {
            //buy things
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
            player.sendMessage(lm.selfTransaction());
        });

        ItemStack storageitem = new ItemStack(Material.CHEST, 1);
        storageitem.addUnsafeEnchantment(Enchantment.LURE, 1);
        ItemMeta storagemeta = storageitem.getItemMeta();
        storagemeta.setDisplayName(lm.buttonStorage());
        storagemeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        storageitem.setItemMeta(storagemeta);

        GuiItem storageGUI = new GuiItem(storageitem, event -> {
            event.setCancelled(true);
            Inventory lastinv = chest.getInventory();
            if (lastinv instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) lastinv.getHolder();
                lastinv = doubleChest.getInventory();
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 0.5f);
            player.openInventory(lastinv);
        });


        gui.getFiller().fillBorder(glasses);

        gui.setItem(10, glasses);
        gui.setItem(16, glasses);
        gui.setItem(13, guiitem);
        //1x sell (12)
        gui.setItem(12, oneSell);
        //64x sell (11)
        gui.setItem(11, moreSell);
        //1x buy (14)
        gui.setItem(14, oneBuy);
        //64x buy (15)
        gui.setItem(15, moreBuy);
        //chest storage
        gui.setItem(18, storageGUI);

        gui.open(player);



    }

    private long roundDecimals(double num) {
        return (long) (((long)(num * 1e1)) / 1e1);
    }

}
