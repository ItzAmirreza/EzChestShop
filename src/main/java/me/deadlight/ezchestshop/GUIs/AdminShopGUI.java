package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.LanguageManager;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class AdminShopGUI {
    private Economy econ = EzChestShop.getEconomy();

    public AdminShopGUI() {

    }

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

        Gui gui = new Gui(3, lm.guiAdminTitle(shopOwner));
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
            sellItem(chest, sellPrice, 1, mainitem, Bukkit.getOfflinePlayer(shopOwner), player);
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
            sellItem(chest, sellPrice * 64, 64, mainitem, Bukkit.getOfflinePlayer(shopOwner), player);
        });

        //buy 1x

        ItemStack oneBuyIS = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta3 = oneBuyIS.getItemMeta();
        meta3.setDisplayName(Utils.color(lm.buttonBuy1Title()));
        List<String> lores3 = Arrays.asList(lm.buttonBuy1Lore(roundDecimals(buyPrice)));
        meta3.setLore(lores3);
        oneBuyIS.setItemMeta(meta3);

        GuiItem oneBuy = new GuiItem(oneBuyIS, event -> {
            //buy things
            event.setCancelled(true);
            buyItem(chest, buyPrice, 1, player, mainitem, Bukkit.getOfflinePlayer(shopOwner));
        });


        ItemStack moreBuyIS = new ItemStack(Material.LIME_DYE, 64);
        ItemMeta meta4 = moreBuyIS.getItemMeta();
        meta4.setDisplayName(Utils.color(lm.buttonBuy64Title()));
        List<String> lores4 = Arrays.asList(lm.buttonBuy64Lore(roundDecimals(buyPrice * 64)));
        meta4.setLore(lores4);
        moreBuyIS.setItemMeta(meta4);

        GuiItem moreBuy = new GuiItem(moreBuyIS, event -> {
            //buy things
            event.setCancelled(true);
            buyItem(chest, buyPrice * 64, 64, player, mainitem, Bukkit.getOfflinePlayer(shopOwner));
        });

        ItemStack storageitem = new ItemStack(Material.REDSTONE, 1);
        storageitem.addUnsafeEnchantment(Enchantment.LURE, 1);
        ItemMeta storagemeta = storageitem.getItemMeta();
        storagemeta.setDisplayName(lm.buttonAdminView());
        storageitem.setItemMeta(storagemeta);

        GuiItem storageGUI = new GuiItem(storageitem, event -> {
            event.setCancelled(true);
            Inventory lastinv = chest.getInventory();
            if (lastinv instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) lastinv.getHolder();
                lastinv = doubleChest.getInventory();
            }

            if (player.hasPermission("ecs.admin")) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 0.5f);
                player.openInventory(lastinv);
            }
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

    private void buyItem(Chest chest, double price, int count, Player player, ItemStack tthatItem, OfflinePlayer owner) {
        ItemStack thatItem = tthatItem.clone();
        LanguageManager lm = new LanguageManager();

        //check for money

        if (chest.getInventory().containsAtLeast(thatItem , count)) {

            if (ifHasMoney(Bukkit.getOfflinePlayer(player.getUniqueId()), price)) {

                if (player.getInventory().firstEmpty() != -1) {

                    thatItem.setAmount(count);
                    getandgive(Bukkit.getOfflinePlayer(player.getUniqueId()), price, owner);
                    chest.getInventory().removeItem(thatItem);
                    player.getInventory().addItem(thatItem);
                    player.sendMessage(Utils.color(lm.messageSuccBuy(price)));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

                } else {
                    player.sendMessage(lm.fullinv());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
                }

            } else {

                player.sendMessage(lm.cannotAfford());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);

            }

        } else {
            player.sendMessage(lm.outofStock());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }

    }

    private void sellItem(Chest chest, double price, int count, ItemStack tthatItem , OfflinePlayer owner, Player player) {


        ItemStack thatItem = tthatItem.clone();
        LanguageManager lm = new LanguageManager();

        if (player.getInventory().containsAtLeast(thatItem, count)) {

            if (ifHasMoney(owner, price)) {

                if (chest.getInventory().firstEmpty() != -1) {
                    thatItem.setAmount(count);
                    getandgive(owner, price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                    player.getInventory().removeItem(thatItem);
                    chest.getInventory().addItem(thatItem);
                    player.sendMessage(lm.messageSuccSell(price));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

                } else {
                    player.sendMessage(lm.chestIsFull());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
                }

            } else {

                player.sendMessage(lm.shopCannotAfford());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);

            }
        } else {
            player.sendMessage(lm.notEnoughItemToSell());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }

    }




    private boolean ifHasMoney(OfflinePlayer player, double price) {
        if (econ.has(player, price)) {
            return true;
        }
        return false;
    }

    private void getandgive(OfflinePlayer withdraw, double price, OfflinePlayer deposit) {

        econ.withdrawPlayer(withdraw, price);
        econ.depositPlayer(deposit, price);

    }

}
