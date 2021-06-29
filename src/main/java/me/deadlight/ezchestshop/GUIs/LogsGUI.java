package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.Utils.LogType;
import me.deadlight.ezchestshop.Utils.TransactionLogObject;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogsGUI {
    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public LogsGUI() {

    }

    public void showGUI(Player player, PersistentDataContainer data, Chest chest, LogType type, boolean isAdmin) {
        String guititle;
        if (type == LogType.TRANSACTION) {
            guititle = "&aTransaction logs";
        } else {
            guititle = "&aAction logs";
        }

        Gui gui = new Gui(6, Utils.color(guititle));

        ItemStack door = new ItemStack(Material.DARK_OAK_DOOR, 1);
        ItemMeta doorMeta = door.getItemMeta();
        doorMeta.setDisplayName(Utils.color("&eBack to settings"));
        door.setItemMeta(doorMeta);
        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });
        GuiItem doorItem = new GuiItem(door, event -> {
           event.setCancelled(true);
           OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
           ownerShopGUI.showGUI(player, data, chest, chest, isAdmin);
        });


        if (type == LogType.TRANSACTION) {

            //trans
            List<TransactionLogObject> transLogs = Utils.getListOfTransactions(data);
            Collections.reverse(transLogs);

            for (int count = 1; count <= transLogs.size(); count++) {

                ItemStack paperItem = new ItemStack(Material.PAPER);
                ItemMeta paperMeta = paperItem.getItemMeta();
                //set kardane etelaat
                TransactionLogObject thelog = transLogs.get(count - 1);
                if (thelog.type.equalsIgnoreCase("buy")) {
                    paperMeta.setDisplayName(Utils.color("&aBuy | " + thelog.pname));
                    paperMeta.setLore(generateLore(thelog));
                } else {
                    paperMeta.setDisplayName(Utils.color("&cSell | " + thelog.pname));
                    paperMeta.setLore(generateLore(thelog));
                }
                paperItem.setItemMeta(paperMeta);
                GuiItem paper = new GuiItem(paperItem, event -> {
                    event.setCancelled(true);
                });
                gui.setItem(count, paper);

            }

        }

        //until slot 53 there is space
        gui.setItem(0, doorItem);
        gui.open(player);

    }


    private List<String> generateLore(TransactionLogObject log) {
        List<String> lore = new ArrayList<>();

        LocalDateTime time = LocalDateTime.parse(log.time, formatter);

        if (log.type.equalsIgnoreCase("buy")) {
            lore.add(Utils.color("&7Total Price: " + log.price));
            lore.add(Utils.color("&7Count: " + log.count));
            lore.add(Utils.color("&7Transaction Type: &aBought from you"));
            lore.add(Utils.color(Utils.color(getTimeString(time))));
            return lore;
        } else {
            lore.add(Utils.color("&7Total Price: " + log.price));
            lore.add(Utils.color("&7Count: " + log.count));
            lore.add(Utils.color("&7Transaction Type: &cSold to you"));
            lore.add(Utils.color(Utils.color(getTimeString(time))));
            return lore;
        }

    }


    private String getTimeString(LocalDateTime time) {

        String finalString;

        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        long hours = ChronoUnit.HOURS.between(time, LocalDateTime.now());

        if (minutes < 1) {
            finalString = "&eless than a minute ago";
        } else {

            if (hours < 1) {
                finalString = "&e" + minutes + " minute(s) ago";
            } else {

                if (hours < 24) {
                    finalString = "&e" + hours + " hour(s) ago";
                } else {
                    int days = (int) hours/24;
                    finalString = "&e" + days + " days ago";
                }

            }

        }


        return finalString;

    }


}
