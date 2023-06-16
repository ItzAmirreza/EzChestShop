package me.deadlight.ezchestshop.Utils;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class BlockBoundHologram {

    public enum HologramRotation {
        NORTH, SOUTH, EAST, WEST, UP, DOWN;
    }

    private Location location;
    private HologramRotation rotation;
    private List<String> contents;
    private HashMap<String, String> textReplacements;
    private HashMap<String, ItemStack> itemReplacements;
    private HashMap<String,  Boolean> conditionalTags;

    private HashMap<String,  List<Integer>> textReplacementLines;
    private HashMap<String,  List<Integer>> itemReplacementLines;
    private HashMap<String,  List<Integer>> conditionalTagLines;



    private List<UUID> viewers = new ArrayList<>();



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

    public BlockBoundHologram(Location location, HologramRotation rotation, List<String> contents, HashMap<String, String> textReplacements, HashMap<String, ItemStack> itemReplacements, HashMap<String, Boolean> conditionalTags) {
        this.location = location;
        this.rotation = rotation;
        this.contents = contents;
        this.textReplacements = textReplacements;
        this.itemReplacements = itemReplacements;
        this.conditionalTags = conditionalTags;

        queryReplacementLines();
    }



    public Location getLocation() {
        return location;
    }

    public HologramRotation getRotation() {
        return rotation;
    }

    public List<String> getContents() {
        return contents;
    }

    public HashMap<String, String> getTextReplacements() {
        return textReplacements;
    }

    public HashMap<String, ItemStack> getItemReplacements() {
        return itemReplacements;
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


    /**
     * Show the hologram to a player.
     * <br>
     * This is the initial method to call to show the hologram to a player.
     * Only once spawned, updates can be made to the hologram.
     * @param player
     */
    public void show(Player player) {
        if (viewers.contains(player.getUniqueId())) {
            return;
        }
        viewers.add(player.getUniqueId());

        /*
         Process the contents of the hologram
         */
        List<String> processedContents = new ArrayList<>(contents);
        // Reverse the contents if the hologram is upside down
        if (rotation == HologramRotation.DOWN) {
            Collections.reverse(processedContents);
        }
        // Process the text replacements
        for (String key : textReplacements.keySet()) {
            String replacement = textReplacements.get(key);
            for (int line : textReplacementLines.get(key)) {
                // replace the placeholder with the replacement text
                processedContents.set(line, processedContents.get(line).replace(key, replacement));
            }
        }
        // Process the conditional tags
        for (String key : conditionalTags.keySet()) {
            boolean value = conditionalTags.get(key);
            for (int line : conditionalTagLines.get(key)) {
                // if the value is true, remove the tag, but keep the inner contents
                // otherwise, remove the entire tag and its contents
                if (value) {
                    processedContents.set(line, processedContents.get(line)
                            .replaceAll("<\\/?" + key + ">", ""));
                } else {
                    processedContents.set(line, processedContents.get(line)
                            .replaceAll("<" + key + ">.*?<\\/" + key + ">", ""));
                }
            }
        }


        //TODO calculate the location of the hologram lines, insert the items, and send the packets
        // to spawn them to the player.
    }

    public void hide(Player player) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        viewers.remove(player.getUniqueId());
        //TODO
    }

    /**
     * Updates the hologram location for a player.
     * @param player
     */
    public void updateLocation(Player player) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        hide(player);
        show(player);
    }

    public void updateContents(Player player, List<String> contents) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        this.contents = contents;
        queryReplacementLines();
        //TODO
    }

    public void updateTextReplacement(Player player, String key, String replacement) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        textReplacements.put(key, replacement);
        queryReplacementLines();
        //TODO
    }

    public void updateTextReplacements(Player player, HashMap<String, String> replacements) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        textReplacements = replacements;
        queryReplacementLines();
        //TODO
    }

    public void updateItemReplacement(Player player, String key, ItemStack replacement) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        itemReplacements.put(key, replacement);
        queryReplacementLines();
        //TODO
    }

    public void updateItemReplacements(Player player, HashMap<String, ItemStack> replacements) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        itemReplacements = replacements;
        queryReplacementLines();
        //TODO
    }

    public void updateConditionalTag(Player player, String key, boolean value) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        conditionalTags.put(key, value);
        queryReplacementLines();
        //TODO
    }

    public void updateConditionalTags(Player player, HashMap<String, Boolean> tags) {
        if (!viewers.contains(player.getUniqueId())) {
            return;
        }
        conditionalTags = tags;
        queryReplacementLines();
        //TODO
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

    private Location getHoloLoc(Block containerBlock) {
        Location holoLoc;
        Inventory inventory = Utils.getBlockInventory(containerBlock);
        PersistentDataContainer container = ((TileState) containerBlock.getState()).getPersistentDataContainer();
        String rotation = container.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"),
                PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
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
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, -1.5, 0));
                break;
            default:
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 1, 0));
                break;
        }
        return holoLoc;
    }

    private Location getCentralLocation(Block containerBlock, Inventory inventory, Vector direction) {
        Location holoLoc;
        if (inventory instanceof DoubleChestInventory) {
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();
            holoLoc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D)
                    .add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5);
            if (direction.getY() == 0) {
                Location lloc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D);
                Location hloc = holoLoc.clone();
                double angle = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle = (-(angle / Math.PI) * 360.0d) / 2.0d + 180.0d;
                hloc = hloc.add(direction);
                double angle2 = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle2 = (-(angle2 / Math.PI) * 360.0d) / 2.0d + 180.0d;
                if (angle == angle2 || angle == angle2 - 180 || angle == angle2 + 180) {
                    holoLoc.add(direction.multiply(1.625));
                } else {
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
     * Queries the lines that need to be replaced for each replacement.
     * <br>
     * <br>
     * This method is called when the replacements are updated or when the constructor is called.
     * <br>
     * <br>
     * <p>
     *     This is done by iterating through the contents and checking if the line contains the key
     *     If it does, the line number is added to the list of lines that need to be replaced
     *     This is done for both text and item replacements
     *     The results are stored in the textReplacementLines and itemReplacementLines HashMaps
     *     The key is the replacement key and the value is a list of line numbers that need to be replaced
     * </p>
     */
    private void queryReplacementLines() {

        queryReplacementLinesIndividual(textReplacements.keySet());

        queryReplacementLinesIndividual(itemReplacements.keySet());

        // The conditional tags come without the < and >, as they also have a </> equivalent tag.
        // This is why we need to add them in, to check if the line contains the full tag.
        queryReplacementLinesIndividual(conditionalTags.keySet()
                .stream().map(s -> "<" + s + ">").collect(Collectors.toSet()));
    }

    /**
     * This method is only called by queryReplacementLines and is only used to reduce code duplication
     *
     * @param strings The set of keys that need to be queried
     * <br>
     * <br>
     * @see #queryReplacementLines()
     */
    private void queryReplacementLinesIndividual(Set<String> strings) {
        for (String key : strings) {
            List<Integer> lines = new ArrayList<>();
            for (int i = 0; i < contents.size(); i++) {
                if (contents.get(i).contains(key)) {
                    lines.add(i);
                }
            }
            textReplacementLines.put(key, lines);
        }
    }

}
