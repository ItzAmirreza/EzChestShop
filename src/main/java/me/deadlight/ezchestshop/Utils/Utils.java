package me.deadlight.ezchestshop.Utils;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.LanguageManager;
import me.deadlight.ezchestshop.Listeners.PlayerLookingAtChestShop;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Utils {

    public static List<Object> onlinePackets = new ArrayList<>();

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

        //reloading config.yml

        EzChestShop.getPlugin().reloadConfig();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();
        PlayerLookingAtChestShop.showholo = config.getBoolean("show-holograms");
        PlayerLookingAtChestShop.firstLine = config.getString("hologram-first-line");
        PlayerLookingAtChestShop.secondLine = config.getString("hologram-second-line");
        PlayerLookingAtChestShop.holodelay = config.getInt("hologram-disappearance-delay");

    }

    public static void reloadLanguages() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
        EzChestShop.setLanguages(fc);
        MainCommands.updateLM(new LanguageManager());
    }

    //this one checks for the config.yml ima make one for language.yml
    public static void checkForConfigYMLupdate() throws IOException {

        //update 1.2.4 config.yml
        boolean result = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml")).isInt("hologram-disappearance-delay");
        if (!result) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
            fc.set("hologram-disappearance-delay", 10);
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));

            Utils.reloadConfigs();
        }
    }

    public static void checkForLanguagesYMLupdate() throws IOException {

        //update 1.2.8 Languages
        boolean result = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml")).isString("commandmsg-negativeprice");
        if (!result) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));
            //new values that were added in update 1.2.8
            fc.set("commandmsg-negativeprice", "&cNegative price? but you have to use positive price...");
            fc.set("commandmsg-notenoughargs", "&cYou haven't provided enough arguments! \\n &cCorrect usage: /ecs create (Buy price) (Sell price)");
            fc.set("commandmsg-consolenotallowed", "&cYou are not allowed to execute any command from console.");
            fc.set("commandmsg-help", "&7- &c/ecs create (Buy Price) (Sell Price) &7| Create a chest shop by looking at a chest and having the item that you want to sell in your hand. \n &7- &c/ecs remove &7| Removes the chest shop that you are looking at \n &7Eazy right? :)");
            fc.set("commandmsg-alreadyashop", "&cThis chest is already a shop!");
            fc.set("commandmsg-shopcreated", "&aYou have successfully created a chest shop!");
            fc.set("commandmsg-holdsomething", "&cPlease hold something in your main hand!");
            fc.set("commandmsg-notallowdtocreate", "&cYou are not allowed to create/remove a chest shop in this location.");
            fc.set("commandmsg-notchest", "&cThe block that you are looking at is not supported type of chest/is not a chest.");
            fc.set("commandmsg-lookatchest", "&cPlease look at a chest.");
            fc.set("commandmsg-csremoved", "&eThis chest shop successfully removed.");
            fc.set("commandmsg-notowner", "&aYou are not the owner of this chest shop!");
            fc.set("commandmsg-notachestorcs", "&cThe block that you are looking at is not a chest/or this is not a chest shop.");
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "languages.yml"));


            Utils.reloadLanguages();
            EzChestShop.getPlugin().logConsole("&c[&eEzChestShop&c]&r &bNew languages.yml generated...");
        }
    }

    public static HashMap<String, Block> blockBreakMap = new HashMap<>();

    public static LanguageManager lm;

    public static List<UUID> getAdminsList(PersistentDataContainer data) {

        String adminsString = data.get(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING);
        //UUID@UUID@UUID
        if (adminsString.equalsIgnoreCase("none")) {
            return new ArrayList<>();
        } else {
            String[] stringUUIDS = adminsString.split("@");
            List<UUID> finalList = new ArrayList<>();
            for (String uuidInString : stringUUIDS) {
                finalList.add(UUID.fromString(uuidInString));
            }
            return finalList;
        }
    }

    //







}
