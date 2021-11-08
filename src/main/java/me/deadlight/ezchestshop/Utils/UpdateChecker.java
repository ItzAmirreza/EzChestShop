import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateChecker iplements Listener{
    
    private String url = "https://api.spigotmc.org/legacy/update.php?resource=";
    private String id = "90411";

    private boolean isAvailable;

    public UpdateChecker() {

    }

    public boolean isAvailable() {
        return isAvailable;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        if(event.getPlayer().isOp())
            if(isAvailable)
                event.getPlayer().sendMessage("Update available message");
    }

    public void check() {
        isAvailable = checkUpdate();
    }

    private boolean checkUpdate() {
        Log.info("Check for updates...");
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

            if(!localVersion.equalsIgnoreCase(remoteVersion))
                return true;

        } catch (IOException e) {
            return false;
        }
        return false;
    }

}