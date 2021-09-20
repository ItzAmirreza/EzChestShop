package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Utils.SignMenuFactory;
import me.deadlight.ezchestshop.Utils.Utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ServerShopGUI {

    private Economy econ = EzChestShop.getEconomy();
    public ServerShopGUI() {

    }

    public void showGUI(Player player, PersistentDataContainer data, Block containerBlock) {
        LanguageManager lm = new LanguageManager();
        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
        String shopOwner = offlinePlayerOwner.getName();
        if (shopOwner == null) {
            player.sendMessage(lm.chestShopProblem());
            return;
        }
        double sellPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        double buyPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        boolean disabledBuy = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean disabledSell = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;


        ItemStack mainitem = Utils.decodeItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
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
        glassmeta.setDisplayName(Utils.colorify("&d"));
        glassis.setItemMeta(glassmeta);

        GuiItem glasses = new GuiItem(glassis, event -> {
            // Handle your click action here
            event.setCancelled(true);
        });

        ItemStack oneSellIS = new ItemStack(Material.RED_DYE, 1);
        ItemMeta meta = oneSellIS.getItemMeta();
        meta.setDisplayName(lm.buttonSell1Title());
        List<String> lores = lm.buttonSell1Lore(sellPrice);
        meta.setLore(lores);
        oneSellIS.setItemMeta(meta);

        GuiItem oneSell = new GuiItem(disablingCheck(oneSellIS, disabledSell), event -> {
            // sell things
            event.setCancelled(true);
            if (disabledSell) {
                return;
            }
            ShopContainer.sellServerItem(containerBlock, sellPrice, 1, mainitem, player, data);
        });

        ItemStack moreSellIS = new ItemStack(Material.RED_DYE, 64);
        ItemMeta meta2 = moreSellIS.getItemMeta();
        meta2.setDisplayName(lm.buttonSell64Title());
        List<String> lores2 = lm.buttonSell64Lore(sellPrice * 64);
        meta2.setLore(lores2);
        moreSellIS.setItemMeta(meta2);

        GuiItem moreSell = new GuiItem(disablingCheck(moreSellIS, disabledSell), event -> {

            event.setCancelled(true);
            //sell things
            if (disabledSell) {
                return;
            }
            ShopContainer.sellServerItem(containerBlock, sellPrice * 64, 64, mainitem, player, data);
        });

        //buy 1x

        ItemStack oneBuyIS = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta3 = oneBuyIS.getItemMeta();
        meta3.setDisplayName(lm.buttonBuy1Title());
        List<String> lores3 = lm.buttonBuy1Lore(buyPrice);
        meta3.setLore(lores3);
        oneBuyIS.setItemMeta(meta3);

        GuiItem oneBuy = new GuiItem(disablingCheck(oneBuyIS, disabledBuy), event -> {
            //buy things
            event.setCancelled(true);
            if (disabledBuy) {
                return;
            }
            ShopContainer.buyServerItem(containerBlock, buyPrice, 1, player, mainitem, data);
        });


        ItemStack moreBuyIS = new ItemStack(Material.LIME_DYE, 64);
        ItemMeta meta4 = moreBuyIS.getItemMeta();
        meta4.setDisplayName(lm.buttonBuy64Title());
        List<String> lores4 = lm.buttonBuy64Lore(buyPrice * 64);
        meta4.setLore(lores4);
        moreBuyIS.setItemMeta(meta4);

        GuiItem moreBuy = new GuiItem(disablingCheck(moreBuyIS, disabledBuy), event -> {
            //buy things
            event.setCancelled(true);
            if (disabledBuy) {
                return;
            }
            ShopContainer.buyServerItem(containerBlock, buyPrice * 64, 64, player, mainitem, data);
        });

        //settings item
        ItemStack settingsItem = new ItemStack(Material.SMITHING_TABLE, 1);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(lm.settingsButton());
        settingsItem.setItemMeta(settingsMeta);

        boolean result = isAdmin(((TileState)containerBlock.getState()).getPersistentDataContainer(), player.getUniqueId().toString());
        //place moved vvv because of settingsGUI
        gui.getFiller().fillBorder(glasses);

        if (player.hasPermission("ecs.admin") || result) {
            GuiItem settingsGui = new GuiItem(settingsItem, event -> {
                event.setCancelled(true);
                //opening the settigns menu
                SettingsGUI settingsGUI = new SettingsGUI();

                settingsGUI.showGUI(player, containerBlock, result);
                player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.5f, 0.5f);
            });

            gui.setItem(26, settingsGui);
        }


        ItemStack signItem = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta signMeta = signItem.getItemMeta();
        signMeta.setDisplayName(lm.customAmountSignTitle());
        List<String> possibleCounts = Utils.calculatePossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()), null, player.getInventory().getStorageContents(), null, buyPrice, sellPrice, mainitem);
        signMeta.setLore(lm.customAmountSignLore(possibleCounts.get(0), possibleCounts.get(1)));
        signItem.setItemMeta(signMeta);

        GuiItem guiSignItem = new GuiItem(signItem, event -> {
            event.setCancelled(true);
           if (event.isLeftClick()) {
               //buy
               if (disabledBuy) {
                   player.sendMessage(lm.disabledBuyingMessage());
                   return;
               }
               player.closeInventory();
               player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
               SignMenuFactory signMenuFactory = new SignMenuFactory(EzChestShop.getPlugin());
               SignMenuFactory.Menu menu = signMenuFactory.newMenu(lm.signEditorGuiBuy(possibleCounts.get(0)))
                       .reopenIfFail(false).response((thatplayer, strings) -> {
                           try {
                               if (strings[0].equalsIgnoreCase("")) {
                                   return false;
                               }
                               if (Utils.isInteger(strings[0])) {
                                   int amount = Integer.parseInt(strings[0]);
                                   if (!Utils.amountCheck(amount)) {
                                       player.sendMessage(lm.unsupportedInteger());
                                       return false;
                                   }
                                   Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
                                       @Override
                                       public void run() {
                                           ShopContainer.buyServerItem(containerBlock, buyPrice * amount, amount, thatplayer, mainitem, data);
                                       }
                                   });
                               } else {
                                   thatplayer.sendMessage(lm.wrongInput());
                               }

                           } catch (Exception e) {
                               return false;
                           }
                           return true;
                       });
               menu.open(player);
               player.sendMessage(lm.enterTheAmount());


           } else if (event.isRightClick()) {
               //sell
               if (disabledSell) {
                   player.sendMessage(lm.disabledSellingMessage());
                   return;
               }
               player.closeInventory();
               player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
               SignMenuFactory signMenuFactory = new SignMenuFactory(EzChestShop.getPlugin());
               SignMenuFactory.Menu menu = signMenuFactory.newMenu(lm.signEditorGuiSell(possibleCounts.get(1)))
                       .reopenIfFail(false).response((thatplayer, strings) -> {
                           try {
                               if (strings[0].equalsIgnoreCase("")) {
                                   return false;
                               }
                               if (Utils.isInteger(strings[0])) {
                                   int amount = Integer.parseInt(strings[0]);
                                   if (!Utils.amountCheck(amount)) {
                                       player.sendMessage(lm.unsupportedInteger());
                                       return false;
                                   }
                                   Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
                                       @Override
                                       public void run() {
                                           ShopContainer.sellServerItem(containerBlock, sellPrice * amount, amount, mainitem, thatplayer, data);
                                       }
                                   });
                               } else {
                                   thatplayer.sendMessage(lm.wrongInput());
                               }

                           } catch (Exception e) {
                               return false;
                           }
                           return true;
                       });
               menu.open(player);
               player.sendMessage(lm.enterTheAmount());


           }
         });


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
        //custom buy item
        gui.setItem(22, guiSignItem);

//        if (player.hasPermission("ecs.admin") || isAdmin(data, player.getUniqueId().toString())) {
//            gui.setItem();
//        }

        gui.open(player);


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
