package me.deadlight.ezchestshop.guis;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.PlayerContainer;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.SignMenuFactory;
import me.deadlight.ezchestshop.utils.Utils;
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

    public NonOwnerShopGUI() {}

    public void showGUI(Player player, PersistentDataContainer data, Block containerBlock) {
        LanguageManager lm = new LanguageManager();
        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
        String shopOwner = offlinePlayerOwner.getName();
        if (shopOwner == null) {
            boolean result = Utils.reInstallNamespacedKeyValues(data, containerBlock.getLocation());
            if (!result) {
                player.sendMessage(lm.chestShopProblem());
                return;
            }
            containerBlock.getState().update();
            EzShop shop = ShopContainer.getShop(containerBlock.getLocation());
            shopOwner = Bukkit.getOfflinePlayer(shop.getOwnerID()).getName();
            if (shopOwner == null) {
                player.sendMessage(lm.chestShopProblem());
                System.out.println("EzChestShop ERROR: Shop owner is STILL null. Please report this to the EzChestShop developer for furthur investigation.");
                return;
            }
        }
        double sellPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        double buyPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        boolean disabledBuy = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean disabledSell = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;

        ContainerGui container = GuiData.getShop();

        Gui gui = new Gui(container.getRows(), lm.guiNonOwnerTitle(shopOwner));
        gui.getFiller().fill(container.getBackground());

        ItemStack mainitem = Utils.decodeItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        if (container.hasItem("shop-item")) {
            ItemStack guiMainItem = mainitem.clone();
            ItemMeta mainmeta = guiMainItem.getItemMeta();
            // Set the lore and keep the old one if available
            if (mainmeta.hasLore()) {
                List<String> prevLore = mainmeta.getLore();
                prevLore.add("");
                List<String> mainItemLore = Arrays.asList(lm.initialBuyPrice(buyPrice), lm.initialSellPrice(sellPrice));
                prevLore.addAll(mainItemLore);
                mainmeta.setLore(prevLore);
            } else {
                List<String> mainItemLore = Arrays.asList(lm.initialBuyPrice(buyPrice), lm.initialSellPrice(sellPrice));
                mainmeta.setLore(mainItemLore);
            }
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
                } else if (amountString.equals("maxStackSize")) {
                    amount = mainitem.getMaxStackSize();
                    container.getItem(key).setAmount(amount);
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
                } else if (amountString.equals("maxStackSize")) {
                    amount = mainitem.getMaxStackSize();
                    container.getItem(key).setAmount(amount);
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
            } else if (key.startsWith("decorative-")) {

                ContainerGuiItem decorativeItemStack = container.getItem(key).setName(Utils.colorify("&d"));

                GuiItem buyItem = new GuiItem(decorativeItemStack.getItem(), event -> {
                    event.setCancelled(true);
                });

                Utils.addItemIfEnoughSlots(gui, decorativeItemStack.getSlot(), buyItem);
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
                                        EzChestShop.getScheduler().scheduleSyncDelayedTask(() -> ShopContainer.buyItem(containerBlock, buyPrice * amount, amount, mainitem, player, offlinePlayerOwner, data));
                                    } else {
                                        thatplayer.sendMessage(lm.wrongInput());
                                    }

                                } catch (Exception e) {
                                    return false;
                                }
                                return true;
                            });
                    menu.open(player);
                    PlayerContainer pc = PlayerContainer.get(player);
                    pc.openSignMenu(menu, containerBlock.getLocation());
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
                                        EzChestShop.getScheduler().scheduleSyncDelayedTask(() -> ShopContainer.sellItem(containerBlock, sellPrice * amount, amount, mainitem, player, offlinePlayerOwner, data));
                                    } else {
                                        thatplayer.sendMessage(lm.wrongInput());
                                    }


                                } catch (Exception e) {
                                    return false;
                                }
                                return true;
                            });
                    menu.open(player);
                    PlayerContainer pc = PlayerContainer.get(player);
                    pc.openSignMenu(menu, containerBlock.getLocation());
                    player.sendMessage(lm.enterTheAmount());


                }
            });
            if (Config.settings_custom_amout_transactions) {
                //sign item
                Utils.addItemIfEnoughSlots(gui, customBuySellItemStack.getSlot(), guiSignItem);
            }
        }


        gui.open(player);
        PlayerContainer pc = PlayerContainer.get(player);
        pc.openGUI(gui, containerBlock.getLocation());



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
}
