package me.deadlight.ezchestshop;
import me.deadlight.ezchestshop.Listeners.PlayerLookingAtChestShop;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

public class Utils {

    public static String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static void storeItem(ItemStack item, PersistentDataContainer data) throws IOException {

        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);

            os.writeObject(item);

            os.flush();
            byte[] rawData = io.toByteArray();

            String encodedData = Base64.getEncoder().encodeToString(rawData);

            data.set(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING, encodedData);
            os.close();

        } catch (IOException ex) {
            System.out.println(ex);
        }


    }

    public static ItemStack getItem(String encodedItem) {

        byte[] rawData = Base64.getDecoder().decode(encodedItem);

        try {

            ByteArrayInputStream io = new ByteArrayInputStream(rawData);
            BukkitObjectInputStream in = new BukkitObjectInputStream(io);

            ItemStack thatItem = (ItemStack) in.readObject();

            in.close();

            return thatItem;

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
            return null;
        }

    }


    public static void reloadConfigs() {

        EzChestShop.getPlugin().reloadConfig();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();
        PlayerLookingAtChestShop.showholo = config.getBoolean("show-holograms");
        PlayerLookingAtChestShop.firstLine = config.getString("hologram-first-line");
        PlayerLookingAtChestShop.secondLine = config.getString("hologram-second-line");
        PlayerLookingAtChestShop.holodelay = config.getInt("hologram-disappearance-delay");

    }

    public static void checkForConfigYMLupdate() throws IOException {

        //update 1.2.4 Languages
        boolean result = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).isInt("hologram-disappearance-delay");
        if (!result) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            fc.set("hologram-disappearance-delay", 10);
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));

            EzChestShop.getPlugin().reloadConfig();
            Utils.reloadConfigs();
        }
    }

    public static HashMap<String, Block> blockBreakMap = new HashMap<>();
    public static HashMap<String, Block> blockBreakMap2 = new HashMap<>();
    //







}
