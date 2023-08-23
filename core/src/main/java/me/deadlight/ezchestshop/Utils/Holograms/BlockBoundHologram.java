package me.deadlight.ezchestshop.Utils.Holograms;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * This class is used to create a hologram that is bound to a block.
 * It will manage and update the shop hologram for all the players that are viewing it.
 */
public class BlockBoundHologram {

    public enum HologramRotation {
        NORTH, SOUTH, EAST, WEST, UP, DOWN;
    }

    private static final LanguageManager lm = new LanguageManager();

    private Location location;
    private List<String> contents;

    // Viewers
    /** Contains all the players that are currently in range/viewing this shops hologram. */
    private HashMap<UUID, PlayerBlockBoundHologram> viewerHolograms = new HashMap<>();
    /** Contains all the players that are currently inspecting (looking at) this shops hologram. */
    private HashMap<UUID, PlayerBlockBoundHologram> inspectorHolograms = new HashMap<>();

    // Replacements
    protected HashMap<String, String> textDefaultReplacements;
    protected HashMap<String, ItemStack> itemDefaultReplacements;
    protected HashMap<String,  Boolean> conditionalDefaultTags;
    protected HashMap<String,  String> conditionalTextReplacements;
    protected List<String> alwaysVisibleTextReplacements;



    /*
 ███████████             █████     ████   ███              ██████████              █████
░░███░░░░░███           ░░███     ░░███  ░░░              ░░███░░░░███            ░░███
 ░███    ░███ █████ ████ ░███████  ░███  ████   ██████     ░███   ░░███  ██████   ███████    ██████
 ░██████████ ░░███ ░███  ░███░░███ ░███ ░░███  ███░░███    ░███    ░███ ░░░░░███ ░░░███░    ░░░░░███
 ░███░░░░░░   ░███ ░███  ░███ ░███ ░███  ░███ ░███ ░░░     ░███    ░███  ███████   ░███      ███████
 ░███         ░███ ░███  ░███ ░███ ░███  ░███ ░███  ███    ░███    ███  ███░░███   ░███ ███ ███░░███
 █████        ░░████████ ████████  █████ █████░░██████     ██████████  ░░████████  ░░█████ ░░████████
░░░░░          ░░░░░░░░ ░░░░░░░░  ░░░░░ ░░░░░  ░░░░░░     ░░░░░░░░░░    ░░░░░░░░    ░░░░░   ░░░░░░░░
      */

    public BlockBoundHologram(Location location, List<String> contents,
                              HashMap<String, String> textReplacements, HashMap<String, ItemStack> itemReplacements,
                              HashMap<String,  Boolean> conditionalTags, HashMap<String, String> conditionalTextReplacements,
                              List<String> alwaysVisibleTextReplacements) {
        this.location = location;
        this.contents = contents;
        this.textDefaultReplacements = textReplacements;
        this.itemDefaultReplacements = itemReplacements;
        this.conditionalDefaultTags = conditionalTags;
        this.conditionalTextReplacements = conditionalTextReplacements;
        this.alwaysVisibleTextReplacements = alwaysVisibleTextReplacements;
    }



    public Location getLocation() {
        return location;
    }

    public HologramRotation getRotation() {

        String rotation = ShopContainer.getShop(location).getSettings().getRotation();
        rotation = Config.holo_rotation ? rotation : Config.settings_defaults_rotation;

        // Make sure we're not getting a null value or something that isn't a valid rotation
        HologramRotation hologramRotation;
        try {
            hologramRotation = HologramRotation.valueOf(rotation.toUpperCase());
        } catch (Exception e) {
            hologramRotation = HologramRotation.UP;
        }

        return hologramRotation;
    }

    public List<String> getContents() {
        return contents;
    }

    /*
 ██████   ██████              █████  ███     ██████                 █████████   █████               █████
░░██████ ██████              ░░███  ░░░     ███░░███               ███░░░░░███ ░░███               ░░███
 ░███░█████░███   ██████   ███████  ████   ░███ ░░░  █████ ████   ░███    ░░░  ███████    ██████   ███████    ██████
 ░███░░███ ░███  ███░░███ ███░░███ ░░███  ███████   ░░███ ░███    ░░█████████ ░░░███░    ░░░░░███ ░░░███░    ███░░███
 ░███ ░░░  ░███ ░███ ░███░███ ░███  ░███ ░░░███░     ░███ ░███     ░░░░░░░░███  ░███      ███████   ░███    ░███████
 ░███      ░███ ░███ ░███░███ ░███  ░███   ░███      ░███ ░███     ███    ░███  ░███ ███ ███░░███   ░███ ███░███░░░
 █████     █████░░██████ ░░████████ █████  █████     ░░███████    ░░█████████   ░░█████ ░░████████  ░░█████ ░░██████
░░░░░     ░░░░░  ░░░░░░   ░░░░░░░░ ░░░░░  ░░░░░       ░░░░░███     ░░░░░░░░░     ░░░░░   ░░░░░░░░    ░░░░░   ░░░░░░
                                                      ███ ░███
                                                     ░░██████
                                                      ░░░░░░
     */

    public void hideForAll() {
        for (PlayerBlockBoundHologram playerHolo : viewerHolograms.values()) {
            playerHolo.hide();
        }
    }

    /**
     * Updates the hologram location
     */
    public void updateLocation(Location location) {
        this.location = location;
    }

    public void updateContents(List<String> contents) {
        this.contents = contents;
    }

    /**
     * Gets the PlayerBlockBoundHologram for the specified player. Creates a new one if it doesn't exist.
     * @param player The player to get the hologram for.
     * @return The PlayerBlockBoundHologram for the specified player.
     */
    public PlayerBlockBoundHologram getPlayerHologram(Player player) {
        if (!viewerHolograms.containsKey(player.getUniqueId())) {
            PlayerBlockBoundHologram playerHolo =
                    new PlayerBlockBoundHologram(player, this, textDefaultReplacements,
                            itemDefaultReplacements, conditionalDefaultTags);
            viewerHolograms.put(player.getUniqueId(), playerHolo);
        }
        return viewerHolograms.get(player.getUniqueId());
    }

    /**
     * Removes the viewer hologram for the specified player. This doesn't hide the hologram for the player.
     * @param player The player to remove the hologram for.
     */
    protected void removeViewer(Player player) {
        if (!viewerHolograms.containsKey(player.getUniqueId())) {
            return;
        }
        viewerHolograms.remove(player.getUniqueId());
    }

    /**
     * Checks if the specified player is inspecting (looking at) this hologram.
     * @param player The player to check.
     * @return True if the player is inspecting this hologram, otherwise false.
     */
    public boolean hasInspector(Player player) {
        return inspectorHolograms.containsKey(player.getUniqueId());
    }

    /**
     * Removes the inspector (looking at) status for the specified player. This doesn't hide the hologram for the player.
     * @param player The player to remove the hologram inspection status for.
     */
    protected void removeInspector(Player player) {
        if (!inspectorHolograms.containsKey(player.getUniqueId())) {
            return;
        }
        inspectorHolograms.remove(player.getUniqueId());
    }

    /**
     * Adds the specified player to the inspector (looking at) list.
     * @param player The player to add to the inspector list.
     * @param hologram The PlayerBlockBoundHologram that the player is inspecting.
     */
    protected void addInspector(Player player, PlayerBlockBoundHologram hologram) {
        if (inspectorHolograms.containsKey(player.getUniqueId())) {
            return;
        }
        inspectorHolograms.put(player.getUniqueId(), hologram);
    }

    /**
     * @return The PlayerBlockBoundHolograms visible for the specified player.
     */
    public List<PlayerBlockBoundHologram> getViewerHolograms() {
        return new ArrayList<>(viewerHolograms.values());
    }

    public String getConditionalText(String tag) {
        if (conditionalTextReplacements.containsKey(tag)) {
            return conditionalTextReplacements.get(tag);
        } else {
            return null;
        }
    }

    public void setConditionalText(String tag, String text) {
        conditionalTextReplacements.put(tag, text);
    }

    public void removeConditionalText(String tag) {
        conditionalTextReplacements.remove(tag);
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

    /**
     * Calculates the location for the hologram to be displayed at. This is based on the location & rotation of the shop,
     * and also checks if the shop is a double chest.
     * @param containerBlock The container block of the shop.
     * @return The location for the hologram to be displayed at.
     */
    public Location getHoloLoc(Block containerBlock) {
        Location holoLoc;
        Inventory inventory = Utils.getBlockInventory(containerBlock);

        // get the rotation via memory as it updates faster
        String rotation = ShopContainer.getShop(containerBlock.getLocation()).getSettings().getRotation();
        rotation = Config.holo_rotation ? rotation : Config.settings_defaults_rotation;

        // Add rotation checks
        switch (rotation) {
            case "north":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 0, -0.8));
                break;
            case "east":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0.8, 0, 0));
                break;
            case "south":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 0, 0.8));
                break;
            case "west":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(-0.8, 0, 0));
                break;
            case "down":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, -0.75, 0));
                break;
            default:
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 1, 0));
                break;
        }


        return holoLoc;
    }

    /**
     * Calculates a central location for the hologram to be displayed at. This is based on the location of the shop,
     * and also checks if the shop is a double chest. The direction vector is used to offset the location to fit a certain
     * shop rotation.
     * @param containerBlock The container block of the shop.
     * @param inventory The inventory of the shop (used to check if it's a double chest).
     * @param direction The direction vector to offset the location by, to fit a certain shop rotation.
     * @return The central location for the hologram to be displayed at.
     */
    private Location getCentralLocation(Block containerBlock, Inventory inventory, Vector direction) {
        Location holoLoc;
        if (inventory instanceof DoubleChestInventory) {
            // Do some "fancy" math to rotate the hologram around the double chest
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();
            // Get the center of the double chest
            holoLoc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D)
                    .add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5);
            if (direction.getY() == 0) {
                // if the direction is not up or down, we may need to offset the hologram a bit more.
                Location lloc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D);
                Location hloc = holoLoc.clone();
                double angle = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle = (-(angle / Math.PI) * 360.0d) / 2.0d + 180.0d;
                hloc = hloc.add(direction);
                double angle2 = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle2 = (-(angle2 / Math.PI) * 360.0d) / 2.0d + 180.0d;
                if (angle == angle2 || angle == angle2 - 180 || angle == angle2 + 180) {
                    // if the direction is on the long side of the chest, we need to offset it a bit more
                    holoLoc.add(direction.multiply(1.625));
                } else {
                    // otherwise, we can just offset it like normal
                    holoLoc.add(direction);
                }
            } else {
                holoLoc.add(direction);
            }

        } else {
            holoLoc = containerBlock.getLocation().clone().add(0.5D, 0, 0.5D).add(direction);
        }
        return holoLoc;
    }

    /**
     * Gets the location of the shop container, if the block is part of a double chest. If the block is not a
     * double chest or not even a shop, it will return the location of the block.
     * @param target The block to get the shop container location for.
     * @return The shop container location. If not found, the location of the block.
     */
    public static Location getShopChestLocation(Block target ) {
        Location loc = target.getLocation();
        Inventory inventory = ((Container) target.getState()).getInventory();
        if (inventory instanceof DoubleChestInventory) {
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();

            if (leftchest.getPersistentDataContainer().has(
                    new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                loc = leftchest.getLocation();
            } else if (rightchest.getPersistentDataContainer().has(
                    new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {
                loc = rightchest.getLocation();
            }
        }
        return loc;
    }

    /**
     * Gets data of a shop Item. This is used to show some extra info when sneaking while looking at a shop.
     * <br>
     * <br>
     * Currently supported:
     *  <ul>
     *      <li>Shulker boxes: Shows the amount of each item in the shulker box.</li>
     *      <li>Enchanted books: Shows the enchantments on the book.</li>
     *      <li>Enchanted items: Shows the enchantments on the item.</li>
     *  </ul>
     * @param lineNum The item data index (e.g. first item in a shulker or highest enchantment). If -1, it will return dataSize - lines.
     * @param item The item to get the data for.
     * @param lines The amount of lines to show. Used to show how many rest items there are.
     * @return The item data string formatted by the language file.
     */
    public static String getHologramItemData(int lineNum, ItemStack item, int lines) {
        String itemData = "";
        if (item.getType().name().contains("SHULKER_BOX")) {
            // Get the shulker box inventory
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta shulkerBlockStateMeta = (BlockStateMeta)item.getItemMeta();
                if (shulkerBlockStateMeta.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulker = (ShulkerBox) shulkerBlockStateMeta.getBlockState();
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
}
