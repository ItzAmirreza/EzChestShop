package me.deadlight.ezchestshop.guis.shop;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.enums.LogType;
import me.deadlight.ezchestshop.guis.shop.SettingsGUI;
import me.deadlight.ezchestshop.utils.InventoryUtils;
import me.deadlight.ezchestshop.utils.StringUtils;
import me.deadlight.ezchestshop.utils.objects.TransactionLogObject;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class LogsGUI {
    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public LogsGUI() {

    }

    public void showGUI(Player player, PersistentDataContainer data, Block containerBlock, LogType type, boolean isAdmin) {

        if (true) {
            player.sendMessage(StringUtils.colorify("&cUnfortunately, this feature is not available anymore, but it will be available again soon!"));
            player.closeInventory();
            return;
        }


        LanguageManager lm = new LanguageManager();
        String guititle;
        if (type == LogType.TRANSACTION) {
            guititle = lm.transactionButtonTitle();
        } else {
            guititle = "&aAction logs";
        }

        ContainerGui container = GuiData.getLogs();

        Gui gui = new Gui(container.getRows(), StringUtils.colorify(guititle));
        gui.setDefaultClickAction(event -> event.setCancelled(true));

        ContainerGuiItem door = null;
        if (container.hasItem("back")) {
            door = container.getItem("back").setName(lm.backToSettingsButton());
            GuiItem doorItem = new GuiItem(door.getItem(), event -> {
               event.setCancelled(true);
               SettingsGUI settingsGUI = new SettingsGUI();
               settingsGUI.showGUI(player, containerBlock, isAdmin);
            });
            InventoryUtils.addItemIfEnoughSlots(gui, door.getSlot(), doorItem);
        }


        if (type == LogType.TRANSACTION) {

            //trans
            List<TransactionLogObject> transLogs = Utils.getListOfTransactions(containerBlock.getLocation());
            Collections.reverse(transLogs);

            int slot = 0;
            if (container.hasItem("transaction-item")) {
                for (int count = 1; count <= transLogs.size(); count++) {

                    ContainerGuiItem transactionItem = container.getItem("transaction-item");
                    if (door != null && slot == door.getSlot()) {
                        slot++;
                    }
                    //set kardane etelaat
                    TransactionLogObject thelog = transLogs.get(count - 1);
                    if (thelog.type.equalsIgnoreCase("buy")) {
                        transactionItem.setName(lm.transactionPaperTitleBuy(thelog.pname));
                        transactionItem.setLore(generateLore(thelog, lm));
                    } else {
                        transactionItem.setName(lm.transactionPaperTitleSell(thelog.pname));
                        transactionItem.setLore(generateLore(thelog, lm));
                    }
                    GuiItem paper = new GuiItem(transactionItem.getItem(), event -> {
                        event.setCancelled(true);
                    });
                    InventoryUtils.addItemIfEnoughSlots(gui, slot, paper);
                    slot++;
                }
            }

        }

        gui.open(player);

    }


    private List<String> generateLore(TransactionLogObject log, LanguageManager lm) {
        List<String> lore;

        LocalDateTime time = LocalDateTime.parse(log.time, formatter);

        if (log.type.equalsIgnoreCase("buy")) {
            lore = lm.transactionPaperLoreBuy(log.price, log.count, getTimeString(time, lm));
            return lore;
        } else {
           lore = lm.transactionPaperLoreSell(log.price, log.count, getTimeString(time, lm));
            return lore;
        }

    }


    private String getTimeString(LocalDateTime time, LanguageManager lm) {

        String finalString;

        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        long hours = ChronoUnit.HOURS.between(time, LocalDateTime.now());

        if (minutes < 1) {
            finalString = lm.lessthanminute();
        } else {

            if (hours < 1) {
                finalString = lm.minutesago(minutes);
            } else {

                if (hours < 24) {
                    finalString = lm.hoursago(hours);
                } else {
                    int days = (int) hours/24;
                    finalString = lm.daysago(days);
                }

            }

        }


        return finalString;

    }


}
