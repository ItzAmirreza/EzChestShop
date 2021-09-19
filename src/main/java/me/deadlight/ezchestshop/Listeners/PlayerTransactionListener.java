package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Data.PlayerContainer;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.Events.PlayerTransactEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Objects.TransactionLogObject;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
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
        logProfits(event);
        if (((TileState)event.getContainerBlock().getState()).getPersistentDataContainer().get(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER) == 1) {

            List<UUID> getters = event.getAdminsUUID();
            getters.add(event.getOwner().getUniqueId());

            if (event.isBuy()) {
                for (UUID adminUUID : getters) {
                    Player admin = Bukkit.getPlayer(adminUUID);
                    if (admin != null) {
                        if (admin.isOnline()) {
                            event.getOwner().getPlayer().sendMessage(Utils.colorify("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has bought &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                        }
                    }
                }
            } else {
                for (UUID adminUUID : getters) {
                    Player admin = Bukkit.getPlayer(adminUUID);
                    if (admin != null) {
                        if (admin.isOnline()) {
                            event.getOwner().getPlayer().sendMessage(Utils.colorify("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has sold &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
                        }
                    }
                }
            }






        }

    }

    private void log(PlayerTransactEvent event) {

        TileState state = ((TileState)event.getContainerBlock().getState());
        PersistentDataContainer data = state.getPersistentDataContainer();
        //player name@buy|sell@price@time@count#

        String ttype;
        if (event.isBuy()) {
            ttype = "buy";
        } else {
            ttype = "sell";
        }
        String formattedDateTime = event.getTime().format(formatter);

        List<TransactionLogObject> logObjectList = Utils.getListOfTransactions(event.getContainerBlock());
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
        //NEED TO CHECK FOR OFFICIAL RELEASE <---
        state.update();
        ShopContainer.getShopSettings(event.getContainerBlock().getLocation()).setTrans(finalString.toString());

    }


    private void logProfits(PlayerTransactEvent event) {
        Double price = event.getPrice();
        Integer count = event.getCount();
        // These next 4 are interesting:
        //Integer count = amount / defaultAmount; // How many times were items bought. Considers Stack buying.
        // Double single_price = price / count;
        String id = Utils.LocationtoString(event.getContainerBlock().getLocation());
        ItemStack item = event.getItem(); // Item shop sells
        PlayerContainer owner = PlayerContainer.get(event.getOwner());
        if (event.isBuy()) {
            if (event.isShareIncome()) {
                int admin_count = event.getAdminsUUID().size();
                for (UUID uuid : event.getAdminsUUID()) {
                    if (uuid.equals(event.getOwner().getUniqueId()))
                        continue;
                    PlayerContainer admin = PlayerContainer.get(Bukkit.getOfflinePlayer(uuid));
                    admin.updateProfits(id, item, count, price / (admin_count + 1), price / count, 0, 0.0, 0.0);
                }

                owner.updateProfits(id, item, count, price / (admin_count + 1), event.getBuyPrice(), 0, 0.0, event.getSellPrice());
            } else {
                owner.updateProfits(id, item, count, price, event.getBuyPrice(), 0, 0.0, event.getSellPrice());
            }
        } else {
            owner.updateProfits(id, item, 0, 0.0, event.getBuyPrice(), count, price, event.getSellPrice());
        }
            // ItemStack,BuyAmount,BuyPrice,SellAmount,SellPrice
    }


}
