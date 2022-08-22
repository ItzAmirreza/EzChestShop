package me.deadlight.ezchestshop.GUIs;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Enums.LogType;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.ChatListener;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Utils.Objects.ChatWaitObject;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.SignMenuFactory;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class SettingsGUI {
    public static LanguageManager lm = new LanguageManager();
    //msgtoggle 0/1
    //dbuy 0/1
    //dsell 0/1
    //admins [list of uuids seperated with @ in string form]
    //shareincome 0/1
    //logs [list of infos seperated by @ in string form]
    //trans [list of infos seperated by @ in string form]
    //adminshop 0/1

    public void showGUI(Player player, Block containerBlock, boolean isAdmin) {

        PersistentDataContainer dataContainer = ((TileState)containerBlock.getState()).getPersistentDataContainer();

        boolean isAdminShop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;


        Gui gui = new Gui(3, lm.settingsGuiTitle());
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.colorify("&d"));
        glassis.setItemMeta(glassmeta);
        GuiItem glasses = new GuiItem(glassis, event -> {
            event.setCancelled(true);
        });
        //I changed its place to first part of code because of different GUI sections for admins and owners

        gui.getFiller().fillBorder(glasses);

        //trans
        ItemStack lastTransItem = new ItemStack(Material.PAPER, 1);
        ItemMeta lastTransMeta = lastTransItem.getItemMeta();
        lastTransMeta.setDisplayName(lm.latestTransactionsTitle());
        lastTransItem.setItemMeta(lastTransMeta);
        GuiItem lastTrans = new GuiItem(lastTransItem, event -> {
           event.setCancelled(true);
           LogsGUI logsGUI = new LogsGUI();
           logsGUI.showGUI(player, dataContainer, containerBlock, LogType.TRANSACTION, isAdmin);
        });

        //I was going to add logs section which would be about actions in that containerBlock shop but I decided not to implement it. maybe later
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
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
                state.update();
                player.sendMessage(lm.toggleTransactionMessageOffInChat());
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(toggleMessageChooser(false, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(containerBlock.getLocation()).setMsgtoggle(false);
            } else {
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 1);
                state.update();
                player.sendMessage(lm.toggleTransactionMessageOnInChat());
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(toggleMessageChooser(true, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(containerBlock.getLocation()).setMsgtoggle(true);

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
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
                state.update();
                player.sendMessage(lm.disableBuyingOffInChat());
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(buyMessageChooser(false, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(containerBlock.getLocation()).setDbuy(false);
            } else {
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 1);
                state.update();
                player.sendMessage(lm.disableBuyingOnInChat());
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(buyMessageChooser(true, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(containerBlock.getLocation()).setDbuy(true);
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
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
                state.update();
                player.sendMessage(lm.disableSellingOffInChat());
                event.getCurrentItem().setType(Material.GRAY_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(sellMessageChooser(false, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(containerBlock.getLocation()).setDsell(false);
            } else {
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 1);
                state.update();
                player.sendMessage(lm.disableSellingOnInChat());
                event.getCurrentItem().setType(Material.LIME_DYE);
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(sellMessageChooser(true, lm));
                event.getCurrentItem().setItemMeta(meta);
                ShopContainer.getShopSettings(containerBlock.getLocation()).setDsell(true);
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

                    ChatListener.chatmap.put(player.getUniqueId(), new ChatWaitObject("none", "add", containerBlock));
                    player.closeInventory();
                    player.sendMessage(lm.addingAdminWaiting());


                } else if (event.getClick() == ClickType.RIGHT) {
                    //right click == remove admin
                    ChatListener.chatmap.put(player.getUniqueId(), new ChatWaitObject("none", "remove", containerBlock));
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
                    TileState state = ((TileState)containerBlock.getState());
                    state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 0);
                    state.update();
                    player.sendMessage(lm.sharedIncomeOffInChat());
                    event.getCurrentItem().setType(Material.GRAY_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setLore(shareIncomeLoreChooser(false, lm));
                    event.getCurrentItem().setItemMeta(meta);
                    ShopContainer.getShopSettings(containerBlock.getLocation()).setShareincome(false);
                } else {
                    TileState state = ((TileState)containerBlock.getState());
                    state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
                    state.update();
                    player.sendMessage(lm.sharedIncomeOnInChat());
                    event.getCurrentItem().setType(Material.LIME_DYE);
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    meta.setLore(shareIncomeLoreChooser(true, lm));
                    event.getCurrentItem().setItemMeta(meta);
                    ShopContainer.getShopSettings(containerBlock.getLocation()).setShareincome(true);
                }


            });
            gui.setItem(13, signItemg);
            if (hastAtLeastOneAdmin) {
                if (dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 0) {
                    gui.setItem(22, sharedIncome);
                }

            }
        }



        String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        ItemStack rotationItemStack= new ItemStack(Material.COMPASS, 1);
        ItemMeta rotationItemMeta = rotationItemStack.getItemMeta();
        rotationItemMeta.setDisplayName(lm.rotateHologramButtonTitle());
        rotationItemMeta.setLore(lm.rotateHologramButtonLore(rotation));
        rotationItemStack.setItemMeta(rotationItemMeta);
        GuiItem rotationItem = new GuiItem(rotationItemStack, event -> {
            event.setCancelled(true);
            String next_rotation;
            if (event.getClick() == ClickType.LEFT) {
                next_rotation = Utils.getPreviousRotation(ShopContainer.getShopSettings(containerBlock.getLocation()).getRotation());
            } else if (event.getClick() == ClickType.RIGHT) {
                next_rotation = Utils.getNextRotation(ShopContainer.getShopSettings(containerBlock.getLocation()).getRotation());
            } else {
                return;
            }
            TileState state = ((TileState)containerBlock.getState());
            state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, next_rotation);
            state.update();
            player.sendMessage(lm.rotateHologramInChat(next_rotation));
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            meta.setLore(lm.rotateHologramButtonLore(next_rotation));
            event.getCurrentItem().setItemMeta(meta);
            ShopContainer.getShopSettings(containerBlock.getLocation()).setRotation(next_rotation);
            if (Config.holodistancing) {
                PlayerCloseToChestListener.hideHologram(containerBlock.getLocation());
            }
        });


        ItemStack priceItemStack= new ItemStack(Material.HOPPER, 1);
        ItemMeta priceItemMeta = priceItemStack.getItemMeta();
        priceItemMeta.setDisplayName(lm.changePricesButtonTitle());
        priceItemMeta.setLore(lm.changePricesButtonLore());
        priceItemStack.setItemMeta(priceItemMeta);
        GuiItem priceItem = new GuiItem(priceItemStack, event -> {
            event.setCancelled(true);
            if (event.getClick() == ClickType.LEFT) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                SignMenuFactory signMenuFactory = new SignMenuFactory(EzChestShop.getPlugin());
                SignMenuFactory.Menu menu = signMenuFactory.newMenu(lm.changePriceSingGUI(false))
                        .reopenIfFail(false).response((thatplayer, strings) -> {
                            try {
                                if (strings[0].equalsIgnoreCase("")) {
                                    return false;
                                }
                                if (Utils.isInteger(strings[0])) {
                                    int amount = Integer.parseInt(strings[0]);
                                    if (amount < 0) {
                                        player.sendMessage(lm.negativePrice());
                                        return false;
                                    }
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(),
                                            () -> {
                                                // If these checks complete successfully continue.
                                                if (changePrice(containerBlock.getState(), false, amount, player, containerBlock)) {
                                                    ShopContainer.changePrice(containerBlock.getState(), amount, false);
                                                    PlayerCloseToChestListener.hideHologram(containerBlock.getState().getLocation());
                                                    player.sendMessage(lm.shopSellPriceUpdated());
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
            } else if (event.getClick() == ClickType.RIGHT) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                SignMenuFactory signMenuFactory = new SignMenuFactory(EzChestShop.getPlugin());
                SignMenuFactory.Menu menu = signMenuFactory.newMenu(lm.changePriceSingGUI(true))
                        .reopenIfFail(false).response((thatplayer, strings) -> {
                            try {
                                if (strings[0].equalsIgnoreCase("")) {
                                    return false;
                                }
                                if (Utils.isInteger(strings[0])) {
                                    int amount = Integer.parseInt(strings[0]);
                                    if (amount < 0) {
                                        player.sendMessage(lm.negativePrice());
                                        return false;
                                    }
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(),
                                            () -> {
                                                // If these checks complete successfully continue.
                                                if (changePrice(containerBlock.getState(), true, amount, player, containerBlock)) {
                                                    ShopContainer.changePrice(containerBlock.getState(), amount, true);
                                                    PlayerCloseToChestListener.hideHologram(containerBlock.getState().getLocation());
                                                    player.sendMessage(lm.shopBuyPriceUpdated());
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
            } else {
                return;
            }
        });



        ItemStack backItemStack = new ItemStack(Material.DARK_OAK_DOOR, 1);
        ItemMeta backItemMeta = backItemStack.getItemMeta();
        backItemMeta.setDisplayName(lm.backToShopGuiButton());
        backItemStack.setItemMeta(backItemMeta);
        GuiItem backItem = new GuiItem(backItemStack, event -> {
           event.setCancelled(true);
            String owneruuid = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

            if (isAdminShop) {
                ServerShopGUI serverShopGUI = new ServerShopGUI();
                serverShopGUI.showGUI(player, dataContainer, containerBlock);
                return;
            }

            if (player.getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {
                if (player.hasPermission("ecs.admin")) {
                    AdminShopGUI adminShopGUI = new AdminShopGUI();
                    adminShopGUI.showGUI(player, dataContainer, containerBlock);
                    return;
                }
                //owner show special gui
                OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
                ownerShopGUI.showGUI(player, dataContainer, containerBlock, isAdmin);


            } else {

                //the only scenario that remains is when that person has an admin permission
                //just in case, i check again for player's permission
                if (player.hasPermission("ecs.admin")) {
                    AdminShopGUI adminShopGUI = new AdminShopGUI();
                    adminShopGUI.showGUI(player, dataContainer, containerBlock);
                } else {
                    player.closeInventory();
                }
            }

        });


        gui.setItem(0, backItem);
        //14-15-16
        if (Config.holo_rotation) {
            gui.setItem(14, rotationItem);
        } else {
            gui.setItem(14, glasses);
        }
        gui.setItem(15, priceItem);
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

     private boolean changePrice(BlockState blockState, boolean isBuy, double price, Player player, Block containerBlock) {
         EzShop shop = ShopContainer.getShop(blockState.getLocation());
         // Enforce buy > sell.
         if (Config.settings_buy_greater_than_sell) {
             if (
                     (isBuy && shop.getSellPrice() > price && price != 0) ||
                             (!isBuy && price > shop.getBuyPrice() && shop.getBuyPrice() != 0)
             ) {
                 player.sendMessage(lm.buyGreaterThanSellRequired());
                 return false;
             }
         }
         // Revert from disabling buy sell.
         if (Config.settings_zero_equals_disabled && isBuy && shop.getBuyPrice() == 0 && price != 0) {
             TileState state = ((TileState)containerBlock.getState());
             state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
             state.update();
             player.sendMessage(lm.disableBuyingOffInChat());
             ShopContainer.getShopSettings(containerBlock.getLocation()).setDbuy(false);
         }
         if (Config.settings_zero_equals_disabled && !isBuy && shop.getSellPrice() == 0 && price != 0) {
             TileState state = ((TileState)containerBlock.getState());
             state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
             state.update();
             player.sendMessage(lm.disableSellingOffInChat());
             ShopContainer.getShopSettings(containerBlock.getLocation()).setDsell(false);
         }
         // Disable buy/sell
         if (price == 0 && Config.settings_zero_equals_disabled) {
             if (isBuy && shop.getBuyPrice() != 0) {
                 TileState state = ((TileState)containerBlock.getState());
                 state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 1);
                 state.update();
                 player.sendMessage(lm.disableBuyingOffInChat());
                 ShopContainer.getShopSettings(containerBlock.getLocation()).setDbuy(true);
             }
             if (!isBuy && shop.getSellPrice() != 0) {
                 TileState state = ((TileState)containerBlock.getState());
                 state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 1);
                 state.update();
                 player.sendMessage(lm.disableSellingOffInChat());
                 ShopContainer.getShopSettings(containerBlock.getLocation()).setDsell(true);
             }
         }
         return true;
     }





}
