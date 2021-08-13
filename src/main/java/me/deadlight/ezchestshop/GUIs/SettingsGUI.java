package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Listeners.ChatListener;
import me.deadlight.ezchestshop.Utils.Objects.ChatWaitObject;
import me.deadlight.ezchestshop.Utils.LogType;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class SettingsGUI {
    //msgtoggle 0/1
    //dbuy 0/1
    //dsell 0/1
    //admins [list of uuids seperated with @ in string form]
    //shareincome 0/1
    //logs [list of infos seperated by @ in string form]
    //trans [list of infos seperated by @ in string form]
    //adminshop 0/1

    public void ShowGUI(Player player, Chest rightChest, boolean isAdmin) {

        LanguageManager lm = new LanguageManager();

        PersistentDataContainer dataContainer = rightChest.getPersistentDataContainer();

        boolean isAdminShop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;


        Gui gui = new Gui(3, lm.settingsGuiTitle());
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.color("&d"));
        glassis.setItemMeta(glassmeta);
        GuiItem glasses = new GuiItem(glassis, event -> {
            event.setCancelled(true);
        });
        //I changed its place to first part of code because of different GUI sections for admins and owners

        gui.getFiller().fillBorder(glasses);

        //trans
        ItemStack lastTransItem = new ItemStack(Material.PAPER, 1);
        ItemMeta lastTransMeta = lastTransItem.getItemMeta();
        lastTransMeta.setDisplayName(lm.latestTransactionsButton());
        lastTransItem.setItemMeta(lastTransMeta);
        GuiItem lastTrans = new GuiItem(lastTransItem, event -> {
           event.setCancelled(true);
           LogsGUI logsGUI = new LogsGUI();
           logsGUI.showGUI(player, dataContainer, rightChest, LogType.TRANSACTION, isAdmin);
        });

        //I was going to add logs section which would be about actions in that chest shop but I decided not to implement it. maybe later
//        //settings change logs
//        ItemStack lastLogsItem = new ItemStack(Material.PAPER, 1);
//        ItemMeta lastLogsMeta = lastLogsItem.getItemMeta();
//        lastLogsMeta.setDisplayName(Utils.color("&aLatest Logs"));
//        lastLogsItem.setItemMeta(lastLogsMeta);
//        GuiItem lastLogs = new GuiItem(lastLogsItem, event -> {
//           event.setCancelled(true);
//            LogsGUI logsGUI = new LogsGUI();
//            logsGUI.showGUI(player, dataContainer, rightChest, LogType.ACTION, isAdmin);
//        });


        //Message Toggle Item
        boolean isToggleMessageOn = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
        ItemStack messageToggleItem = new ItemStack(grayGreenChooser(isToggleMessageOn), 1);
        ItemMeta messageToggleMeta = messageToggleItem.getItemMeta();
        messageToggleMeta.setDisplayName(lm.toggleTransactionMessageButton());
        messageToggleMeta.setLore(toggleMessageChooser(isToggleMessageOn, lm));
        messageToggleItem.setItemMeta(messageToggleMeta);



        GuiItem messageToggle = new GuiItem(messageToggleItem, event -> {
            event.setCancelled(true);
            //start the functionality for toggle message
            if (checkIfOn(event.getCurrentItem().getType())) {

                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
                rightChest.update();
                player.sendMessage(lm.toggleTransactionMessageOffInChat());
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(toggleMessageChooser(false, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(rightChest.getLocation()).setMsgtoggle(false);
            } else {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 1);
                rightChest.update();
                player.sendMessage(lm.toggleTransactionMessageOnInChat());
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(toggleMessageChooser(true, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(rightChest.getLocation()).setMsgtoggle(true);

            }
        });

        //Message Toggle Item
        boolean isBuyDisabled = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        ItemStack buyDisabledItem= new ItemStack(grayGreenChooser(isBuyDisabled), 1);
        ItemMeta buyDisabledMeta = buyDisabledItem.getItemMeta();
        buyDisabledMeta.setDisplayName(lm.disableBuyingButtonTitle());
        buyDisabledMeta.setLore(buyMessageChooser(isBuyDisabled, lm));
        buyDisabledItem.setItemMeta(buyDisabledMeta);
        GuiItem buyDisabled = new GuiItem(buyDisabledItem, event -> {
            event.setCancelled(true);
            //start the functionality for disabling buy
            if (checkIfOn(event.getCurrentItem().getType())) {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
                rightChest.update();
                player.sendMessage(lm.disableBuyingOffInChat());
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(buyMessageChooser(false, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(rightChest.getLocation()).setDbuy(false);
            } else {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 1);
                rightChest.update();
                player.sendMessage(lm.disableBuyingOnInChat());
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(buyMessageChooser(true, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(rightChest.getLocation()).setDbuy(true);
            }

        });

        boolean isSellDisabled = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;
        ItemStack sellDisabledItem= new ItemStack(grayGreenChooser(isSellDisabled), 1);
        ItemMeta sellDisabledMeta = sellDisabledItem.getItemMeta();
        sellDisabledMeta.setDisplayName(lm.disableSellingButtonTitle());
        sellDisabledMeta.setLore(sellMessageChooser(isSellDisabled, lm));
        sellDisabledItem.setItemMeta(sellDisabledMeta);
        GuiItem sellDisabled = new GuiItem(sellDisabledItem, event -> {
            event.setCancelled(true);
            //start the functionality for disabling sell
            if (checkIfOn(event.getCurrentItem().getType())) {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
                rightChest.update();
                player.sendMessage(lm.disableSellingOffInChat());
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(sellMessageChooser(false, lm));
                event.getCurrentItem().setItemMeta(meta);

                ShopContainer.getShopSettings(rightChest.getLocation()).setDsell(false);
            } else {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 1);
                rightChest.update();
                player.sendMessage(lm.disableSellingOnInChat());
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(sellMessageChooser(true, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(rightChest.getLocation()).setDsell(true);
            }
        });

        //How it saves the owners? like this man
        //if empty: "none"
        //otherwise: UUID@UUID@UUID@UUID
        if (!isAdmin) {
            boolean hastAtLeastOneAdmin = !dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING).equals("none");
            ItemStack signItem = new ItemStack(Material.OAK_SIGN, 1);
            ItemMeta signMeta = signItem.getItemMeta();
            signMeta.setDisplayName(lm.shopAdminsButtonTitle());
            signMeta.setLore(signLoreChooser(hastAtLeastOneAdmin, dataContainer, lm));
            signItem.setItemMeta(signMeta);
            GuiItem signItemg = new GuiItem(signItem, event -> {
                event.setCancelled(true);
                //do the job
                if (event.getClick() == ClickType.LEFT) {
                    //left click == add admin

                    ChatListener.chatmap.put(player.getUniqueId(), new ChatWaitObject("none", "add", rightChest));
                    player.closeInventory();
                    player.sendMessage(lm.addingAdminWaiting());


                } else if (event.getClick() == ClickType.RIGHT) {
                    //right click == remove admin
                    ChatListener.chatmap.put(player.getUniqueId(), new ChatWaitObject("none", "remove", rightChest));
                    player.closeInventory();
                    player.sendMessage(lm.removingAdminWaiting());
                }

            });
            boolean isSharedIncome = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER) == 1;
            ItemStack sharedIncomeItem = new ItemStack(grayGreenChooser(isSharedIncome), 1);
            ItemMeta sharedIncomeMeta = sharedIncomeItem.getItemMeta();
            sharedIncomeMeta.setDisplayName(lm.shareIncomeButtonTitle());
            sharedIncomeMeta.setLore(shareIncomeLoreChooser(isSharedIncome, lm));
            sharedIncomeItem.setItemMeta(sharedIncomeMeta);
            GuiItem sharedIncome = new GuiItem(sharedIncomeItem, event -> {
                event.setCancelled(true);
                //start the functionality for shared income

                if (checkIfOn(event.getCurrentItem().getType())) {
                    rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 0);
                    rightChest.update();
                    player.sendMessage(lm.sharedIncomeOffInChat());
                    event.getCurrentItem().setType(Material.GRAY_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setLore(shareIncomeLoreChooser(false, lm));
                    event.getCurrentItem().setItemMeta(meta);
                    ShopContainer.getShopSettings(rightChest.getLocation()).setShareincome(false);
                } else {
                    rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
                    rightChest.update();
                    player.sendMessage(lm.sharedIncomeOnInChat());
                    event.getCurrentItem().setType(Material.LIME_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setLore(shareIncomeLoreChooser(true, lm));
                    event.getCurrentItem().setItemMeta(meta);
                    ShopContainer.getShopSettings(rightChest.getLocation()).setShareincome(true);
                }


            });
            gui.setItem(13, signItemg);
            if (hastAtLeastOneAdmin) {
                if (dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 0) {
                    gui.setItem(22, sharedIncome);
                }

            }
        }





        ItemStack backItemStack = new ItemStack(Material.DARK_OAK_DOOR, 1);
        ItemMeta backItemMeta = backItemStack.getItemMeta();
        backItemMeta.setDisplayName(lm.backToShopGuiButton());
        backItemStack.setItemMeta(backItemMeta);
        GuiItem backItem = new GuiItem(backItemStack, event -> {
           event.setCancelled(true);
            String owneruuid = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

            if (isAdminShop) {
                ServerShopGUI serverShopGUI = new ServerShopGUI();
                serverShopGUI.showGUI(player, dataContainer, rightChest, rightChest);
                return;
            }

            if (player.getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {
                if (player.hasPermission("ecs.admin")) {
                    AdminShopGUI adminShopGUI = new AdminShopGUI();
                    adminShopGUI.showGUI(player, dataContainer, rightChest, rightChest);
                    return;
                }
                //owner show special gui
                OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
                ownerShopGUI.showGUI(player, dataContainer, rightChest, rightChest, isAdmin);


            } else {

                //the only scenario that remains is when that person has an admin permission
                //just in case, i check again for player's permission
                if (player.hasPermission("ecs.admin")) {
                    AdminShopGUI adminShopGUI = new AdminShopGUI();
                    adminShopGUI.showGUI(player, dataContainer, rightChest, rightChest);
                } else {
                    player.closeInventory();
                }
            }

        });


        gui.setItem(0, backItem);
        //14-15-16
        gui.setItem(14, glasses);
        gui.setItem(15, glasses);
        gui.setItem(16, glasses);
        //17-26
        gui.setItem(26, lastTrans);

        //for forgotten logs button
        //this.themain.setItem(26, lastLogs);

        //10-11-12-13-(22 optional)
        gui.setItem(10, messageToggle);
        gui.setItem(11, buyDisabled);
        gui.setItem(12, sellDisabled);
        if (isAdmin) {
            gui.setItem(13, glasses);
        }

        gui.open(player);

    }


    private Material grayGreenChooser(boolean data) {
        if (data) {
            return Material.LIME_DYE;
        }
        return Material.GRAY_DYE;
    }

    private List<String> toggleMessageChooser(boolean data, LanguageManager lm) {
        List<String> lores;
        String status;
        if (data) {
            status = lm.statusOn();
        } else {
            status = lm.statusOff();
        }
        lores = lm.toggleTransactionMessageButtonLore(status);

        return lores;
     }
     private List<String> buyMessageChooser(boolean data, LanguageManager lm) {
        List<String> lores;
        String status;
         if (data) {
             status = lm.statusOn();
         } else {
             status = lm.statusOff();
         }
         lores = lm.disableBuyingButtonLore(status);

        return lores;
     }

    private List<String> sellMessageChooser(boolean data, LanguageManager lm) {
        List<String> lores;
        String status;
        if (data) {
            status = lm.statusOn();
        } else {
            status = lm.statusOff();
        }
        lores = lm.disableSellingButtonLore(status);
        return lores;
    }

    private List<String> shareIncomeLoreChooser(boolean data, LanguageManager lm) {
        List<String> lores;
        String status;
        if (data) {
            status = lm.statusOn();
        } else {
            status = lm.statusOff();
        }
        lores = lm.shareIncomeButtonLore(status);
        return lores;
    }

     private List<String> signLoreChooser(boolean data, PersistentDataContainer container, LanguageManager lm) {
        List<String> lores;
        String status;

        if (data) {
            //has at least one admin
            StringBuilder adminsListString = new StringBuilder("&a");
            List<UUID> admins = Utils.getAdminsList(container);
            boolean first = false;
            for (UUID admin : admins) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(admin);
                if (first) {

                    adminsListString.append(", ").append(offlinePlayer.getName());

                } else {
                    adminsListString.append(offlinePlayer.getName());
                    first = true;
                }
            }

            status = adminsListString.toString();
        } else {
            status = lm.nobodyStatusAdmins();
        }

        lores = lm.shopAdminsButtonLore(status);

        return lores;
     }

     private boolean checkIfOn(Material itemMat) {
        if (itemMat.equals(Material.LIME_DYE)) {
            return true;
        } else {
            return false;
        }

     }





}
