package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.TransactionLogObject;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerTransactionListener implements Listener {

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @EventHandler
    public void onTransaction(PlayerTransactEvent event) {
        log(event);
        if (event.getOwner().isOnline()) {

            if (event.isBuy()) {
                event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has bought &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                for (UUID adminUUID : event.getAdminsUUID()) {
                    Player admin = Bukkit.getPlayer(adminUUID);
                    if (admin != null) {
                        if (admin.isOnline()) {
                            event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has bought &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                        }
                    }
                }
            } else {
                event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has sold &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                for (UUID adminUUID : event.getAdminsUUID()) {
                    Player admin = Bukkit.getPlayer(adminUUID);
                    if (admin != null) {
                        if (admin.isOnline()) {
                            event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has sold &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                        }
                    }
                }
            }

        }
    }

    private void log(PlayerTransactEvent event) {

        PersistentDataContainer data = event.getChest().getPersistentDataContainer();
        //player name@but|sell@price@time#

        String ttype;
        if (event.isBuy()) {
            ttype = "Buy";
        } else {
            ttype = "Sell";
        }
        String formattedDateTime = event.getTime().format(formatter);

        List<TransactionLogObject> logObjectList = getListOfTransactions(data);
        if (logObjectList.size() == 53) {
            logObjectList.remove(52);
        }
        logObjectList.add(new TransactionLogObject(ttype, event.getCustomer().getName(), String.valueOf(event.getPrice()), formattedDateTime));
//        StringBuilder logString = new StringBuilder(event.getCustomer().getName());
//        logString.append("@").append(ttype).append("@").append(event.getPrice()).append("@").append(formattedDateTime);
        //convert all into strings
        StringBuilder finalString = new StringBuilder();
        boolean first = false;
        for (TransactionLogObject log : logObjectList) {

            if (first) {
                finalString.append("#").append(log.pname).append("@").append(log.type).append("@").append(log.price).append("@").append(log.time);
            } else {
                first = true;
                finalString.append(log.pname).append("@").append(log.type).append("@").append(log.price).append("@").append(log.time);
            }
        }

        data.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, finalString.toString());
        event.getChest().update();

    }

    private List<TransactionLogObject> getListOfTransactions(PersistentDataContainer data) {
        String wholeString = data.get(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING);
        if (wholeString.equalsIgnoreCase("none")) {
            return new ArrayList<>();
        } else {
            List<TransactionLogObject> logObjectList = new ArrayList<>();
            String[] logs = wholeString.split("#");
            for (String log : logs) {
                String[] datas = log.split("@");
                String pname = datas[0];
                String type = datas[1];
                String price = datas[2];
                String time = datas[3];
                logObjectList.add(new TransactionLogObject(type, pname, price, time));

            }
            return logObjectList;

        }
    }

}
