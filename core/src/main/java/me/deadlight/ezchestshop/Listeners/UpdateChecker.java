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
        if (Config.notify_updates && event.getPlayer().isOp() && isSpigotUpdateAvailable) {
            Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                event.getPlayer().spigot().sendMessage(lm.updateNotification(EzChestShop.getPlugin().getDescription().getVersion(), newVersion));
            }, 10l);
        }
        // TODO remove the next 3 lines for production!
        overlappingItems.clear();
        requiredOverflowRows.clear();
        checkGuiUpdate();
        if (isGuiUpdateAvailable && event.getPlayer().isOp()) {
            Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                event.getPlayer().sendMessage("Your GUI is outdated! Please update it!\n" +
                        "You have unusual overlapping items in" + overlappingItems.keySet().stream().map(Enum::name).collect(Collectors.joining(", "))
                        + "! And you need more rows for overflow protection in " + requiredOverflowRows.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")) + "!");
            }, 10l);
            EzChestShop.logDebug("Overlapping items: " + overlappingItems.entrySet().stream().map(entry -> entry.getKey().name() + ": " + entry.getValue().stream().map(List::toString).collect(Collectors.joining(", "))).collect(Collectors.joining("!\n")));
        }


    }

    public void check() {
        isSpigotUpdateAvailable = checkUpdate();
        checkGuiUpdate();
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