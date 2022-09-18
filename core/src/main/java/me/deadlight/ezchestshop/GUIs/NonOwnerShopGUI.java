package me.deadlight.ezchestshop.GUIs;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.GUI.ContainerGui;
import me.deadlight.ezchestshop.Data.GUI.ContainerGuiItem;
import me.deadlight.ezchestshop.Data.GUI.GuiData;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.SignMenuFactory;
import me.deadlight.ezchestshop.Utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
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

        ContainerGui container = GuiData.getShop();

        Gui gui = new Gui(container.getRows(), lm.guiNonOwnerTitle(shopOwner));
        gui.getFiller().fill(container.getBackground());

        ItemStack mainitem = Utils.decodeItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        if (container.hasItem("main-item")) {
            ItemStack guiMainItem = mainitem.clone();
            ItemMeta mainmeta = guiMainItem.getItemMeta();
            List<String> mainItemLore = Arrays.asList(lm.initialBuyPrice(buyPrice), lm.initialSellPrice(sellPrice));
            mainmeta.setLore(mainItemLore);
            guiMainItem.setItemMeta(mainmeta);
            GuiItem guiitem = new GuiItem(guiMainItem, event -> {
                event.setCancelled(true);
            });

            Utils.addItemIfEnoughSlots(gui, container.getItem("shop-item").getSlot(), guiitem);
        }

        container.getItemKeys().forEach(key -> {
            if (key.startsWith("sell-")) {
                String amountString = key.split("-")[1];
                int amount = 1;
                if (amountString.equals("all")) {
                    amount = Integer.parseInt(Utils.calculateSellPossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()), player.getInventory().getStorageContents(), Utils.getBlockInventory(containerBlock).getStorageContents(), sellPrice, mainitem));
                } else {
                    try {
                        amount = Integer.parseInt(amountString);
                    } catch (NumberFormatException e) {}
                }

                ContainerGuiItem sellItemStack = container.getItem(key).setLore(lm.buttonSellXLore(sellPrice * amount, amount)).setName(lm.buttonSellXTitle(amount));

                final int finalAmount = amount;
                GuiItem sellItem = new GuiItem(disablingCheck(sellItemStack.getItem(), disabledSell), event -> {
                    // sell things
                    event.setCancelled(true);
                    if (disabledSell) {
                        return;
                    }
                    ShopContainer.sellItem(containerBlock, sellPrice * finalAmount, finalAmount, mainitem, player, offlinePlayerOwner, data);
                    showGUI(player, data, containerBlock);
                });

                Utils.addItemIfEnoughSlots(gui, sellItemStack.getSlot(), sellItem);
            } else if (key.startsWith("buy-")) {
                String amountString = key.split("-")[1];
                int amount = 1;
                if (amountString.equals("all")) {
                    amount = Integer.parseInt(Utils.calculateBuyPossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()), player.getInventory().getStorageContents(), Utils.getBlockInventory(containerBlock).getStorageContents(), buyPrice, mainitem));
                } else {
                    try {
                        amount = Integer.parseInt(amountString);
                    } catch (NumberFormatException e) {}
                }

                ContainerGuiItem buyItemStack = container.getItem(key).setLore(lm.buttonBuyXLore(buyPrice * amount, amount)).setName(lm.buttonBuyXTitle(amount));

                final int finalAmount = amount;
                GuiItem buyItem = new GuiItem(disablingCheck(buyItemStack.getItem(), disabledBuy), event -> {
                    // buy things
                    event.setCancelled(true);
                    if (disabledBuy) {
                        return;
                    }
                    ShopContainer.buyItem(containerBlock, buyPrice * finalAmount, finalAmount, mainitem, player, offlinePlayerOwner, data);
                    showGUI(player, data, containerBlock);
                });

                Utils.addItemIfEnoughSlots(gui, buyItemStack.getSlot(), buyItem);
            }
        });

        if (container.hasItem("custom-buy-sell")) {

            List<String> possibleCounts = Utils.calculatePossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()), offlinePlayerOwner, player.getInventory().getStorageContents(), Utils.getBlockInventory(containerBlock).getStorageContents(), buyPrice, sellPrice, mainitem);
            ContainerGuiItem customBuySellItemStack = container.getItem("custom-buy-sell").setName(lm.customAmountSignTitle()).setLore(lm.customAmountSignLore(possibleCounts.get(0), possibleCounts.get(1)));

            GuiItem guiSignItem = new GuiItem(customBuySellItemStack.getItem(), event -> {
                event.setCancelled(true);
                if (event.isRightClick()) {
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
                                                ShopContainer.buyItem(containerBlock, buyPrice * amount, amount, mainitem, player, offlinePlayerOwner, data);
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


                } else if (event.isLeftClick()) {
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
                                                ShopContainer.sellItem(containerBlock, sellPrice * amount, amount, mainitem, player, offlinePlayerOwner, data);
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
            if (Config.settings_custom_amout_transactions) {
                //sign item
                Utils.addItemIfEnoughSlots(gui, customBuySellItemStack.getSlot(), guiSignItem);
            }
        }


        gui.open(player);



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
