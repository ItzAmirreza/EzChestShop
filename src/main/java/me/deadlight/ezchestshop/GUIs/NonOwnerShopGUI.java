package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Listeners.PlayerTransactEvent;
import me.deadlight.ezchestshop.Utils.Utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Chest;
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

public class NonOwnerShopGUI {

    private Economy econ = EzChestShop.getEconomy();

    public NonOwnerShopGUI() {}

    public void showGUI(Player player, PersistentDataContainer data, Chest chest) {
        LanguageManager lm = new LanguageManager();

        String shopOwner = Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();
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

        Gui gui = new Gui(3, lm.guiNonOwnerTitle(shopOwner));
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
            //disabling arg
            if (disabledSell) {
                return;
            }
            sellItem(chest, sellPrice, 1, mainitem, Bukkit.getOfflinePlayer(shopOwner), player, data);
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
            //disabling arg
            if (disabledSell) {
                return;
            }
            sellItem(chest, sellPrice * 64, 64, mainitem, Bukkit.getOfflinePlayer(shopOwner), player, data);
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
            //disabling arg
            if (disabledBuy) {
                return;
            }
            buyItem(chest, buyPrice, 1, player, mainitem, Bukkit.getOfflinePlayer(shopOwner), data);
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
            //disabling arg
            if (disabledBuy) {
                return;
            }
            buyItem(chest, buyPrice * 64, 64, player, mainitem, Bukkit.getOfflinePlayer(shopOwner), data);
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


        gui.open(player);



    }

    private long roundDecimals(double num) {
        return (long) (((long)(num * 1e1)) / 1e1);
    }



    private void buyItem(Chest chest, double price, int count, Player player, ItemStack tthatItem, OfflinePlayer owner, PersistentDataContainer data) {
        ItemStack thatItem = tthatItem.clone();

        LanguageManager lm = new LanguageManager();
        //check for money

        if (chest.getInventory().containsAtLeast(thatItem , count)) {

            if (ifHasMoney(Bukkit.getOfflinePlayer(player.getUniqueId()), price)) {

                if (player.getInventory().firstEmpty() != -1) {

                    thatItem.setAmount(count);
                    getandgive(Bukkit.getOfflinePlayer(player.getUniqueId()), price, owner);
                    sharedIncomeCheck(data, price);
                    chest.getInventory().removeItem(thatItem);
                    player.getInventory().addItem(thatItem);
                    transactionMessage(data, owner, player, price, true, getFinalItemName(tthatItem), count, (Chest) chest.getLocation().getBlock().getState());
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

        } else {
            player.sendMessage(lm.outofStock());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
        }

    }

    private void sellItem(Chest chest, double price, int count, ItemStack tthatItem , OfflinePlayer owner, Player player, PersistentDataContainer data) {

        LanguageManager lm = new LanguageManager();

        ItemStack thatItem = tthatItem.clone();

        if (player.getInventory().containsAtLeast(thatItem, count)) {

            if (ifHasMoney(owner, price)) {

                if (chest.getInventory().firstEmpty() != -1) {
                    thatItem.setAmount(count);
                    getandgive(owner, price, Bukkit.getOfflinePlayer(player.getUniqueId()));
                    transactionMessage(data, owner, Bukkit.getOfflinePlayer(player.getUniqueId()), price, false, getFinalItemName(tthatItem), count, (Chest) chest.getLocation().getBlock().getState());
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

    private void transactionMessage(PersistentDataContainer data, OfflinePlayer owner, OfflinePlayer customer, double price, boolean isBuy, String itemName, int count, Chest chest) {

            //kharidan? true forokhtan? false
            PlayerTransactEvent transactEvent = new PlayerTransactEvent(owner, customer, price, isBuy, itemName, count, Utils.getAdminsList(data), chest);
            Bukkit.getPluginManager().callEvent(transactEvent);


    }

    private String getFinalItemName(ItemStack item) {
        if (item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        } else {
            return item.getType().name();
        }
    }

    private ItemStack disablingCheck(ItemStack mainItem, boolean disabling) {
        LanguageManager lm = new LanguageManager();
        if (disabling){
            //disabled Item
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

    private void sharedIncomeCheck(PersistentDataContainer data, double price) {
        boolean isSharedIncome = data.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER) == 1;
        if (isSharedIncome) {
            UUID ownerUUID = UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING));
            List<UUID> adminsList = Utils.getAdminsList(data);
            double profit = price/(adminsList.size() + 1);
            if (adminsList.size() > 0) {
                if (econ.has(Bukkit.getOfflinePlayer(ownerUUID), profit * adminsList.size())) {
                    EconomyResponse details = econ.withdrawPlayer(Bukkit.getOfflinePlayer(ownerUUID), profit * adminsList.size());
                    if (details.transactionSuccess()) {
                        for (UUID adminUUID : adminsList) {
                            econ.depositPlayer(Bukkit.getOfflinePlayer(adminUUID), profit);
                        }
                    }
                }

            }
        }

    }





}
