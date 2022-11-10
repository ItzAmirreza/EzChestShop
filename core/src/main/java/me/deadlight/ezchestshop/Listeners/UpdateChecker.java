package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.GUI.ContainerGui;
import me.deadlight.ezchestshop.Data.GUI.ContainerGuiItem;
import me.deadlight.ezchestshop.Data.GUI.GuiData;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpdateChecker implements Listener{

    LanguageManager lm = new LanguageManager();

    private String url = "https://api.spigotmc.org/legacy/update.php?resource=";
    private String id = "90411";

    private static String newVersion = EzChestShop.getPlugin().getDescription().getVersion();

    private static boolean isSpigotUpdateAvailable;
    public static boolean isSpigotUpdateAvailable() {
        return isSpigotUpdateAvailable;
    }

    private static boolean isGuiUpdateAvailable;
    public static boolean isGuiUpdateAvailable() {
        return isGuiUpdateAvailable;
    }

    private static HashMap<GuiData.GuiType, List<List<String>>> overlappingItems = new HashMap<>();
    private static HashMap<GuiData.GuiType, Integer> requiredOverflowRows = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            if (Config.notify_updates && isSpigotUpdateAvailable) {
                Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                    event.getPlayer().spigot().sendMessage(lm.updateNotification(EzChestShop.getPlugin().getDescription().getVersion(), newVersion));
                }, 10l);
            }
            if (isGuiUpdateAvailable) {
                if (Config.notify_overflowing_gui_items && !requiredOverflowRows.isEmpty()) {
                    Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                        event.getPlayer().spigot().sendMessage(lm.overflowingGuiItemsNotification(requiredOverflowRows));
                    }, 10l);
                }
                if (Config.notify_overlapping_gui_items && !overlappingItems.isEmpty()) {
                    Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                        event.getPlayer().spigot().sendMessage(lm.overlappingItemsNotification(overlappingItems));
                    }, 10l);
                }
            }
        }


    }

    public void check() {
        isSpigotUpdateAvailable = checkUpdate();
        checkGuiUpdate();
    }

    public void resetGuiCheck() {
        overlappingItems.clear();
        requiredOverflowRows.clear();
        isGuiUpdateAvailable = false;
        checkGuiUpdate();
    }

    public static int getGuiOverflow(GuiData.GuiType guiType) {
        if (requiredOverflowRows.containsKey(guiType)) {
            return requiredOverflowRows.get(guiType);
        } else {
            return -1;
        }
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
            try {
                // No version update required if the local version is newer. Doesn't work if you have a version like 1.2.3.4 or 1.2.
                if (Integer.parseInt(localVersion.split(".")[0]) > Integer.parseInt(remoteVersion.split(".")[0])) {
                    return false;
                }
                if (Integer.parseInt(localVersion.split(".")[0]) == Integer.parseInt(remoteVersion.split(".")[0]) &&
                        Integer.parseInt(localVersion.split(".")[1]) > Integer.parseInt(remoteVersion.split(".")[1])) {
                    return false;
                }
                if (Integer.parseInt(localVersion.split(".")[0]) == Integer.parseInt(remoteVersion.split(".")[0]) &&
                        Integer.parseInt(localVersion.split(".")[1]) == Integer.parseInt(remoteVersion.split(".")[1]) &&
                        Integer.parseInt(localVersion.split(".")[2]) > Integer.parseInt(remoteVersion.split(".")[2])) {
                    return false;
                }
            }
            catch (NumberFormatException e) {}
            catch (IndexOutOfBoundsException e) {}

            if(!localVersion.equalsIgnoreCase(remoteVersion))
                return true;

        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private void checkGuiUpdate() {
        // Check all GUIs (GuiData.getViaType()) for updates. See if items are outside bounds and if items that should not overlap suddenly overlap.
        // If any of these are true, return true.

        for (GuiData.GuiType type : GuiData.GuiType.values()) {
            ContainerGui container = GuiData.getViaType(type);
            if (container == null) continue;

            // Save the item keys for the items and check them against a list of item keys that may be combined.
            // If the overlapping items are not in this list, remember them!

            HashMap<Integer, List<String>> items = new HashMap<>();

            container.getItemKeys().forEach(key -> {
                ContainerGuiItem item = container.getItem(key);
                if (item == null) return;
                if (item.getRow() > container.getRows()) {
                    Integer row = requiredOverflowRows.get(type);
                    if (row == null) {
                        row = item.getRow();
                    } else {
                        row = Math.max(row, item.getRow());
                    }
                    requiredOverflowRows.put(type, row);
                    isGuiUpdateAvailable = true;
                }
                // Save the items to the hashmap
                if (items.containsKey(item.getSlot())) {
                    List<String> list = new ArrayList<>(items.get(item.getSlot()));
                    list.add(key);
                    items.put(item.getSlot(), list);
                } else {
                    items.put(item.getSlot(), Arrays.asList(key));
                }

            });

            // Filter out all items that don't overlap
            items.entrySet().removeIf(entry -> entry.getValue().size() == 1);

            // Check if the overlapping items are allowed to overlap
            // get the List from GuiData (so I don't forget to update it) and
            // loop over the list and try to match the entry to the list with containsAll!
            if (GuiData.getAllowedDefaultOverlappingItems(type) != null) {
                List<List<String>> overlapping = items.entrySet().stream().filter(entry -> {
                    List<String> list = entry.getValue();

                    // Make sure that the list doesn't contain values that are not allowed to overlap
                    // The list may contain all allowed values, some allowed values, no allowed values or a mix of allowed and not allowed values
                    if (list.isEmpty()) return false;

                    // Check and see if a value is not contained in the allowed lists at all by converting the list of lists to a list of strings
                    List<String> containing = GuiData.getAllowedDefaultOverlappingItems(type).stream().flatMap(List::stream).collect(Collectors.toList());
                    List<String> subtractList = new ArrayList<>(list);
                    subtractList.removeAll(containing);
                    if (subtractList.size() > 0) {
                        return true;
                    }

                    // Check if the items is in the overlap allowlist.
                    AtomicBoolean returnValue = new AtomicBoolean(false);
                    GuiData.getAllowedDefaultOverlappingItems(type).forEach(allowedList -> {
                        List<String> subtractList2 = new ArrayList<>(list);
                        // Only run this check if there is at least a connection between the two lists
                        if (!Collections.disjoint(allowedList, subtractList2)) {
                            subtractList2.removeAll(allowedList);
                            if (!subtractList2.isEmpty()) {
                                returnValue.set(true);
                            }
                        }
                    });
                    return returnValue.get();


                }).map(entry -> entry.getValue()).collect(Collectors.toList());

                if (overlapping.size() > 0) {
                    overlappingItems.put(type, overlapping);
                    isGuiUpdateAvailable = true;
                }
            }

        }

    }

}