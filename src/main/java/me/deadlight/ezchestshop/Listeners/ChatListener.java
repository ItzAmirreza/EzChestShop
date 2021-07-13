package me.deadlight.ezchestshop.Listeners;
import me.deadlight.ezchestshop.Commands.MainCommands;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.SettingsGUI;
import me.deadlight.ezchestshop.LanguageManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {

    public static HashMap<UUID, ChatWaitObject> chatmap = new HashMap<>();
    public static LanguageManager lm = new LanguageManager();
    public static void updateLM(LanguageManager languageManager) {
        ChatListener.lm = languageManager;
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        if (chatmap.containsKey(player.getUniqueId())) {
            //waiting for the answer
            event.setCancelled(true);
            ChatWaitObject waitObject = chatmap.get(player.getUniqueId());
            String owneruuid = waitObject.rightChest.getPersistentDataContainer().get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);
            if (event.getMessage().equalsIgnoreCase(player.getName())) {
                OfflinePlayer ofplayer = Bukkit.getOfflinePlayer(owneruuid);
                if (ofplayer.getName().equalsIgnoreCase(event.getPlayer().getName())) {
                    chatmap.remove(player.getUniqueId());
                    player.sendMessage(lm.selfAdmin());
                    return;
                }

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
                player.sendMessage(lm.noPlayer());
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
        List<UUID> admins = Utils.getAdminsList(rightChest.getPersistentDataContainer());
        if (!admins.contains(answerUUID)) {

            admins.add(answerUUID);
            String adminsString = convertListUUIDtoString(admins);
            PersistentDataContainer data = rightChest.getPersistentDataContainer();
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, adminsString);
            rightChest.update();
            player.sendMessage(lm.sucAdminAdded(answer));

        } else {
            player.sendMessage(lm.alreadyAdmin());
        }
    }


    public void removeThePlayer(String answer, Chest rightChest, Player player) {
        UUID answerUUID = Bukkit.getOfflinePlayer(answer).getUniqueId();
        List<UUID> admins = Utils.getAdminsList(rightChest.getPersistentDataContainer());
        if (admins.contains(answerUUID)) {

            admins.remove(answerUUID);
            if (admins.size() == 0) {
                PersistentDataContainer data = rightChest.getPersistentDataContainer();
                data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
                rightChest.update();
                player.sendMessage(lm.sucAdminRemoved(answer));
                return;
            }
            String adminsString = convertListUUIDtoString(admins);
            PersistentDataContainer data = rightChest.getPersistentDataContainer();
            data.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, adminsString);
            rightChest.update();
            player.sendMessage(lm.sucAdminRemoved(answer));

        } else {
            player.sendMessage(lm.notInAdminList());
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

