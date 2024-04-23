package me.deadlight.ezchestshop.listeners;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.data.LanguageManager;
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
                EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                    event.getPlayer().spigot().sendMessage(lm.updateNotification(EzChestShop.getPlugin().getDescription().getVersion(), newVersion));
                }, 10l);
            }
            if (isGuiUpdateAvailable) {
                if (Config.notify_overflowing_gui_items && !requiredOverflowRows.isEmpty()) {
                    EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                        event.getPlayer().spigot().sendMessage(lm.overflowingGuiItemsNotification(requiredOverflowRows));
                    }, 10l);
                }
                if (Config.notify_overlapping_gui_items && !overlappingItems.isEmpty()) {
                    EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
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

    /**
     * Checks if there is an update available on spigot
     * @return true if there is an update available, false if not
     */
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
            if (versionCompare(localVersion, remoteVersion) < 0)
                // localVersion is smaller (older) than remoteVersion
                return true;
            else
                // localVersion is greater (newer) than or equal to the remoteVersion
                return false;

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Compares two version strings
     * Source: https://www.geeksforgeeks.org/compare-two-version-numbers/
     * @param v1 version 1
     * @param v2 version 2
     * @return 1 if v1 is greater than v2, -1 if v1 is smaller than v2, 0 if v1 is equal to v2
     */
    static int versionCompare(String v1, String v2)
    {
        // vnum stores each numeric part of version
        int vnum1 = 0, vnum2 = 0;

        // loop until both String are processed
        for (int i = 0, j = 0; (i < v1.length()
                || j < v2.length());) {
            // Storing numeric part of
            // version 1 in vnum1
            while (i < v1.length()
                    && v1.charAt(i) != '.') {
                vnum1 = vnum1 * 10
                        + (v1.charAt(i) - '0');
                i++;
            }

            // storing numeric part
            // of version 2 in vnum2
            while (j < v2.length()
                    && v2.charAt(j) != '.') {
                vnum2 = vnum2 * 10
                        + (v2.charAt(j) - '0');
                j++;
            }

            if (vnum1 > vnum2)
                return 1;
            if (vnum2 > vnum1)
                return -1;

            // if equal, reset variables and
            // go for next numeric part
            vnum1 = vnum2 = 0;
            i++;
            j++;
        }
        return 0;
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