package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.LanguageManager;
import me.deadlight.ezchestshop.Listeners.PlayerTransactEvent;
import me.deadlight.ezchestshop.Utils.Utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ServerShopGUI {

    private Economy econ = EzChestShop.getEconomy();
    private Chest chest;
    public ServerShopGUI() {

    }

    public void showGUI(Player player, PersistentDataContainer data, Chest chest, Chest rightChest) {
        this.chest = rightChest;
        LanguageManager lm = new LanguageManager();

        String owneruuid = data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
        double sellPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        double buyPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        boolean disabledBuy = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean disabledSell = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;


        ItemStack mainitem = Utils.getItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        ItemStack guiMainItem = mainitem.clone();
        ItemMeta mainmeta = guiMainItem.getItemMeta();
        List<String> mainItemLore = Arrays.asList(lm.initialBuyPrice(buyPrice), lm.initialSellPrice(sellPrice));
        mainmeta.setLore(mainItemLore);
        guiMainItem.setItemMeta(mainmeta);
        GuiItem guiitem = new GuiItem(guiMainItem, event -> {
            event.setCancelled(true);
        });

        Gui gui = new Gui(3, lm.adminshopguititle());
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.color("&d"));
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

        GuiItem oneSell = new GuiItem(disablingCheck(oneSellIS, disabledSell), event -> {
            // sell things
            event.setCancelled(true);
            if (disabledSell) {
                return;
            }
            sellItem(sellPrice, 1, mainitem, player, data);
        });

        ItemStack moreSellIS = new ItemStack(Material.RED_DYE, 64);
        ItemMeta meta2 = moreSellIS.getItemMeta();
        meta2.setDisplayName(lm.buttonSell64Title());
        List<String> lores2 = Arrays.asList(lm.buttonSell64Lore(roundDecimals(sellPrice * 64)));
        meta2.setLore(lores2);
        moreSellIS.setItemMeta(meta2);

        GuiItem moreSell = new GuiItem(disablingCheck(moreSellIS, disabledSell), event -> {

            event.setCancelled(true);
            //sell things
            if (disabledSell) {
                return;
            }
            sellItem(sellPrice * 64, 64, mainitem, player, data);
        });

        //buy 1x

        ItemStack oneBuyIS = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta3 = oneBuyIS.getItemMeta();
        meta3.setDisplayName(lm.buttonBuy1Title());
        List<String> lores3 = Arrays.asList(lm.buttonBuy64Lore(roundDecimals(buyPrice)));
        meta3.setLore(lores3);
        oneBuyIS.setItemMeta(meta3);

        GuiItem oneBuy = new GuiItem(disablingCheck(oneBuyIS, disabledBuy), event -> {
            //buy things
            event.setCancelled(true);
            if (disabledBuy) {
                return;
            }
            buyItem(buyPrice, 1, player, mainitem, data);
        });


        ItemStack moreBuyIS = new ItemStack(Material.LIME_DYE, 64);
        ItemMeta meta4 = moreBuyIS.getItemMeta();
        meta4.setDisplayName(lm.buttonBuy64Title());
        List<String> lores4 = Arrays.asList(lm.buttonBuy64Lore(roundDecimals(buyPrice * 64)));
        meta4.setLore(lores4);
        moreBuyIS.setItemMeta(meta4);

        GuiItem moreBuy = new GuiItem(disablingCheck(moreBuyIS, disabledBuy), event -> {
            //buy things
            event.setCancelled(true);
            if (disabledBuy) {
                return;
            }
            buyItem(buyPrice * 64, 64, player, mainitem, data);
        });

        //settings item
        ItemStack settingsItem = new ItemStack(Material.SMITHING_TABLE, 1);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(lm.settingsButton());
        settingsItem.setItemMeta(settingsMeta);

        boolean result = isAdmin(rightChest.getPersistentDataContainer(), player.getUniqueId().toString());
        //place moved vvv because of settingsGUI
        gui.getFiller().fillBorder(glasses);

        if (player.hasPermission("ecs.admin") || result) {
            GuiItem settingsGui = new GuiItem(settingsItem, event -> {
                event.setCancelled(true);
                //opening the settigns menu
                SettingsGUI settingsGUI = new SettingsGUI();

                settingsGUI.ShowGUI(player, rightChest, result);
                player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.5f, 0.5f);
            });

            gui.setItem(26, settingsGui);
        }



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

//        if (player.hasPermission("ecs.admin") || isAdmin(data, player.getUniqueId().toString())) {
//            gui.setItem();
//        }

        gui.open(player);


    }

    private long roundDecimals(double num) {
        return (long) (((long)(num * 1e1)) / 1e1);
    }



    private void buyItem(double price, int count, Player player, ItemStack tthatItem, PersistentDataContainer data) {
        ItemStack thatItem = tthatItem.clone();

        LanguageManager lm = new LanguageManager();
        //check for money

            if (ifHasMoney(Bukkit.getOfflinePlayer(player.getUniqueId()), price)) {

                if (player.getInventory().firstEmpty() != -1) {

                    thatItem.setAmount(count);
                    take(price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                    transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))), Bukkit.getOfflinePlayer(player.getUniqueId()), price, true, getFinalItemName(tthatItem), count);
                    player.getInventory().addItem(thatItem);
                    player.sendMessage(lm.messageSuccBuy(price));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

                } else {
                    player.sendMessage(lm.fullinv());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
                }

            } else {

                player.sendMessage(lm.cannotAfford());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);

            }



    }

    private void sellItem(double price, int count, ItemStack tthatItem, Player player, PersistentDataContainer data) {

        LanguageManager lm = new LanguageManager();

        ItemStack thatItem = tthatItem.clone();

        if (player.getInventory().containsAtLeast(thatItem, count)) {

            thatItem.setAmount(count);
            deposit(price, Bukkit.getOfflinePlayer(player.getUniqueId()));
            transactionMessage(data, Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))), Bukkit.getOfflinePlayer(player.getUniqueId()), price, false, getFinalItemName(tthatItem), count);
            player.getInventory().removeItem(thatItem);
            player.sendMessage(lm.messageSuccSell(price));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);

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

    private void deposit(double price, OfflinePlayer deposit) {

        econ.depositPlayer(deposit, price);

    }
    private void take(double price, OfflinePlayer deposit) {

        econ.withdrawPlayer(deposit, price);

    }

    private void transactionMessage(PersistentDataContainer data, OfflinePlayer owner, OfflinePlayer customer, double price, boolean isBuy, String itemName, int count) {
        if (data.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1) {
            //kharidan? true forokhtan? false
            PlayerTransactEvent transactEvent = new PlayerTransactEvent(owner, customer, price, isBuy, itemName, count, Utils.getAdminsList(data), chest);
            Bukkit.getPluginManager().callEvent(transactEvent);

        }
    }

    private String getFinalItemName(ItemStack item) {
        if (item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        } else {
            return item.getType().name();
        }
    }

    private ItemStack disablingCheck(ItemStack mainItem, boolean disabling) {
        if (disabling){
            //disabled Item
            LanguageManager lm = new LanguageManager();
            ItemStack disabledItemStack = new ItemStack(Material.BARRIER, mainItem.getAmount());
            ItemMeta disabledItemMeta = disabledItemStack.getItemMeta();
            disabledItemMeta.setDisplayName(lm.disabledButtonTitle());
            disabledItemMeta.setLore(lm.disabledButtonLore());
            disabledItemStack.setItemMeta(disabledItemMeta);

            return disabledItemStack;
        } else {
            return mainItem;
        }
    }



    private boolean isAdmin(PersistentDataContainer data, String uuid) {
        UUID owneruuid = UUID.fromString(uuid);
        List<UUID> adminsUUID = Utils.getAdminsList(data);
        if (adminsUUID.contains(owneruuid)) {
            return true;
        } else {
            return false;
        }

    }


}
