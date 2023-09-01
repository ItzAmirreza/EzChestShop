package me.deadlight.ezchestshop.utils;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.Config;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryUtils {


    /**
     * Check if the given inventory has enough space for the given amount of the given item
     * @param player the Player whose inventory to check
     * @param amount the amount of the item
     * @param item the item
     * @return true if the inventory has enough space, false otherwise
     */
    public static boolean hasEnoughSpace(Player player, int amount, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : player.getInventory().getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (ItemUtils.isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }

        return emptySlots >= amount;
    }


    /**
     * Get how many items of the given type can be stored in the given inventory
     * @param storageContents the inventory to check
     * @param item the item to check
     * @return the amount of items that can be stored
     */
    public static int containerEmptyCount(ItemStack[] storageContents, ItemStack item) {

        if (storageContents == null) {
            return Integer.MAX_VALUE;
        }

        int emptySlots = 0;
        for (ItemStack content : storageContents) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (ItemUtils.isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }
        return emptySlots;
    }

    /**
     * Get how many items of the given type are in the given inventory
     * @param itemStacks the inventory to check
     * @param mainItem the item to check
     * @return the amount of items that are in the inventory
     */
    public static int howManyOfItemExists(ItemStack[] itemStacks, ItemStack mainItem) {

        if (itemStacks == null) {
            return Integer.MAX_VALUE;
        }

        int amount = 0;
        for (ItemStack item : itemStacks) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (ItemUtils.isSimilar(item, mainItem)) {
                amount += item.getAmount();
            }

        }
        return amount;

    }

    /**
     * Check if the given inventory has enough space for the given amount of the given item
     * @param container the inventory to check
     * @param amount the amount of the item
     * @param item the item
     * @return true if the inventory has enough space, false otherwise
     */
    public static boolean containerHasEnoughSpace(Inventory container, int amount, ItemStack item) {
        int emptySlots = 0;
        for (ItemStack content : container.getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                emptySlots += item.getMaxStackSize();
            } else {
                if (ItemUtils.isSimilar(content, item) && !(content.getAmount() >= content.getMaxStackSize())) {

                    int remaining = content.getMaxStackSize() - content.getAmount();
                    emptySlots += remaining;

                }
            }
        }

        return emptySlots >= amount;
    }

    public static List<String> calculatePossibleAmount(OfflinePlayer offlineCustomer, OfflinePlayer offlineSeller,
                                                       ItemStack[] playerInventory, ItemStack[] storageInventory, double eachBuyPrice, double eachSellPrice,
                                                       ItemStack itemStack) {

        List<String> results = new ArrayList<>();

        String buyCount = calculateBuyPossibleAmount(offlineCustomer, playerInventory, storageInventory, eachBuyPrice,
                itemStack);
        String sellCount = calculateSellPossibleAmount(offlineSeller, playerInventory, storageInventory, eachSellPrice,
                itemStack);

        results.add(buyCount);
        results.add(sellCount);
        return results;
    }

    public static String calculateBuyPossibleAmount(OfflinePlayer offlinePlayer, ItemStack[] playerInventory,
                                                    ItemStack[] storageInventory, double eachBuyPrice, ItemStack itemStack) {
        // I was going to run this in async but maybe later...
        int possibleCount = 0;
        double buyerBalance =
                Config.useXP ? XPEconomy.getXP(offlinePlayer) : EzChestShop.getEconomy().getBalance(offlinePlayer);
        return calculateForBuySellPossibleAmount(storageInventory, itemStack, playerInventory, possibleCount, eachBuyPrice, buyerBalance);
    }

    public static String calculateSellPossibleAmount(OfflinePlayer offlinePlayer, ItemStack[] playerInventory,
                                                     ItemStack[] storageInventory, double eachSellPrice, ItemStack itemStack) {

        int possibleCount = 0;
        double buyerBalance;
        if (offlinePlayer == null) {
            buyerBalance = Double.MAX_VALUE;
        } else {
            if (offlinePlayer.hasPlayedBefore()) {
                buyerBalance = Config.useXP ?
                        XPEconomy.getXP(offlinePlayer) :
                        EzChestShop.getEconomy().getBalance(offlinePlayer);
            } else {
                buyerBalance = 0;
            }
        }
        return calculateForBuySellPossibleAmount(storageInventory, itemStack, playerInventory, possibleCount, eachSellPrice, buyerBalance);
    }

    /**
     * Code repetition removal, used by calculateBuyPossibleAmount and calculateSellPossibleAmount.
     */
    private static String calculateForBuySellPossibleAmount(ItemStack[] storageInventory, ItemStack itemStack,
                                                            ItemStack[] playerInventory, int possibleCount,
                                                            double eachPrice, double buyerBalance) {
        int emptyCount = containerEmptyCount(storageInventory, itemStack);
        int howManyExists = howManyOfItemExists(playerInventory, itemStack);

        for (int num = 0; num < emptyCount; num++) {
            if (possibleCount + 1 > howManyExists) {
                break;
            }
            possibleCount += 1;
        }

        int result = 0;
        for (int num = 0; num < possibleCount; num++) {
            result += 1;
            if ((num + 1) * eachPrice > buyerBalance) {
                return String.valueOf(num);
            }
        }

        return String.valueOf(result);
    }

    /**
     * Calculate the possible trade amount
     * @param offlineCustomer The customer
     * @param playerInventory The customer's inventory
     * @param storageInventory The shops inventory
     * @param item1 The first item of the trade shop
     * @param item2 The second item of the trade shop
     * @return A list containing [0] = item1 possible amount, [1] = item2 possible amount
     */
    public static List<String> calculatePossibleTradeAmount(OfflinePlayer offlineCustomer, ItemStack[] playerInventory,
                                                            ItemStack[] storageInventory, ItemStack item1, ItemStack item2) {
        List<String> results = new ArrayList<>();

        int possibleItem1 = 0;
        int possibleItem2 = 0;

        int emptyCountPlayerItem1 = containerEmptyCount(playerInventory, item1);
        int emptyCountPlayerItem2 = containerEmptyCount(playerInventory, item2);

        int emptyCountStorageItem1 = containerEmptyCount(storageInventory, item1);
        int emptyCountStorageItem2 = containerEmptyCount(storageInventory, item2);

        int howManyExistsPlayerItem1 = howManyOfItemExists(playerInventory, item1);
        int howManyExistsPlayerItem2 = howManyOfItemExists(playerInventory, item2);

        int howManyExistsStorageItem1 = howManyOfItemExists(storageInventory, item1);
        int howManyExistsStorageItem2 = howManyOfItemExists(storageInventory, item2);

        int item1Amount = item1.getAmount();
        int item2Amount = item2.getAmount();

        // item1 -> item2
        for (int num = 0; num < emptyCountPlayerItem2; num++) {
            if (possibleItem1 + 1 > howManyExistsPlayerItem1) {
                break;
            }
            possibleItem1 += 1;
        }

        // item2 -> item1
        for (int num = 0; num < emptyCountPlayerItem1; num++) {
            if (possibleItem2 + 1 > howManyExistsPlayerItem2) {
                break;
            }
            possibleItem2 += 1;
        }

        // TODO this probably doesn't work yet, revisit it when you're more in mood for that logic/math mess

        results.add(String.valueOf(possibleItem1));
        results.add(String.valueOf(possibleItem2));

        return results;
    }

    /**
     * Check if the given inventory has enough space for the given amount of the given item
     * @param inventory the inventory to check
     * @param amount the amount of the item
     * @param item the item
     * @return true if the inventory has enough space, false otherwise
     */
    public static boolean containsAtLeast(Inventory inventory, ItemStack item, int amount) {
        if (item.getType() == Material.FIREWORK_ROCKET) {
            int count = 0;
            for (ItemStack content : inventory.getStorageContents()) {
                if (content == null || content.getType() == Material.AIR) {
                    continue;
                }
                if (ItemUtils.isSimilar(content, item)) {
                    count += content.getAmount();
                }
            }
            return count >= amount;
        } else {
            return inventory.containsAtLeast(item, amount);
        }
    }

    /**
    Removes the given ItemStacks from the inventory.

    It will try to remove 'as much as possible' from the types and amounts you give as arguments.

    The returned HashMap contains what it couldn't remove, where the key is the index of the parameter, and the value is the ItemStack at that index of the varargs parameter. If all the given ItemStacks are removed, it will return an empty HashMap.

    It is known that in some implementations this method will also set the inputted argument amount to the number of that item not removed from slots.
     */
    public static HashMap<Integer, ItemStack> removeItem(@NotNull Inventory inventory, @NotNull ItemStack... stacks) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            if (stack.getType() == Material.FIREWORK_ROCKET) {
                int amount = stack.getAmount();
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack item = inventory.getItem(slot);
                    if (item == null || item.getType() == Material.AIR) {
                        continue;
                    }
                    if (ItemUtils.isSimilar(item, stack)) {
                        int newAmount = item.getAmount() - amount;
                        if (newAmount > 0) {
                            item.setAmount(newAmount);
                            amount = 0;
                        } else {
                            amount = -newAmount;
                            inventory.setItem(slot, null);
                        }
                    }
                    if (amount <= 0) {
                        break;
                    }
                }
                if (amount > 0) {
                    stack.setAmount(amount);
                    leftover.put(i, stack);
                }
            } else {
                inventory.removeItem(stack);
            }
        }
        return leftover;
    }

    /**
     * Add the given GUI item to the given GUI if there are enough slots
     * @param gui the GUI to add the item to
     * @param slot the slot to add the item to
     * @param item the item to add
     */
    public static void addItemIfEnoughSlots (Gui gui, int slot, GuiItem item){
        if ((gui.getRows() * 9) > slot) {
            gui.setItem(slot, item);
        }
    }

    /**
     * Add the given GUI item to the given GUI if there are enough slots
     * @param gui the GUI to add the item to
     * @param slot the slot to add the item to
     * @param item the item to add
     */
    public static void addItemIfEnoughSlots (PaginatedGui gui, int slot, GuiItem item){
        if ((gui.getRows() * 9) > slot) {
            gui.setItem(slot, item);
        }
    }
}
