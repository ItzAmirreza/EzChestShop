package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Objects.TransactionLogObject;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class PlayerTransactionListener implements Listener {

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @EventHandler
    public void onTransaction(PlayerTransactEvent event) {
        log(event);
        if (event.getChest().getPersistentDataContainer().get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1) {

            List<UUID> getters = event.getAdminsUUID();
            getters.add(event.getOwner().getUniqueId());

            if (event.isBuy()) {
                for (UUID adminUUID : getters) {
                    Player admin = Bukkit.getPlayer(adminUUID);
                    if (admin != null) {
                        if (admin.isOnline()) {
                            event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has bought &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                        }
                    }
                }
            } else {
                for (UUID adminUUID : getters) {
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
        //player name@but|sell@price@time@count#

        String ttype;
        if (event.isBuy()) {
            ttype = "buy";
        } else {
            ttype = "sell";
        }
        String formattedDateTime = event.getTime().format(formatter);

        List<TransactionLogObject> logObjectList = Utils.getListOfTransactions(data);
        if (logObjectList.size() == 53) {
            logObjectList.remove(0);
        }
        logObjectList.add(new TransactionLogObject(ttype, event.getCustomer().getName(), String.valueOf(event.getPrice()), formattedDateTime, event.getCount()));
//        StringBuilder logString = new StringBuilder(event.getCustomer().getName());
//        logString.append("@").append(ttype).append("@").append(event.getPrice()).append("@").append(formattedDateTime) + count;
        //convert all into strings
        StringBuilder finalString = new StringBuilder();
        boolean first = false;
        for (TransactionLogObject log : logObjectList) {

            if (first) {
                finalString.append("#").append(log.pname).append("@").append(log.type).append("@").append(log.price).append("@").append(log.time).append("@").append(log.count);
            } else {
                first = true;
                finalString.append(log.pname).append("@").append(log.type).append("@").append(log.price).append("@").append(log.time).append("@").append(log.count);
            }
        }

        data.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, finalString.toString());
        event.getChest().update();

    }



}
