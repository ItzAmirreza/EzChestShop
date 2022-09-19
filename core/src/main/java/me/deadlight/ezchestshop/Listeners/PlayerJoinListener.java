package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.DatabaseManager;
import me.deadlight.ezchestshop.Data.SQLite.SQLite;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Utils.versionUtils.injectConnection(player);
        /**
         * Prepare the database player values.
         *
         * @param evt
         */
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        UUID uuid = event.getPlayer().getUniqueId();
        SQLite.playerTables.forEach(t -> {
            if (db.hasTable(t)) {
                if (!db.hasPlayer(t, uuid)) {
                    db.preparePlayerData(t, uuid.toString());
                }
            }
        });
    }

}
