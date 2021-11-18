package me.deadlight.ezchestshop.Utils;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.EzChestShop;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener{

    LanguageManager lm = new LanguageManager();

    private String url = "https://api.spigotmc.org/legacy/update.php?resource=";
    private String id = "90411";

    private static String newVersion = EzChestShop.getPlugin().getDescription().getVersion();

    private static boolean isAvailable;

    public UpdateChecker() {

    }

    public static boolean isAvailable() {
        return isAvailable;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (Config.notify_updates && event.getPlayer().isOp() && isAvailable)
            Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                event.getPlayer().spigot().sendMessage(lm.updateNotification(EzChestShop.getPlugin().getDescription().getVersion(), newVersion));
            }, 10l);
    }

    public void check() {
        isAvailable = checkUpdate();
    }

    private boolean checkUpdate() {
        try {
            String localVersion = EzChestShop.getPlugin().getDescription().getVersion();
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url + id).openConnection();
            connection.setRequestMethod("GET");
            String raw = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

            String remoteVersion;
            if(raw.contains("-")) {
                remoteVersion = raw.split("-")[0].trim();
            } else {
                remoteVersion = raw;
            }
            newVersion = remoteVersion;

            if(!localVersion.equalsIgnoreCase(remoteVersion))
                return true;

        } catch (IOException e) {
            return false;
        }
        return false;
    }

}