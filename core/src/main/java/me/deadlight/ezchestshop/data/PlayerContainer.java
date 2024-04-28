package me.deadlight.ezchestshop.data;


import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.Pair;
import me.deadlight.ezchestshop.utils.SignMenuFactory;
import me.deadlight.ezchestshop.utils.objects.CheckProfitEntry;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerContainer {

    private static HashMap<UUID, PlayerContainer> playerContainerMap = new HashMap<>();
    private static HashMap<Location, HashMap<Gui, List<OfflinePlayer>>> shopOpenGuiPlayerList = new HashMap();
    private static HashMap<Location, HashMap<PaginatedGui, List<OfflinePlayer>>> shopOpenPaginatedGuiPlayerList = new HashMap();
    private static HashMap<Location, HashMap<SignMenuFactory.Menu, List<OfflinePlayer>>> shopOpenSignMenuPlayerList = new HashMap();
    private static HashMap<Location, HashMap<Inventory, List<OfflinePlayer>>> shopOpenInventoryPlayerList = new HashMap();

    private UUID uuid;
    private OfflinePlayer offlinePlayer;
    private String suuid;

    private HashMap<String, CheckProfitEntry> checkProfits = null;

    public PlayerContainer(OfflinePlayer offlinePlayer) {
        this.uuid = offlinePlayer.getUniqueId();
        this.suuid = this.uuid.toString();
        this.offlinePlayer = offlinePlayer;
    }

    public static PlayerContainer get(OfflinePlayer offlinePlayer) {
        UUID uuid = offlinePlayer.getUniqueId();
        if (playerContainerMap.containsKey(uuid)) {
            return playerContainerMap.get(uuid);
        } else {
            PlayerContainer pc = new PlayerContainer(offlinePlayer);
            playerContainerMap.put(uuid, pc);
            return pc;
        }
    }

    /*
         ▄▀▀ █▄█ ▄▀▄ █▀▄
         ▄██ █ █ ▀▄▀ █▀
     */
    // ShopProfits
    public HashMap<String, CheckProfitEntry> getProfits() {
        if (checkProfits == null) {
            checkProfits = new HashMap<>();
            DatabaseManager db = EzChestShop.getPlugin().getDatabase();
            String checkProfitsList = db.getString("uuid", suuid, "checkprofits", "playerdata");
            if (checkProfitsList == null || checkProfitsList.equalsIgnoreCase("") || checkProfitsList.equalsIgnoreCase("NULL")) {
                checkProfits = new HashMap<>();
                return checkProfits;
            }
            for (String entry : checkProfitsList.split(CheckProfitEntry.itemSpacer)) {
                CheckProfitEntry profEntry = new CheckProfitEntry(entry);
                checkProfits.put(profEntry.getId(), profEntry);
            }
            return checkProfits;
        } else {
            return checkProfits;
        }
    }

    public static void closeGUI(Location location) {
        if (shopOpenGuiPlayerList.containsKey(location)) {
        shopOpenGuiPlayerList.get(location).forEach((gui, playerList) -> {
                if (!playerList.isEmpty()) {
                    for (OfflinePlayer player : playerList) {
                        if (player.isOnline() && gui != null) {
                            gui.close(player.getPlayer());
                        }
                    }
                }
        });
        }
        if (shopOpenPaginatedGuiPlayerList.containsKey(location)) {
            shopOpenPaginatedGuiPlayerList.get(location).forEach((gui, playerList) -> {
                if (!playerList.isEmpty()) {
                    for (OfflinePlayer player : playerList) {
                        if (player.isOnline() && gui != null) {
                            gui.close(player.getPlayer());
                        }
                    }
                }
            });
        }
        if (shopOpenSignMenuPlayerList.containsKey(location)) {
            shopOpenSignMenuPlayerList.get(location).forEach((menu, playerList) -> {
                if (!playerList.isEmpty()) {
                    for (OfflinePlayer player : playerList) {
                        if (player.isOnline() && menu != null) {
                            menu.close(player.getPlayer());
                        }
                    }
                }
            });
        }
    }

    public void openGUI(Gui gui, Location location) {
        shopOpenGuiPlayerList.putIfAbsent(location, new HashMap<>());
        shopOpenGuiPlayerList.get(location).putIfAbsent(gui, new ArrayList<>());
        shopOpenGuiPlayerList.get(location).get(gui).add(offlinePlayer);
    }

    public void openGUI(PaginatedGui gui, Location location) {
        shopOpenPaginatedGuiPlayerList.putIfAbsent(location, new HashMap<>());
        shopOpenPaginatedGuiPlayerList.get(location).putIfAbsent(gui, new ArrayList<>());
        shopOpenPaginatedGuiPlayerList.get(location).get(gui).add(offlinePlayer);
    }

    public void openSignMenu(SignMenuFactory.Menu menu, Location location) {
        shopOpenSignMenuPlayerList.putIfAbsent(location, new HashMap<>());
        shopOpenSignMenuPlayerList.get(location).putIfAbsent(menu, new ArrayList<>());
        shopOpenSignMenuPlayerList.get(location).get(menu).add(offlinePlayer);
    }

    public static void closeInventory(Location location) {
        if (!shopOpenInventoryPlayerList.containsKey(location)) {
            return;
        }
        shopOpenInventoryPlayerList.get(location).forEach((inventory, playerList) -> {
            if (!playerList.isEmpty()) {
                for (OfflinePlayer player : playerList) {
                    if (player.isOnline() && inventory != null && inventory.getViewers().contains(player.getPlayer())) {
                        player.getPlayer().closeInventory();
                    }
                }
            }
        });
    }

    public void openInventory(Inventory inventory, Location location) {
        shopOpenInventoryPlayerList.putIfAbsent(location, new HashMap<>());
        shopOpenInventoryPlayerList.get(location).putIfAbsent(inventory, new ArrayList<>());
        shopOpenInventoryPlayerList.get(location).get(inventory).add(offlinePlayer);
    }

    public void updateProfits(String id, ItemStack item, Integer buyAmount, Double buyPrice, Double buyUnitPrice, Integer sellAmount,
                              Double sellPrice, Double sellUnitPrice) {
        if (checkProfits == null) {
            checkProfits = getProfits();
        }
        if (!checkProfits.containsKey(id)) {
            checkProfits.put(id, new CheckProfitEntry(id, item, buyAmount, buyPrice, buyUnitPrice, sellAmount, sellPrice, sellUnitPrice));
        } else {
            CheckProfitEntry entry = checkProfits.get(id);
            entry.setBuyAmount(entry.getBuyAmount() + buyAmount);
            entry.setBuyPrice(entry.getBuyPrice() + buyPrice);
            entry.setSellAmount(entry.getSellAmount() + sellAmount);
            entry.setSellPrice(entry.getSellPrice() + sellPrice);
            checkProfits.put(id, entry);
        }
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        String profit_string = checkProfits.entrySet().stream().map(x -> x.getValue().toString())
                .collect(Collectors.joining(CheckProfitEntry.itemSpacer));
        if (profit_string == null)
            db.setString("uuid", suuid, "checkprofits", "playerdata", "NULL");
        else
            db.setString("uuid", suuid, "checkprofits", "playerdata", profit_string);
    }

    public void clearProfits() {
        DatabaseManager db = EzChestShop.getPlugin().getDatabase();
        checkProfits.clear();
        db.setString("uuid", suuid, "checkprofits", "playerdata", "NULL");
    }

}
