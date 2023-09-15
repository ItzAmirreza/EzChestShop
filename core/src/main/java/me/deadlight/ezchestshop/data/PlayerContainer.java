package me.deadlight.ezchestshop.data;


import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.objects.CheckProfitEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerContainer {

    private static HashMap<UUID, PlayerContainer> playerContainerMap = new HashMap<>();

    private UUID uuid;
    private String suuid;

    private HashMap<String, CheckProfitEntry> checkProfits = null;

    public PlayerContainer(OfflinePlayer offlinePlayer) {
        this.uuid = offlinePlayer.getUniqueId();
        this.suuid = this.uuid.toString();
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
