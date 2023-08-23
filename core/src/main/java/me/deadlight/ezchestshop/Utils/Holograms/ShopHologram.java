package me.deadlight.ezchestshop.Utils.Holograms;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class ShopHologram {

    private static LanguageManager lm = new LanguageManager();
    private static HashMap<UUID, HashMap<Location, ShopHologram>> playerShopHolograms = new HashMap<>();
    private static HashMap<Location, BlockBoundHologram> shopHolograms = new HashMap<>();

    private static HashMap<UUID, ShopHologram> hologramInspections = new HashMap<>();

    private Location location;
    private Player player;
    private BlockBoundHologram hologram;
    private EzShop shop;

    public ShopHologram(Location location, Player player) {
        // Save the location and player
        this.location = location;
        this.player = player;

        // Make sure the BockBoundHologram is created
        if (!shopHolograms.containsKey(location)) {
            // Get the shop and it's hologram structure
           shop = ShopContainer.getShop(location);
            List<String> structure = new ArrayList<>(shop.getSettings().isAdminshop() ?
                    Config.holostructure_admin : Config.holostructure);

            String itemName = Utils.getFinalItemName(shop.getShopItem());
            Inventory shopInventory = Utils.getBlockInventory(location.getBlock());
            int availableSlots = shopInventory.getSize();
            for (ItemStack item : shopInventory.getStorageContents()) {
                // if item is one of the below, then it is a slot that can be used, otherwise subtract from available slots.
                if (!(item == null || item.getType() == Material.AIR || item.isSimilar(shop.getShopItem()))) {
                    availableSlots--;
                }
            }
            List<String> possibleCounts = Utils.calculatePossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()),
                    Bukkit.getOfflinePlayer(
                            UUID.fromString(((TileState) location.getBlock().getState()).getPersistentDataContainer()
                                    .get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))),
                    player.getInventory().getStorageContents(),
                    Utils.getBlockInventory(location.getBlock()).getStorageContents(),
                    shop.getBuyPrice(), shop.getSellPrice(), shop.getShopItem());

            // Default placeholders
            HashMap<String, String> textReplacements = new HashMap<>();
            textReplacements.put("%item%", itemName);
            textReplacements.put("%buy%", shop.getBuyPrice() + "");
            textReplacements.put("%sell%", shop.getSellPrice() + "");
            textReplacements.put("%currency%", Config.currency);
            textReplacements.put("%owner%", Bukkit.getOfflinePlayer(shop.getOwnerID()).getName());
            textReplacements.put("%maxbuy%", possibleCounts.get(0));
            textReplacements.put("%maxsell%", possibleCounts.get(1));
            textReplacements.put("%maxStackSize%", shop.getShopItem().getMaxStackSize() + "");
            textReplacements.put("%stock%", Utils.howManyOfItemExists(shopInventory.getStorageContents(), shop.getShopItem()) + "");
            textReplacements.put("%capacity%", availableSlots * shop.getShopItem().getMaxStackSize() + "");
            // the amount of itemdata replacements is defined in the config and may wary
            int itemDataLines = structure.stream()
                    .filter(s -> s.startsWith("<itemdata") && !s.startsWith("<itemdataRest"))
                    .collect(Collectors.toList()).size();
            for (int i = 0; i < itemDataLines; i++) {
                textReplacements.put("<itemdata" + (i + 1) + "/>", "");
            }
            textReplacements.put("<itemdataRest/>", "");
            // visible if the shop does not contain at least 1 item.
            boolean visible = !Utils.containsAtLeast(shopInventory, shop.getShopItem(), 1);
            textReplacements.put("<emptyShopInfo/>", visible ? lm.emptyShopHologramInfo() : "");
            // the amount of custom message replacements is defined in the config and may wary
            int customLines = structure.stream()
                    .filter(s -> s.startsWith("<itemdata") && !s.startsWith("<itemdataRest"))
                    .collect(Collectors.toList()).size();
            for (int i = 0; i < customLines; i++) {
                if (shop.getSettings().getCustomMessages().size() > i) {
                    textReplacements.put("<custom" + (i + 1) + "/>", shop.getSettings().getCustomMessages().get(i));
                    EzChestShop.logDebug("Added custom message to hologram: " + shop.getSettings().getCustomMessages().get(i));
                } else {
                    textReplacements.put("<custom" + (i + 1) + "/>", "");
                }
            }

            HashMap<String, ItemStack> itemReplacements = new HashMap<>();
            itemReplacements.put("[item]", shop.getShopItem());

            HashMap<String,  Boolean> conditionalTags = new HashMap<>();
            // buy and sell are inverted because true for the Hologram means it is shown
            // and true for isDbuy/isDsell means it is disabled aka hidden
            conditionalTags.put("buy", !shop.getSettings().isDbuy());
            conditionalTags.put("sell", !shop.getSettings().isDsell());
            // true if both are visible aka false -> buy & sell is enabled
            conditionalTags.put("separator", !shop.getSettings().isDbuy() && !shop.getSettings().isDsell());

            // Also set the separator replacement text if both are disabled
            HashMap<String, String> conditionalTextReplacements = new HashMap<>();
            EzShop shop = ShopContainer.getShop(location);
            if (shop != null && shop.getSettings() != null && shop.getSettings().isDbuy() && shop.getSettings().isDsell()) {
                conditionalTextReplacements.put("separator", lm.disabledButtonTitle());
                conditionalTags.put("separator", true);
            }

            List<String> alwaysVisibleTextReplacements = new ArrayList<>();
            alwaysVisibleTextReplacements.add("<emptyShopInfo/>");
            for (int i = 0; i < customLines; i++) {
                alwaysVisibleTextReplacements.add("<custom" + (i + 1) + "/>");
            }


            // Create the hologram
            BlockBoundHologram shopHologram = new BlockBoundHologram(location,
                    BlockBoundHologram.HologramRotation.valueOf(shop.getSettings().getRotation().toUpperCase()),
                    structure, textReplacements, itemReplacements, conditionalTags, conditionalTextReplacements,
                    alwaysVisibleTextReplacements);
            EzChestShop.logDebug("Created new BlockBoundHologram");
            // Add the hologram to the map
            shopHolograms.put(location, shopHologram);
        }
        // else the hologram already exists, so just get it
        this.hologram = shopHolograms.get(location);

        // Save the just created hologram to the player's hologram map
        if (playerShopHolograms.containsKey(player.getUniqueId())) {
            playerShopHolograms.get(player.getUniqueId()).put(location, this);
        } else {
            HashMap<Location, ShopHologram> holograms = new HashMap<>();
            holograms.put(location, this);
            playerShopHolograms.put(player.getUniqueId(), holograms);
        }

        // Make sure inventory replacements are up to date for all players.
        ShopHologram.updateInventoryReplacements(location);
    }

    /**
     * Get the hologram of a shop, if it does not exist, create it.
     * @param location The location of the shop
     * @param player The player that is viewing the hologram
     * @return The hologram
     */
    public static ShopHologram getHologram(Location location, Player player) {
        if (!playerShopHolograms.containsKey(player.getUniqueId()) || !playerShopHolograms.get(player.getUniqueId()).containsKey(location)) {
            EzChestShop.logDebug("Creating new hologram for " + location.toString() + " for player " + player.getName());
            new ShopHologram(location, player);
        }
        return playerShopHolograms.get(player.getUniqueId()).get(location);
    }

    public static boolean hasHologram(Location location, Player player) {
        return playerShopHolograms.containsKey(player.getUniqueId()) &&
                playerShopHolograms.get(player.getUniqueId()).containsKey(location);
    }

    public static List<ShopHologram> getViewedHolograms(Player player) {
        if (playerShopHolograms.containsKey(player.getUniqueId())) {
            return new ArrayList<>(playerShopHolograms.get(player.getUniqueId()).values());
        }
        return new ArrayList<>();
    }

    public static List<EzShop> getShopsInRadius(Location location, int radius) {
        //TODO: Consider using a more efficient algorithm like a kd-tree
        return ShopContainer.getShops().stream()
                .filter(ezShop -> ezShop.getLocation() != null
                        && location.getWorld().equals(ezShop.getLocation().getWorld())
                        && location.distance(ezShop.getLocation()) < Config.holodistancing_distance + 5)
                .collect(Collectors.toList());
    }


    public static void reloadAll() {
        List<ShopHologram> shopHolograms = playerShopHolograms.values().stream().map(holo -> holo.values())
                .flatMap(Collection::stream).collect(Collectors.toList());
        shopHolograms.forEach(hologram -> {
            hologram.hologram.updateContents(Config.holostructure);
            hideForAll(hologram.getLocation());
        });
    }

    public static void hideAll(Player player) {
        if (playerShopHolograms.containsKey(player.getUniqueId())) {
//            playerShopHolograms.get(player).values().forEach(ShopHologram::hide);
            List<ShopHologram> shopHolograms = playerShopHolograms.get(player.getUniqueId()).values()
                    .stream().collect(Collectors.toList());

            shopHolograms.forEach(hologram -> hologram.hide());
            playerShopHolograms.remove(player.getUniqueId());
            if (ShopHologram.isPlayerInspectingShop(player)) {
                ShopHologram.getInspectedShopHologram(player).removeInspectedShop();
            }
        }
    }

    public static void hideForAll(Location location) {
        playerShopHolograms.values().forEach(holograms -> {
            if (holograms.containsKey(location)) {
//                holograms.get(location).removeInspectedShop();
                holograms.get(location).hide();
            }
        });
        ShopHologram.hologramInspections.values().stream().filter(holo -> holo.getLocation().equals(location)).collect(Collectors.toSet())
                .forEach(holo -> holo.removeInspectedShop());
    }

    public void hide() {
        hologram.getPlayerHologram(player).hide();
        playerShopHolograms.get(player.getUniqueId()).remove(location);
    }

    public boolean hasInspector() {
        return hologram.hasInspector(player);
    }

    public void showOnlyItem() {
        hologram.getPlayerHologram(player).showOnlyItem();
    }

    public void showTextAfterItem() {
        hologram.getPlayerHologram(player).showTextAfterItem();
    }

    public void showAlwaysVisibleText() {
        hologram.getPlayerHologram(player).showAlwaysVisibleText();
    }

    public void show() {
        hologram.getPlayerHologram(player).show();
    }


    public void setCustomHologramMessage(List<String> messages) {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            int lines = playerHolo.getBlockHologram().getContents().stream()
                    .filter(s -> s.startsWith("<custom"))
                    .collect(Collectors.toList()).size();
            // Update at most x lines
            for (int i = 0; i < lines; i++) {
                if (i >= messages.size()) {
                    playerHolo.updateTextReplacement("<custom" + (i + 1) + "/>", "", true, true);
                } else {
                    playerHolo.updateTextReplacement("<custom" + (i + 1) + "/>", messages.get(i), true, true);
                }
            }
        }
    }

    public void setItemDataVisible(boolean visible) {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        int lines = playerHolo.getBlockHologram().getContents().stream()
                .filter(s -> s.startsWith("<itemdata") && !s.startsWith("<itemdataRest"))
                .collect(Collectors.toList()).size();
        shop = ShopContainer.getShop(location);
        if (playerHolo != null) {
            for (int i = -1; i < lines; i++) {
                ItemStack item = shop.getShopItem();
                if (item.getType().name().contains("SHULKER_BOX") || item.getEnchantments().size() > 0 ||
                        (item.getType() == Material.ENCHANTED_BOOK
                                && ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().size() > 1)) {
                    if (i == -1) {
                        playerHolo.updateTextReplacement("<itemdataRest/>", visible ?
                                getHologramItemData(i, item, lines) : "", false, true);
                    } else {
                        playerHolo.updateTextReplacement("<itemdata" + (i + 1) + "/>", visible ?
                                getHologramItemData(i, item, lines) : "", false, true);
                    }
                }
            }
        }
    }

    public void updateEmptyShopInfo() {
        if (shop.getOwnerID().equals(player.getUniqueId()) ||
                shop.getSettings().getAdmins().contains(player.getUniqueId().toString())) {
            PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
            if (playerHolo != null) {
                Inventory shopInventory = Utils.getBlockInventory(location.getBlock());
                // visible if the shop does not contain at least 1 item.
                boolean visible = !Utils.containsAtLeast(shopInventory, shop.getShopItem(), 1);
                playerHolo.updateTextReplacement("<emptyShopInfo/>", visible ?
                        lm.emptyShopHologramInfo() : "", false, true);
            }
        }
    }

    public void updateBuyPrice() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            playerHolo.updateTextReplacement("%buy%", shop.getBuyPrice() + "", true, true);
        }
    }

    public void updateSellPrice() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            playerHolo.updateTextReplacement("%sell%", shop.getSellPrice() + "", true, true);
        }
    }

    public void updateDbuy() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            if (shop.getSettings().isDbuy() && shop.getSettings().isDsell()) {
                updateBuySellSeparator(playerHolo);
                playerHolo.updateConditionalTag("buy", !shop.getSettings().isDbuy(), true);
            } else {
                playerHolo.updateConditionalTag("buy", !shop.getSettings().isDbuy(), true);
                updateBuySellSeparator(playerHolo);
            }

        }
    }

    public void updateDsell() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            if (shop.getSettings().isDbuy() && shop.getSettings().isDsell()) {
                updateBuySellSeparator(playerHolo);
                playerHolo.updateConditionalTag("sell", !shop.getSettings().isDsell(), true);
            } else {
                playerHolo.updateConditionalTag("sell", !shop.getSettings().isDsell(), true);
                updateBuySellSeparator(playerHolo);
            }
        }
    }

    public void updateOwner() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            playerHolo.updateTextReplacement("%owner%", Bukkit.getOfflinePlayer(shop.getOwnerID()).getName(), true, true);
        }
    }

    public void updateStockAndCapacity() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            Inventory shopInventory = Utils.getBlockInventory(location.getBlock());
            int availableSlots = shopInventory.getSize();
            playerHolo.updateTextReplacement("%stock%", Utils.howManyOfItemExists(shopInventory.getStorageContents(),
                    shop.getShopItem()) + "", true, false);
            playerHolo.updateTextReplacement("%capacity%", availableSlots * shop.getShopItem().getMaxStackSize() + "",
                    true, false);
        }
    }

    public void updateMaxBuyAndSell() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            shop = ShopContainer.getShop(location);
            List<String> possibleCounts = Utils.calculatePossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()),
                    Bukkit.getOfflinePlayer(UUID.fromString(((TileState) shop.getLocation().getBlock().getState()).getPersistentDataContainer()
                            .get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))), player.getInventory().getStorageContents(),
                    Utils.getBlockInventory(shop.getLocation().getBlock()).getStorageContents(),
                    shop.getBuyPrice(), shop.getSellPrice(), shop.getShopItem());
            playerHolo.updateTextReplacement("%maxbuy%", possibleCounts.get(0) + "", false, false);
            playerHolo.updateTextReplacement("%maxsell%", possibleCounts.get(1) + "", false, false);
        }
    }

    public static void updateInventoryReplacements(Location location) {
        shopHolograms.get(location).getViewerHolograms().forEach(playerBlockBoundHologram -> {
            ShopHologram shopHolo = ShopHologram.getHologram(location, playerBlockBoundHologram.getPlayer());
            shopHolo.updateStockAndCapacity();
            shopHolo.updateEmptyShopInfo();
            shopHolo.updateMaxBuyAndSell();
        });
    }

    public void updatePosition() {
        PlayerBlockBoundHologram playerHolo = hologram.getPlayerHologram(player);
        if (playerHolo != null) {
            playerHolo.updatePosition();
        }
    }

    public void setAsInspectedShop() {
        if (!hologramInspections.containsKey(player.getUniqueId())) {
            hologramInspections.put(player.getUniqueId(), this);
        }
    }

    public void removeInspectedShop() {
        if (hologramInspections.containsKey(player.getUniqueId())) {
            hologramInspections.remove(player.getUniqueId());
            hologram.removeInspector(player);
        }
    }

    public static boolean isPlayerInspectingShop(Player player) {
        return hologramInspections.containsKey(player.getUniqueId());
    }

    public static ShopHologram getInspectedShopHologram(Player player) {
        return hologramInspections.get(player.getUniqueId());
    }


    public Location getLocation() {
        return location;
    }

    /*
     █████   █████          ████
    ░░███   ░░███          ░░███
     ░███    ░███   ██████  ░███  ████████   ██████  ████████   █████
     ░███████████  ███░░███ ░███ ░░███░░███ ███░░███░░███░░███ ███░░
     ░███░░░░░███ ░███████  ░███  ░███ ░███░███████  ░███ ░░░ ░░█████
     ░███    ░███ ░███░░░   ░███  ░███ ░███░███░░░   ░███      ░░░░███
     █████   █████░░██████  █████ ░███████ ░░██████  █████     ██████
    ░░░░░   ░░░░░  ░░░░░░  ░░░░░  ░███░░░   ░░░░░░  ░░░░░     ░░░░░░
                                  ░███
                                  █████
                                 ░░░░░
        */
    public static String getHologramItemData(int lineNum, ItemStack item, int lines) {
        String itemData = "";
        if (item.getType().name().contains("SHULKER_BOX")) {
            // Get the shulker box inventory
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                    Inventory inv = Bukkit.createInventory(null, 27, "Shulker Box");
                    inv.setContents(shulker.getInventory().getContents());

                    // Collect all the item counts into a map and sort them by count
                    Map<String, Integer> itemCounts = new HashMap<>();
                    for (ItemStack itemStack : inv.getContents()) {
                        if (itemStack != null) {
                            String itemName = Utils.getFinalItemName(itemStack);
                            if (itemCounts.containsKey(itemName)) {
                                itemCounts.put(itemName, itemCounts.get(itemName) + itemStack.getAmount());
                            } else {
                                itemCounts.put(itemName, itemStack.getAmount());
                            }
                        }
                    }
                    List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemCounts.entrySet());
                    sortedItems.sort(Map.Entry.comparingByValue());
                    Collections.reverse(sortedItems);

                    if (lineNum == -1 && sortedItems.size() - lines > 0) {
                        itemData = lm.shulkerboxItemHologramMore(sortedItems.size() - lines);
                    } else if (lineNum - 1 >= 0 && lineNum - 1 < sortedItems.size()) {
                        itemData = lm.shulkerboxItemHologram(sortedItems.get(lineNum - 1).getKey(), sortedItems.get(lineNum - 1).getValue());
                    } else {
                        itemData = "";
                    }
                }
            }
        } else if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta emeta = (EnchantmentStorageMeta) item.getItemMeta();
            // Get the enchantments
            Map<Enchantment, Integer> enchantments = emeta.getStoredEnchants();
            List<Map.Entry<Enchantment, Integer>> sortedEnchants = new ArrayList<>(enchantments.entrySet());
            sortedEnchants.sort(Map.Entry.comparingByValue());
            Collections.reverse(sortedEnchants);

            if (lineNum == -1 && sortedEnchants.size() - lines > 0) {
                itemData = lm.itemEnchantHologramMore((sortedEnchants.size() - lines));
            } else if (lineNum - 1 >= 0 && lineNum - 1 < sortedEnchants.size()) {
                itemData = lm.itemEnchantHologram(sortedEnchants.get(lineNum - 1).getKey(),
                        sortedEnchants.get(lineNum - 1).getValue());
            } else {
                itemData = "";
            }
        } else {
            // Get the enchantments
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            List<Map.Entry<Enchantment, Integer>> sortedEnchants = new ArrayList<>(enchantments.entrySet());
            sortedEnchants.sort(Map.Entry.comparingByValue());
            Collections.reverse(sortedEnchants);

            if (lineNum == -1 && sortedEnchants.size() - lines > 0) {
                itemData = lm.itemEnchantHologramMore((sortedEnchants.size() - lines));
            } else if (lineNum - 1 >= 0 && lineNum - 1 < sortedEnchants.size()) {
                itemData = lm.itemEnchantHologram(sortedEnchants.get(lineNum - 1).getKey(),
                        sortedEnchants.get(lineNum - 1).getValue());
            } else {
                itemData = "";
            }
        }
        return itemData;
    }

    private void updateBuySellSeparator(PlayerBlockBoundHologram playerHolo) {
        if (!shop.getSettings().isDbuy() && !shop.getSettings().isDsell()) {
            playerHolo.getBlockHologram().removeConditionalText("separator");
            playerHolo.updateConditionalTag("separator", true, true);
        } else if (shop.getSettings().isDbuy() && shop.getSettings().isDsell())  {
            playerHolo.getBlockHologram().setConditionalText("separator", lm.disabledButtonTitle());
            playerHolo.updateConditionalTag("separator", true, true);
        } else {
            playerHolo.getBlockHologram().removeConditionalText("separator");
            playerHolo.updateConditionalTag("separator", false, true);
        }
    }
}