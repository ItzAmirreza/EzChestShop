package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTransactionListener implements Listener {

    @EventHandler
    public void onTransaction(PlayerTransactEvent event) {

        if (event.getOwner().isOnline()) {

            if (event.isBuy()) {
                event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has bought &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
            } else {
                event.getOwner().getPlayer().sendMessage(Utils.color("&7[&aECS&7] &b" + event.getCustomer().getName() + " &7has sold &e" + event.getCount() + "x &7of &d" + event.getItemName() + "&7 with the worth of &a" + event.getPrice() + "$"));
            }

        }
    }

}
