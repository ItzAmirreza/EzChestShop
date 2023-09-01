package me.deadlight.ezchestshop.guis.tradeshop;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.TradeShopContainer;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.guis.shared.CustomMessageManageGUI;
import me.deadlight.ezchestshop.listeners.ChatListener;
import me.deadlight.ezchestshop.utils.InventoryUtils;
import me.deadlight.ezchestshop.utils.NumberUtils;
import me.deadlight.ezchestshop.utils.SignMenuFactory;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.holograms.TradeShopHologram;
import me.deadlight.ezchestshop.utils.objects.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TradeSettingsGUI {
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

        EzTradeShop tradeShop = TradeShopContainer.getTradeShop(containerBlock.getLocation());
        ContainerGui container = GuiData.getTradeShopSettings();
        PersistentDataContainer dataContainer = ((TileState)containerBlock.getState()).getPersistentDataContainer();

        boolean isAdminShop = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1;


        Gui gui = new Gui(container.getRows(), lm.settingsGuiTitle());

        gui.getFiller().fill(container.getBackground());

        //trans This menu will not be available for now, until I find a way to make it work properly
//        if (container.hasItem("latest-transactions")) {
//            ContainerGuiItem lastTransItem = container.getItem("latest-transactions").setName(lm.latestTransactionsTitle());
//            GuiItem lastTrans = new GuiItem(lastTransItem.getItem(), event -> {
//               event.setCancelled(true);
//               LogsGUI logsGUI = new LogsGUI();
//               logsGUI.showGUI(player, dataContainer, containerBlock, LogType.TRANSACTION, isAdmin);
//            });
//
//            InventoryUtils.addItemIfEnoughSlots(gui, lastTransItem.getSlot(), lastTrans);
//        }

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
        if (container.hasItem("toggle-transaction-message-on") && container.hasItem("toggle-transaction-message-off")) {
            boolean isToggleMessageOn = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1;
            ContainerGuiItem messageToggleItem = container.getItem(isToggleMessageOn ? "toggle-transaction-message-on" : "toggle-transaction-message-off")
                    .setName(lm.toggleTransactionMessageButton()).setLore(toggleMessageChooser(isToggleMessageOn, lm));
            GuiItem messageToggle = new GuiItem(messageToggleItem.getItem(), event -> {
                event.setCancelled(true);
                //start the functionality for toggle message
                if (dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1) {
                    TileState state = ((TileState)containerBlock.getState());
                    state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
                    state.update();
                    player.sendMessage(lm.toggleTransactionMessageOffInChat());
                    showGUI(player, containerBlock, isAdmin);
                    TradeShopContainer.getTradeShopSettings(containerBlock.getLocation()).setMsgtoggle(false);
                } else {
                    TileState state = ((TileState)containerBlock.getState());
                    state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 1);
                    state.update();
                    player.sendMessage(lm.toggleTransactionMessageOnInChat());
                    showGUI(player, containerBlock, isAdmin);
                    TradeShopContainer.getTradeShopSettings(containerBlock.getLocation()).setMsgtoggle(true);

                }
            });
            InventoryUtils.addItemIfEnoughSlots(gui, messageToggleItem.getSlot(), messageToggle);
        }

        if (container.hasItem("toggle-trade-direction-item1toitem2") || container.hasItem("toggle-trade-direction-item2toitem1") ||
                container.hasItem("toggle-trade-direction-both") || container.hasItem("toggle-trade-direction-disabled")) {
            ItemStack tradeDirectionItem;
            ContainerGuiItem tradeDirectionContainerGuiItem;
            GuiItem tradeDirectionGuiItem;
            switch (tradeShop.getSettings().getTradeDirection()) {
                case ITEM1_TO_ITEM2:
                    tradeDirectionContainerGuiItem = container.getItem("toggle-trade-direction-item1toitem2");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    tradeDirectionGuiItem = new GuiItem(tradeDirectionItem, event -> {
                        event.setCancelled(true);
                        tradeShop.getSettings().setTradeDirection(TradeShopSettings.TradeDirection.ITEM2_TO_ITEM1);
                        TileState state = ((TileState)containerBlock.getState());
                        state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"),
                                PersistentDataType.STRING, tradeShop.getSettings().getTradeDirection().toString());
                        state.update();
                        TradeShopHologram.getHologram(containerBlock.getLocation(), player).updateTradeDirection();
                        showGUI(player, containerBlock, isAdmin);
                    });
                    break;
                case ITEM2_TO_ITEM1:
                    tradeDirectionContainerGuiItem = container.getItem("toggle-trade-direction-item2toitem1");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    tradeDirectionGuiItem = new GuiItem(tradeDirectionItem, event -> {
                        event.setCancelled(true);
                        tradeShop.getSettings().setTradeDirection(TradeShopSettings.TradeDirection.BOTH);
                        TileState state = ((TileState)containerBlock.getState());
                        state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"),
                                PersistentDataType.STRING, tradeShop.getSettings().getTradeDirection().toString());
                        state.update();
                        TradeShopHologram.getHologram(containerBlock.getLocation(), player).updateTradeDirection();
                        showGUI(player, containerBlock, isAdmin);
                    });
                    break;
                case BOTH:
                    tradeDirectionContainerGuiItem = container.getItem("toggle-trade-direction-both");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    tradeDirectionGuiItem = new GuiItem(tradeDirectionItem, event -> {
                        event.setCancelled(true);
                        tradeShop.getSettings().setTradeDirection(TradeShopSettings.TradeDirection.DISABLED);
                        TileState state = ((TileState)containerBlock.getState());
                        state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"),
                                PersistentDataType.STRING, tradeShop.getSettings().getTradeDirection().toString());
                        state.update();
                        TradeShopHologram.getHologram(containerBlock.getLocation(), player).updateTradeDirection();
                        showGUI(player, containerBlock, isAdmin);
                    });
                    break;
                default:
                    tradeDirectionContainerGuiItem = container.getItem("toggle-trade-direction-disabled");
                    tradeDirectionItem = tradeDirectionContainerGuiItem.getItem();
                    tradeDirectionGuiItem = new GuiItem(tradeDirectionItem, event -> {
                        event.setCancelled(true);
                        tradeShop.getSettings().setTradeDirection(TradeShopSettings.TradeDirection.ITEM1_TO_ITEM2);
                        TileState state = ((TileState)containerBlock.getState());
                        state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "tradedirection"),
                                PersistentDataType.STRING, tradeShop.getSettings().getTradeDirection().toString());
                        state.update();
                        TradeShopHologram.getHologram(containerBlock.getLocation(), player).updateTradeDirection();
                        showGUI(player, containerBlock, isAdmin);
                    });
            }

            InventoryUtils.addItemIfEnoughSlots(gui, tradeDirectionContainerGuiItem.getSlot(), tradeDirectionGuiItem);
        }

        //How it saves the owners? like this man
        //if empty: "none"
        //otherwise: UUID@UUID@UUID@UUID
        if (!isAdmin) {
            boolean hastAtLeastOneAdmin = !dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING).equals("none");
            if (container.hasItem("shop-admins")) {
                ContainerGuiItem signItem = container.getItem("shop-admins").setName(lm.shopAdminsButtonTitle()).setLore(signLoreChooser(hastAtLeastOneAdmin, dataContainer, lm));
                GuiItem signItemg = new GuiItem(signItem.getItem(), event -> {
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
                InventoryUtils.addItemIfEnoughSlots(gui, signItem.getSlot(), signItemg);
            }

            if (container.hasItem("customize-hologram-message")) {

                int maxMessages = Utils.getMaxPermission(player, "ecs.shops.hologram.messages.limit.");
                int currentMessages = TradeShopSettings.getAllCustomMessages(TradeShopContainer.getTradeShop(containerBlock.getLocation()).getOwnerID().toString()).size();

                ContainerGuiItem customMessageItemStack = container.getItem("customize-hologram-message")
                        .setName(lm.hologramMessageButtonTitle());
                boolean hasNoMaxShopLimit = maxMessages == -1;
                boolean hasPermissionLimit = Config.permission_hologram_message_limit;
                // if hasNoMaxShopLimit and hasPermissionLimit are true, value should be false.
                // if hasNoMaxShopLimit is true and hasPermissionLimit is false, value should be false.
                // if hasNoMaxShopLimit is false and hasPermissionLimit is true, value should be true.
                // if hasNoMaxShopLimit is false and hasPermissionLimit is false, value should be false.
                boolean value = hasNoMaxShopLimit && hasPermissionLimit ? false : hasNoMaxShopLimit ? false : hasPermissionLimit;
                customMessageItemStack.setLore(((maxMessages - currentMessages > 0 && hasPermissionLimit) || !hasPermissionLimit) || hasNoMaxShopLimit  ?
                        lm.hologramMessageButtonLore(player, TradeShopContainer.getTradeShop(containerBlock.getLocation()).getOwnerID().toString()) :
                        lm.hologramMessageButtonLoreMaxReached(player));
                GuiItem customMessageItem = new GuiItem(customMessageItemStack.getItem(), event -> {
                    event.setCancelled(true);
                    if (maxMessages - currentMessages > 0 || !value) {
                        if (event.isRightClick()) {
                            CustomMessageManageGUI customMessageManageGUI = new CustomMessageManageGUI();
                            customMessageManageGUI.showGUI(player, containerBlock, isAdmin);
                        } else {
                            openCustomMessageEditor(player, containerBlock.getLocation());
                        }
                    } else {
                        CustomMessageManageGUI customMessageManageGUI = new CustomMessageManageGUI();
                        customMessageManageGUI.showGUI(player, containerBlock, isAdmin);
                    }
                });
                if (Config.settings_hologram_message_enabled) {
                    InventoryUtils.addItemIfEnoughSlots(gui, customMessageItemStack.getSlot(), customMessageItem);
                }
            }
        }


        String rotation = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        if (container.hasItem("hologram-rotation-" + rotation) || container.hasItem("hologram-rotation-all")) {
            ContainerGuiItem rotationItemStack;
            if (container.hasItem("hologram-rotation-" + rotation)) {
                rotationItemStack = container.getItem("hologram-rotation-" + rotation)
                        .setName(lm.rotateHologramButtonTitle()).setLore(lm.rotateHologramButtonLore(rotation));
            } else {
                rotationItemStack = container.getItem("hologram-rotation-all")
                        .setName(lm.rotateHologramButtonTitle()).setLore(lm.rotateHologramButtonLore(rotation));
            }

            GuiItem rotationItem = new GuiItem(rotationItemStack.getItem(), event -> {
                event.setCancelled(true);
                String next_rotation;
                if (event.getClick() == ClickType.LEFT) {
                    next_rotation = Utils.getPreviousRotation(TradeShopContainer.getTradeShopSettings(containerBlock.getLocation()).getRotation());
                } else if (event.getClick() == ClickType.RIGHT) {
                    next_rotation = Utils.getNextRotation(TradeShopContainer.getTradeShopSettings(containerBlock.getLocation()).getRotation());
                } else {
                    return;
                }
                TileState state = ((TileState)containerBlock.getState());
                state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, next_rotation);
                state.update();
                player.sendMessage(lm.rotateHologramInChat(next_rotation));
                showGUI(player, containerBlock, isAdmin);
                TradeShopContainer.getTradeShopSettings(containerBlock.getLocation()).setRotation(next_rotation);
                if (Config.holodistancing) {
                    TradeShopHologram.getHologram(containerBlock.getLocation(), player).updatePosition();
                }
            });

            if (Config.holo_rotation) {
                InventoryUtils.addItemIfEnoughSlots(gui, rotationItemStack.getSlot(), rotationItem);
            }
        }

//TODO change to change-item1-amount and change-item2-amount
        if (container.hasItem("change-price")) {

            ContainerGuiItem priceItemStack = container.getItem("change-price")
                    .setName(lm.changePricesButtonTitle()).setLore(lm.changePricesButtonLore());
            GuiItem priceItem = new GuiItem(priceItemStack.getItem(), event -> {
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
                                    if (NumberUtils.isInteger(strings[0])) {
                                        int amount = Integer.parseInt(strings[0]);
                                        if (amount < 0) {
                                            player.sendMessage(lm.negativePrice());
                                            return false;
                                        }
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(),
                                                () -> {
                                                    // If these checks complete successfully continue.
                                                    if (changePrice(containerBlock.getState(), false, amount, player, containerBlock)) {
//                                                        TradeShopContainer.changePrice(containerBlock.getState(), amount, false);
//                                                        TradeShopHologram.getHologram(containerBlock.getLocation(), player).updateSellPrice();
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
                                    if (NumberUtils.isInteger(strings[0])) {
                                        int amount = Integer.parseInt(strings[0]);
                                        if (amount < 0) {
                                            player.sendMessage(lm.negativePrice());
                                            return false;
                                        }
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(),
                                                () -> {
                                                    // If these checks complete successfully continue.
                                                    if (changePrice(containerBlock.getState(), true, amount, player, containerBlock)) {
//                                                        TradeShopContainer.changePrice(containerBlock.getState(), amount, true);
//                                                        TradeShopHologram.getHologram(containerBlock.getLocation(), player).updateBuyPrice();
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
                }
            });
            InventoryUtils.addItemIfEnoughSlots(gui, priceItemStack.getSlot(), priceItem);
        }

        if (container.hasItem("back")) {
            ContainerGuiItem backItemStack = container.getItem("back")
                    .setName(lm.backToShopGuiButton());
            GuiItem backItem = new GuiItem(backItemStack.getItem(), event -> {
               event.setCancelled(true);
                String owneruuid = dataContainer.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

                if (isAdminShop) {
                    ServerTradeShopGUI serverTradeShopGUI = new ServerTradeShopGUI();
                    serverTradeShopGUI.showGUI(player, dataContainer, containerBlock);
                    return;
                }

                if (player.getUniqueId().toString().equalsIgnoreCase(owneruuid) || isAdmin) {
                    if (player.hasPermission("ecs.admin")) {
                        AdminTradeShopGUI adminTradeShopGUI = new AdminTradeShopGUI();
                        adminTradeShopGUI.showGUI(player, dataContainer, containerBlock);
                        return;
                    }
                    //owner show special gui
                    OwnerTradeShopGUI ownerTradeShopGUI = new OwnerTradeShopGUI();
                    ownerTradeShopGUI.showGUI(player, dataContainer, containerBlock, isAdmin);


                } else {

                    //the only scenario that remains is when that person has an admin permission
                    //just in case, i check again for player's permission
                    if (player.hasPermission("ecs.admin")) {
                        AdminTradeShopGUI adminTradeShopGUI = new AdminTradeShopGUI();
                        adminTradeShopGUI.showGUI(player, dataContainer, containerBlock);
                    } else {
                        player.closeInventory();
                    }
                }

            });
            InventoryUtils.addItemIfEnoughSlots(gui, backItemStack.getSlot(), backItem);
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
//         EzShop shop = ShopContainer.getShop(blockState.getLocation());
//         // Enforce buy > sell.
//         if (Config.settings_buy_greater_than_sell) {
//             if (
//                     (isBuy && shop.getSellPrice() > price && price != 0) ||
//                             (!isBuy && price > shop.getBuyPrice() && shop.getBuyPrice() != 0)
//             ) {
//                 player.sendMessage(lm.buyGreaterThanSellRequired());
//                 return false;
//             }
//         }
//         // Revert from disabling buy sell.
//         if (Config.settings_zero_equals_disabled && isBuy && shop.getBuyPrice() == 0 && price != 0) {
//             TileState state = ((TileState)containerBlock.getState());
//             state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
//             state.update();
//             player.sendMessage(lm.disableBuyingOffInChat());
//             ShopContainer.getShopSettings(containerBlock.getLocation()).setDbuy(false);
//             ShopHologram.getHologram(containerBlock.getState().getLocation(), player).updateDbuy();
//         }
//         if (Config.settings_zero_equals_disabled && !isBuy && shop.getSellPrice() == 0 && price != 0) {
//             TileState state = ((TileState)containerBlock.getState());
//             state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
//             state.update();
//             player.sendMessage(lm.disableSellingOffInChat());
//             ShopContainer.getShopSettings(containerBlock.getLocation()).setDsell(false);
//             ShopHologram.getHologram(containerBlock.getState().getLocation(), player).updateDsell();
//         }
//         // Disable buy/sell
//         if (price == 0 && Config.settings_zero_equals_disabled) {
//             if (isBuy && shop.getBuyPrice() != 0) {
//                 TileState state = ((TileState)containerBlock.getState());
//                 state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 1);
//                 state.update();
//                 player.sendMessage(lm.disableBuyingOffInChat());
//                 ShopContainer.getShopSettings(containerBlock.getLocation()).setDbuy(true);
//                 ShopHologram.getHologram(containerBlock.getState().getLocation(), player).updateBuyPrice();
//             }
//             if (!isBuy && shop.getSellPrice() != 0) {
//                 TileState state = ((TileState)containerBlock.getState());
//                 state.getPersistentDataContainer().set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 1);
//                 state.update();
//                 player.sendMessage(lm.disableSellingOffInChat());
//                 ShopContainer.getShopSettings(containerBlock.getLocation()).setDsell(true);
//                 ShopHologram.getHologram(containerBlock.getState().getLocation(), player).updateSellPrice();
//             }
//         }
         return true;
     }

     public static void openCustomMessageEditor(Player player, Location location) {
         player.closeInventory();
         player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
         SignMenuFactory signMenuFactory = new SignMenuFactory(EzChestShop.getPlugin());
         SignMenuFactory.Menu menu = signMenuFactory.newMenu(lm.hologramMessageSingGUI(player, location))
                 .reopenIfFail(false).response((thatplayer, strings) -> {
                     try {
                         // Run checks here if allowed
                         Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(),
                                 () -> {
                                     int lines = Config.settings_hologram_message_line_count_default;
                                     if (Config.permission_hologram_message_line_count) {
                                         int maxShops = Utils.getMaxPermission(player, "ecs.shops.hologram.messages.lines.");
                                         maxShops = maxShops == -1 ? 4 : maxShops == 0 ? 1 : maxShops;
                                         lines = maxShops;
                                     }
                                     List<String> messages = Arrays.asList(strings).subList(0, lines).stream()
                                             .filter(s -> !s.trim().equals("")).collect(Collectors.toList());
                                     // Save data!
                                     TradeShopContainer.getTradeShopSettings(location)
                                             .setCustomMessages(messages);
                                     TradeShopHologram.getHologram(location, player).setCustomHologramMessage(messages);

                                 });

                     } catch (Exception e) {
                         return false;
                     }
                     return true;
                 });
         menu.open(player);
     }





}
