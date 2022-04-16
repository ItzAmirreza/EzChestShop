package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeavingListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Utils.versionUtils.ejectConnection(event.getPlayer());
        if (ChatListener.chatmap.containsKey(event.getPlayer().getUniqueId())) {
            ChatListener.chatmap.remove(event.getPlayer().getUniqueId());
        }
    }


}
