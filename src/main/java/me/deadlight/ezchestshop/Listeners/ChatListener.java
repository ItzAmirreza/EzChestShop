package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.SettingsGUI;
import me.deadlight.ezchestshop.Utils.ChatWaitObject;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {

    public static HashMap<UUID, ChatWaitObject> chatmap = new HashMap<>();

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        if (chatmap.containsKey(player.getUniqueId())) {
            //waiting for the answer
            event.setCancelled(true);

            if (event.getMessage().equalsIgnoreCase(player.getName())) {
                chatmap.remove(player.getUniqueId());
                player.sendMessage(Utils.color("&cYou can't add or remove yourself in the admins list!"));

                return;
            }

            String type = chatmap.get(player.getUniqueId()).type;
            Chest rightChest = chatmap.get(player.getUniqueId()).rightChest;
            chatmap.put(player.getUniqueId(), new ChatWaitObject(event.getMessage(), type, rightChest));
            SettingsGUI guiInstance = new SettingsGUI();

            if (checkIfPlayerExists(event.getMessage())) {

                if (type.equalsIgnoreCase("add")) {
                    chatmap.remove(player.getUniqueId());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            addThePlayer(event.getMessage(), rightChest, player);
                            guiInstance.ShowGUI(player, rightChest, false);
                        }
                    }, 0);
                } else {
                    chatmap.remove(player.getUniqueId());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EzChestShop.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            removeThePlayer(event.getMessage(), rightChest, player);
                            guiInstance.ShowGUI(player, rightChest, false);
                        }
                    }, 0);
                }


            } else {
                player.sendMessage(Utils.color("&cThis player doesn't exist or haven't played here before."));
                chatmap.remove(player.getUniqueId());
            }

        }

    }


    public boolean checkIfPlayerExists(String name) {
        Player player = Bukkit.getPlayer(name);

        if (player != null) {

            if (player.isOnline()) {
                return true;
            } else {
                OfflinePlayer thaPlayer = Bukkit.getOfflinePlayer(name);
                if (thaPlayer.hasPlayedBefore()) {
                    return true;
                } else {
                    return false;
                }
            }

        } else {
            OfflinePlayer thaPlayer = Bukkit.getOfflinePlayer(name);
            return thaPlayer.hasPlayedBefore();
        }



    }



    public void addThePlayer(String answer, Chest rightChest, Player player) {

        UUID answerUUID = Bukkit.getOfflinePlayer(answer).getUniqueId();
        List<UUID> admins = getAdminsList(rightChest.getPersistentDataContainer());
        if (!admins.contains(answerUUID)) {

            admins.add(answerUUID);
            String adminsString = convertListUUIDtoString(admins);
            PersistentDataContainer data = rightChest.getPersistentDataContainer();
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, adminsString);
            rightChest.update();
            player.sendMessage(Utils.color("&e" + answer + " &asuccessfully added to the admins list."));

        } else {
            player.sendMessage(Utils.color("&cThis player is already in the admins list!"));
        }
    }


    public void removeThePlayer(String answer, Chest rightChest, Player player) {
        UUID answerUUID = Bukkit.getOfflinePlayer(answer).getUniqueId();
        List<UUID> admins = getAdminsList(rightChest.getPersistentDataContainer());
        if (admins.contains(answerUUID)) {

            admins.remove(answerUUID);
            if (admins.size() == 0) {
                PersistentDataContainer data = rightChest.getPersistentDataContainer();
                data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
                rightChest.update();
                player.sendMessage(Utils.color("&e" + answer + " &asuccessfully removed from the admins list."));
                return;
            }
            String adminsString = convertListUUIDtoString(admins);
            PersistentDataContainer data = rightChest.getPersistentDataContainer();
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, adminsString);
            rightChest.update();
            player.sendMessage(Utils.color("&e" + answer + " &asuccessfully removed from the admins list."));

        } else {
            player.sendMessage(Utils.color("&cThis player is not in the admins list!"));
        }
    }


    public List<UUID> getAdminsList(PersistentDataContainer data) {

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




    public String convertListUUIDtoString(List<UUID> uuidList) {
        StringBuilder finalString = new StringBuilder();
        boolean first = false;
        for (UUID uuid : uuidList) {
            if (first) {

                finalString.append("@").append(uuid.toString());

            } else {
                first = true;
                finalString = new StringBuilder(uuid.toString());
            }
        }
        return finalString.toString();
    }






}

