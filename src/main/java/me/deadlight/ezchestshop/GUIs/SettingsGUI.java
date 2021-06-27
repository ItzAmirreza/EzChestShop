package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.LanguageManager;
import me.deadlight.ezchestshop.Listeners.ChatListener;
import me.deadlight.ezchestshop.Utils.ChatWaitObject;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
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

import java.util.ArrayList;
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

    private Gui themain;

    public void ShowGUI(Player player, Chest rightChest, boolean isAdmin) {

        LanguageManager lm = new LanguageManager();

        PersistentDataContainer dataContainer = rightChest.getPersistentDataContainer();

        boolean isAdminShop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;


        this.themain = new Gui(3, Utils.color("&b&lSettings"));
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.color("&d"));
        glassis.setItemMeta(glassmeta);

        GuiItem glasses = new GuiItem(glassis, event -> {
            // Handle your click action here
            event.setCancelled(true);
        });
        //I changed its place to first part of code because of different GUI sections for admins and owners
        this.themain.getFiller().fillBorder(glasses);

        //trans
        ItemStack lastTransItem = new ItemStack(Material.PAPER, 1);
        ItemMeta lastTransMeta = lastTransItem.getItemMeta();
        lastTransMeta.setDisplayName(Utils.color("&aLatest Transactions"));
        lastTransItem.setItemMeta(lastTransMeta);
        GuiItem lastTrans = new GuiItem(lastTransItem, event -> {
           event.setCancelled(true);
        });

        //settings change logs
        ItemStack lastLogsItem = new ItemStack(Material.PAPER, 1);
        ItemMeta lastLogsMeta = lastLogsItem.getItemMeta();
        lastLogsMeta.setDisplayName(Utils.color("&aLatest Logs"));
        lastLogsItem.setItemMeta(lastLogsMeta);
        GuiItem lastLogs = new GuiItem(lastLogsItem, event -> {
           event.setCancelled(true);
        });
        //Message Toggle Item
        boolean isToggleMessageOn = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
        ItemStack messageToggleItem = new ItemStack(grayGreenChooser(isToggleMessageOn), 1);
        ItemMeta messageToggleMeta = messageToggleItem.getItemMeta();
        messageToggleMeta.setDisplayName(Utils.color("&eToggle Transaction Message"));
        messageToggleMeta.setLore(toggleMessageChooser(isToggleMessageOn));
        messageToggleItem.setItemMeta(messageToggleMeta);



        GuiItem messageToggle = new GuiItem(messageToggleItem, event -> {
            event.setCancelled(true);
            //start the functionality for toggle message
            if (checkIfOn(event.getCurrentItem().getType())) {

                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
                rightChest.update();
                player.sendMessage(Utils.color("&7Toggle Transaction Messages: &cOFF"));
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(toggleMessageChooser(false));
                event.getCurrentItem().setItemMeta(meta);
            } else {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 1);
                rightChest.update();
                player.sendMessage(Utils.color("&7Toggle Transaction Messages: &aON"));
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(toggleMessageChooser(true));
                event.getCurrentItem().setItemMeta(meta);

            }
        });

        //Message Toggle Item
        boolean isBuyDisabled = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        ItemStack buyDisabledItem= new ItemStack(grayGreenChooser(isBuyDisabled), 1);
        ItemMeta buyDisabledMeta = buyDisabledItem.getItemMeta();
        buyDisabledMeta.setDisplayName(Utils.color("&eDisable Buying"));
        buyDisabledMeta.setLore(buyMessageChooser(isBuyDisabled));
        buyDisabledItem.setItemMeta(buyDisabledMeta);
        GuiItem buyDisabled = new GuiItem(buyDisabledItem, event -> {
            event.setCancelled(true);
            //start the functionality for disabling buy
            if (checkIfOn(event.getCurrentItem().getType())) {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
                rightChest.update();
                player.sendMessage(Utils.color("&7Disable Buying: &cOFF"));
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(buyMessageChooser(false));
                event.getCurrentItem().setItemMeta(meta);
            } else {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 1);
                rightChest.update();
                player.sendMessage(Utils.color("&7Disable Buying: &aON"));
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(buyMessageChooser(true));
                event.getCurrentItem().setItemMeta(meta);
            }

        });

        boolean isSellDisabled = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;
        ItemStack sellDisabledItem= new ItemStack(grayGreenChooser(isSellDisabled), 1);
        ItemMeta sellDisabledMeta = sellDisabledItem.getItemMeta();
        sellDisabledMeta.setDisplayName(Utils.color("&eDisable Selling"));
        sellDisabledMeta.setLore(sellMessageChooser(isSellDisabled));
        sellDisabledItem.setItemMeta(sellDisabledMeta);
        GuiItem sellDisabled = new GuiItem(sellDisabledItem, event -> {
            event.setCancelled(true);
            //start the functionality for disabling sell
            if (checkIfOn(event.getCurrentItem().getType())) {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
                rightChest.update();
                player.sendMessage(Utils.color("&7Disable Selling: &cOFF"));
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(sellMessageChooser(false));
                event.getCurrentItem().setItemMeta(meta);
            } else {
                rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 1);
                rightChest.update();
                player.sendMessage(Utils.color("&7Disable Selling: &aON"));
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(sellMessageChooser(true));
                event.getCurrentItem().setItemMeta(meta);
            }
        });

        //How it saves the owners? like this man
        //if empty: "none"
        //otherwise: UUID@UUID@UUID@UUID
        if (!isAdmin) {
            boolean hastAtLeastOneAdmin = !dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING).equals("none");
            ItemStack signItem = new ItemStack(Material.OAK_SIGN, 1);
            ItemMeta signMeta = signItem.getItemMeta();
            signMeta.setDisplayName(Utils.color("&eShop admins"));
            signMeta.setLore(signLoreChooser(hastAtLeastOneAdmin, dataContainer));
            signItem.setItemMeta(signMeta);
            GuiItem signItemg = new GuiItem(signItem, event -> {
                event.setCancelled(true);
                //do the job
                if (event.getClick() == ClickType.LEFT) {
                    //left click == add admin

                    ChatListener.chatmap.put(player.getUniqueId(), new ChatWaitObject("none", "add", rightChest));
                    player.closeInventory();
                    player.sendMessage(Utils.color("&ePlease enter the name of the person you want to add to the list of admins."));


                } else if (event.getClick() == ClickType.RIGHT) {
                    //right click == remove admin
                    ChatListener.chatmap.put(player.getUniqueId(), new ChatWaitObject("none", "remove", rightChest));
                    player.closeInventory();
                    player.sendMessage(Utils.color("&ePlease enter the name of the person you want to remove from the list of admins."));
                }

            });
            boolean isSharedIncome = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER) == 1;
            ItemStack sharedIncomeItem = new ItemStack(grayGreenChooser(isSharedIncome), 1);
            ItemMeta sharedIncomeMeta = sharedIncomeItem.getItemMeta();
            sharedIncomeMeta.setDisplayName(Utils.color("&eShared income"));
            sharedIncomeMeta.setLore(shareIncomeLoreChooser(isSharedIncome));
            sharedIncomeItem.setItemMeta(sharedIncomeMeta);
            GuiItem sharedIncome = new GuiItem(sharedIncomeItem, event -> {
                event.setCancelled(true);
                //start the functionality for shared income

                if (checkIfOn(event.getCurrentItem().getType())) {
                    rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 0);
                    rightChest.update();
                    player.sendMessage(Utils.color("&7Shared income: &cOFF"));
                    event.getCurrentItem().setType(Material.GRAY_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setLore(shareIncomeLoreChooser(false));
                    event.getCurrentItem().setItemMeta(meta);
                } else {
                    rightChest.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
                    rightChest.update();
                    player.sendMessage(Utils.color("&7Shared income: &aON"));
                    event.getCurrentItem().setType(Material.LIME_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setLore(shareIncomeLoreChooser(true));
                    event.getCurrentItem().setItemMeta(meta);
                }


            });
            this.themain.setItem(13, signItemg);
            if (hastAtLeastOneAdmin) {
                this.themain.setItem(22, sharedIncome);
            }
        }

        GuiItem fillerItem = new GuiItem(glassis, event -> {
            // Handle your click action here
            event.setCancelled(true);
        });



        ItemStack backItemStack = new ItemStack(Material.DARK_OAK_DOOR, 1);
        ItemMeta backItemMeta = backItemStack.getItemMeta();
        backItemMeta.setDisplayName(Utils.color("&eBack to shop"));
        backItemStack.setItemMeta(backItemMeta);
        GuiItem backItem = new GuiItem(backItemStack, event -> {
           event.setCancelled(true);
            String owneruuid = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

            if (isAdminShop) {
                ServerShopGUI serverShopGUI = new ServerShopGUI();
                serverShopGUI.showGUI(player, dataContainer, rightChest, rightChest);
                return;
            }

            if (player.getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) { //I have to check for admins later
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


        this.themain.setItem(0, backItem);
        //14-15-16
        this.themain.setItem(14, glasses);
        this.themain.setItem(15, glasses);
        this.themain.setItem(16, glasses);
        //17-26
        this.themain.setItem(17, lastTrans);
        this.themain.setItem(26, lastLogs);

        //10-11-12-13-(22 optional)
        this.themain.setItem(10, messageToggle);
        this.themain.setItem(11, buyDisabled);
        this.themain.setItem(12, sellDisabled);
        if (isAdmin) {
            this.themain.setItem(13, fillerItem);
        }

        this.themain.open(player);

    }


    private Material grayGreenChooser(boolean data) {
        if (data) {
            return Material.LIME_DYE;
        }
        return Material.GRAY_DYE;
    }

    private List<String> toggleMessageChooser(boolean data) {
        List<String> lores = new ArrayList<>();
        if (data) {
            lores.add(Utils.color("&7Current status: &aOn \n \n"));
        } else {
            lores.add(Utils.color("&7Current status: &cOff \n \n"));
        }
        lores.add(Utils.color("&7If you keep this option on,"));
        lores.add(Utils.color("&7you will recieve transaction"));
        lores.add(Utils.color("&7messages in chat whenever someone"));
        lores.add(Utils.color("&7buy/sell something from this shop"));
        return lores;
     }
     private List<String> buyMessageChooser(boolean data) {
        List<String> lores = new ArrayList<>();
        if (data) {
            lores.add(Utils.color("&7Current status: &aOn \n \n"));
        } else {
            lores.add(Utils.color("&7Current status: &cOff \n \n"));
        }
        lores.add(Utils.color("&7If you keep this option on,"));
        lores.add(Utils.color("&7the shop won't let anyone buy"));
        lores.add(Utils.color("&7from your chest shop."));
        return lores;
     }

    private List<String> sellMessageChooser(boolean data) {
        List<String> lores = new ArrayList<>();
        if (data) {
            lores.add(Utils.color("&7Current status: &aOn \n \n"));
        } else {
            lores.add(Utils.color("&7Current status: &cOff \n \n"));
        }
        lores.add(Utils.color("&7If you keep this option on,"));
        lores.add(Utils.color("&7the shop won't let anyone sell"));
        lores.add(Utils.color("&7anything to the shop."));
        return lores;
    }

    private List<String> shareIncomeLoreChooser(boolean data) {
        List<String> lores = new ArrayList<>();
        if (data) {
            lores.add(Utils.color("&7Current status: &aOn \n \n"));
        } else {
            lores.add(Utils.color("&7Current status: &cOff \n \n"));
        }
        lores.add(Utils.color("&7If you keep this option on,"));
        lores.add(Utils.color("&7the profit of ONLY sales, will be"));
        lores.add(Utils.color("&7shared with admins as well."));
        return lores;
    }

     private List<String> signLoreChooser(boolean data, PersistentDataContainer container) {
        List<String> lores = new ArrayList<>();
         lores.add(Utils.color("&7You can add/remove admins to"));
         lores.add(Utils.color("&7your chest shop. Admins are able to"));
         lores.add(Utils.color("&7access the shop storage & access certain"));
         lores.add(Utils.color("&7settings (everything except share income and add/remove-ing admins)."));
         lores.add(Utils.color("&aLeft Click &7to add an admin"));
         lores.add(Utils.color("&cRight Click &7to remove an admin \n \n"));
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

            lores.add(Utils.color("&7Current admins: " + adminsListString));
        } else {
            lores.add(Utils.color("&7Current admins: &aNobody \n \n"));
        }

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
